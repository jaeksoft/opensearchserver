/*
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2011-2017 Emmanuel Keller / Jaeksoft
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
 */
package com.jaeksoft.searchlib.webservice.crawler.rest;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.common.process.CrawlStatus;
import com.jaeksoft.searchlib.crawler.rest.RestCrawlItem;
import com.jaeksoft.searchlib.crawler.rest.RestCrawlThread;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.util.Variables;
import com.jaeksoft.searchlib.webservice.CommonListResult;
import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.CommonServices;

import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RestCrawlerImpl extends CommonServices implements RestRestCrawler {

	@Override
	public CommonListResult<String> list(String index, String login, String key) {
		try {
			ClientFactory.INSTANCE.properties.checkApi();
			Client client = getLoggedClientAnyRole(index, login, key, Role.GROUP_REST_CRAWLER);
			List<String> nameList = new ArrayList<String>(0);
			RestCrawlItem[] items = client.getRestCrawlList().getArray();
			if (items != null)
				for (RestCrawlItem item : items)
					nameList.add(item.getName());
			return new CommonListResult<String>(nameList);
		} catch (SearchLibException | InterruptedException | IOException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public CommonResult run(String index, String login, String key, String crawl_name, Boolean returnIds,
			Map<String, String> variables) {
		try {
			ClientFactory.INSTANCE.properties.checkApi();
			Client client = getLoggedClient(index, login, key, Role.REST_CRAWLER_EXECUTE);
			RestCrawlItem restCrawlItem = client.getRestCrawlList().get(crawl_name);
			if (restCrawlItem == null)
				throw new CommonServiceException(Status.NOT_FOUND, "Crawl item not found: " + crawl_name);
			if (returnIds == null)
				returnIds = false;
			CommonResult result = returnIds ? new CommonListResult<String>(new ArrayList<String>(0)) : new CommonResult(
					true, null);
			RestCrawlThread restCrawlThread = client.getRestCrawlMaster().execute(client, restCrawlItem, true,
					variables == null ? null : new Variables(variables), result);
			if (restCrawlThread.getStatus() == CrawlStatus.ERROR)
				throw new CommonServiceException(restCrawlThread.getException());
			if (result instanceof CommonListResult)
				((CommonListResult<?>) result).computeInfos();
			return result;
		} catch (SearchLibException | InterruptedException | IOException e) {
			throw new CommonServiceException(e);
		}

	}
}
