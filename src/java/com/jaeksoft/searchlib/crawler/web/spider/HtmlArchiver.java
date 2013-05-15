/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.web.spider;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.ClientProtocolException;
import org.htmlcleaner.ContentNode;
import org.htmlcleaner.TagNode;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.CSSImportRule;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleRule;
import org.w3c.dom.css.CSSStyleSheet;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.stylesheets.MediaList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.parser.htmlParser.HtmlCleanerParser;
import com.jaeksoft.searchlib.util.LinkUtils;
import com.jaeksoft.searchlib.util.ParserErrorHandler;
import com.jaeksoft.searchlib.util.RegExpUtils;
import com.jaeksoft.searchlib.util.ThreadUtils.RecursiveTracker;
import com.jaeksoft.searchlib.util.ThreadUtils.RecursiveTracker.RecursiveEntry;
import com.steadystate.css.parser.CSSOMParser;

public class HtmlArchiver {

	final File filesDir;
	final File indexFile;
	final File sourceFile;
	final Map<String, Integer> fileCountMap;
	final Map<String, String> urlFileMap;
	final URL url;
	final HttpDownloader downloader;
	final RecursiveTracker recursiveSecurity;

	public HtmlArchiver(File parentDir, HttpDownloader httpDownloader, URL url) {
		filesDir = new File(parentDir, "files");
		indexFile = new File(parentDir, "index.html");
		sourceFile = new File(parentDir, "source.html");
		this.url = url;
		this.downloader = httpDownloader;
		fileCountMap = new TreeMap<String, Integer>();
		urlFileMap = new TreeMap<String, String>();
		filesDir.mkdir();
		recursiveSecurity = new RecursiveTracker(20);
	}

	final private static String buildFileName(String baseName,
			String extension, Integer fileCount) {
		if (baseName.length() > 160)
			baseName = baseName.substring(0, 160);
		if (extension.length() > 32)
			extension = extension.substring(0, 32);
		StringBuffer sb = new StringBuffer(baseName);
		if (fileCount != null && fileCount > 0) {
			sb.append('_');
			sb.append(fileCount);
		}
		if (extension != null && extension.length() > 0) {
			sb.append('.');
			sb.append(extension);
		}
		return sb.toString();
	}

	final private String getLocalPath(URL parentUrl, String fileName) {
		if (!parentUrl.equals(url))
			return fileName;
		StringBuffer sb = new StringBuffer("./");
		sb.append(filesDir.getName());
		sb.append('/');
		sb.append(fileName);
		return sb.toString();
	}

	final private String downloadObject(URL parentUrl, String src)
			throws ClientProtocolException, IllegalStateException, IOException,
			SearchLibException, URISyntaxException {
		RecursiveEntry recursiveEntry = recursiveSecurity.enter();
		if (recursiveEntry == null) {
			Logging.warn("Max recursion reached - " + recursiveSecurity
					+ " src: " + src + " url: " + parentUrl);
			return src;
		}
		try {
			URL objectURL = LinkUtils.getLink(parentUrl, src, null, false);
			if (objectURL == null)
				return src;
			String urlString = objectURL.toExternalForm();
			String fileName = urlFileMap.get(urlString);
			if (fileName != null)
				return getLocalPath(parentUrl, fileName);
			DownloadItem downloadItem = downloader.get(objectURL.toURI(), null);
			fileName = downloadItem.getFileName();
			if (fileName == null || fileName.length() == 0)
				return src;
			if (!downloadItem.checkNoError(200, 300)) {
				Logging.warn("WRONG HTTP CODE: " + downloadItem.getStatusCode()
						+ " src: " + src + " url: " + urlString);
				return src;
			}
			String baseName = FilenameUtils.getBaseName(fileName);
			String extension = FilenameUtils.getExtension(fileName);
			String contentType = downloadItem.getContentBaseType();
			if ("text/html".equalsIgnoreCase(contentType))
				extension = "html";
			if ("text/javascript".equalsIgnoreCase(contentType))
				extension = "js";
			if ("text/css".equalsIgnoreCase(contentType))
				extension = "css";
			fileName = buildFileName(baseName, extension, null);
			Integer fileCount = fileCountMap.get(fileName);
			fileCount = fileCount == null ? new Integer(0) : fileCount + 1;
			fileCountMap.put(fileName, fileCount);
			fileName = buildFileName(baseName, extension, fileCount);
			File destFile = new File(filesDir, fileName);
			if ("css".equals(extension)) {
				StringBuffer sb = checkCSSContent(objectURL,
						downloadItem.getContentAsString());
				FileUtils.write(destFile, sb);
			} else
				downloadItem.writeToFile(destFile);
			urlFileMap.put(urlString, fileName);
			return getLocalPath(parentUrl, fileName);
		} finally {
			recursiveEntry.release();
		}
	}

	final private Pattern cssUrlPattern = Pattern
			.compile("(?s)[\\s]*url\\([\"']?(.*?)[\"']?\\)");

	final private Pattern cssErronousCommentPattern = Pattern
			.compile("(?m)^(\\/)$");

	// .compile("(?s)^[\\s]*[/]{1}[\\s]*$");

