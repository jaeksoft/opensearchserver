/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 *
 * OpenSearchServer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSearchServer.  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.web.controller.crawler.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.database.WebPropertyManager;
import com.jaeksoft.searchlib.crawler.web.process.WebCrawlMaster;
import com.jaeksoft.searchlib.web.controller.crawler.CrawlerController;

public class CrawlWebController extends CrawlerController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5309269960746544920L;

	private transient int sheetRows;

	public CrawlWebController() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() {
		sheetRows = 20;
	}

	public int getSheetRows() {
		return sheetRows;
	}

	public WebPropertyManager getProperties() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getWebPropertyManager();
	}

	private final static String[] fetchIntervalUnitValues = { "days", "hours",
			"minutes" };

	public String[] getFetchIntervalUnitValues() {
		return fetchIntervalUnitValues;
	}

	@Override
	public WebCrawlMaster getCrawlMaster() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getWebCrawlMaster();
	}

	public List<String> getSchedulerTaskList() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		List<String> list = new ArrayList<String>(0);
		list.add("");
		client.getJobList().populateNameList(list);
		return list;
	}

	public void onRun() throws SearchLibException, IOException {
		if (!isWebCrawlerStartStopRights())
			throw new SearchLibException("Not allowed");
		WebPropertyManager propertyManager = getClient()
				.getWebPropertyManager();
		if (getCrawlMaster().isRunning()) {
			propertyManager.getCrawlEnabled().setValue(false);
			getCrawlMaster().abort();
		} else {
			boolean once = RunMode.RunOnce == getRunMode();
			if (!once)
				propertyManager.getCrawlEnabled().setValue(true);
			getCrawlMaster().start(once);
		}
		reloadPage();
	}

}
