/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.parser;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.xalan.xsltc.trax.SAX2DOM;
import org.cyberneko.html.parsers.DOMParser;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.knallgrau.utils.textcat.TextCategorizer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Configuration;
import org.w3c.tidy.Tidy;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.crawler.web.database.UrlItemFieldEnum;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.Lang;
import com.jaeksoft.searchlib.util.LinkUtils;

public class HtmlParser extends Parser {

	private final static TreeSet<String> sentenceTagSet = new TreeSet<String>();

	private static ParserFieldEnum[] fl = { ParserFieldEnum.title,
			ParserFieldEnum.body, ParserFieldEnum.meta_keywords,
			ParserFieldEnum.meta_description, ParserFieldEnum.meta_robots,
			ParserFieldEnum.internal_link, ParserFieldEnum.internal_nofollow,
			ParserFieldEnum.external_link,
			ParserFieldEnum.external_link_nofollow, ParserFieldEnum.lang };

	public HtmlParser() {
		super(fl);
		synchronized (this) {
			if (sentenceTagSet.size() == 0) {
				sentenceTagSet.add("p");
				sentenceTagSet.add("td");
				sentenceTagSet.add("div");
				sentenceTagSet.add("h1");
				sentenceTagSet.add("h2");
				sentenceTagSet.add("h3");
				sentenceTagSet.add("h4");
				sentenceTagSet.add("h5");
				sentenceTagSet.add("h6");
				sentenceTagSet.add("hr");
				sentenceTagSet.add("li");
				sentenceTagSet.add("option");
				sentenceTagSet.add("pre");
				sentenceTagSet.add("select");
				sentenceTagSet.add("table");
				sentenceTagSet.add("tbody");
				sentenceTagSet.add("td");
				sentenceTagSet.add("textarea");
				sentenceTagSet.add("tfoot");
				sentenceTagSet.add("thead");
				sentenceTagSet.add("th");
				sentenceTagSet.add("title");
				sentenceTagSet.add("tr");
				sentenceTagSet.add("ul");
			}
		}
	}

	private void getBodyTextContent(StringBuffer sb, Node node,
			boolean bAddBlock) {
		if (node.getNodeType() == Node.COMMENT_NODE)
			return;
		String nodeName = node.getNodeName();
		if ("script".equalsIgnoreCase(nodeName))
			return;
		if ("style".equalsIgnoreCase(nodeName))
			return;
		if ("title".equalsIgnoreCase(nodeName))
			return;
		if (node.getNodeType() == Node.TEXT_NODE) {
			String text = node.getNodeValue();
			text = text.replaceAll("\\r", "");
			text = text.replaceAll("\\n", "");
			text = text.replaceAll("\\s+", " ");
			text = text.trim();
			if (text.length() > 0) {
				text = StringEscapeUtils.unescapeHtml(text);
				if (sb.length() > 0)
					sb.append(' ');
				sb.append(text);
			}
		}
		NodeList children = node.getChildNodes();
		if (children == null)
			return;
		int len = children.getLength();
		for (int i = 0; i < len; i++)
			getBodyTextContent(sb, children.item(i), bAddBlock);

		if (bAddBlock && nodeName != null && sb.length() > 0
				&& sb.charAt(sb.length() - 1) != '.'
				&& sentenceTagSet.contains(nodeName.toLowerCase())) {
			addField(ParserFieldEnum.body, sb);
			sb.setLength(0);
		}
	}

	private static Document htmlCleanerDomDocument(String charset,
			LimitInputStream inputStream) throws IOException,
			ParserConfigurationException {
		HtmlCleaner cleaner = new HtmlCleaner();
		CleanerProperties props = cleaner.getProperties();
		props.setNamespacesAware(true);
		TagNode node = cleaner.clean(inputStream, charset);
		if (!inputStream.isComplete())
			throw new LimitException();
		return new DomSerializer(props, true).createDOM(node);
	}

