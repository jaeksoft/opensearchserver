/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2014 Emmanuel Keller / Jaeksoft
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.common.process.CrawlStatus;
import com.jaeksoft.searchlib.crawler.database.DatabaseCrawlAbstract;
import com.jaeksoft.searchlib.crawler.database.DatabaseCrawlThread;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.util.Variables;
import com.jaeksoft.searchlib.webservice.CommonListResult;
import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.CommonServices;

public class DatabaseImpl extends CommonServices implements RestDatabase {

	@Override
	public CommonListResult<String> list(UriInfo uriInfo, String index,
			String login, String key) {
		try {
			ClientFactory.INSTANCE.properties.checkApi();
			Client client = getLoggedClientAnyRole(uriInfo, index, login, key,
					Role.GROUP_DATABASE_CRAWLER);
			List<String> nameList = new ArrayList<String>(0);
			DatabaseCrawlAbstract[] items = client.getDatabaseCrawlList()
					.getArray();
			if (items != null)
				for (DatabaseCrawlAbstract item : items)
					nameList.add(item.getName());
			return new CommonListResult<String>(nameList);
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public CommonResult run(UriInfo uriInfo, String index, String login,
			String key, String databaseName, Map<String, String> variables) {
		try {
			ClientFactory.INSTANCE.properties.checkApi();
			Client client = getLoggedClient(uriInfo, index, login, key,
					Role.DATABASE_CRAWLER_START_STOP);
			if (StringUtils.isEmpty(databaseName))
				throw new CommonServiceException(
						"The database crawler name is missing");
			DatabaseCrawlAbstract databaseCrawl = client.getDatabaseCrawlList()
					.get(databaseName);
			if (databaseCrawl == null)
				throw new CommonServiceException(Response.Status.NOT_FOUND,
						"Database crawl name not found: " + databaseName);
			CommonResult result = new CommonResult(true, null);
			DatabaseCrawlThread databaseCrawlThread = client
					.getDatabaseCrawlMaster()
					.execute(
							client,
							databaseCrawl,
							true,
							variables == null ? null : new Variables(variables),
							result);
			if (databaseCrawlThread.getStatus() == CrawlStatus.ERROR)
				throw new CommonServiceException(
						databaseCrawlThread.getException());
			return result;
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		}

	}

}
