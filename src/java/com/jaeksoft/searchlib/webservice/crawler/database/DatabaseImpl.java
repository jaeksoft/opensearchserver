/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2013 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.webservice.crawler.database;

import java.io.IOException;

import javax.xml.ws.WebServiceException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.common.process.CrawlStatus;
import com.jaeksoft.searchlib.crawler.database.DatabaseCrawlAbstract;
import com.jaeksoft.searchlib.crawler.database.DatabaseCrawlThread;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.CommonServices;

public class DatabaseImpl extends CommonServices implements SoapDatabase,
		RestDatabase {

	@Override
	public CommonResult crawl(String use, String login, String key,
			String databaseName) {
		try {
			ClientFactory.INSTANCE.properties.checkApi();
			Client client = getLoggedClient(use, login, key,
					Role.DATABASE_CRAWLER_START_STOP);
			long databaseIndexedCount = 0;
			DatabaseCrawlAbstract databaseCrawl = client.getDatabaseCrawlList()
					.get(databaseName);
			if (databaseCrawl == null) {
				throw new WebServiceException("Database crawl name not found ("
						+ databaseName + ")");
			} else {
				DatabaseCrawlThread databaseCrawlThread = client
						.getDatabaseCrawlMaster().execute(client,
								databaseCrawl, true, null);
				if (databaseCrawlThread.getStatus() == CrawlStatus.ERROR)
					throw new WebServiceException("Error");
				else
					databaseIndexedCount = databaseCrawlThread
							.getUpdatedIndexDocumentCount();
			}
			return new CommonResult(true, Long.toString(databaseIndexedCount));
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		} catch (InterruptedException e) {
			throw new WebServiceException(e);
		} catch (IOException e) {
			throw new WebServiceException(e);
		}

	}

	@Override
	public CommonResult crawlXML(String use, String login, String key,
			String databaseName) {
		return crawl(use, login, key, databaseName);
	}

	@Override
	public CommonResult crawlJSON(String use, String login, String key,
			String databaseName) {
		return crawl(use, login, key, databaseName);
	}
}
