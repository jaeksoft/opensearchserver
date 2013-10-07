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
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.conn.HttpHostConnectException;
import org.htmlcleaner.ContentNode;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.browser.BrowserDriver;
import com.jaeksoft.searchlib.crawler.web.spider.NaiveCSSParser.CSSImportRule;
import com.jaeksoft.searchlib.crawler.web.spider.NaiveCSSParser.CSSProperty;
import com.jaeksoft.searchlib.crawler.web.spider.NaiveCSSParser.CSSRule;
import com.jaeksoft.searchlib.crawler.web.spider.NaiveCSSParser.CSSStyleRule;
import com.jaeksoft.searchlib.parser.htmlParser.HtmlCleanerParser;
import com.jaeksoft.searchlib.util.LinkUtils;
import com.jaeksoft.searchlib.util.ThreadUtils.RecursiveTracker;
import com.jaeksoft.searchlib.util.ThreadUtils.RecursiveTracker.RecursiveEntry;

public class HtmlArchiver {

	private final BrowserDriver<?> browserDriver;
	private final File filesDir;
	private final File indexFile;
	private final File sourceFile;
	private final Map<String, Integer> fileCountMap;
	private final Map<String, String> urlFileMap;
	private final URL pageUrl;
	private final HttpDownloader downloader;
	private final RecursiveTracker recursiveSecurity;
	private URL baseUrl;

	public HtmlArchiver(BrowserDriver<?> browserDriver, File parentDir,
			HttpDownloader httpDownloader, URL url) {
		this.browserDriver = browserDriver;
		filesDir = new File(parentDir, "files");
		indexFile = new File(parentDir, "index.html");
		sourceFile = new File(parentDir, "source.html");
		this.pageUrl = url;
		this.baseUrl = url;
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
		if (parentUrl == null
				|| urlFileMap.get(parentUrl.toExternalForm()) != null)
			return fileName;
		StringBuffer sb = new StringBuffer("./");
		sb.append(filesDir.getName());
		sb.append('/');
		sb.append(fileName);
		return sb.toString();
	}

	final public File getLocalFile(String fileName) {
		return new File(filesDir, fileName);
	}

	final public String getUrlFileName(String src) {
		if (urlFileMap == null)
			return null;
		URL objectURL = LinkUtils.getLink(pageUrl, src, null, false);
		String url = objectURL == null ? src : objectURL.toExternalForm();
		return urlFileMap.get(url);
	}

	final private File getAndRegisterDestFile(String urlString,
			String baseName, String extension) {
		String fileName = buildFileName(baseName, extension, null);
		Integer fileCount = fileCountMap.get(fileName);
		fileCount = fileCount == null ? new Integer(0) : fileCount + 1;
		fileCountMap.put(fileName, fileCount);
		fileName = buildFileName(baseName, extension, fileCount);
		urlFileMap.put(urlString, fileName);
		return new File(filesDir, fileName);
	}

	final private String downloadObject(URL parentUrl, String src,
			String contentType) throws ClientProtocolException,
			IllegalStateException, IOException, SearchLibException,
			URISyntaxException {
		RecursiveEntry recursiveEntry = recursiveSecurity.enter();
		if (recursiveEntry == null) {
			Logging.warn("Max recursion reached - " + recursiveSecurity
					+ " src: " + src + " url: " + parentUrl);
			return src;
		}
		try {
			src = StringEscapeUtils.unescapeXml(src);
			URL objectURL = LinkUtils.getLink(parentUrl, src, null, false);
			if (objectURL == null)
				return src;
			if (objectURL.equals(pageUrl)) {
				return "index.html";
			}
			String urlString = objectURL.toExternalForm();
			String fileName = urlFileMap.get(urlString);
			if (fileName != null)
				return getLocalPath(parentUrl, fileName);
			DownloadItem downloadItem = null;
			try {
				downloadItem = downloader.get(objectURL.toURI(), null);
			} catch (IOException e) {
				Logging.warn("IO Exception on " + objectURL.toURI(), e);
				return src;
			}
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
			if (contentType == null)
				contentType = downloadItem.getContentBaseType();
			if ("text/html".equalsIgnoreCase(contentType))
				extension = "html";
			else if ("text/javascript".equalsIgnoreCase(contentType))
				extension = "js";
			else if ("text/css".equalsIgnoreCase(contentType))
				extension = "css";
			else if ("application/x-shockwave-flash"
					.equalsIgnoreCase(contentType))
				extension = "swf";
			else if ("image/png".equalsIgnoreCase(contentType))
				extension = "png";
			else if ("image/gif".equalsIgnoreCase(contentType))
				extension = "gif";
			else if ("image/jpeg".equalsIgnoreCase(contentType))
				extension = "jpg";
			else if ("image/jpg".equalsIgnoreCase(contentType))
				extension = "jpg";
			File destFile = getAndRegisterDestFile(urlString, baseName,
					extension);
			if ("css".equals(extension)) {
				String cssContent = downloadItem.getContentAsString();
				StringBuffer sb = checkCSSContent(objectURL, cssContent);
				if (sb != null && sb.length() > 0)
					cssContent = sb.toString();
				FileUtils.write(destFile, cssContent);
			} else
				downloadItem.writeToFile(destFile);

			return getLocalPath(parentUrl, destFile.getName());
		} catch (HttpHostConnectException e) {
			Logging.warn(e);
			return src;
		} catch (UnknownHostException e) {
			Logging.warn(e);
			return src;
		} finally {
			recursiveEntry.release();
		}
	}

