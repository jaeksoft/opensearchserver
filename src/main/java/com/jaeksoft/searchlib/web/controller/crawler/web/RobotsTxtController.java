/*
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2008-2017 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jaeksoft.searchlib.web.controller.crawler.web;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.robotstxt.RobotsTxtItem;
import com.jaeksoft.searchlib.util.properties.PropertyItem;
import com.jaeksoft.searchlib.web.controller.crawler.CrawlerController;
import org.apache.http.HttpException;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

@AfterCompose(superclass = true)
public class RobotsTxtController extends CrawlerController {

	private transient int pageSize;

	private transient String searchString;

	private transient RobotsTxtItem[] robotsTxtList;

	private transient RobotsTxtItem selectedRobotsTxt;

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

	public RobotsTxtItem[] getRobotsTxtList()
			throws SearchLibException, MalformedURLException, URISyntaxException, ClassNotFoundException {
		Client client = getClient();
		if (client == null)
			return null;
		if (robotsTxtList != null)
			return robotsTxtList;
		if (searchString == null || searchString.length() == 0) {
			robotsTxtList = client.getRobotsTxtCache().getRobotsTxtList();
			return robotsTxtList;
		}
		RobotsTxtItem robotsTxt = client.getRobotsTxtCache().findRobotsTxt(searchString);
		if (robotsTxt == null)
			return null;
		selectedRobotsTxt = robotsTxt;
		robotsTxtList = new RobotsTxtItem[1];
		robotsTxtList[0] = robotsTxt;
		return robotsTxtList;
	}

	public PropertyItem<Boolean> getEnabled() throws SearchLibException, IOException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getWebPropertyManager().getRobotsTxtEnabled();
	}

	@Command
	public void onSearch()
			throws IOException, URISyntaxException, SearchLibException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, HttpException {
		selectedRobotsTxt = null;
		robotsTxtList = null;
		reload();
	}

	@Command
	public void onReset()
			throws IOException, URISyntaxException, SearchLibException, InstantiationException, IllegalAccessException,
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

	public void setSelectedItem(RobotsTxtItem robotsTxt) {
		selectedRobotsTxt = robotsTxt;
	}

	public RobotsTxtItem getSelectedItem() {
		return selectedRobotsTxt;
	}

	public boolean isSelectedEntry() {
		return selectedRobotsTxt != null;
	}

}