	private static Document nekoHtmlDomDocument(String charset,
			LimitInputStream inputStream) throws XPathExpressionException,
			SAXException, IOException, ParserConfigurationException {
		DOMParser parser = new DOMParser();
		parser.setFeature("http://xml.org/sax/features/namespaces", true);
		parser
				.setFeature(
						"http://cyberneko.org/html/features/balance-tags/ignore-outside-content",
						false);
		parser.setFeature("http://cyberneko.org/html/features/balance-tags",
				true);
		parser.setFeature("http://cyberneko.org/html/features/report-errors",
				false);
		parser.setProperty("http://cyberneko.org/html/properties/names/elems",
				"lower");
		parser.setProperty("http://cyberneko.org/html/properties/names/attrs",
				"lower");
		InputSource inputSource = new InputSource(inputStream);
		inputSource.setEncoding(charset);
		parser.parse(inputSource);
		return parser.getDocument();
	}

	private static Document tagSoupDomDocument(String charset,
			LimitInputStream inputStream) throws XPathExpressionException,
			SAXException, IOException, ParserConfigurationException {
		org.ccil.cowan.tagsoup.Parser parser = new org.ccil.cowan.tagsoup.Parser();
		parser.setFeature("http://xml.org/sax/features/namespace-prefixes",
				true);
		SAX2DOM sax2dom = new SAX2DOM();
		parser.setContentHandler(sax2dom);
		InputSource inputSource = new InputSource(inputStream);
		inputSource.setEncoding(charset);
		parser.parse(inputSource);
		return (Document) sax2dom.getDOM();
	}

	private static Document tidyDomDocument(String charset,
			LimitInputStream inputStream) throws LimitException {
		Tidy tidy = new Tidy();
		tidy.setQuiet(true);
		tidy.setOnlyErrors(false);
		tidy.setShowWarnings(false);

		if ("utf-8".equalsIgnoreCase(charset))
			tidy.setCharEncoding(Configuration.UTF8);
		else if ("iso-8859-1".equalsIgnoreCase(charset))
			tidy.setCharEncoding(Configuration.LATIN1);
		else if ("macroman".equalsIgnoreCase(charset))
			tidy.setCharEncoding(Configuration.MACROMAN);
		else if ("iso-2022".equalsIgnoreCase(charset))
			tidy.setCharEncoding(Configuration.ISO2022);

		// Cr�ation de l'arbre DOM avec Tidy
		Document dom = tidy.parseDOM(inputStream, null);
		if (!inputStream.isComplete())
			throw new LimitException();
		return dom;
	}

	private String getTitle(Document doc) {
		String[] p = { "html", "head", "title" };
		List<Node> nodes = DomUtils.getNodes(doc, p);
		if (nodes == null)
			return null;
		if (nodes.size() < 1)
			return null;
		return DomUtils.getText(nodes.get(0));
	}

	private static List<Node> getMetas(Document doc) {
		String[] p = { "html", "head", "meta" };
		List<Node> metas = DomUtils.getNodes(doc, p);
		return metas;
	}

	private static String getMetaContent(Node node) {
		String content = DomUtils.getAttributeText(node, "content");
		if (content == null)
			return null;
		return StringEscapeUtils.unescapeHtml(content);
	}

