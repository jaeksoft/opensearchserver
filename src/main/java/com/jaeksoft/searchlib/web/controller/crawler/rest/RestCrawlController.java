/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013-2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.controller.crawler.rest;

import java.io.IOException;

import javax.naming.NamingException;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.database.DatabaseDriverNames;
import com.jaeksoft.searchlib.crawler.rest.RestCrawlItem;
import com.jaeksoft.searchlib.crawler.rest.RestCrawlItem.CallbackMode;
import com.jaeksoft.searchlib.crawler.rest.RestCrawlList;
import com.jaeksoft.searchlib.crawler.rest.RestCrawlMaster;
import com.jaeksoft.searchlib.crawler.rest.RestCrawlThread;
import com.jaeksoft.searchlib.crawler.web.database.CredentialItem.CredentialType;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;
import com.jaeksoft.searchlib.web.controller.AlertController;
import com.jaeksoft.searchlib.web.controller.crawler.CommonFieldTargetCrawlerController;

@AfterCompose(superclass = true)
public class RestCrawlController
		extends
		CommonFieldTargetCrawlerController<RestCrawlItem, RestCrawlThread, RestCrawlMaster> {

	private transient RestCrawlList crawlList = null;
	private transient boolean debug = false;

	public RestCrawlController() throws SearchLibException, NamingException {
		super();
	}

	@Override
	protected void reset() throws SearchLibException {
		super.reset();
		crawlList = null;
		debug = false;
	}

	public RestCrawlList getRestCrawlList() throws SearchLibException {
		if (crawlList != null)
			return crawlList;
		Client client = getClient();
		if (client == null)
			return null;
		crawlList = client.getRestCrawlList();
		return crawlList;
	}

	public String[] getDriverClassList() {
		return DatabaseDriverNames.getAvailableList(getDesktop().getWebApp()
				.getClass().getClassLoader());
	}

	public CredentialType[] getCredentialTypes() {
		return CredentialType.values();
	}

	public CallbackMode[] getCallbackModes() {
		return CallbackMode.values();
	}

	public HttpDownloader.Method[] getHttpMethods() {
		return HttpDownloader.Method.values();
	}

	@Command
	@NotifyChange("currentCrawl")
	public void reloadCurrentCredential() {
	}

	@Override
	@Command
	public void onSave() throws InterruptedException, SearchLibException,
			IOException {
		getRestCrawlList();
		if (getSelectedCrawl() != null)
			getCurrentCrawl().copyTo(getSelectedCrawl());
		else {
			if (crawlList.get(getCurrentCrawl().getName()) != null) {
				new AlertController("The crawl name is already used");
				return;
			}
			crawlList.add(getCurrentCrawl());
		}
		getClient().saveRestCrawlList();
		onCancel();
	}

	@Override
	@Command
	public void onNew() throws SearchLibException {
		RestCrawlItem oldCurrentCrawl = getCurrentCrawl();
		setSelectedCrawl(null);
		RestCrawlItem newCrawl = new RestCrawlItem(getCrawlMaster());
		setCurrentCrawl(newCrawl);
		if (oldCurrentCrawl != null)
			oldCurrentCrawl.copyTo(newCrawl);
		newCrawl.setName(null);
		reload();
	}

	@Override
	@Command
	public void reload() throws SearchLibException {
		crawlList = null;
		super.reload();
	}

	@Override
	protected void doDelete(RestCrawlItem crawlItem) throws SearchLibException,
			IOException {
		Client client = getClient();
		client.getRestCrawlList().remove(crawlItem);
		client.saveRestCrawlList();
	}

	@Override
	protected RestCrawlItem newCrawlItem(RestCrawlItem crawl) {
		return crawl.duplicate();
	}

	@Override
	public RestCrawlMaster getCrawlMaster() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getRestCrawlMaster();
	}

	@Override
	public boolean isCrawlerEditRights() throws SearchLibException {
		return super.isRestCrawlerEditRights();
	}

	@Override
	public boolean isRefresh() throws SearchLibException {
		RestCrawlMaster crawlMaster = getCrawlMaster();
		if (crawlMaster == null)
			return false;
		return crawlMaster.getThreadsCount() > 0;
	}

	@Override
	@Command
	public void onTimer() throws SearchLibException {
		reload();
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}
}
