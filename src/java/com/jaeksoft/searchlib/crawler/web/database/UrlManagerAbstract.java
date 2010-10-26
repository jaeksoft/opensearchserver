/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.crawler.web.database;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.TargetStatus;
import com.jaeksoft.searchlib.crawler.common.database.FetchStatus;
import com.jaeksoft.searchlib.crawler.common.database.IndexStatus;
import com.jaeksoft.searchlib.crawler.common.database.ParserStatus;
import com.jaeksoft.searchlib.crawler.web.database.UrlManager.Field;
import com.jaeksoft.searchlib.crawler.web.spider.Crawl;
import com.jaeksoft.searchlib.index.IndexDocument;

public abstract class UrlManagerAbstract {

	public enum SearchTemplate {
		urlSearch, urlExport;
	}

	protected Client targetClient;

	public abstract void init(Client client, File dataDir)
			throws SearchLibException, URISyntaxException,
			FileNotFoundException;

	public abstract void reload(boolean optimize) throws SearchLibException;

	public abstract void deleteUrls(Collection<String> workDeleteUrlList)
			throws SearchLibException;

	public abstract void updateUrlItems(List<UrlItem> urlItems)
			throws SearchLibException;

	public abstract void getOldHostToFetch(Date fetchIntervalDate, int limit,
			List<NamedItem> hostList) throws SearchLibException;

	public abstract void getNewHostToFetch(Date fetchIntervalDate, int limit,
			List<NamedItem> hostList) throws SearchLibException;

	public abstract void getOldUrlToFetch(NamedItem host,
			Date fetchIntervalDate, long limit, List<UrlItem> urlList)
			throws SearchLibException;

	public abstract void getNewUrlToFetch(NamedItem host,
			Date fetchIntervalDate, long limit, List<UrlItem> urlList)
			throws SearchLibException;

	public abstract UrlItem getUrlToFetch(URL url) throws SearchLibException;

	public abstract Client getUrlDbClient();

	public abstract void inject(List<InjectUrlItem> list)
			throws SearchLibException;

	public abstract long getUrls(SearchTemplate urlSearchTemplate, String like,
			String host, boolean includingSubDomain, String lang,
			String langMethod, String contentBaseType,
			String contentTypeCharset, String contentEncoding,
			Integer minContentLength, Integer maxContentLength,
			RobotsTxtStatus robotsTxtStatus, FetchStatus fetchStatus,
			Integer responseCode, ParserStatus parserStatus,
			IndexStatus indexStatus, Date startDate, Date endDate,
			Field orderBy, boolean orderAsc, long start, long rows,
			List<UrlItem> list) throws SearchLibException;

	public void injectPrefix(List<PatternItem> patternList)
			throws SearchLibException {
		Iterator<PatternItem> it = patternList.iterator();
		List<InjectUrlItem> urlList = new ArrayList<InjectUrlItem>();
		while (it.hasNext()) {
			PatternItem item = it.next();
			if (item.getStatus() == PatternItem.Status.INJECTED)
				urlList.add(new InjectUrlItem(item));
		}
		inject(urlList);
	}

	public abstract void removeExisting(List<String> urlList)
			throws SearchLibException;

	// TODO : can be mutualised
	public Date getPastDate(long fetchInterval, String intervalUnit) {
		long l;
		if ("hours".equalsIgnoreCase(intervalUnit))
			l = fetchInterval * 1000 * 3600;
		else if ("minutes".equalsIgnoreCase(intervalUnit))
			l = fetchInterval * 1000 * 60;
		else
			// Default is days
			l = fetchInterval * 1000 * 86400;
		return new Date(System.currentTimeMillis() - l);
	}

	public void updateCrawls(List<Crawl> crawls) throws SearchLibException {
		try {
			if (crawls == null)
				return;
			// Update target index
			List<IndexDocument> documentsToUpdate = new ArrayList<IndexDocument>(
					crawls.size());
			List<String> documentsToDelete = new ArrayList<String>(
					crawls.size());
			for (Crawl crawl : crawls) {
				if (crawl == null)
					continue;
				IndexDocument indexDocument = crawl.getTargetIndexDocument();
				TargetStatus targetStatus = crawl.getUrlItem()
						.getTargetResult();
				if (targetStatus == TargetStatus.TARGET_UPDATE)
					documentsToUpdate.add(indexDocument);
				else if (targetStatus == TargetStatus.TARGET_DELETE)
					documentsToDelete.add(crawl.getUrlItem().getUrl());
			}
			if (documentsToUpdate.size() > 0)
				targetClient.updateDocuments(documentsToUpdate);
			if (documentsToDelete.size() > 0)
				targetClient.deleteDocuments(documentsToDelete);

			// Update URL DB
			List<UrlItem> urlItems = new ArrayList<UrlItem>();
			for (Crawl crawl : crawls) {
				if (crawl == null)
					continue;
				urlItems.add(crawl.getUrlItem());
			}
			updateUrlItems(urlItems);

		} catch (NoSuchAlgorithmException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
			throw new SearchLibException(e);
		}
	}

}
