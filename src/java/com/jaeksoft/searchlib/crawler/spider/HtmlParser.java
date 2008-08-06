/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.crawler.spider;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.knallgrau.utils.textcat.TextCategorizer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Configuration;
import org.w3c.tidy.Tidy;

import com.jaeksoft.searchlib.crawler.urldb.IndexStatus;
import com.jaeksoft.searchlib.crawler.urldb.ParserStatus;
import com.jaeksoft.searchlib.index.FieldContent;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.util.Lang;
import com.jaeksoft.searchlib.util.XPathParser;

public class HtmlParser implements Parser {

	final private static Logger logger = Logger.getLogger(HtmlParser.class);

	private String title;
	private ArrayList<String> textBlock;
	private String metaKeywords;
	private String metaDescription;
	private Locale lang;
	private String langMethod;
	private boolean metaRobotsIndex;
	private boolean metaRobotsFollow;
	private LinkList outlinks;
	private LinkList inlinks;

	private static HashSet<String> sentenceTagSet = null;

	public HtmlParser() {
		super();
		title = null;
		textBlock = null;
		metaKeywords = null;
		metaDescription = null;
		outlinks = null;
		inlinks = null;
		metaRobotsIndex = true;
		metaRobotsFollow = true;
		if (sentenceTagSet == null) {
			sentenceTagSet = new HashSet<String>();
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

	private static String getMetaContent(XPathParser xpp, String query)
			throws XPathExpressionException {
		NodeList nodes = xpp.getNodeList(query);
		if (nodes == null)
			return null;
		if (nodes.getLength() < 1)
			return null;
		String mc = XPathParser.getAttributeString(nodes.item(0), "content");
		if (mc == null)
			return null;
		return StringEscapeUtils.unescapeHtml(mc);
	}

	private static void getTextContent(ArrayList<String> textBlock,
			StringBuffer sb, Node node) {
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
			getTextContent(textBlock, sb, children.item(i));
		if (sb.length() > 0 && sb.charAt(sb.length() - 1) != '.'
				&& sentenceTagSet.contains(nodeName.toLowerCase())) {
			addBlock(textBlock, sb);
		}
	}

	private static TextCategorizer textCategorizer = new TextCategorizer();

	private static void addBlock(ArrayList<String> textBlock, StringBuffer sb) {
		String text = sb.toString().trim();
		sb.setLength(0);
		if (text.length() == 0)
			return;
		String[] frags = text.split("\\. ");
		for (String frag : frags)
			textBlock.add(frag);
	}

	public void parseContent(Crawl crawl, InputStream inputStream)
			throws IOException {

		Tidy tidy = new Tidy();
		tidy.setQuiet(true);
		tidy.setOnlyErrors(false);
		tidy.setShowWarnings(false);

		// On recherche le charset dans le contentType HTTP.
		String charset = null;
		if (crawl.getContentType() != null)
			charset = crawl.getContentType().getParameter("charset");
		if (charset != null) {
			charset = charset.toLowerCase();
			if ("utf-8".equals(charset))
				tidy.setCharEncoding(Configuration.UTF8);
			else if ("iso-8859-1".equals(charset))
				tidy.setCharEncoding(Configuration.LATIN1);
			else if ("macroman".equals(charset))
				tidy.setCharEncoding(Configuration.MACROMAN);
			else if ("iso-2022".equals(charset))
				tidy.setCharEncoding(Configuration.ISO2022);
		}

		// Cr�ation de l'arbre DOM avec Tidy
		Document document = tidy.parseDOM(inputStream, null);
		XPathParser xpp = new XPathParser(document);

		try {
			// R�cup�ration du titre
			title = xpp.getNodeString("/html/head/title");
			if (title == null || title.length() == 0)
				title = xpp.getNodeString("//head/title");
			if (title == null || title.length() == 0)
				title = xpp.getNodeString("//title");

			// R�cup�ration des metas
			metaKeywords = getMetaContent(xpp,
					"/html/head/meta[translate(@name,'KEYWORDS', 'keywords')='keywords']");
			metaDescription = getMetaContent(xpp,
					"/html/head/meta[translate(@name,'DESCRIPTION', 'description')='description']");
			String metaRobots = getMetaContent(xpp,
					"/html/head/meta[translate(@name,'ROBOTS', 'robots')='robots']");
			if (metaRobots != null) {
				metaRobots = metaRobots.toLowerCase();
				if (metaRobots.contains("noindex"))
					metaRobotsIndex = false;
				if (metaRobots.contains("nofollow"))
					metaRobotsFollow = false;
			}

			// R�cup�ration des liens
			NodeList nodes = xpp.getNodeList("//*[@href]");
			if (nodes != null) {
				outlinks = new LinkList();
				inlinks = new LinkList();
				URL currentURL = crawl.getUrlItem().getURL();
				for (int i = 0; i < nodes.getLength(); i++) {
					Node node = nodes.item(i);
					String href = XPathParser.getAttributeString(node, "href");
					String rel = XPathParser.getAttributeString(node, "rel");
					boolean follow = true;
					if (rel != null)
						if (rel.contains("nofollow"))
							follow = false;
					Link link = null;
					if (href != null)
						if (!href.startsWith("javascript:"))
							link = Link
									.getLink(currentURL, href, follow, false);
					if (link != null) {
						LinkList links = null;
						if (link.getType() == Link.Type.INLINK)
							links = inlinks;
						else if (link.getType() == Link.Type.OUTLINK)
							links = outlinks;
						Link oldLink = links.get(link.getUrl());
						if (oldLink == null)
							links.put(link.getUrl(), link);
						else
							oldLink.increment();
					}
				}
			}

			// R�cup�ration du texte dans le body
			Node node = xpp.getNode("/html/body");
			if (node != null) {
				StringBuffer sb = new StringBuffer();
				textBlock = new ArrayList<String>();
				getTextContent(textBlock, sb, node);
				addBlock(textBlock, sb);
			}

			// Identification de la langue:
			node = xpp.getNode("/html[@lang]");
			if (node != null) {
				langMethod = "html lang attribute";
				String l = XPathParser.getAttributeString(node, "lang");
				if (l != null)
					lang = Lang.findLocaleISO639(l);
			}
			if (lang == null) {
				langMethod = "meta http-equiv content-language";
				lang = Lang
						.findLocaleISO639(getMetaContent(
								xpp,
								"/html/head/meta[translate(@http-equiv,'CONTENT-LANGUAGE', 'content-language')='content-language']"));
			}
			if (lang == null) {
				langMethod = "meta dc.language";
				lang = Lang
						.findLocaleISO639(getMetaContent(xpp,
								"/html/head/meta[translate(@name,'DC.LANGUAGE', 'dc.language')='dc.language']"));
			}
			if (lang == null && textBlock != null) {
				langMethod = "ngram recognition";
				StringBuffer sb = new StringBuffer();
				for (String s : textBlock) {
					sb.append(s);
					if (sb.length() > 1000)
						break;
				}
				String textcat = textCategorizer
						.categorize(sb.toString(), 1000);
				lang = Lang.findLocaleDescription(textcat);
			}
			if (!metaRobotsIndex)
				crawl.getUrlItem().setIndexStatus(IndexStatus.META_NOINDEX);
			crawl.getUrlItem().setParserStatus(ParserStatus.PARSED);
		} catch (XPathExpressionException e) {
			logger.error(e.getMessage(), e);
			crawl.getUrlItem().setParserStatus(ParserStatus.PARSER_ERROR);
			crawl.setError(e.getMessage());
		}
	}

	public void xmlInfo(PrintWriter writer, HashSet<String> classDetail) {
		writer.print("<htmlParser>");
		if (title != null)
			writer.println("<title>" + StringEscapeUtils.escapeXml(title)
					+ "</title>");
		if (metaKeywords != null)
			writer.println("<metakeyword>"
					+ StringEscapeUtils.escapeXml(metaKeywords)
					+ "</metakeyword>");
		if (metaDescription != null)
			writer.println("<metaDescription>"
					+ StringEscapeUtils.escapeXml(metaDescription)
					+ "</metaDescription>");
		writer.println("<metaRobots index=\"" + metaRobotsIndex
				+ "\" follow=\"" + metaRobotsFollow + "\" />");
		writer.println("<lang method=\"" + langMethod + "\">"
				+ lang.getLanguage() + "</lang>");
		if (textBlock != null)
			writer.println("<textContent lenth=\"" + textBlock.size() + "\">"
					+ StringEscapeUtils.escapeXml(textBlock.toString())
					+ "</textContent>");
		if (inlinks != null) {
			writer.println("<inlinks num=\"" + inlinks.size() + "\">");
			for (Link link : inlinks.values())
				link.xmlInfo(writer, classDetail);
			writer.println("</inlinks>");
		}
		if (outlinks != null) {
			writer.println("<outlinks num=\"" + outlinks.size() + "\">");
			for (Link link : outlinks.values())
				link.xmlInfo(writer, classDetail);
			writer.println("</outlinks>");
		}
		writer.println("</htmlParser>");
	}

	public IndexDocument getDocument() {
		IndexDocument document = new IndexDocument(lang);
		if (!metaRobotsIndex)
			return null;
		if (title != null)
			document.add("title", title);
		if (metaDescription != null)
			document.add("meta_description", metaDescription);
		if (metaKeywords != null)
			document.add("meta_keywords", metaKeywords);
		if (textBlock != null)
			for (String s : textBlock)
				if (s != null)
					document.add("content", s);
		if (outlinks != null) {
			FieldContent fieldContent = new FieldContent("outlink");
			outlinks.populate(fieldContent);
			document.set(fieldContent);
		}
		if (inlinks != null) {
			FieldContent fieldContent = new FieldContent("inlink");
			inlinks.populate(fieldContent);
			document.set(fieldContent);
		}
		if (lang != null)
			document.add("lang", lang.getLanguage());
		return document;
	}

	public LinkList getOutlinks() {
		return this.outlinks;
	}

	public LinkList getInlinks() {
		return this.inlinks;
	}
}
