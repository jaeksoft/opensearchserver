/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.crawler.database.url;

import java.net.MalformedURLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.jaeksoft.searchlib.crawler.database.CrawlDatabaseException;
import com.jaeksoft.searchlib.crawler.database.pattern.PatternUrlItem;
import com.jaeksoft.searchlib.crawler.process.CrawlStatistics;
import com.jaeksoft.searchlib.crawler.spider.Crawl;

public abstract class UrlManager {

	public enum Field {

		URL("url"), WHEN("when"), RETRY("retry"), FETCHSTATUS("fetchStatus"), PARSERSTATUS(
				"parserStatus"), INDEXSTATUS("indexStatus"), HOST("host");

		private String name;

		private Field(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	public void injectPrefix(List<PatternUrlItem> patternList)
			throws CrawlDatabaseException {
		Iterator<PatternUrlItem> it = patternList.iterator();
		List<InjectUrlItem> urlList = new ArrayList<InjectUrlItem>();
		while (it.hasNext()) {
			PatternUrlItem item = it.next();
			if (item.getStatus() == PatternUrlItem.Status.INJECTED)
				urlList.add(new InjectUrlItem(item));
		}
		inject(urlList);
	}

	public abstract void delete(String sUrl) throws CrawlDatabaseException;

	public abstract void update(Crawl crawl) throws MalformedURLException,
			CrawlDatabaseException;

	public abstract void inject(List<InjectUrlItem> list)
			throws CrawlDatabaseException;

	protected Timestamp getNewTimestamp(long fetchInterval) {
		long t = System.currentTimeMillis() - fetchInterval * 1000 * 86400;
		return new Timestamp(t);
	}

	public abstract void getHostToFetch(int fetchInterval, int limit,
			CrawlStatistics stats, List<HostItem> hostList)
			throws CrawlDatabaseException;

	public abstract List<UrlItem> getUrlToFetch(HostItem host,
			int fetchInterval, long limit) throws CrawlDatabaseException;

	public abstract void getUrls(String like, String host,
			FetchStatus fetchStatus, ParserStatus parserStatus,
			IndexStatus indexStatus, Date startDate, Date endDate,
			Field orderBy, long start, long rows, UrlList urlList)
			throws CrawlDatabaseException;
}