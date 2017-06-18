/**
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2017 Emmanuel Keller / Jaeksoft
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

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.GenericCache;
import com.jaeksoft.searchlib.crawler.web.spider.DownloadItem;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.IOUtils;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SiteMapCache extends GenericCache<URI, SiteMapCache.Item> {

	private static volatile SiteMapCache INSTANCE;

	public static SiteMapCache getInstance() {
		if (INSTANCE != null)
			return INSTANCE;
		synchronized (SiteMapCache.class) {
			if (INSTANCE != null)
				return INSTANCE;
			INSTANCE = new SiteMapCache();
			return INSTANCE;
		}
	}

	/**
	 * Return the SiteMap object related to the URL.
	 *
	 * @param uri
	 * @param forceReload
	 * @return
	 * @throws SearchLibException
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	Item getSiteMapItemUrls(final URI uri, final HttpDownloader httpDownloader, final boolean forceReload)
			throws SearchLibException {

		try {
			return getOrCreate(uri, forceReload, new ItemSupplier<Item>() {
				@Override
				public Item get() throws IOException, SearchLibException {
					return new Item(uri, httpDownloader);
				}
			});
		} catch (URISyntaxException | IOException e) {
			throw new SearchLibException(e);
		}
	}

	private static void load(final URI uri, final HttpDownloader httpDownloader, final Set<SiteMapUrl> siteMapUrlSet)
			throws SearchLibException {
		InputStream inputStream = null;
		try {
			DownloadItem downloadItem = httpDownloader.get(uri, null);
			downloadItem.checkNoErrorList(200);
			if ("application/x-gzip".equals(downloadItem.getContentBaseType())) {
				inputStream = new CompressorStreamFactory().createCompressorInputStream(CompressorStreamFactory.GZIP,
						downloadItem.getContentInputStream());
			} else
				inputStream = downloadItem.getContentInputStream();
			Document doc = DomUtils.readXml(new InputSource(inputStream), true);
			if (doc != null) {
				List<Node> nodes = DomUtils.getAllNodes(doc, "url");
				if (nodes != null)
					for (Node node : nodes)
						siteMapUrlSet.add(new SiteMapUrl(node));
			}
		} catch (SearchLibException.WrongStatusCodeException e) {
			Logging.warn("Error while loading the sitemap: " + uri, e);
		} catch (IllegalStateException | IOException | ParserConfigurationException | URISyntaxException | CompressorException | SAXException e) {
			throw new SearchLibException(e);
		} finally {
			IOUtils.close(inputStream);
		}
	}

	@Override
	protected Item[] newArray(int size) {
		return new Item[size];
	}

	final class Item implements GenericCache.Expirable {

		private final Date crawlDate;
		private final long expirableTime;
		private final Set<SiteMapUrl> siteMapUrls;
		private final String error;

		Item(final URI uri, final HttpDownloader httpDownloader) {
			crawlDate = new Date(System.currentTimeMillis());
			expirableTime = crawlDate.getTime() + 1000 * 60 * 15;
			siteMapUrls = new LinkedHashSet<>();
			String err = null;
			try {
				load(uri, httpDownloader, siteMapUrls);
			} catch (SearchLibException e) {
				err = e.getMessage();
			}
			error = err;
		}

		void fill(final Set<SiteMapUrl> set) {
			set.addAll(siteMapUrls);
		}

		@Override
		public long getExpirationTime() {
			return expirableTime;
		}

		@Override
		public boolean isCacheable() {
			return true;
		}

		public String getError() {
			return error;
		}

		public Date getCrawlDate() {
			return crawlDate;
		}

	}
}
