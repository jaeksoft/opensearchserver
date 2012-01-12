/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 *
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.parser;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
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
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.crawler.web.database.UrlFilterItem;
import com.jaeksoft.searchlib.crawler.web.database.UrlItemFieldEnum;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.Lang;
import com.jaeksoft.searchlib.util.LinkUtils;
import com.jaeksoft.searchlib.util.MimeUtils;
import com.jaeksoft.searchlib.util.XPathParser;

public class HtmlParser extends Parser {

	private final static TreeSet<String> sentenceTagSet = new TreeSet<String>();

	private static ParserFieldEnum[] fl = { ParserFieldEnum.title,
			ParserFieldEnum.body, ParserFieldEnum.meta_keywords,
			ParserFieldEnum.meta_description, ParserFieldEnum.meta_robots,
			ParserFieldEnum.internal_link,
			ParserFieldEnum.internal_link_nofollow,
			ParserFieldEnum.external_link,
			ParserFieldEnum.external_link_nofollow, ParserFieldEnum.lang };

	private UrlItemFieldEnum urlItemFieldEnum = null;

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

	@Override
	public void initProperties() throws SearchLibException {
		super.initProperties();
		addProperty(ClassPropertyEnum.SIZE_LIMIT, "0", null);
		addProperty(ClassPropertyEnum.DEFAULT_CHARSET, "UTF-8", null);
		if (config != null)
			urlItemFieldEnum = config.getUrlManager().getUrlItemFieldEnum();
	}

	private final static String OPENSEARCHSERVER_FIELD = "opensearchserver.field.";
	private final static String OPENSEARCHSERVER_IGNORE = "opensearchserver.ignore";
	private final static int OPENSEARCHSERVER_FIELD_LENGTH = OPENSEARCHSERVER_FIELD
			.length();