	final private boolean handleCssProperty(URL objectUrl, CSSProperty property)
			throws ClientProtocolException, IllegalStateException, IOException,
			SearchLibException, URISyntaxException {
		if (property == null)
			return false;
		String oldValue = property.getValue();
		if (oldValue == null)
			return false;
		Matcher matcher = NaiveCSSParser.findUrl(oldValue);
		if (!matcher.find())
			return false;
		String url = matcher.group(1);
		if (url == null || url.length() == 0)
			return false;
		String newSrc = downloadObject(objectUrl, url, null);
		if (newSrc == null)
			return false;
		property.setValue(NaiveCSSParser.replaceUrl(oldValue, matcher, newSrc));
		return true;
	}

	final private boolean handleCssStyle(URL objectUrl, CSSStyleRule rule)
			throws ClientProtocolException, IllegalStateException, IOException,
			SearchLibException, URISyntaxException {
		boolean change = false;
		for (CSSProperty property : rule.getProperties()) {
			if (handleCssProperty(objectUrl, property))
				change = true;
		}
		return change;
	}

	final private StringBuffer checkCSSContent(URL objectUrl, String css)
			throws ClientProtocolException, IllegalStateException, IOException,
			SearchLibException, URISyntaxException {
		StringWriter sw = null;
		PrintWriter pw = null;

		try {
			NaiveCSSParser cssParser = new NaiveCSSParser();
			Collection<CSSRule> rules = cssParser.parseStyleSheet(css);
			if (rules == null)
				return null;
			if (rules.size() == 0)
				return null;
			sw = new StringWriter();
			pw = new PrintWriter(sw);
			for (CSSRule rule : rules) {
				if (rule instanceof CSSStyleRule) {
					handleCssStyle(objectUrl, (CSSStyleRule) rule);
				} else if (rule instanceof CSSImportRule) {
					CSSImportRule importRule = (CSSImportRule) rule;
					String newSrc = downloadObject(objectUrl,
							importRule.getHref(), "text/css");
					importRule.setHref(newSrc);
				}
			}
			cssParser.write(pw);
			return sw.getBuffer();
		} catch (IOException e) {
			Logging.warn("CSS ISSUE", e);
			return null;
		} finally {
			if (pw != null)
				IOUtils.closeQuietly(pw);
			if (sw != null)
				IOUtils.closeQuietly(sw);
		}
	}

	final private void checkStyleCSS(TagNode node)
			throws ClientProtocolException, IllegalStateException, IOException,
			SearchLibException, URISyntaxException {
		if (!("style".equalsIgnoreCase(node.getName())))
			return;
		String attr = node.getAttributeByName("type");
		if (attr != null && attr.length() > 0
				&& !"text/css".equalsIgnoreCase(attr))
			return;
		attr = node.getAttributeByName("media");
		if (attr != null && attr.length() > 0 && !"screen".equals(attr))
			return;
		StringBuilder builder = (StringBuilder) node.getText();
		if (builder == null)
			return;
		String content = builder.toString();
		String newContent = StringEscapeUtils.unescapeXml(content);
		StringBuffer sb = checkCSSContent(baseUrl, newContent);
		if (sb != null)
			newContent = sb.toString();
		if (newContent.equals(content))
			return;
		node.removeAllChildren();
		node.addChild(new ContentNode(newContent));
	}

