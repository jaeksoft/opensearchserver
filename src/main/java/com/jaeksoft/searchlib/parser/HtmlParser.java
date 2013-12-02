/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2013 Emmanuel Keller / Jaeksoft
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
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.memory.MemoryIndex;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.similar.MoreLikeThis;
import org.apache.lucene.util.Version;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.crawler.web.database.UrlFilterItem;
import com.jaeksoft.searchlib.crawler.web.database.UrlItemFieldEnum;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.parser.htmlParser.HtmlDocumentProvider;
import com.jaeksoft.searchlib.parser.htmlParser.HtmlNodeAbstract;
import com.jaeksoft.searchlib.parser.htmlParser.HtmlParserEnum;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.streamlimiter.LimitException;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiter;
import com.jaeksoft.searchlib.util.Lang;
import com.jaeksoft.searchlib.util.LinkUtils;
import com.jaeksoft.searchlib.util.StringUtils;

public class HtmlParser extends Parser {

	private final static TreeSet<String> sentenceTagSet = new TreeSet<String>();

	private static ParserFieldEnum[] fl = { ParserFieldEnum.parser_name,
			ParserFieldEnum.title, ParserFieldEnum.generated_title,
			ParserFieldEnum.body, ParserFieldEnum.meta_keywords,
			ParserFieldEnum.meta_description, ParserFieldEnum.meta_robots,
			ParserFieldEnum.internal_link,
			ParserFieldEnum.internal_link_nofollow,
			ParserFieldEnum.external_link,
			ParserFieldEnum.external_link_nofollow, ParserFieldEnum.lang,
			ParserFieldEnum.htmlProvider, ParserFieldEnum.htmlSource };

	private UrlItemFieldEnum urlItemFieldEnum = null;

	private class BoostTag {
		private final Float boost;
		private String firstContent;

		private BoostTag(ClassPropertyEnum classPropertyEnum) {
			this.boost = getFloatProperty(classPropertyEnum);
			this.firstContent = null;
		}
	}

	private Map<String, BoostTag> boostTagMap;

