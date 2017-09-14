/*
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2013-2017 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.crawler.rest;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.common.process.CrawlMasterAbstract;
import com.jaeksoft.searchlib.process.ThreadItem;
import com.jaeksoft.searchlib.util.InfoCallback;
import com.jaeksoft.searchlib.util.Variables;

public class RestCrawlMaster extends CrawlMasterAbstract<RestCrawlMaster, RestCrawlThread> {

	public RestCrawlMaster(Config config) {
		super(config, "RestCrawler");
	}

	@Override
	public RestCrawlThread getNewThread(Client client, ThreadItem<?, RestCrawlThread> restCrawl, Variables variables,
			InfoCallback infoCallback) throws SearchLibException {
		return new RestCrawlThread(client, this, (RestCrawlItem) restCrawl, variables, infoCallback);
	}

	@Override
	protected RestCrawlThread[] getNewArray(int size) {
		return new RestCrawlThread[size];
	}
}