	final private void checkStyleAttribute(TagNode node)
			throws ClientProtocolException, IllegalStateException, IOException,
			SearchLibException, URISyntaxException {
		String style = node.getAttributeByName("style");
		if (style == null)
			return;
		if (style.length() == 0)
			return;

		NaiveCSSParser cssParser = new NaiveCSSParser();
		CSSStyleRule cssStyle = cssParser.parseStyleAttribute(style);
		if (!handleCssStyle(baseUrl, cssStyle))
			return;
		node.addAttribute("style", cssStyle.getPropertyString());
	}

	final boolean hasAncestorId(String[] ids, TagNode node) {
		if (node == null)
			return false;
		String nodeId = node.getAttributeByName("id");
		if (nodeId != null)
			for (String id : ids)
				if (id.equalsIgnoreCase(nodeId))
					return true;
		return hasAncestorId(ids, node.getParent());
	}

	final boolean hasAncestorXPath(Set<TagNode> xpathSelectorSet, TagNode node) {
		if (node == null)
			return false;
		if (xpathSelectorSet.contains(node))
			return true;
		return hasAncestorXPath(xpathSelectorSet, node.getParent());
	}

	final private void checkScriptContent(TagNode node,
			Set<TagNode> disableScriptNodeSet) {
		if (!("script".equalsIgnoreCase(node.getName())))
			return;
		if (disableScriptNodeSet != null
				&& hasAncestorXPath(disableScriptNodeSet, node)) {
			node.removeFromTree();
			return;
		}
		StringBuilder builder = (StringBuilder) node.getText();
		if (builder == null)
			return;
		String content = builder.toString();
		if (content == null)
			return;
		String newContent = StringEscapeUtils.unescapeXml(content);
		if (newContent.equals(content))
			return;
		node.removeAllChildren();
		node.addChild(new ContentNode(newContent));
	}

	final private String downloadIframe(URL parentUrl, TagNode node,
			Map<TagNode, WebElement> iframeNodeMap,
			List<ClickCaptureResult> clickCaptureResults) throws IOException,
			ParserConfigurationException, SAXException, IllegalStateException,
			SearchLibException, URISyntaxException {
		if (iframeNodeMap == null) {
			Logging.warn("Unable to download IFRAME (no iframeNodeNap) " + node);
			return null;
		}
		WebElement webElement = iframeNodeMap.get(node);
		if (webElement == null) {
			Logging.warn("Issue when finding IFRAME for " + node);
			return null;
		}
		URL oldBaseUrl = baseUrl;
		String src = node.getAttributeByName("src");
		baseUrl = LinkUtils.getLink(parentUrl, src, null, false);
		String urlFileMapKey = null;
		if (baseUrl != null
				&& !urlFileMap.containsKey(baseUrl.toExternalForm()))
			urlFileMapKey = baseUrl.toExternalForm();
		else
			urlFileMapKey = Integer.toString(node.hashCode());
		File destFile = getAndRegisterDestFile(urlFileMapKey, "iframe", "html");
		String iFrameXPath = browserDriver.getXPath(webElement, true);
		browserDriver.switchToFrame(webElement);
		String frameSource = browserDriver.getSourceCode();
		if (clickCaptureResults != null)
			ClickCaptureResult.locateIFrame(clickCaptureResults, browserDriver,
					iFrameXPath);
		browserDriver.switchToMain();
		HtmlCleanerParser htmlCleanerParser = new HtmlCleanerParser();
		htmlCleanerParser.init(frameSource);
		recursiveArchive(htmlCleanerParser.getTagNode(), null, iframeNodeMap,
				clickCaptureResults);
		htmlCleanerParser.writeHtmlToFile(destFile);
		baseUrl = oldBaseUrl;
		return getLocalPath(parentUrl, destFile.getName());
	}

	final private boolean downloadObjectIframe(TagNode node,
			Map<TagNode, WebElement> iframeNodeMap,
			List<ClickCaptureResult> clickCaptureResults)
			throws IllegalStateException, IOException,
			ParserConfigurationException, SAXException, SearchLibException,
			URISyntaxException {
		if (!"iframe".equalsIgnoreCase(node.getName()))
			return false;
		String src = downloadIframe(baseUrl, node, iframeNodeMap,
				clickCaptureResults);
		if (src != null)
			node.addAttribute("src", src);
		return true;
	}

	final private boolean downloadObjectSrc(TagNode node)
			throws ClientProtocolException, IllegalStateException, IOException,
			SearchLibException, URISyntaxException {
		String src = node.getAttributeByName("src");
		if (src == null)
			return false;
		src = downloadObject(baseUrl, src, null);
		if (src != null)
			node.addAttribute("src", src);
		return true;
	}

