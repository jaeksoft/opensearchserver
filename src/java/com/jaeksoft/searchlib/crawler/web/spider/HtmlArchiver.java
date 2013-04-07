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
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.ClientProtocolException;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.SimpleHtmlSerializer;
import org.htmlcleaner.TagNode;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.util.LinkUtils;

public class HtmlArchiver {

	final File filesDir;
	final File indexFile;
	final Map<String, Integer> fileCountMap;
	final Map<String, String> urlFileMap;
	final URL url;
	final HttpDownloader downloader;

	public HtmlArchiver(File parentDir, HttpDownloader httpDownloader, URL url) {
		filesDir = new File(parentDir, "files");
		indexFile = new File(parentDir, "index.html");
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

	final private void downloadObjects(TagNode node, String tagName,
			String attributeName) throws ClientProtocolException,
			IllegalStateException, IOException, SearchLibException,
			URISyntaxException {
		if (tagName != null)
			if (!tagName.equalsIgnoreCase(node.getName()))
				return;
		String src = node.getAttributeByName(attributeName);
		if (src == null)
			return;
		URL objectURL = LinkUtils.getLink(url, src, null, false);
		DownloadItem downloadItem = downloader.get(
				LinkUtils.getLink(objectURL, src, null, false).toURI(), null);
		String urlString = objectURL.toExternalForm();
		String fileName = urlFileMap.get(urlString);
		if (fileName != null)
			return;
		fileName = downloadItem.getFileName();
		if (fileName == null || fileName.length() == 0)
			return;
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
		node.setAttribute(attributeName, "./" + filesDir.getName() + "/"
				+ fileName);
	}

	final private void recursiveArchive(TagNode node)
			throws ClientProtocolException, IllegalStateException, IOException,
			SearchLibException, URISyntaxException {
		if (node == null)
			return;
		downloadObjects(node, null, "src");
		downloadObjects(node, "link", "href");
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
		TagNode rootTag = htmlCleaner.clean(pageSource);
		recursiveArchive(rootTag);
		FileWriter writer = null;
		StringReader reader = null;
		try {
			writer = new FileWriter(indexFile);
			rootTag.serialize(
					new SimpleHtmlSerializer(htmlCleaner.getProperties()),
					writer);
		} finally {
			if (writer != null)
				IOUtils.closeQuietly(writer);
			if (reader != null)
				IOUtils.closeQuietly(reader);
		}
	}
}
