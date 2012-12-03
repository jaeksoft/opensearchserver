/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.web.database;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.crawler.web.spider.DownloadItem;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.XmlWriter;

public class SiteMapItem implements Comparable<SiteMapItem> {

	private String uri;

	private SiteMapItem() {
	}

	public SiteMapItem(String uri) {
		this();
		setUri(uri);
	}

	public SiteMapItem(Node node) {
		this();
		setUri(DomUtils.getAttributeText(node, "uri"));
	}

	public void copyTo(SiteMapItem siteMap) {
		siteMap.uri = this.uri;
	}

	/**
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * @param uri
	 *            the uri to set
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}

	public void writeXml(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement("siteMap", "uri", uri);
		xmlWriter.endElement();
	}

	@Override
	public int compareTo(SiteMapItem o) {
		return this.uri.compareTo(o.uri);
	}

	public List<String> getListOfUrls(HttpDownloader httpDownloader) {
		List<String> urls = new ArrayList<String>();
		InputStream inputStream = null;
		try {
			DownloadItem downloadItem = httpDownloader.get(new URI(uri), null);
			inputStream = downloadItem.getContentInputStream();
			Document doc = DomUtils.readXml(new InputSource(inputStream), true);
			if (doc != null) {
				List<Node> nodes = DomUtils.getAllNodes(doc, "loc");
				if (nodes != null) {
					for (Node node : nodes) {
						String href = DomUtils.getText(node);
						if (href != null && !href.equalsIgnoreCase("")) {
							// check url format
							URL newUrl = new URL(href);
							urls.add(newUrl.toExternalForm());
						}
					}
				}
			}
		} catch (Exception ex) {
			Logging.warn(ex);
		} finally {
			if (inputStream != null)
				IOUtils.closeQuietly(inputStream);
			httpDownloader.release();
		}
		return urls;
	}
}