	private Float titleBoost;
	private boolean ignoreMetaNoIndex;
	private boolean ignoreMetaNoFollow;
	private boolean ignoreUntitledDocuments;
	private boolean ignoreNonCanonical;
	private boolean isCanonical = true;

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
		addProperty(ClassPropertyEnum.HTML_PARSER,
				HtmlParserEnum.BestScoreParser.getLabel(),
				HtmlParserEnum.getLabelArray());
		addProperty(ClassPropertyEnum.URL_FRAGMENT,
				ClassPropertyEnum.KEEP_REMOVE_LIST[0],
				ClassPropertyEnum.KEEP_REMOVE_LIST);
		addProperty(ClassPropertyEnum.IGNORE_META_NOINDEX,
				Boolean.FALSE.toString(), ClassPropertyEnum.BOOLEAN_LIST);
		addProperty(ClassPropertyEnum.IGNORE_META_NOFOLLOW,
				Boolean.FALSE.toString(), ClassPropertyEnum.BOOLEAN_LIST);
		addProperty(ClassPropertyEnum.IGNORE_UNTITLED_DOCUMENTS,
				Boolean.FALSE.toString(), ClassPropertyEnum.BOOLEAN_LIST);
		addProperty(ClassPropertyEnum.IGNORE_NON_CANONICAL,
				Boolean.TRUE.toString(), ClassPropertyEnum.BOOLEAN_LIST);
		if (config != null)
			urlItemFieldEnum = config.getUrlManager().urlItemFieldEnum;
		addProperty(ClassPropertyEnum.TITLE_BOOST, "2", null);
		addProperty(ClassPropertyEnum.H1_BOOST, "1.8", null);
		addProperty(ClassPropertyEnum.H2_BOOST, "1.6", null);
		addProperty(ClassPropertyEnum.H3_BOOST, "1.4", null);
		addProperty(ClassPropertyEnum.H4_BOOST, "1.2", null);
		addProperty(ClassPropertyEnum.H5_BOOST, "1.1", null);
		addProperty(ClassPropertyEnum.H6_BOOST, "1.1", null);
		addProperty(ClassPropertyEnum.XPATH_EXCLUSION, "", null);
	}

	private final static String OPENSEARCHSERVER_FIELD = "opensearchserver.field.";
	private final static String OPENSEARCHSERVER_IGNORE = "opensearchserver.ignore";
	private final static int OPENSEARCHSERVER_FIELD_LENGTH = OPENSEARCHSERVER_FIELD
			.length();

	private void getBodyTextContent(ParserResultItem result, StringBuilder sb,
			HtmlNodeAbstract<?> node, boolean bAddBlock, String[] directFields,
			int recursion) {
		if (recursion == 0) {
			Logging.warn("Max recursion reached (getBodyTextContent)");
			return;
		}
		recursion--;
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
			String[] classNames = org.apache.commons.lang.StringUtils
					.split(classNameAttribute);
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
			String text = node.getText();
			text = text.replaceAll("\\r", " ");
			text = text.replaceAll("\\n", " ");
			text = StringUtils.replaceConsecutiveSpaces(text, " ");
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
				getBodyTextContent(result, sb, htmlNode, bAddBlock,
						directFields, recursion);

		if (bAddBlock && nodeName != null && sb.length() > 0) {
			String currentTag = nodeName.toLowerCase();
			boolean bForSentence = sb.charAt(sb.length() - 1) != '.'
					&& sentenceTagSet.contains(currentTag);
			if (bForSentence || bEnterDirectField) {
				if (directFields != null)
					result.addDirectFields(directFields, sb.toString());
				else
					addFieldBody(result, currentTag, sb.toString());
				sb.setLength(0);
			}
		}
	}

	protected void addFieldTitle(ParserResultItem result, String value) {
		result.addField(ParserFieldEnum.title, value, titleBoost);
	}

	protected void addFieldBody(ParserResultItem result, String tag,
			String value) {
		BoostTag boostTag = boostTagMap.get(tag);
		Float boost = null;
		if (boostTag != null) {
			boost = boostTag.boost;
			if (boostTag.firstContent == null)
				boostTag.firstContent = value;
		}
		if (boost == null)
			boost = 1.0F;
		result.addField(ParserFieldEnum.body, value, boost);
	}

	private final static String selectCharset(String... charsets) {
		if (charsets.length == 0)
			return null;
		String first = null;
		int position = 0;
		int selected = 0;
		for (String charset : charsets) {
			position++;
			if (charset == null)
				continue;
			if (first == null) {
				first = charset;
				selected = position;
				continue;
			}
			if (!first.equals(charset))
				break;
		}
		if (Logging.isDebug)
			Logging.debug("SelectedCharset : " + first + " (" + selected + '/'
					+ position + ')');
		return first;
	}

	private HtmlDocumentProvider getHtmlDocumentProvider(
			HtmlParserEnum htmlParserEnum, String charset,
			StreamLimiter streamLimiter) throws LimitException, IOException,
			SearchLibException {
		String xPathExclusions = getProperty(ClassPropertyEnum.XPATH_EXCLUSION)
				.getValue();
		boolean isXPath = StringUtils.isEmpty(xPathExclusions);
		HtmlDocumentProvider htmlProvider = htmlParserEnum.getHtmlParser(
				charset, streamLimiter, isXPath);
		if (htmlProvider == null)
			return null;
		if (isXPath) {
			String[] xPathLines = StringUtils.splitLines(xPathExclusions);
			try {
				htmlProvider.removeXPath(xPathLines);
			} catch (XPathExpressionException e) {
				throw new SearchLibException(e);
			}
		}
		return htmlProvider;
	}

	@Override
	protected void parseContent(StreamLimiter streamLimiter,
			LanguageEnum forcedLang) throws IOException, SearchLibException {

		titleBoost = getFloatProperty(ClassPropertyEnum.TITLE_BOOST);
		boostTagMap = new TreeMap<String, BoostTag>();
		boostTagMap.put("h1", new BoostTag(ClassPropertyEnum.H1_BOOST));
		boostTagMap.put("h2", new BoostTag(ClassPropertyEnum.H2_BOOST));
		boostTagMap.put("h3", new BoostTag(ClassPropertyEnum.H3_BOOST));
		boostTagMap.put("h4", new BoostTag(ClassPropertyEnum.H4_BOOST));
		boostTagMap.put("h5", new BoostTag(ClassPropertyEnum.H5_BOOST));
		boostTagMap.put("h6", new BoostTag(ClassPropertyEnum.H6_BOOST));
		ignoreMetaNoIndex = getBooleanProperty(ClassPropertyEnum.IGNORE_META_NOINDEX);
		ignoreMetaNoFollow = getBooleanProperty(ClassPropertyEnum.IGNORE_META_NOFOLLOW);
		ignoreUntitledDocuments = getBooleanProperty(ClassPropertyEnum.IGNORE_UNTITLED_DOCUMENTS);
		ignoreNonCanonical = getBooleanProperty(ClassPropertyEnum.IGNORE_NON_CANONICAL);

		String currentCharset = null;
		String headerCharset = null;
		String detectedCharset = null;

		IndexDocument sourceDocument = getSourceDocument();
		if (sourceDocument != null && urlItemFieldEnum != null) {
			FieldValueItem fieldValueItem = sourceDocument.getFieldValue(
					urlItemFieldEnum.contentTypeCharset.getName(), 0);
			if (fieldValueItem != null)
				headerCharset = fieldValueItem.getValue();
			if (headerCharset == null) {
				fieldValueItem = sourceDocument.getFieldValue(
						urlItemFieldEnum.contentEncoding.getName(), 0);
				if (fieldValueItem != null)
					headerCharset = fieldValueItem.getValue();
			}
			currentCharset = headerCharset;
		}

		if (currentCharset == null) {
			detectedCharset = streamLimiter.getDetectedCharset();
			currentCharset = detectedCharset;
		}

		if (currentCharset == null) {
			currentCharset = getProperty(ClassPropertyEnum.DEFAULT_CHARSET)
					.getValue();
		}

		HtmlParserEnum htmlParserEnum = HtmlParserEnum.find(getProperty(
				ClassPropertyEnum.HTML_PARSER).getValue());

		HtmlDocumentProvider htmlProvider = getHtmlDocumentProvider(
				htmlParserEnum, currentCharset, streamLimiter);
		if (htmlProvider == null)
			return;

		URL currentURL = htmlProvider.getBaseHref();
		IndexDocument srcDoc = getSourceDocument();
		try {
			if (currentURL == null)
				currentURL = LinkUtils.newEncodedURL(streamLimiter
						.getOriginURL());
			if (currentURL == null && urlItemFieldEnum != null) {
				FieldValueItem fvi = srcDoc.getFieldValue(
						urlItemFieldEnum.url.getName(), 0);
				if (fvi != null)
					currentURL = LinkUtils.newEncodedURL(fvi.getValue());
			}
		} catch (URISyntaxException e) {
			throw new IOException(e);
		}

		URL canonicalURL = htmlProvider.getCanonicalLink(currentURL);
		if (canonicalURL != null) {
			addDetectedLink(canonicalURL.toExternalForm());
			if (ignoreNonCanonical) {
				if (!canonicalURL.equals(currentURL)) {
					isCanonical = false;
					return;
				}
			}
		}
		isCanonical = true;

		String title = htmlProvider.getTitle();
		if (ignoreUntitledDocuments)
			if (title == null || title.length() == 0)
				return;

		ParserResultItem result = getNewParserResultItem();

		addFieldTitle(result, title);

		result.addField(ParserFieldEnum.htmlProvider, htmlProvider.getName());

		// Check ContentType charset in meta http-equiv
		String metaCharset = htmlProvider.getMetaCharset();

		String selectedCharset = selectCharset(headerCharset, detectedCharset,
				metaCharset);

		if (selectedCharset != null) {
			if (!selectedCharset.equals(currentCharset)) {
				currentCharset = selectedCharset;
				htmlProvider = getHtmlDocumentProvider(htmlParserEnum,
						currentCharset, streamLimiter);
			}
		}

		StringWriter writer = new StringWriter();
		IOUtils.copy(streamLimiter.getNewInputStream(), writer, currentCharset);
		result.addField(ParserFieldEnum.htmlSource, writer.toString());
		writer.close();

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
					result.addDirectFields(fields, content);
				}
			}
		}

		result.addField(ParserFieldEnum.charset, currentCharset);

		String metaRobots = null;

		String metaDcLanguage = null;

		String metaContentLanguage = null;

		for (HtmlNodeAbstract<?> node : htmlProvider.getMetas()) {
			String attr_name = node.getAttributeText("name");
			String attr_http_equiv = node.getAttributeText("http-equiv");
			if ("keywords".equalsIgnoreCase(attr_name))
				result.addField(ParserFieldEnum.meta_keywords,
						HtmlDocumentProvider.getMetaContent(node));
			else if ("description".equalsIgnoreCase(attr_name))
				result.addField(ParserFieldEnum.meta_description,
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
			if (metaRobots.contains("noindex") && !ignoreMetaNoIndex) {
				metaRobotsNoIndex = true;
				result.addField(ParserFieldEnum.meta_robots, "noindex");
			}
			if (metaRobots.contains("nofollow") && !ignoreMetaNoFollow) {
				metaRobotsFollow = false;
				result.addField(ParserFieldEnum.meta_robots, "nofollow");
			}
		}

		UrlFilterItem[] urlFilterList = getUrlFilterList();

		boolean removeFragment = ClassPropertyEnum.KEEP_REMOVE_LIST[1]
				.equalsIgnoreCase(getProperty(ClassPropertyEnum.URL_FRAGMENT)
						.getValue());

		List<HtmlNodeAbstract<?>> nodes = rootNode.getAllNodes("a", "frame",
				"img");
		if (srcDoc != null && nodes != null && metaRobotsFollow) {
			for (HtmlNodeAbstract<?> node : nodes) {
				String href = null;
				String rel = null;
				String nodeName = node.getNodeName();
				if ("a".equals(nodeName)) {
					href = node.getAttributeText("href");
					rel = node.getAttributeText("rel");
				} else if ("frame".equals(nodeName) || "img".equals(nodeName)) {
					href = node.getAttributeText("src");
				}
				boolean follow = true;
				if (rel != null)
					if (rel.contains("nofollow"))
						follow = false;
				URL newUrl = null;
				if (href != null)
					if (!href.startsWith("javascript:"))
						if (currentURL != null) {
							href = org.apache.commons.lang3.StringEscapeUtils
									.unescapeXml(href);
							newUrl = LinkUtils.getLink(currentURL, href,
									urlFilterList, removeFragment);
						}
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
					String link = newUrl.toExternalForm();
					result.addField(field, link);
					if (follow)
						addDetectedLink(link);
				}
			}
		}

		if (!metaRobotsNoIndex) {
			nodes = rootNode.getNodes("html", "body");
			if (nodes == null || nodes.size() == 0)
				nodes = rootNode.getNodes("html");
			if (nodes != null && nodes.size() > 0) {
				StringBuilder sb = new StringBuilder();
				getBodyTextContent(result, sb, nodes.get(0), true, null, 1024);
				result.addField(ParserFieldEnum.body, sb);
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
			result.addField(ParserFieldEnum.lang, lang.getLanguage());
			result.addField(ParserFieldEnum.lang_method, langMethod);
		} else if (!metaRobotsNoIndex)
			lang = result.langDetection(10000, ParserFieldEnum.body);

		if (getFieldMap().isMapped(ParserFieldEnum.generated_title)) {

			StringBuilder sb = new StringBuilder();
			try {
				sb.append(new URI(streamLimiter.getOriginURL()).getHost());
			} catch (URISyntaxException e) {
				Logging.error(e);
			}

			String generatedTitle = null;
			for (Map.Entry<String, BoostTag> entry : boostTagMap.entrySet()) {
				BoostTag boostTag = entry.getValue();
				if (boostTag.firstContent != null) {
					generatedTitle = boostTag.firstContent;
					break;
				}
			}

			if (generatedTitle == null) {
				final String FIELD_TITLE = "contents";

				MemoryIndex bodyMemoryIndex = new MemoryIndex();
				Analyzer bodyAnalyzer = new WhitespaceAnalyzer(
						Version.LUCENE_36);
				String bodyText = result.getMergedBodyText(100000, " ",
						ParserFieldEnum.body);
				bodyMemoryIndex.addField(FIELD_TITLE, bodyText, bodyAnalyzer);

				IndexSearcher indexSearcher = bodyMemoryIndex.createSearcher();
				IndexReader indexReader = indexSearcher.getIndexReader();
				MoreLikeThis mlt = new MoreLikeThis(indexReader);
				mlt.setAnalyzer(bodyAnalyzer);
				mlt.setFieldNames(new String[] { FIELD_TITLE });
				mlt.setMinWordLen(3);
				mlt.setMinTermFreq(1);
				mlt.setMinDocFreq(1);

				String[] words = mlt.retrieveInterestingTerms(0);
				if (words != null && words.length > 0)
					generatedTitle = words[0];
			}

			if (generatedTitle != null) {
				if (sb.length() > 0)
					sb.append(" - ");
				sb.append(generatedTitle);
			}

			if (sb.length() > 67) {
				int pos = sb.indexOf(" ", 60);
				if (pos == -1)
					pos = 67;
				sb.setLength(pos);
				sb.append("...");
			}
			result.addField(ParserFieldEnum.generated_title, sb.toString());
		}

	}

	/**
	 * @return the isCanonical
	 */
	public boolean isCanonical() {
		return isCanonical;
	}

}
