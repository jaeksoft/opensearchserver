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
 *  
 *  Contributor: Richard Sinelle
 **/

package com.jaeksoft.searchlib.crawler.web.sitemap;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class SiteMapList {

	final private ReadWriteLock rwl = new ReadWriteLock();

	private final File configFile;
	private final Map<String, Set<SiteMapItem>> sitemapMap;
	private SiteMapItem[] sitemapArray;

	public SiteMapList(File indexDir, String filename)
			throws SearchLibException {
		configFile = new File(indexDir, filename);
		sitemapMap = new TreeMap<String, Set<SiteMapItem>>();
		sitemapArray = null;
		try {
			load();
		} catch (ParserConfigurationException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (XPathExpressionException e) {
			throw new SearchLibException(e);
		}
	}

	private void load() throws ParserConfigurationException, SAXException,
			IOException, XPathExpressionException, SearchLibException {
		if (!configFile.exists())
			return;
		XPathParser xpp = new XPathParser(configFile);
		NodeList nodeList = xpp.getNodeList("/siteMaps/siteMap");
		int l = nodeList.getLength();
		for (int i = 0; i < l; i++) {
			try {
				addNoLock(new SiteMapItem(nodeList.item(i)));
			} catch (URISyntaxException e) {
				Logging.error(e);
			}
		}

	}

	private void addNoLock(SiteMapItem item) {
		Set<SiteMapItem> set = sitemapMap.get(item.getHostname());
		if (set == null) {
			set = new TreeSet<SiteMapItem>();
			sitemapMap.put(item.getHostname(), set);
		}
		set.add(item);
	}

	public void writeXml(XmlWriter xmlWriter) throws IOException,
			TransformerConfigurationException, SAXException {
		rwl.w.lock();
		try {
			xmlWriter.startElement("siteMaps");
			for (Set<SiteMapItem> set : sitemapMap.values())
				for (SiteMapItem item : set)
					item.writeXml(xmlWriter);
			xmlWriter.endElement();
			xmlWriter.endDocument();
		} finally {
			rwl.w.unlock();
		}
	}

	public SiteMapItem[] getArray() {
		rwl.r.lock();
		try {
			if (sitemapArray != null)
				return sitemapArray;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (sitemapArray != null)
				return sitemapArray;
			sitemapArray = new SiteMapItem[countSiteMapNoLock()];
			int i = 0;
			for (Set<SiteMapItem> set : sitemapMap.values())
				for (SiteMapItem item : set)
					sitemapArray[i++] = item;
			return sitemapArray;
		} finally {
			rwl.w.unlock();
		}
	}

	private int countSiteMapNoLock() {
		int i = 0;
		for (Set<SiteMapItem> set : sitemapMap.values())
			i += set.size();
		return i;
	}

	public void add(SiteMapItem item) {
		rwl.w.lock();
		try {
			addNoLock(item);
			sitemapArray = null;
		} finally {
			rwl.w.unlock();
		}
	}

	public void remove(SiteMapItem item) {
		rwl.w.lock();
		try {
			Set<SiteMapItem> set = sitemapMap.get(item.getHostname());
			if (set == null)
				return;
			set.remove(item);
			if (set.isEmpty())
				sitemapMap.remove(set);
			sitemapArray = null;
		} finally {
			rwl.w.unlock();
		}
	}

	public Set<SiteMapUrl> load(String hostname, HttpDownloader downloader,
			Set<SiteMapUrl> siteMapUrlSet) throws SearchLibException {
		rwl.r.lock();
		try {
			if (siteMapUrlSet == null)
				siteMapUrlSet = new TreeSet<SiteMapUrl>();
			Set<SiteMapItem> set = sitemapMap.get(hostname);
			if (set == null)
				return siteMapUrlSet;
			for (SiteMapItem item : set)
				item.load(downloader, siteMapUrlSet);
			return siteMapUrlSet;
		} finally {
			rwl.r.unlock();
		}
	}
}