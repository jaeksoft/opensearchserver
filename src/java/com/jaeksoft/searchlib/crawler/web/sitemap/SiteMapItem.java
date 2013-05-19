/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.web.sitemap;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.ClientProtocolException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.spider.DownloadItem;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.XmlWriter;

public class SiteMapItem implements Comparable<SiteMapItem> {

	private URI uri;

	public SiteMapItem() {
		uri = null;
	}

	public SiteMapItem(String uri) throws URISyntaxException {
		this();
		setUri(uri);
	}

	public SiteMapItem(Node node) throws URISyntaxException {
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
		return uri == null ? null : uri.toString();
	}

	/**
	 * @param uri
	 *            the uri to set
	 * @throws URISyntaxException
	 */
	public void setUri(String uri) throws URISyntaxException {
		this.uri = new URI(uri);
	}

	public String getHostname() {
		return uri == null ? null : uri.getHost();
	}

	public void writeXml(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement("siteMap", "uri", uri != null ? uri.toString()
				: null);
		xmlWriter.endElement();
	}

	@Override
	public int compareTo(SiteMapItem o) {
		return this.uri.compareTo(o.uri);
	}

	public Set<SiteMapUrl> load(HttpDownloader httpDownloader,
			Set<SiteMapUrl> siteMapUrlSet) throws SearchLibException {
		if (siteMapUrlSet == null)
			siteMapUrlSet = new TreeSet<SiteMapUrl>();
		InputStream inputStream = null;
		try {
			DownloadItem downloadItem = httpDownloader.get(uri, null);
			inputStream = downloadItem.getContentInputStream();
			Document doc = DomUtils.readXml(new InputSource(inputStream), true);
			if (doc != null) {
				List<Node> nodes = DomUtils.getAllNodes(doc, "url");
				if (nodes != null)
					for (Node node : nodes)
						siteMapUrlSet.add(new SiteMapUrl(node));
			}
			return siteMapUrlSet;
		} catch (ClientProtocolException e) {
			throw new SearchLibException(e);
		} catch (IllegalStateException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} catch (ParserConfigurationException e) {
			throw new SearchLibException(e);
		} finally {
			if (inputStream != null)
				IOUtils.closeQuietly(inputStream);
			httpDownloader.release();
		}
	}

}