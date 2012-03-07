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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.crawler.web.database.UrlFilterItem;
import com.jaeksoft.searchlib.crawler.web.database.UrlItemFieldEnum;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.parser.htmlParser.HtmlCleanerParser;
import com.jaeksoft.searchlib.parser.htmlParser.HtmlDocumentProvider;
import com.jaeksoft.searchlib.parser.htmlParser.HtmlNodeAbstract;
import com.jaeksoft.searchlib.parser.htmlParser.JSoupParser;
import com.jaeksoft.searchlib.parser.htmlParser.NekoHtmlParser;
import com.jaeksoft.searchlib.parser.htmlParser.StrictXhtmlParser;
import com.jaeksoft.searchlib.parser.htmlParser.TagsoupParser;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiter;
import com.jaeksoft.searchlib.util.Lang;
import com.jaeksoft.searchlib.util.LinkUtils;
import com.jaeksoft.searchlib.util.MimeUtils;

public class HtmlParser extends Parser {

	private final static TreeSet<String> sentenceTagSet = new TreeSet<String>();

	private static ParserFieldEnum[] fl = { ParserFieldEnum.title,
			ParserFieldEnum.body, ParserFieldEnum.meta_keywords,
			ParserFieldEnum.meta_description, ParserFieldEnum.meta_robots,
			ParserFieldEnum.internal_link,
			ParserFieldEnum.internal_link_nofollow,
			ParserFieldEnum.external_link,
			ParserFieldEnum.external_link_nofollow, ParserFieldEnum.lang,
			ParserFieldEnum.htmlProvider };

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

