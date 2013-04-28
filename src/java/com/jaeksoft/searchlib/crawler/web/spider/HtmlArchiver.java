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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.ClientProtocolException;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DoctypeToken;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.SimpleHtmlSerializer;
import org.htmlcleaner.TagNode;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.util.LinkUtils;

public class HtmlArchiver {

	final File filesDir;
	final File indexFile;
	final File sourceFile;
	final Map<String, Integer> fileCountMap;
	final Map<String, String> urlFileMap;
	final URL url;
	final HttpDownloader downloader;

	public HtmlArchiver(File parentDir, HttpDownloader httpDownloader, URL url) {
		filesDir = new File(parentDir, "files");
		indexFile = new File(parentDir, "index.html");
		sourceFile = new File(parentDir, "source.html");
		this.url = url;
		this.downloader = httpDownloader;
		fileCountMap = new TreeMap<String, Integer>();
		urlFileMap = new TreeMap<String, String>();
		filesDir.mkdir();
	}

	final private static String buildFileName(String baseName,
			String extension, Integer fileCount) {
		StringBuffer sb = new StringBuffer(baseName);
		if (fileCount != null && fileCount > 0) {
			sb.append('(');
			sb.append(fileCount);
			sb.append(')');
		}
		if (extension != null && extension.length() > 0) {
			sb.append('.');
			sb.append(extension);
		}
		return sb.toString();
	}

	final private String getLocalPath(String fileName) {
		StringBuffer sb = new StringBuffer("./");
		sb.append(filesDir.getName());
		sb.append('/');
		sb.append(fileName);
		return sb.toString();
	}

	final private String downloadObject(String src)
			throws ClientProtocolException, IllegalStateException, IOException,
			SearchLibException, URISyntaxException {
		URL objectURL = LinkUtils.getLink(url, src, null, false);
		String urlString = objectURL.toExternalForm();
		String fileName = urlFileMap.get(urlString);
		if (fileName != null)
			return getLocalPath(fileName);
		DownloadItem downloadItem = downloader.get(
				LinkUtils.getLink(objectURL, src, null, false).toURI(), null);
		fileName = downloadItem.getFileName();
		if (fileName == null || fileName.length() == 0)
			return null;
		String baseName = FilenameUtils.getBaseName(fileName);
		String extension = FilenameUtils.getExtension(fileName);
		String contentType = downloadItem.getContentBaseType();
		if ("text/html".equalsIgnoreCase(contentType))
			extension = "html";
		if ("text/javascript".equalsIgnoreCase(contentType))
			extension = "js";
		fileName = buildFileName(baseName, extension, null);
		Integer fileCount = fileCountMap.get(fileName);
		fileCount = fileCount == null ? new Integer(0) : fileCount + 1;
		fileCountMap.put(fileName, fileCount);
		fileName = buildFileName(baseName, extension, fileCount);
		downloadItem.writeToFile(new File(filesDir, fileName));
		urlFileMap.put(urlString, fileName);
		return getLocalPath(fileName);
	}

	final private Pattern importUrlPattern = Pattern
			.compile("(?s)@import\\p{Space}+url\\(\"?(.*?)\"?\\)\\p{Space}*;");

	final private void checkCSSContent(StringBuilder css)
			throws ClientProtocolException, IllegalStateException, IOException,
			SearchLibException, URISyntaxException {
		StringBuffer sb = new StringBuffer();
		int pos = 0;
		int len = css.length();
		Matcher matcher = importUrlPattern.matcher(css);
		while (matcher.find()) {
			int start = matcher.start(1);
			sb.append(css.subSequence(pos, start));
			String src = matcher.group(1);
			String newSrc = downloadObject(src);
			System.out.println(src + " -> " + newSrc);
			sb.append(newSrc != null ? newSrc : src);
			pos = matcher.end(1);
		}
		if (pos < len)
			sb.append(css.subSequence(pos, len));
		css.replace(0, len, sb.toString());
	}

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
		checkCSSContent(builder);
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
		String newSrc = downloadObject(src);
		if (newSrc != null)
			node.addAttribute(attributeName, newSrc);
	}

	final private void recursiveArchive(TagNode node)
			throws ClientProtocolException, IllegalStateException, IOException,
			SearchLibException, URISyntaxException {
		if (node == null)
			return;
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
			IllegalAccessException {
		HtmlCleaner htmlCleaner = new HtmlCleaner();
		CleanerProperties cleanerProps = htmlCleaner.getProperties();
		TagNode rootTag = htmlCleaner.clean(pageSource);
		recursiveArchive(rootTag);
		FileWriter fw = null;
		PrintWriter pw = null;
		StringReader reader = null;
		try {
			fw = new FileWriter(indexFile);
			pw = new PrintWriter(fw);
			DoctypeToken docType = rootTag.getDocType();
			if (docType != null)
				pw.println(docType.getContent());
			rootTag.serialize(new SimpleHtmlSerializer(cleanerProps), pw);
			pw.close();
			pw = null;
			fw.close();
			fw = new FileWriter(sourceFile);
			IOUtils.write(pageSource, fw);
		} finally {
			if (pw != null)
				IOUtils.closeQuietly(pw);
			if (fw != null)
				IOUtils.closeQuietly(fw);
			if (reader != null)
				IOUtils.closeQuietly(reader);
		}
	}
}
