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

package com.jaeksoft.searchlib.web.controller;

import javax.naming.NamingException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.scheduler.JobItem;

public class SchedulerController extends CommonController {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5736529335058096440L;

	private JobItem selectedItem;

	private JobItem currentItem;

	public SchedulerController() throws SearchLibException, NamingException {
		super();
	}

	@Override
	protected void reset() throws SearchLibException {
		selectedItem = null;
		currentItem = null;
	}

	public JobItem getItem() {
		return currentItem;
	}

	public String getCurrentEditMode() throws SearchLibException {
		return selectedItem == null ? "Create a new job"
				: "Edit the selected job";
	}

	public boolean selected() {
		return selectedItem != null;
	}

	public boolean notSelected() {
		return !selected();
	}

}
