/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2012 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.web.controller.crawler;

import java.util.ArrayList;
import java.util.List;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.common.process.CrawlMasterAbstract;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.web.controller.CommonController;

public class CrawlerController extends CommonController {

	private static final long serialVersionUID = 2308774605231174474L;

	public enum RunMode {

		RunForever("Run forever"),

		RunOnce("Run once");

		private final String label;

		private RunMode(String label) {
			this.label = label;
		}

		@Override
		public String toString() {
			return label;
		}
	}

	private RunMode runMode = RunMode.RunForever;

	public CrawlerController() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() throws SearchLibException {
	}

	public CrawlMasterAbstract getCrawlMaster() throws SearchLibException {
		return null;
	}

	public boolean isWebCrawlerRights() throws SearchLibException {
		if (!isLogged() || !isInstanceValid())
			return false;
		if (isNoUserList())
			return true;
		return getLoggedUser().hasAnyRole(getIndexName(),
				Role.GROUP_WEB_CRAWLER);
	}

	public boolean isWebCrawlerParametersRights() throws SearchLibException {
		if (!isLogged() || !isInstanceValid())
			return false;
		if (isNoUserList())
			return true;
		return getLoggedUser().hasAnyRole(getIndexName(),
				Role.WEB_CRAWLER_EDIT_PARAMETERS);
	}

	public boolean isWebCrawlerEditPatternsRights() throws SearchLibException {
		if (!isLogged() || !isInstanceValid())
			return false;
		if (isNoUserList())
			return true;
		return getLoggedUser().hasAnyRole(getIndexName(),
				Role.WEB_CRAWLER_EDIT_PATTERN_LIST);
	}

	public boolean isWebCrawlerStartStopRights() throws SearchLibException {
		if (!isLogged() || !isInstanceValid())
			return false;
		if (isNoUserList())
			return true;
		return getLoggedUser().hasAnyRole(getIndexName(),
				Role.WEB_CRAWLER_START_STOP);
	}

	public boolean isFileCrawlerRights() throws SearchLibException {
		if (!isLogged() || !isInstanceValid())
			return false;
		if (isNoUserList())
			return true;
		return getLoggedUser().hasAnyRole(getIndexName(),
				Role.GROUP_FILE_CRAWLER);
	}

	public boolean isFileCrawlerParametersRights() throws SearchLibException {
		if (!isLogged() || !isInstanceValid())
			return false;
		if (isNoUserList())
			return true;
		return getLoggedUser().hasAnyRole(getIndexName(),
				Role.FILE_CRAWLER_EDIT_PARAMETERS);
	}

	public boolean isFileCrawlerEditPatternsRights() throws SearchLibException {
		if (!isLogged() || !isInstanceValid())
			return false;
		if (isNoUserList())
			return true;
		return getLoggedUser().hasAnyRole(getIndexName(),
				Role.FILE_CRAWLER_EDIT_PATTERN_LIST);
	}

	public boolean isFileCrawlerStartStopRights() throws SearchLibException {
		if (!isLogged() || !isInstanceValid())
			return false;
		if (isNoUserList())
			return true;
		return getLoggedUser().hasAnyRole(getIndexName(),
				Role.FILE_CRAWLER_START_STOP);
	}

	public boolean isDatabaseCrawlerRights() throws SearchLibException {
		if (!isLogged() || !isInstanceValid())
			return false;
		if (isNoUserList())
			return true;
		return getLoggedUser().hasAnyRole(getIndexName(),
				Role.GROUP_DATABASE_CRAWLER);
	}

	public boolean isDatabaseCrawlerEditPatternsRights()
			throws SearchLibException {
		if (!isLogged() || !isInstanceValid())
			return false;
		if (isNoUserList())
			return true;
		return getLoggedUser().hasAnyRole(getIndexName(),
				Role.DATABASE_CRAWLER_EDIT_PARAMETERS);
	}

	public boolean isDatabaseCrawlerStartStopRights() throws SearchLibException {
		if (!isLogged() || !isInstanceValid())
			return false;
		if (isNoUserList())
			return true;
		return getLoggedUser().hasAnyRole(getIndexName(),
				Role.DATABASE_CRAWLER_START_STOP);
	}

	public boolean isIndexCrawlerRights() throws SearchLibException {
		if (!isLogged() || !isInstanceValid())
			return false;
		if (isNoUserList())
			return true;
		return getLoggedUser().hasAnyRole(getIndexName(),
				Role.GROUP_INDEX_CRAWLER);
	}

	public boolean isIndexCrawlerEditRights() throws SearchLibException {
		if (!isLogged() || !isInstanceValid())
			return false;
		if (isNoUserList())
			return true;
		return getLoggedUser().hasAnyRole(getIndexName(),
				Role.INDEX_CRAWLER_EDIT);
	}

	public boolean isIndexCrawlerExecuteRights() throws SearchLibException {
		if (!isLogged() || !isInstanceValid())
			return false;
		if (isNoUserList())
			return true;
		return getLoggedUser().hasAnyRole(getIndexName(),
				Role.INDEX_CRAWLER_EXECUTE);
	}

	public RunMode[] getRunModes() {
		return RunMode.values();
	}

	public RunMode getRunMode() {
		return runMode;
	}

	public void setRunMode(RunMode runMode) {
		this.runMode = runMode;
	}

	public boolean isRefresh() throws SearchLibException {
		CrawlMasterAbstract crawlMaster = getCrawlMaster();
		if (crawlMaster == null)
			return false;
		return crawlMaster.isRunning() || crawlMaster.isAborting();
	}

	public boolean isNotRefresh() throws SearchLibException {
		return !isRefresh();
	}

	public String getRunButtonLabel() throws SearchLibException {
		CrawlMasterAbstract crawlMaster = getCrawlMaster();
		if (crawlMaster == null)
			return null;
		if (crawlMaster.isAborting())
			return "Aborting...";
		else if (crawlMaster.isRunning()) {
			if (crawlMaster.isOnce())
				return "Running - Click to stop";
			else
				return "Running - Click to stop";
		} else
			return "Not running - Click to run";
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

	public void onTimer() throws SearchLibException {
		reloadPage();
	}

}
