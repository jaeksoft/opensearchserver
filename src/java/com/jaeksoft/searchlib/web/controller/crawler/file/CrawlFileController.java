/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
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

package com.jaeksoft.searchlib.web.controller.crawler.file;

import java.io.IOException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.database.PropertyManager;
import com.jaeksoft.searchlib.crawler.web.process.CrawlFileMaster;
import com.jaeksoft.searchlib.web.controller.CommonController;

public class CrawlFileController extends CommonController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5309269960746544920L;

	private int sheetRows;

	private boolean refresh;

	public CrawlFileController() throws SearchLibException {
		super();
		sheetRows = 20;
		refresh = false;
	}

	public int getSheetRows() {
		return sheetRows;
	}

	public void onRun() throws IOException, SearchLibException {
		PropertyManager propertyManager = getClient().getPropertyManager();
		if (getFileCrawlMaster().isRunning()) {
			propertyManager.setCrawlEnabled(false);
			getFileCrawlMaster().abort();
		} else {
			propertyManager.setCrawlEnabled(true);
			getFileCrawlMaster().start();
			refresh = true;
		}
		reloadPage();
	}

	public PropertyManager getProperties() throws SearchLibException {
		return getClient().getPropertyManager();
	}

	public CrawlFileMaster getFileCrawlMaster() throws SearchLibException {
		return getClient().getFileCrawlMaster();
	}

	// TODO Publish / vs / plugin
	/*
	 * public boolean isPublish() { PublishIndexList publishList =
	 * getClient().getPublishIndexList(); if (publishList == null) return false;
	 * return publishList.size() > 0; }
	 */

	public boolean isRefresh() throws SearchLibException {
		boolean r = refresh;
		refresh = getFileCrawlMaster().isRunning()
				|| getFileCrawlMaster().isAborting();
		return r;
	}

	public String getRunButtonLabel() throws SearchLibException {
		if (getFileCrawlMaster().isAborting())
			return "Aborting...";
		else if (getFileCrawlMaster().isRunning())
			return "Running - Click to stop";
		else
			return "Not running - Click to start";
	}

	public void onTimer() {
		reloadPage();
	}
}
