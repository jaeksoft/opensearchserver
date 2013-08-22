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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import com.jaeksoft.searchlib.script.commands.Selectors;
import com.jaeksoft.searchlib.script.commands.Selectors.Selector;
import com.jaeksoft.searchlib.script.commands.Selectors.Type;
import com.jaeksoft.searchlib.util.LinkUtils;
import com.jaeksoft.searchlib.util.StringUtils;
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

	final private Selector findSelector(TagNode node) {
		// Check ID selector
		String iframe_id = node.getAttributeByName("id");
		if (iframe_id != null)
			return new Selectors.Selector(Type.ID_SELECTOR, iframe_id);
		// Build XPATH selector
		List<String> pathList = new ArrayList<String>(0);
		while (node != null) {
			String tag = node.getName();
			int pos = 1;
			String id = node.getAttributeByName("id");
			TagNode parent = node.getParent();
			if (id == null) {
				if (parent != null) {
					for (TagNode n : node.getChildTagList())
						if (n == node)
							break;
						else if (n.getName().equals(tag))
							pos++;
				}
			}
			node = parent;
			if (id != null) {
				pathList.add(tag + "[@id='" + id + "']");
				break;
			}
			// POS did not work on Selenium By.XPATH selector
			pathList.add(tag + '[' + pos + ']');
		}
		Collections.reverse(pathList);
		String sel = StringUtils.join(pathList, '/');
		if (node == null)
			sel = '/' + sel;
		else
			sel = "//" + sel;
		return new Selectors.Selector(Type.XPATH_SELECTOR, sel);
	}

	final private String downloadIframe(URL parentUrl, TagNode node)
			throws IOException, ParserConfigurationException, SAXException,
			IllegalStateException, SearchLibException, URISyntaxException {
		Set<WebElement> set = new HashSet<WebElement>();
		Selector selector = findSelector(node);
		browserDriver.locateBy(selector, set, true);
		if (set.size() != 1) {
			Logging.warn("Issue when finding IFRAME using selector: "
					+ selector.type + ' ' + selector.query + " - found: "
					+ set.size());
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
			urlFileMapKey = selector.query;
		File destFile = getAndRegisterDestFile(urlFileMapKey, "iframe", "html");
		String frameSource = browserDriver
				.getFrameSource(set.iterator().next());
		HtmlCleanerParser htmlCleanerParser = new HtmlCleanerParser();
		htmlCleanerParser.init(frameSource);
		recursiveArchive(htmlCleanerParser.getTagNode(), null);
		htmlCleanerParser.writeHtmlToFile(destFile);
		baseUrl = oldBaseUrl;
		return getLocalPath(parentUrl, destFile.getName());
	}

	final private void downloadObjectFromTag(TagNode node, String tagName,
			String srcAttrName, String typeAttrName)
			throws ClientProtocolException, IllegalStateException, IOException,
			SearchLibException, URISyntaxException,
			ParserConfigurationException, SAXException {
		if (tagName != null)
			if (!tagName.equalsIgnoreCase(node.getName()))
				return;
		String src = node.getAttributeByName(srcAttrName);
		if (src == null)
			return;
		String newSrc = null;
		if ("iframe".equalsIgnoreCase(node.getName()))
			newSrc = downloadIframe(baseUrl, node);
		else {
			String type = typeAttrName != null ? node
					.getAttributeByName(typeAttrName) : null;

			if (type == null && node.getName().equalsIgnoreCase("script"))
				type = "text/javascript";

			newSrc = downloadObject(baseUrl, src, type);
		}
		if (newSrc != null)
			node.addAttribute(srcAttrName, newSrc);
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
			Set<TagNode> disableScriptNodeSet) throws ClientProtocolException,
			IllegalStateException, IOException, SearchLibException,
			URISyntaxException, ParserConfigurationException, SAXException {
		if (node == null)
			return;
		checkBaseHref(node);
		downloadObjectFromTag(node, null, "src", null);
		downloadObjectFromTag(node, "link", "href", "type");
		checkStyleCSS(node);
		checkScriptContent(node, disableScriptNodeSet);
		checkStyleAttribute(node);
		TagNode[] nodes = node.getChildTags();
		if (nodes == null)
			return;
		for (TagNode n : nodes)
			recursiveArchive(n, disableScriptNodeSet);
	}

	final public void archive(BrowserDriver<?> browserDriver,
			Set<String> xPathDisableScriptSet) throws IOException,
			ParserConfigurationException, SAXException, IllegalStateException,
			SearchLibException, URISyntaxException, XPatherException {
		String pageSource = browserDriver.getSourceCode();
		HtmlCleanerParser htmlCleanerParser = new HtmlCleanerParser();
		htmlCleanerParser.init(pageSource);
		Set<TagNode> disableScriptNodeSet = null;
		if (xPathDisableScriptSet != null && xPathDisableScriptSet.size() > 0) {
			disableScriptNodeSet = new HashSet<TagNode>();
			for (String xPath : xPathDisableScriptSet)
				if (htmlCleanerParser.xpath(xPath, disableScriptNodeSet) == 0)
					Logging.warn("DisableScript not found using XPath: "
							+ xPath);
		}
		recursiveArchive(htmlCleanerParser.getTagNode(), disableScriptNodeSet);
		htmlCleanerParser.writeHtmlToFile(indexFile);
		String charset = htmlCleanerParser.findCharset();
		if (charset == null)
			FileUtils.write(sourceFile, pageSource);
		else
			FileUtils.write(sourceFile, pageSource, charset);

	}
}
