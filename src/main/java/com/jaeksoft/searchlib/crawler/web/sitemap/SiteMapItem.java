/**
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2010-2017 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.crawler.web.sitemap;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.XmlWriter;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashSet;

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
	 * @param uri the uri to set
	 * @throws URISyntaxException
	 */
	public void setUri(String uri) throws URISyntaxException {
		this.uri = new URI(uri);
	}

	public String getHostname() {
		return uri == null ? null : uri.getHost();
	}

	public void writeXml(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement("siteMap", "uri", uri != null ? uri.toString() : null);
		xmlWriter.endElement();
	}

	@Override
	public int compareTo(SiteMapItem o) {
		return this.uri.compareTo(o.uri);
	}

	public void fill(final SiteMapCache siteMapCache, final HttpDownloader httpDownloader, final boolean forceReload,
			final LinkedHashSet<SiteMapUrl> siteMapUrlSet) throws SearchLibException {
		if (uri == null)
			return;
		final SiteMapCache.Item item = siteMapCache.getSiteMapItemUrls(uri, httpDownloader, forceReload);
		if (item != null)
			item.fill(siteMapUrlSet);
	}
}