	final private boolean downloadObjectLink(TagNode node)
			throws ClientProtocolException, IllegalStateException, IOException,
			SearchLibException, URISyntaxException,
			ParserConfigurationException, SAXException {
		String src = node.getAttributeByName("href");
		if (src == null)
			return false;
		String type = node.getAttributeByName("type");
		if (type == null && node.getName().equalsIgnoreCase("script"))
			type = "text/javascript";
		if (type == null)
			return false;
		src = downloadObject(baseUrl, src, type);
		if (src != null)
			node.addAttribute("href", src);
		return true;
	}

	final private void checkBaseHref(TagNode node) {
		if (node == null)
			return;
		if (!"base".equalsIgnoreCase(node.getName()))
			return;
		String href = node.getAttributeByName("href");
		if (href != null) {
			try {
				baseUrl = new URL(href);
			} catch (MalformedURLException e) {
				Logging.warn(e);
				return;
			}
		}
		node.removeFromTree();
	}

	final private void recursiveArchive(TagNode node,
			Set<TagNode> disableScriptNodeSet,
			Map<TagNode, WebElement> iframeNodeMap,
			List<ClickCaptureResult> clickCaptureResults)
			throws ClientProtocolException, IllegalStateException, IOException,
			SearchLibException, URISyntaxException,
			ParserConfigurationException, SAXException {
		if (node == null)
			return;
		checkBaseHref(node);
		if (!downloadObjectIframe(node, iframeNodeMap, clickCaptureResults))
			if (!downloadObjectSrc(node))
				downloadObjectLink(node);
		checkStyleCSS(node);
		checkScriptContent(node, disableScriptNodeSet);
		checkStyleAttribute(node);
		TagNode[] nodes = node.getChildTags();
		if (nodes == null)
			return;
		for (TagNode n : nodes)
			recursiveArchive(n, disableScriptNodeSet, iframeNodeMap,
					clickCaptureResults);
	}

	final public void archive(BrowserDriver<?> browserDriver,
			Set<String> xPathDisableScriptSet,
			List<ClickCaptureResult> clickCaptureResults) throws IOException,
			ParserConfigurationException, SAXException, IllegalStateException,
			SearchLibException, URISyntaxException, XPatherException {
		String pageSource = browserDriver.getSourceCode();
		HtmlCleanerParser htmlCleanerParser = new HtmlCleanerParser();
		htmlCleanerParser.init(pageSource);
		// Find iframe
		Set<WebElement> iframeWebElementSet = new HashSet<WebElement>();
		browserDriver.locateBy(By.tagName("iframe"), iframeWebElementSet, true);
		Map<TagNode, WebElement> iframeNodeMap = null;
		if (iframeWebElementSet != null && iframeWebElementSet.size() > 0) {
			iframeNodeMap = new HashMap<TagNode, WebElement>();
			Set<TagNode> tagNodeSet = new HashSet<TagNode>();
			for (WebElement webElement : iframeWebElementSet) {
				String xPath = browserDriver.getXPath(webElement, true);
				if (xPath == null)
					continue;
				if (htmlCleanerParser.xpath(xPath, tagNodeSet) == 0) {
					Logging.warn("DisableScript not found using XPath: "
							+ xPath);
					continue;
				}
				for (TagNode tagNode : tagNodeSet)
					iframeNodeMap.put(tagNode, webElement);
				tagNodeSet.clear();
			}
		}
		// Find node that need to be disabled
		Set<TagNode> disableScriptNodeSet = null;
		if (xPathDisableScriptSet != null && xPathDisableScriptSet.size() > 0) {
			disableScriptNodeSet = new HashSet<TagNode>();
			for (String xPath : xPathDisableScriptSet)
				if (htmlCleanerParser.xpath(xPath, disableScriptNodeSet) == 0)
					Logging.warn("DisableScript not found using XPath: "
							+ xPath);
		}
		recursiveArchive(htmlCleanerParser.getTagNode(), disableScriptNodeSet,
				iframeNodeMap, clickCaptureResults);
		htmlCleanerParser.writeHtmlToFile(indexFile);
		String charset = htmlCleanerParser.findCharset();
		if (charset == null)
			FileUtils.write(sourceFile, pageSource);
		else
			FileUtils.write(sourceFile, pageSource, charset);

	}
}
