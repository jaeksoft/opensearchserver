/*
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2010-2017 Emmanuel Keller / Jaeksoft
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

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.database.HostUrlList.ListType;
import com.jaeksoft.searchlib.crawler.web.process.WebCrawlThread;
import com.jaeksoft.searchlib.crawler.web.spider.DownloadItem;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.util.LinkUtils;
import com.jaeksoft.searchlib.web.controller.AlertController;
import com.jaeksoft.searchlib.web.controller.CommonController;
import com.jaeksoft.searchlib.webservice.crawler.webcrawler.WebCrawlerImpl;
import org.json.JSONException;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Messagebox;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

@AfterCompose(superclass = true)
public class ManualWebCrawlController extends CommonController {

	private transient String url;

	private transient WebCrawlThread currentCrawlThread;

	public ManualWebCrawlController() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() {
		url = null;
		currentCrawlThread = null;
	}

	/**
	 * @param url the url to set
	 */
	@NotifyChange("*")
	public void setUrl(String url) {
		this.url = url;
	}

	@Command
	@NotifyChange({ "crawlJsonApi", "crawlXmlApi" })
	public void onChanging(@ContextParam(ContextType.TRIGGER_EVENT) InputEvent event) throws SearchLibException {
		setUrl(event.getValue());
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	public WebCrawlThread getCrawlThread() {
		synchronized (this) {
			return currentCrawlThread;
		}
	}

	private boolean checkNotRunning() throws InterruptedException {
		if (!isCrawlRunning())
			return true;
		new AlertController("A crawl is already running", Messagebox.ERROR);
		return false;
	}

	private boolean checkCrawlCacheEnabled() throws InterruptedException, SearchLibException {
		if (isCrawlCache())
			return true;
		new AlertController("The crawl cache is disabled", Messagebox.ERROR);
		return false;
	}

	@Command
	public void onCrawl() throws SearchLibException, ParseException, IOException, SyntaxError, URISyntaxException,
			ClassNotFoundException, InterruptedException, InstantiationException, IllegalAccessException {
		synchronized (this) {
			if (!checkNotRunning())
				return;
			currentCrawlThread = getClient().getWebCrawlMaster().manualCrawl(LinkUtils.newEncodedURL(url),
					ListType.MANUAL);
			currentCrawlThread.waitForStart(60);
			reload();
		}
	}

	@Command
	public void onFlushCache()
			throws SearchLibException, MalformedURLException, IOException, URISyntaxException, InterruptedException {
		synchronized (this) {
			if (!checkNotRunning())
				return;
			if (!checkCrawlCacheEnabled())
				return;
			boolean deleted = getClient().getCrawlCacheManager().getItem(LinkUtils.newEncodedURI(url)).flush();
			new AlertController(deleted ? "Content deleted" : "Nothing to delete", Messagebox.INFORMATION);
		}
	}

	@Command
	public void onDownload()
			throws IOException, InterruptedException, SearchLibException, URISyntaxException, JSONException {
		synchronized (this) {
			if (!checkNotRunning())
				return;
			if (!checkCrawlCacheEnabled())
				return;
			DownloadItem downloadItem = getClient().getCrawlCacheManager().getItem(LinkUtils.newEncodedURI(url)).load();
			if (downloadItem == null) {
				new AlertController("No content", Messagebox.EXCLAMATION);
				return;
			}
			Filedownload.save(downloadItem.getContentInputStream(), downloadItem.getContentBaseType(), "crawl.cache");
		}
	}

	public boolean isCrawlComplete() {
		synchronized (this) {
			if (currentCrawlThread == null)
				return false;
			if (currentCrawlThread.isRunning())
				return false;
			return true;
		}
	}

	public boolean isCrawlCache() throws SearchLibException {
		synchronized (this) {
			return getClient().getCrawlCacheManager().isEnabled();
		}

	}

	@Command
	public void onTimer() throws SearchLibException {
		reload();
	}

	public boolean isCrawlRunning() {
		synchronized (this) {
			if (currentCrawlThread == null)
				return false;
			return currentCrawlThread.isRunning();
		}
	}

	public boolean isRefresh() {
		return isCrawlRunning();
	}

	public String getCrawlXmlApi() throws UnsupportedEncodingException, SearchLibException {
		return WebCrawlerImpl.getCrawlXML(getLoggedUser(), getClient(), getUrl());
	}

	public String getCrawlJsonApi() throws UnsupportedEncodingException, SearchLibException {
		return WebCrawlerImpl.getCrawlJSON(getLoggedUser(), getClient(), getUrl());
	}
}
