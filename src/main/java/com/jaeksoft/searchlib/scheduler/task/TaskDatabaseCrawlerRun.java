/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2013 Emmanuel Keller / Jaeksoft
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

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.database.DatabaseCrawlAbstract;
import com.jaeksoft.searchlib.crawler.database.DatabaseCrawlList;
import com.jaeksoft.searchlib.crawler.database.DatabaseCrawlMaster;
import com.jaeksoft.searchlib.crawler.database.DatabaseCrawlThread;
import com.jaeksoft.searchlib.scheduler.TaskAbstract;
import com.jaeksoft.searchlib.scheduler.TaskLog;
import com.jaeksoft.searchlib.scheduler.TaskProperties;
import com.jaeksoft.searchlib.scheduler.TaskPropertyDef;
import com.jaeksoft.searchlib.scheduler.TaskPropertyType;
import com.jaeksoft.searchlib.util.Variables;

public class TaskDatabaseCrawlerRun extends TaskAbstract {

	final private TaskPropertyDef propCrawlName = new TaskPropertyDef(
			TaskPropertyType.comboBox, "crawl name", "crawl name",
			"The name of the database crawl item", 50);

	final private TaskPropertyDef propCrawlVariables = new TaskPropertyDef(
			TaskPropertyType.multilineTextBox, "crawl variables",
			"crawl variables", "The name of the database crawl item", 50, 5);

	final private TaskPropertyDef[] taskPropertyDefs = { propCrawlName,
			propCrawlVariables };

	@Override
	public String getName() {
		return "Database crawler - run";
	}

	@Override
	public TaskPropertyDef[] getPropertyList() {
		return taskPropertyDefs;
	}

	@Override
	public String[] getPropertyValues(Config config,
			TaskPropertyDef propertyDef, TaskProperties taskProperties)
			throws SearchLibException {
		DatabaseCrawlList crawlList = config.getDatabaseCrawlList();
		DatabaseCrawlAbstract[] crawls = crawlList.getArray();
		if (crawls == null)
			return null;
		String[] values = new String[crawls.length];
		int i = 0;
		for (DatabaseCrawlAbstract crawl : crawls)
			values[i++] = crawl.getName();
		return values;
	}

	@Override
	public String getDefaultValue(Config config, TaskPropertyDef propertyDef) {
		return null;
	}

	@Override
	public void execute(Client client, TaskProperties properties,
			Variables variables, TaskLog taskLog) throws SearchLibException,
			IOException {
		DatabaseCrawlMaster crawlMaster = client.getDatabaseCrawlMaster();
		DatabaseCrawlList crawlList = client.getDatabaseCrawlList();
		String crawlName = properties.getValue(propCrawlName);
		String vars = properties.getValue(propCrawlVariables);
		if (!StringUtils.isEmpty(vars)) {
			variables = new Variables(variables);
			variables.putProperties(vars);
		}
		if (crawlName == null) {
			taskLog.setInfo("The crawl name is missing");
			return;
		}
		DatabaseCrawlAbstract crawl = crawlList.get(crawlName);
		if (crawl == null) {
			taskLog.setInfo("Crawl not found: " + crawlName);
			return;
		}
		try {
			DatabaseCrawlThread ct = crawlMaster.execute(client, crawl, true,
					variables, taskLog);
			if (ct.getException() != null)
				throw new SearchLibException(ct.getException());
		} catch (InterruptedException e) {
			throw new SearchLibException(e);
		}
	}
}