	final private StringBuffer checkCSSContent(URL parentUrl, String css)
			throws ClientProtocolException, IllegalStateException, IOException,
			SearchLibException, URISyntaxException {
		StringWriter sw = null;
		PrintWriter pw = null;
		css = RegExpUtils.replace(css, cssErronousCommentPattern, "");
		try {
			CSSOMParser parser = new CSSOMParser();
			parser.setErrorHandler(ParserErrorHandler.LOGONLY_ERROR_HANDLER);
			CSSStyleSheet stylesheet = parser.parseStyleSheet(new InputSource(
					new StringReader(css)), null, null);
			CSSRuleList ruleList = stylesheet.getCssRules();
			sw = new StringWriter();
			pw = new PrintWriter(sw);
			for (int i = 0; i < ruleList.getLength(); i++) {
				CSSRule rule = ruleList.item(i);
				if (rule instanceof CSSStyleRule) {
					CSSStyleRule styleRule = (CSSStyleRule) rule;
					CSSStyleDeclaration styleDeclaration = styleRule.getStyle();
					for (int j = 0; j < styleDeclaration.getLength(); j++) {
						String property = styleDeclaration.item(j);
						CSSValue cssValue = styleDeclaration
								.getPropertyCSSValue(property);
						String value = cssValue.getCssText();
						List<String> urls = RegExpUtils.getGroups(
								cssUrlPattern, value);
						if (urls != null && urls.size() > 0) {
							String newSrc = downloadObject(parentUrl,
									urls.get(0));
							String newValue = RegExpUtils.replace(value,
									cssUrlPattern, "url('" + newSrc + "')");
							cssValue.setCssText(newValue);
						}
					}
					pw.println(rule.getCssText());
				} else if (rule instanceof CSSImportRule) {
					CSSImportRule importRule = (CSSImportRule) rule;
					String newSrc = downloadObject(parentUrl,
							importRule.getHref());
					pw.print("@import url('");
					pw.print(newSrc);
					pw.print("')");
					MediaList mediaList = importRule.getMedia();
					boolean first = true;
					for (int k = 0; k < mediaList.getLength(); k++) {
						if (!first) {
							pw.println(", ");
							first = false;
						} else
							pw.print(' ');
						pw.print(mediaList.item(k));
					}
					pw.println(";");
				}
			}
			return sw.getBuffer();
		} finally {
			if (pw != null)
				IOUtils.closeQuietly(pw);
			if (sw != null)
				IOUtils.closeQuietly(sw);
		}
	}

	final private Pattern cssRemoveStartingComment1 = Pattern
			.compile("(?s)^[\\s]*<!--");

	final private Pattern cssRemoveStartingComment2 = Pattern
			.compile("(?s)^[\\s]*&lt;!--");

	final private Pattern cssRemoveEndingComment1 = Pattern
			.compile("(?s)--&gt;[\\s]*$");

	final private Pattern cssRemoveEndingComment2 = Pattern
			.compile("(?s)-->[\\s]*$");

	final private Pattern[] cssCommentPatterns = { cssRemoveStartingComment1,
			cssRemoveStartingComment2, cssRemoveEndingComment1,
			cssRemoveEndingComment2 };

	final private void checkStyleCSS(TagNode node)
			throws ClientProtocolException, IllegalStateException, IOException,
			SearchLibException, URISyntaxException {
		if (!("style".equalsIgnoreCase(node.getName())))
			return;
		String attr = node.getAttributeByName("type");
		if (!"text/css".equalsIgnoreCase(attr))
			return;
		attr = node.getAttributeByName("media");
		if (attr != null)
			if (!"screen".equals(attr))
				return;
		StringBuilder builder = (StringBuilder) node.getText();
		if (builder == null)
			return;
		String cssString = builder.toString();
		for (Pattern p : cssCommentPatterns)
			cssString = RegExpUtils.replace(cssString, p, "");
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		try {
			pw.println("<!--");
			pw.println(checkCSSContent(url, cssString));
			pw.println("-->");
			node.removeAllChildren();
			node.addChild(new ContentNode(sw.toString()));
		} finally {
			IOUtils.closeQuietly(pw);
			IOUtils.closeQuietly(sw);
		}
	}

	final private void downloadObjectFromTag(TagNode node, String tagName,
			String attributeName) throws ClientProtocolException,
			IllegalStateException, IOException, SearchLibException,
			URISyntaxException {
		if (tagName != null)
			if (!tagName.equalsIgnoreCase(node.getName()))
				return;
		String src = node.getAttributeByName(attributeName);
		if (src == null)
			return;
		String newSrc = downloadObject(url, src);
		if (newSrc != null)
			node.addAttribute(attributeName, newSrc);
	}

	final private void removeTag(TagNode node, String... tagNames) {
		if (tagNames == null)
			return;
		String nodeName = node.getName();
		for (String tagName : tagNames) {
			if (tagName.equalsIgnoreCase(nodeName)) {
				node.removeFromTree();
				return;
			}
		}
	}

	final private void recursiveArchive(TagNode node)
			throws ClientProtocolException, IllegalStateException, IOException,
			SearchLibException, URISyntaxException {
		if (node == null)
			return;
		removeTag(node, "base");
		downloadObjectFromTag(node, null, "src");
		downloadObjectFromTag(node, "link", "href");
		checkStyleCSS(node);
		TagNode[] nodes = node.getChildTags();
		if (nodes == null)
			return;
		for (TagNode n : nodes)
			recursiveArchive(n);
	}

	final public void archive(String pageSource)
			throws ClientProtocolException, IllegalStateException, IOException,
			SearchLibException, URISyntaxException, ClassCastException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException, ParserConfigurationException, SAXException {

		HtmlCleanerParser htmlCleanerParser = new HtmlCleanerParser();
		htmlCleanerParser.init(pageSource);
		recursiveArchive(htmlCleanerParser.getTagNode());
		htmlCleanerParser.writeHtmlToFile(indexFile);
		String charset = htmlCleanerParser.findCharset();
		if (charset == null)
			FileUtils.write(sourceFile, pageSource);
		else
			FileUtils.write(sourceFile, pageSource, charset);
	}
}
