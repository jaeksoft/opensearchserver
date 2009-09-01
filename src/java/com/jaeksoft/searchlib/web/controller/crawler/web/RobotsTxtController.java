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

package com.jaeksoft.searchlib.web.controller.crawler.web;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Map.Entry;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.robotstxt.RobotsTxt;
import com.jaeksoft.searchlib.web.controller.CommonController;

public class RobotsTxtController extends CommonController {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5532994554753193496L;

	private int pageSize;

	private Entry<String, RobotsTxt> selectedEntry;

	public RobotsTxtController() throws SearchLibException {
		super();
		pageSize = 20;
		selectedEntry = null;
	}

	public int getPageSize() {
		return pageSize;
	}

	public Map<String, RobotsTxt> getRobotsTxtMap() throws SearchLibException {
		return getClient().getRobotsTxtCache().getRobotsTxtMap();
	}

	public void onSearch() throws IOException, URISyntaxException,
			SearchLibException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		super.onReload();
	}

	public void setSelectedItem(Entry<String, RobotsTxt> entry) {
		selectedEntry = entry;
	}

	public Entry<String, RobotsTxt> getSelectedItem() {
		return selectedEntry;
	}

	public boolean isSelectedEntry() {
		return selectedEntry != null;
	}

}
