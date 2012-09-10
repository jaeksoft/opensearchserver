/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2012 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.webservice;

import java.io.IOException;

import javax.naming.NamingException;
import javax.xml.ws.WebServiceException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.common.process.CrawlStatus;
import com.jaeksoft.searchlib.crawler.database.DatabaseCrawl;
import com.jaeksoft.searchlib.crawler.database.DatabaseCrawlThread;

public class DatabaseImpl extends CommonServicesImpl implements Database {

	@Override
	public Long database(String use, String login, String key,
			String databaseName) {
		try {
			ClientFactory.INSTANCE.properties.checkApi();
			Client client = ClientCatalog.getClient(use);
			Long databaseIndexedCount = null;
			if (isLogged(use, login, key)) {
				DatabaseCrawl databaseCrawl = client.getDatabaseCrawlList()
						.get(databaseName);
				if (databaseCrawl == null) {
					throw new WebServiceException(
							"Database crawl name not found (" + databaseName
									+ ")");
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
			}
			return databaseIndexedCount;
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		} catch (NamingException e) {
			throw new WebServiceException(e);
		} catch (InterruptedException e) {
			throw new WebServiceException(e);
		} catch (IOException e) {
			throw new WebServiceException(e);
		}

	}
}
