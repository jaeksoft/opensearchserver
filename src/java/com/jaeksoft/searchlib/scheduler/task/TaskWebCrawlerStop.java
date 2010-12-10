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

package com.jaeksoft.searchlib.scheduler.task;

import java.util.Properties;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.process.WebCrawlMaster;
import com.jaeksoft.searchlib.scheduler.TaskAbstract;

public class TaskWebCrawlerStop extends TaskAbstract {

	@Override
	public String getName() {
		return "Web crawler - stop";
	}

	@Override
	public String[] getPropertyList() {
		return null;
	}

	@Override
	public String[] getPropertyValues(Client client, String property) {
		return null;
	}

	@Override
	public void execute(Client client, Properties properties)
			throws SearchLibException {
		WebCrawlMaster crawlMaster = client.getWebCrawlMaster();
		if (!crawlMaster.isRunning())
			return;
		crawlMaster.abort();
		crawlMaster.waitForEnd(0);
	}
}