	private void getBodyTextContent(StringBuffer sb, HtmlNodeAbstract<?> node,
			boolean bAddBlock, String[] directFields) {
		if (node.isComment())
			return;
		String nodeName = node.getNodeName();
		if ("script".equalsIgnoreCase(nodeName))
			return;
		if ("style".equalsIgnoreCase(nodeName))
			return;
		if ("object".equalsIgnoreCase(nodeName))
			return;
		if ("title".equalsIgnoreCase(nodeName))
			return;
		if ("oss".equalsIgnoreCase(nodeName)) {
			if ("yes".equalsIgnoreCase(node.getAttribute("ignore")))
				return;
		}

		boolean bEnterDirectField = false;
		String classNameAttribute = node.getAttribute("class");
		if (classNameAttribute != null) {
			String[] classNames = StringUtils.split(classNameAttribute);
			if (classNames != null) {
				for (String className : classNames) {
					if (OPENSEARCHSERVER_IGNORE.equalsIgnoreCase(className))
						return;
					if (className.startsWith(OPENSEARCHSERVER_FIELD)) {
						String directField = classNameAttribute
								.substring(OPENSEARCHSERVER_FIELD_LENGTH);
						if (directField.length() > 0) {
							directFields = directField.split("\\.");
							bEnterDirectField = directFields.length > 0;
						}
					}
				}
			}
		}

		if (node.isTextNode()) {
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
		List<HtmlNodeAbstract<?>> children = node.getChildNodes();
		if (children != null)
			for (HtmlNodeAbstract<?> htmlNode : children)
				getBodyTextContent(sb, htmlNode, bAddBlock, directFields);

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

	private HtmlDocumentProvider findBestProvider(String charset,
			StreamLimiter streamLimiter) throws IOException {

		HtmlDocumentProvider provider = new StrictXhtmlParser(charset,
				streamLimiter);
		if (provider.getRootNode() != null)
			return provider;

		List<HtmlDocumentProvider> providerList = new ArrayList<HtmlDocumentProvider>(
				0);
		providerList.add(new TagsoupParser(charset, streamLimiter));
		providerList.add(new NekoHtmlParser(charset, streamLimiter));
		providerList.add(new HtmlCleanerParser(charset, streamLimiter));
		providerList.add(new JSoupParser(charset, streamLimiter));

		return HtmlDocumentProvider.bestScore(providerList);
	}

	protected void addFieldTitle(String value) {
		addField(ParserFieldEnum.title, value);
	}

	protected void addFieldBody(String tag, String value) {
		addField(ParserFieldEnum.body, value);
	}

	@Override
	protected void parseContent(StreamLimiter streamLimiter) throws IOException {

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

		HtmlDocumentProvider htmlProvider = findBestProvider(charset,
				streamLimiter);
		if (htmlProvider == null)
			return;

		addField(ParserFieldEnum.htmlProvider, htmlProvider.getName());

		// Check ContentType charset in meta http-equiv
		String contentType = htmlProvider.getMetaHttpEquiv("content-type");
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
				charset = htmlProvider.getMetaCharset();
			if (charset != null)
				htmlProvider = findBestProvider(charset, streamLimiter);
		}

		HtmlNodeAbstract<?> rootNode = htmlProvider.getRootNode();
		if (rootNode == null)
			return;

		for (HtmlNodeAbstract<?> metaNode : htmlProvider.getMetas()) {
			String metaName = metaNode.getAttributeText("name");
			if (metaName != null && metaName.startsWith(OPENSEARCHSERVER_FIELD)) {
				String field = metaName
						.substring(OPENSEARCHSERVER_FIELD_LENGTH);
				String[] fields = field.split("\\.");
				if (fields != null) {
					String content = metaNode.getAttributeText("content");
					addDirectFields(fields, content);
				}
			}
		}

		addField(ParserFieldEnum.charset, charset);

		addFieldTitle(htmlProvider.getTitle());

		String metaRobots = null;

		String metaDcLanguage = null;

		String metaContentLanguage = null;

		for (HtmlNodeAbstract<?> node : htmlProvider.getMetas()) {
			String attr_name = node.getAttributeText("name");
			String attr_http_equiv = node.getAttributeText("http-equiv");
			if ("keywords".equalsIgnoreCase(attr_name))
				addField(ParserFieldEnum.meta_keywords,
						HtmlDocumentProvider.getMetaContent(node));
			else if ("description".equalsIgnoreCase(attr_name))
				addField(ParserFieldEnum.meta_description,
						HtmlDocumentProvider.getMetaContent(node));
			else if ("robots".equalsIgnoreCase(attr_name))
				metaRobots = HtmlDocumentProvider.getMetaContent(node);
			else if ("dc.language".equalsIgnoreCase(attr_name))
				metaDcLanguage = HtmlDocumentProvider.getMetaContent(node);
			else if ("content-language".equalsIgnoreCase(attr_http_equiv))
				metaContentLanguage = HtmlDocumentProvider.getMetaContent(node);
		}

		boolean metaRobotsFollow = true;
		boolean metaRobotsNoIndex = false;
		if (metaRobots != null) {
			metaRobots = metaRobots.toLowerCase();
			if (metaRobots.contains("noindex")) {
				metaRobotsNoIndex = true;
				addField(ParserFieldEnum.meta_robots, "noindex");
			}
			if (metaRobots.contains("nofollow")) {
				metaRobotsFollow = false;
				addField(ParserFieldEnum.meta_robots, "nofollow");
			}
		}

		UrlFilterItem[] urlFilterList = getUrlFilterList();

		List<HtmlNodeAbstract<?>> nodes = rootNode.getAllNodes("a", "frame");
		IndexDocument srcDoc = getSourceDocument();
		if (srcDoc != null && nodes != null && metaRobotsFollow) {
			URL currentURL = htmlProvider.getBaseHref();
			if (currentURL == null && urlItemFieldEnum != null) {
				FieldValueItem fvi = srcDoc.getFieldValue(
						urlItemFieldEnum.url.getName(), 0);
				if (fvi != null)
					currentURL = new URL(fvi.getValue());
			}
			for (HtmlNodeAbstract<?> node : nodes) {
				String href = null;
				String rel = null;
				String nodeName = node.getNodeName();
				if ("a".equals(nodeName)) {
					href = node.getAttributeText("href");
					rel = node.getAttributeText("rel");
				} else if ("frame".equals(nodeName)) {
					href = node.getAttributeText("src");
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

		if (!metaRobotsNoIndex) {
			nodes = rootNode.getNodes("html", "body");
			if (nodes == null || nodes.size() == 0)
				nodes = rootNode.getNodes("html");
			if (nodes != null && nodes.size() > 0) {
				StringBuffer sb = new StringBuffer();
				getBodyTextContent(sb, nodes.get(0), true, null);
				addField(ParserFieldEnum.body, sb);
			}
		}

		// Identification de la langue:
		Locale lang = null;
		String langMethod = null;
		String[] pathHtml = { "html" };
		nodes = rootNode.getNodes(pathHtml);
		if (nodes != null && nodes.size() > 0) {
			langMethod = "html lang attribute";
			String l = nodes.get(0).getAttributeText("lang");
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
		} else if (!metaRobotsNoIndex)
			lang = langDetection(10000, ParserFieldEnum.body);

	}

}
