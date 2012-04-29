/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2011 Emmanuel Keller / Jaeksoft
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
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.apache.http.HttpException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.robotstxt.RobotsTxt;
import com.jaeksoft.searchlib.util.properties.PropertyItem;
import com.jaeksoft.searchlib.web.controller.crawler.CrawlerController;

public class RobotsTxtController extends CrawlerController {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5532994554753193496L;

	private transient int pageSize;

	private transient String searchString;

	private transient RobotsTxt[] robotsTxtList;

	private transient RobotsTxt selectedRobotsTxt;

	public RobotsTxtController() throws SearchLibException {
		super();
	}

	@Override
	public void reset() {
		searchString = null;
		pageSize = 20;
		robotsTxtList = null;
		selectedRobotsTxt = null;
	}

	public int getPageSize() {
		return pageSize;
	}

	public RobotsTxt[] getRobotsTxtList() throws SearchLibException,
			MalformedURLException {
		Client client = getClient();
		if (client == null)
			return null;
		if (robotsTxtList != null)
			return robotsTxtList;
		if (searchString == null || searchString.length() == 0) {
			robotsTxtList = client.getRobotsTxtCache().getRobotsTxtList();
			return robotsTxtList;
		}
		RobotsTxt robotsTxt = client.getRobotsTxtCache().findRobotsTxt(
				searchString);
		if (robotsTxt == null)
			return null;
		selectedRobotsTxt = robotsTxt;
		robotsTxtList = new RobotsTxt[1];
		robotsTxtList[0] = robotsTxt;
		return robotsTxtList;
	}

	public PropertyItem<Boolean> getEnabled() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getWebPropertyManager().getRobotsTxtEnabled();
	}

	public void onSearch() throws IOException, URISyntaxException,
			SearchLibException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, HttpException {
		selectedRobotsTxt = null;
		robotsTxtList = null;
		onReload();
	}

	public void onReset() throws IOException, URISyntaxException,
			SearchLibException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, HttpException {
		searchString = null;
		onSearch();
	}

	public void setSearchString(String search) {
		searchString = search;
	}

	public String getSearchString() {
		return searchString;
	}

	public void setSelectedItem(RobotsTxt robotsTxt) {
		selectedRobotsTxt = robotsTxt;
	}

	public RobotsTxt getSelectedItem() {
		return selectedRobotsTxt;
	}

	public boolean isSelectedEntry() {
		return selectedRobotsTxt != null;
	}

}