	@Override
	protected void parseContent(LimitInputStream inputStream)
			throws IOException {

		String charset = getSourceDocument().getFieldValue(
				UrlItemFieldEnum.contentTypeCharset.name(), 0);
		if (charset == null)
			getSourceDocument().getFieldValue(
					UrlItemFieldEnum.contentEncoding.name(), 0);
		if (charset == null)
			charset = Charset.defaultCharset().name();
		addField(ParserFieldEnum.charset, charset);

		Document doc = null;
		if (doc == null) {
			try {
				doc = tagSoupDomDocument(charset, inputStream);
			} catch (LimitException e) {
				throw e;
			} catch (Exception e) {
				e.printStackTrace();
				doc = null;
			}
		}
		if (doc == null) {
			try {
				doc = nekoHtmlDomDocument(charset, inputStream);
			} catch (LimitException e) {
				throw e;
			} catch (Exception e) {
				e.printStackTrace();
				doc = null;
			}
		}
		if (doc == null) {
			try {
				doc = tidyDomDocument(charset, inputStream);
			} catch (LimitException e) {
				throw e;
			} catch (Exception e) {
				e.printStackTrace();
				doc = null;
			}
		}
		if (doc == null) {
			try {
				doc = htmlCleanerDomDocument(charset, inputStream);
			} catch (LimitException e) {
				throw e;
			} catch (Exception e) {
				e.printStackTrace();
				doc = null;
			}
		}

		if (doc == null)
			return;

		addField(ParserFieldEnum.title, getTitle(doc));

		List<Node> metas = getMetas(doc);

		String metaRobots = null;

		String metaDcLanguage = null;

		String metaContentLanguage = null;

		for (Node node : metas) {
			String attr_name = DomUtils.getAttributeText(node, "name");
			String attr_http_equiv = DomUtils.getAttributeText(node,
					"http-equiv");
			if ("keywords".equalsIgnoreCase(attr_name))
				addField(ParserFieldEnum.meta_keywords, getMetaContent(node));
			else if ("description".equalsIgnoreCase(attr_name))
				addField(ParserFieldEnum.meta_description, getMetaContent(node));
			else if ("robots".equalsIgnoreCase(attr_name))
				metaRobots = getMetaContent(node);
			else if ("dc.language".equalsIgnoreCase(attr_name))
				metaDcLanguage = getMetaContent(node);
			else if ("content-language".equalsIgnoreCase(attr_http_equiv))
				metaContentLanguage = getMetaContent(node);
		}

		boolean metaRobotsFollow = true;
		if (metaRobots != null) {
			metaRobots = metaRobots.toLowerCase();
			if (metaRobots.contains("noindex"))
				addField(ParserFieldEnum.meta_robots, "noindex");
			if (metaRobots.contains("nofollow")) {
				metaRobotsFollow = false;
				addField(ParserFieldEnum.meta_robots, "nofollow");
			}
		}

		List<Node> nodes = DomUtils.getAllNodes(doc, "a");
		if (nodes != null && metaRobotsFollow) {
			URL currentURL = new URL(getSourceDocument().getFieldValue(
					UrlItemFieldEnum.url.name(), 0));
			for (Node node : nodes) {
				String href = DomUtils.getAttributeText(node, "href");
				String rel = DomUtils.getAttributeText(node, "rel");
				boolean follow = true;
				if (rel != null)
					if (rel.contains("nofollow"))
						follow = false;
				URL newUrl = null;
				if (href != null)
					if (!href.startsWith("javascript:"))
						newUrl = LinkUtils.getLink(currentURL, href, follow,
								false, true, true);
				if (newUrl != null) {
					ParserFieldEnum field = null;
					if (newUrl.getHost().equalsIgnoreCase(currentURL.getHost())) {
						if (follow)
							field = ParserFieldEnum.internal_link;
						else
							field = ParserFieldEnum.internal_nofollow;
					} else {
						if (follow)
							field = ParserFieldEnum.external_link;
						else
							field = ParserFieldEnum.external_link_nofollow;
					}
					addField(field, newUrl.toExternalForm());
				}
			}
		}

		String[] p = { "html", "body" };
		nodes = DomUtils.getNodes(doc, p);
		if (nodes != null && nodes.size() > 0) {
			StringBuffer sb = new StringBuffer();
			getBodyTextContent(sb, nodes.get(0), true);
			addField(ParserFieldEnum.body, sb);
		}

		// Identification de la langue:
		Locale lang = null;
		String langMethod = null;
		String[] pathHtml = { "html" };
		nodes = DomUtils.getNodes(doc, pathHtml);
		if (nodes != null && nodes.size() > 0) {
			langMethod = "html lang attribute";
			String l = DomUtils.getAttributeText(nodes.get(0), "lang");
			if (l != null)
				lang = Lang.findLocaleISO639(l);
		}
		if (lang == null && metaContentLanguage != null) {
			langMethod = "meta http-equiv content-language";
			lang = Lang.findLocaleISO639(metaContentLanguage);
		}
		if (lang == null && metaDcLanguage != null) {
			langMethod = "meta dc.language";
			lang = Lang.findLocaleISO639(metaDcLanguage);
		}
		if (lang == null) {
			String text = getMergedBodyText(1000, " ");
			if (text != null) {
				langMethod = "ngram recognition";
				String textcat = new TextCategorizer().categorize(text, text
						.length());
				lang = Lang.findLocaleDescription(textcat);
			}
		}

		if (lang != null) {
			addField(ParserFieldEnum.lang, lang.getISO3Language());
			addField(ParserFieldEnum.lang_method, langMethod);
		}
	}

	@Override
	protected void parseContent(LimitReader reader) throws IOException {
		throw new IOException("Unsupported");
	}

	@Override
	public ParserFieldEnum[] getParserFieldList() {
		return fl;
	}

}
