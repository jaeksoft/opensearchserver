/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.scheduler.task;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.rest.RestCrawlItem;
import com.jaeksoft.searchlib.crawler.rest.RestCrawlList;
import com.jaeksoft.searchlib.crawler.rest.RestCrawlMaster;
import com.jaeksoft.searchlib.scheduler.TaskAbstract;
import com.jaeksoft.searchlib.scheduler.TaskLog;
import com.jaeksoft.searchlib.scheduler.TaskProperties;
import com.jaeksoft.searchlib.scheduler.TaskPropertyDef;
import com.jaeksoft.searchlib.scheduler.TaskPropertyType;
import com.jaeksoft.searchlib.utils.Variables;

public class TaskRestCrawlerRun extends TaskAbstract {

	final private TaskPropertyDef propCrawlName = new TaskPropertyDef(
			TaskPropertyType.comboBox, "crawl name", "crawl name",
			"The name of the REST crawl item", 50);

	final private TaskPropertyDef[] taskPropertyDefs = { propCrawlName };

	@Override
	public String getName() {
		return "REST crawler - run";
	}

	@Override
	public TaskPropertyDef[] getPropertyList() {
		return taskPropertyDefs;
	}

	@Override
	public String[] getPropertyValues(Config config,
			TaskPropertyDef propertyDef, TaskProperties taskProperties)
			throws SearchLibException {
		RestCrawlList restList = config.getRestCrawlList();
		RestCrawlItem[] crawls = restList.getArray();
		if (crawls == null)
			return null;
		String[] values = new String[crawls.length];
		int i = 0;
		for (RestCrawlItem crawl : crawls)
			values[i++] = crawl.getName();
		return values;
	}

	@Override
	public String getDefaultValue(Config config, TaskPropertyDef propertyDef) {
		return null;
	}

	@Override
	public void execute(Client client, TaskProperties properties,
			Variables variables, TaskLog taskLog) throws SearchLibException {
		RestCrawlMaster crawlMaster = client.getRestCrawlMaster();
		RestCrawlList crawlList = client.getRestCrawlList();
		String crawlName = properties.getValue(propCrawlName);
		if (crawlName == null) {
			taskLog.setInfo("The crawl name is missing");
			return;
		}
		RestCrawlItem crawl = crawlList.get(crawlName);
		if (crawl == null) {
			taskLog.setInfo("Crawl not found: " + crawlName);
			return;
		}
		try {
			crawlMaster.execute(client, crawl, true, variables, taskLog);
		} catch (InterruptedException e) {
			throw new SearchLibException(e);
		}
	}
}
