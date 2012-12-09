/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.controller.scheduler;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.scheduler.JobItem;
import com.jaeksoft.searchlib.web.controller.ScopeAttribute;
import com.jaeksoft.searchlib.web.controller.crawler.CrawlerController;

public class SchedulerController extends CrawlerController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8828061749282398097L;

	public SchedulerController() throws SearchLibException {
		super();
	}

	protected JobItem getJobItemEdit() {
		return (JobItem) getAttribute(ScopeAttribute.JOBITEM_EDIT);
	}

	protected void setJobItemEdit(JobItem jobItem) {
		setAttribute(ScopeAttribute.JOBITEM_EDIT, jobItem);
	}

	protected JobItem getJobItemSelected() {
		return (JobItem) getAttribute(ScopeAttribute.JOBITEM_SELECTED);
	}

	protected void setJobItemSelected(JobItem jobItem) {
		setAttribute(ScopeAttribute.JOBITEM_SELECTED, jobItem);
	}

	public boolean isJobItemEdit() {
		return getJobItemEdit() != null;
	}

	public boolean isNoJobItemEdit() {
		return !isJobItemEdit();
	}

	public boolean isJobItemSelected() {
		return getJobItemSelected() != null;
	}

	public boolean isNoJobItemSelected() {
		return !isJobItemSelected();
	}

	protected void reloadSchedulerPages() throws SearchLibException {
		SchedulerController ctrl = (SchedulerController) getParent()
				.getFellowIfAny("scheduler", true);
		sendReload(ctrl);
	}
}
