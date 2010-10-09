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

import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.common.database.FetchStatus;
import com.jaeksoft.searchlib.crawler.common.database.IndexStatus;
import com.jaeksoft.searchlib.crawler.common.database.ParserStatus;
import com.jaeksoft.searchlib.crawler.web.database.UrlManager.Field;
import com.jaeksoft.searchlib.crawler.web.database.UrlManager.SearchTemplate;
import com.jaeksoft.searchlib.request.SearchRequest;

public class UrlManagerNative extends UrlManagerAbstract {

	@Override
	public void reload(boolean optimize) throws SearchLibException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteUrls(Collection<String> workDeleteUrlList)
			throws SearchLibException {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateUrlItems(List<UrlItem> urlItems)
			throws SearchLibException {
		// TODO Auto-generated method stub

	}

	@Override
	public void getOldHostToFetch(Date fetchIntervalDate, int limit,
			List<NamedItem> hostList) throws SearchLibException {
		// TODO Auto-generated method stub

	}

	@Override
	public void getNewHostToFetch(Date fetchIntervalDate, int limit,
			List<NamedItem> hostList) throws SearchLibException {
		// TODO Auto-generated method stub

	}

	@Override
	public void getOldUrlToFetch(NamedItem host, Date fetchIntervalDate,
			long limit, List<UrlItem> urlList) throws SearchLibException {
		// TODO Auto-generated method stub

	}

	@Override
	public void getNewUrlToFetch(NamedItem host, Date fetchIntervalDate,
			long limit, List<UrlItem> urlList) throws SearchLibException {
		// TODO Auto-generated method stub

	}

	@Override
	public UrlItem getUrlToFetch(URL url) throws SearchLibException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Client getUrlDbClient() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SearchRequest urlQuery(SearchTemplate urlSearchTemplate,
			String like, String host, boolean includingSubDomain, String lang,
			String langMethod, String contentBaseType,
			String contentTypeCharset, String contentEncoding,
			Integer minContentLength, Integer maxContentLength,
			RobotsTxtStatus robotsTxtStatus, FetchStatus fetchStatus,
			Integer responseCode, ParserStatus parserStatus,
			IndexStatus indexStatus, Date startDate, Date endDate)
			throws SearchLibException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getUrls(SearchRequest searchRequest, Field orderBy,
			boolean orderAsc, long start, long rows, List<UrlItem> list)
			throws SearchLibException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void injectPrefix(List<PatternItem> patternList)
			throws SearchLibException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean exists(String sUrl) throws SearchLibException {
		// TODO Auto-generated method stub
		return false;
	}

}
