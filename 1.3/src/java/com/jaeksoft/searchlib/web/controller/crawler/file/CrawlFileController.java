/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.controller.crawler.file;

import java.io.IOException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.file.database.FilePropertyManager;
import com.jaeksoft.searchlib.crawler.file.process.CrawlFileMaster;
import com.jaeksoft.searchlib.web.controller.crawler.CrawlerController;

public class CrawlFileController extends CrawlerController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5309269960746544920L;

	private transient int sheetRows;

	public CrawlFileController() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() {
		sheetRows = 20;
	}

	public int getSheetRows() {
		return sheetRows;
	}

	public void onRun() throws IOException, SearchLibException {
		if (!isFileCrawlerStartStopRights())
			throw new SearchLibException("Not allowed");
		FilePropertyManager propertyManager = getProperties();
		if (getFileCrawlMaster().isRunning()) {
			propertyManager.getCrawlEnabled().setValue(false);
			getFileCrawlMaster().abort();
		} else {
			propertyManager.getCrawlEnabled().setValue(true);
			getFileCrawlMaster().start();
		}
		reloadPage();
	}

	public FilePropertyManager getProperties() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getFilePropertyManager();
	}

	public CrawlFileMaster getFileCrawlMaster() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getFileCrawlMaster();
	}

	public boolean isRefresh() throws SearchLibException {
		CrawlFileMaster crawlFileMaster = getFileCrawlMaster();
		if (crawlFileMaster == null)
			return false;
		return crawlFileMaster.isRunning() || crawlFileMaster.isAborting();
	}

	public String getRunButtonLabel() throws SearchLibException {
		CrawlFileMaster crawlFileMaster = getFileCrawlMaster();
		if (crawlFileMaster == null)
			return null;
		if (crawlFileMaster.isAborting())
			return "Aborting...";
		else if (crawlFileMaster.isRunning())
			return "Running - Click to stop";
		else
			return "Not running - Click to start";
	}

	public void onTimer() throws SearchLibException {
		reloadPage();
	}

}
