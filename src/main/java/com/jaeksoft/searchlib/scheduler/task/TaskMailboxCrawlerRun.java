/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014 Emmanuel Keller / Jaeksoft
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

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.mailbox.MailboxCrawlItem;
import com.jaeksoft.searchlib.crawler.mailbox.MailboxCrawlList;
import com.jaeksoft.searchlib.crawler.mailbox.MailboxCrawlMaster;
import com.jaeksoft.searchlib.crawler.mailbox.MailboxCrawlThread;
import com.jaeksoft.searchlib.scheduler.TaskAbstract;
import com.jaeksoft.searchlib.scheduler.TaskLog;
import com.jaeksoft.searchlib.scheduler.TaskProperties;
import com.jaeksoft.searchlib.scheduler.TaskPropertyDef;
import com.jaeksoft.searchlib.scheduler.TaskPropertyType;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.Variables;

public class TaskMailboxCrawlerRun extends TaskAbstract {

	final private TaskPropertyDef propCrawlName = new TaskPropertyDef(
			TaskPropertyType.comboBox, "crawl name", "crawl name",
			"The name of the mailbox crawl item", 50);

	final private TaskPropertyDef propCrawlVariables = new TaskPropertyDef(
			TaskPropertyType.multilineTextBox,
			"crawl variables",
			"crawl variables",
			"The name of the mailbox crawl item. Keep it empty to crawl all the mailbox item.",
			50, 5);

	final private TaskPropertyDef[] taskPropertyDefs = { propCrawlName,
			propCrawlVariables };

	@Override
	public String getName() {
		return "Mailbox crawler - run";
	}

	@Override
	public TaskPropertyDef[] getPropertyList() {
		return taskPropertyDefs;
	}

	@Override
	public String[] getPropertyValues(Config config,
			TaskPropertyDef propertyDef, TaskProperties taskProperties)
			throws SearchLibException {
		MailboxCrawlList crawlList = config.getMailboxCrawlList();
		MailboxCrawlItem[] crawls = crawlList.getArray();
		if (crawls == null)
			return null;
		String[] values = new String[crawls.length];
		int i = 0;
		for (MailboxCrawlItem crawl : crawls)
			values[i++] = crawl.getName();
		return values;
	}

	@Override
	public String getDefaultValue(Config config, TaskPropertyDef propertyDef) {
		return null;
	}

	private void crawlAll(Client client, MailboxCrawlMaster crawlMaster,
			MailboxCrawlList crawlList, Variables variables, TaskLog taskLog)
			throws SearchLibException {
		MailboxCrawlItem[] crawlItems = crawlList.getArray();
		if (crawlItems == null)
			return;
		for (MailboxCrawlItem crawlItem : crawlItems) {
			if (taskLog.isAbortRequested())
				return;
			crawlOne(client, crawlMaster, crawlItem, variables, taskLog);
		}
	}

	private void crawlOne(Client client, MailboxCrawlMaster crawlMaster,
			MailboxCrawlItem crawlItem, Variables variables, TaskLog taskLog)
			throws SearchLibException {
		MailboxCrawlThread ct;
		try {
			ct = crawlMaster.execute(client, crawlItem, true, variables,
					taskLog);
		} catch (InterruptedException e) {
			throw new SearchLibException(e);
		}
		if (ct.getException() != null)
			throw new SearchLibException(ct.getException());
	}

	@Override
	public void execute(Client client, TaskProperties properties,
			Variables variables, TaskLog taskLog) throws SearchLibException,
			IOException {
		MailboxCrawlMaster crawlMaster = client.getMailboxCrawlMaster();
		MailboxCrawlList crawlList = client.getMailboxCrawlList();
		String crawlName = properties.getValue(propCrawlName);

		if (StringUtils.isEmpty(crawlName))
			crawlAll(client, crawlMaster, crawlList, variables, taskLog);
		else {
			MailboxCrawlItem crawlItem = crawlList.get(crawlName);
			if (crawlItem == null)
				throw new SearchLibException("Crawl item not found: "
						+ crawlName);
			crawlOne(client, crawlMaster, crawlItem, variables, taskLog);
		}
	}
}