	private void getBodyTextContent(StringBuffer sb, Node node,
			boolean bAddBlock, String[] directFields) {
		if (node.getNodeType() == Node.COMMENT_NODE)
			return;
		String nodeName = node.getNodeName();
		if ("script".equalsIgnoreCase(nodeName))
			return;
		if ("style".equalsIgnoreCase(nodeName))
			return;
		if ("title".equalsIgnoreCase(nodeName))
			return;
		if ("oss".equalsIgnoreCase(nodeName)) {
			if ("yes".equalsIgnoreCase(XPathParser.getAttributeString(node,
					"ignore")))
				return;
		}
		boolean bEnterDirectField = false;
		if ("div".equalsIgnoreCase(nodeName)) {
			String classAttribute = XPathParser.getAttributeString(node,
					"class");
			if (classAttribute != null) {
				if (OPENSEARCHSERVER_IGNORE.equalsIgnoreCase(classAttribute))
					return;
				if (classAttribute.startsWith(OPENSEARCHSERVER_FIELD)) {
					String directField = classAttribute
							.substring(OPENSEARCHSERVER_FIELD_LENGTH);
					if (directField.length() > 0) {
						directFields = directField.split("\\.");
						bEnterDirectField = directFields.length > 0;
					}
				}
			}
		}
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
			getBodyTextContent(sb, children.item(i), bAddBlock, directFields);

		if (bAddBlock && nodeName != null && sb.length() > 0) {
			String currentTag = nodeName.toLowerCase();
			boolean bForSentence = sb.charAt(sb.length() - 1) != '.'
					&& sentenceTagSet.contains(currentTag);
			if (bForSentence || bEnterDirectField) {
				if (directFields != null)
					addDirectFields(directFields, sb.toString());
				else
					addFieldBody(currentTag, sb.toString());
				sb.setLength(0);
			}
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
		parser.setFeature(
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

	// private static Document tidyDomDocument(String charset,
	// LimitInputStream inputStream) throws LimitException {
	// Tidy tidy = new Tidy();
	// tidy.setQuiet(true);
	// tidy.setOnlyErrors(false);
	// tidy.setShowWarnings(false);
	//
	// if ("utf-8".equalsIgnoreCase(charset))
	// tidy.setCharEncoding(Configuration.UTF8);
	// else if ("iso-8859-1".equalsIgnoreCase(charset))
	// tidy.setCharEncoding(Configuration.LATIN1);
	// else if ("macroman".equalsIgnoreCase(charset))
	// tidy.setCharEncoding(Configuration.MACROMAN);
	// else if ("iso-2022".equalsIgnoreCase(charset))
	// tidy.setCharEncoding(Configuration.ISO2022);
	//
	// // Création de l'arbre DOM avec Tidy
	// Document dom = tidy.parseDOM(inputStream, null);
	// if (!inputStream.isComplete())
	// throw new LimitException();
	// return dom;
	// }

	private String getTitleText(Document doc, String[] path) {
		List<Node> nodes = DomUtils.getNodes(doc, path);
		if (nodes == null)
			return null;
		if (nodes.size() < 1)
			return null;
		return DomUtils.getText(nodes.get(0));
	}

	private String getTitle(Document doc) {
		String[] p1 = { "html", "head", "title" };
		String title = getTitleText(doc, p1);
		if (title != null)
			return title;
		String[] p2 = { "html", "title" };
		return getTitleText(doc, p2);
	}

	private static List<Node> getMetas(Document doc) {
		String[] p1 = { "html", "head", "meta" };
		String[] p2 = { "html", "meta" };
		List<Node> metas = new ArrayList<Node>();
		DomUtils.getNodes(metas, doc, p1);
		DomUtils.getNodes(metas, doc, p2);
		return metas;
	}

	private static URL getBaseHref(Document doc) {
		List<Node> list = DomUtils.getNodes(doc, "html", "head", "base");
		if (list == null)
			return null;
		if (list.size() == 0)
			return null;
		Node node = list.get(0);
		if (node == null)
			return null;
		String url = DomUtils.getAttributeText(node, "href");
		if (url == null)
			return null;
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			Logging.warn(e);
			return null;
		}
	}

	private static String getMetaContent(Node node) {
		String content = DomUtils.getAttributeText(node, "content");
		if (content == null)
			return null;
		return StringEscapeUtils.unescapeHtml(content);
	}

	private static String getMetaCharset(List<Node> metas) {
		for (Node node : metas) {
			String charset = DomUtils.getAttributeText(node, "charset");
			if (charset != null && charset.length() > 0)
				return charset;
		}
		return null;
	}

	private static String getMetaHttpEquiv(List<Node> metas, String name) {
		for (Node node : metas) {
			String attr_http_equiv = DomUtils.getAttributeText(node,
					"http-equiv");
			if (name.equalsIgnoreCase(attr_http_equiv))
				return getMetaContent(node);
		}
		return null;
	}

	private boolean checkDocument(Document doc) {
		if (doc == null)
			return false;
		int i = DomUtils.countElements(doc);
		return i > 0;
	}

	private Document htmlParserLine(String charset, LimitInputStream inputStream)
			throws LimitException {
		Document doc = null;
		if (doc == null) {
			try {
				doc = tagSoupDomDocument(charset, inputStream);
				if (checkDocument(doc))
					return doc;
				else
					doc = null;
			} catch (LimitException e) {
				throw e;
			} catch (Exception e) {
				Logging.warn(e.getMessage(), e);
				doc = null;
			}
		}
		if (doc == null) {
			try {
				inputStream.restartFromCache();
				doc = nekoHtmlDomDocument(charset, inputStream);
				if (checkDocument(doc))
					return doc;
				else
					doc = null;
			} catch (LimitException e) {
				throw e;
			} catch (Exception e) {
				Logging.warn(e.getMessage(), e);
				doc = null;
			}
		}
		// if (doc == null) {
		// try {
		// inputStream.restartFromCache();
		// doc = tidyDomDocument(charset, inputStream);
		// if (checkDocument(doc))
		// return doc;
		// else
		// doc = null;
		// } catch (LimitException e) {
		// throw e;
		// } catch (Exception e) {
		// Logging.error(e.getMessage(), e);
		// doc = null;
		// }
		// }
		if (doc == null) {
			try {
				inputStream.restartFromCache();
				doc = htmlCleanerDomDocument(charset, inputStream);
				if (checkDocument(doc))
					return doc;
				else
					doc = null;
			} catch (LimitException e) {
				throw e;
			} catch (Exception e) {
				Logging.warn(e.getMessage(), e);
				doc = null;
			}
		}
		return doc;
	}

	protected void addFieldTitle(String value) {
		addField(ParserFieldEnum.title, value);
	}

	protected void addFieldBody(String tag, String value) {
		addField(ParserFieldEnum.body, value);
	}

	@Override
	protected void parseContent(LimitInputStream inputStream)
			throws IOException {

		String charset = null;
		IndexDocument sourceDocument = getSourceDocument();
		if (sourceDocument != null && urlItemFieldEnum != null) {
			FieldValueItem fieldValueItem = sourceDocument.getFieldValue(
					urlItemFieldEnum.contentTypeCharset.getName(), 0);
			if (fieldValueItem != null)
				charset = fieldValueItem.getValue();
			if (charset == null) {
				fieldValueItem = sourceDocument.getFieldValue(
						urlItemFieldEnum.contentEncoding.getName(), 0);
				if (fieldValueItem != null)
					charset = fieldValueItem.getValue();
			}
		}
		boolean charsetWasNull = charset == null;
		if (charsetWasNull)
			charset = getProperty(ClassPropertyEnum.DEFAULT_CHARSET).getValue();

		Document doc = htmlParserLine(charset, inputStream);
		if (doc == null)
			return;

		List<Node> metas = getMetas(doc);

		// Check ContentType charset in meta http-equiv
		String contentType = getMetaHttpEquiv(metas, "content-type");
		String contentTypeCharset = null;
		if (contentType != null) {
			contentTypeCharset = MimeUtils
					.extractContentTypeCharset(contentType);
			// the meta in charset has priority if it is different from previous
			// charset
			if (contentTypeCharset != null
					&& !contentTypeCharset.equals(charset))
				charsetWasNull = true;
		}

		if (charsetWasNull) {
			if (contentTypeCharset != null)
				charset = contentTypeCharset;
			else
				charset = getMetaCharset(metas);
			if (charset != null) {
				inputStream.restartFromCache();
				doc = htmlParserLine(charset, inputStream);
			}
		}

		for (Node metaNode : metas) {
			String metaName = DomUtils.getAttributeText(metaNode, "name");
			if (metaName != null && metaName.startsWith(OPENSEARCHSERVER_FIELD)) {
				String field = metaName
						.substring(OPENSEARCHSERVER_FIELD_LENGTH);
				String[] fields = field.split("\\.");
				if (fields != null) {
					String content = DomUtils.getAttributeText(metaNode,
							"content");
					addDirectFields(fields, content);
				}
			}
		}

		addField(ParserFieldEnum.charset, charset);

		addFieldTitle(getTitle(doc));

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

		UrlFilterItem[] urlFilterList = getUrlFilterList();

		List<Node> nodes = DomUtils.getAllNodes(doc, "a", "frame");
		IndexDocument srcDoc = getSourceDocument();
		if (srcDoc != null && nodes != null && metaRobotsFollow) {
			URL currentURL = getBaseHref(doc);
			if (currentURL == null && urlItemFieldEnum != null) {
				FieldValueItem fvi = srcDoc.getFieldValue(
						urlItemFieldEnum.url.getName(), 0);
				if (fvi != null)
					currentURL = new URL(fvi.getValue());
			}
			for (Node node : nodes) {
				String href = null;
				String rel = null;
				String nodeName = node.getNodeName();
				if ("a".equals(nodeName)) {
					href = DomUtils.getAttributeText(node, "href");
					rel = DomUtils.getAttributeText(node, "rel");
				} else if ("frame".equals(nodeName)) {
					href = DomUtils.getAttributeText(node, "src");
				}
				boolean follow = true;
				if (rel != null)
					if (rel.contains("nofollow"))
						follow = false;
				URL newUrl = null;
				if (href != null)
					if (!href.startsWith("javascript:"))
						if (currentURL != null)
							newUrl = LinkUtils.getLink(currentURL, href,
									follow, false, true, urlFilterList);
				if (newUrl != null) {
					ParserFieldEnum field = null;
					if (newUrl.getHost().equalsIgnoreCase(currentURL.getHost())) {
						if (follow)
							field = ParserFieldEnum.internal_link;
						else
							field = ParserFieldEnum.internal_link_nofollow;
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

		nodes = DomUtils.getNodes(doc, "html", "body");
		if (nodes == null || nodes.size() == 0)
			nodes = DomUtils.getNodes(doc, "html");
		if (nodes != null && nodes.size() > 0) {
			StringBuffer sb = new StringBuffer();
			getBodyTextContent(sb, nodes.get(0), true, null);
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

		if (lang != null) {
			addField(ParserFieldEnum.lang, lang.getLanguage());
			addField(ParserFieldEnum.lang_method, langMethod);
		} else
			lang = langDetection(10000, ParserFieldEnum.body);

	}

	@Override
	protected void parseContent(LimitReader reader) throws IOException {
		throw new IOException("Unsupported");
	}

}
