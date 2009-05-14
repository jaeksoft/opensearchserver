/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer.  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.web.controller.crawler.web;

import java.io.IOException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.database.PropertyManager;
import com.jaeksoft.searchlib.crawler.web.process.CrawlMaster;
import com.jaeksoft.searchlib.web.controller.CommonController;

public class CrawlController extends CommonController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5309269960746544920L;

	private int sheetRows;

	private boolean refresh;

	public CrawlController() throws SearchLibException {
		super();
		sheetRows = 20;
		refresh = false;
	}

	public int getSheetRows() {
		return sheetRows;
	}

	public void onRun() throws IOException, SearchLibException {
		PropertyManager propertyManager = getClient().getPropertyManager();
		if (getWebCrawlMaster().isRunning()) {
			propertyManager.setCrawlEnabled(false);
			getWebCrawlMaster().abort();
		} else {
			propertyManager.setCrawlEnabled(true);
			getWebCrawlMaster().start();
			refresh = true;
		}
		reloadPage();
	}

	public PropertyManager getProperties() throws SearchLibException {
		return getClient().getPropertyManager();
	}

	public CrawlMaster getWebCrawlMaster() throws SearchLibException {
		return getClient().getWebCrawlMaster();
	}

	// TODO Publish / vs / plugin
	/*
	 * public boolean isPublish() { PublishIndexList publishList =
	 * getClient().getPublishIndexList(); if (publishList == null) return false;
	 * return publishList.size() > 0; }
	 */

	public boolean isRefresh() throws SearchLibException {
		boolean r = refresh;
		refresh = getWebCrawlMaster().isRunning()
				|| getWebCrawlMaster().isAborting();
		return r;
	}

	public String getRunButtonLabel() throws SearchLibException {
		if (getWebCrawlMaster().isAborting())
			return "Aborting...";
		else if (getWebCrawlMaster().isRunning())
			return "Running - Click to stop";
		else
			return "Not running - Click to start";
	}

	public void onTimer() {
		reloadPage();
	}
}
