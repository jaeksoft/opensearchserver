/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.crawler.process;

import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.filter.PrefixUrlFilter;
import com.jaeksoft.searchlib.crawler.spider.Crawl;
import com.jaeksoft.searchlib.crawler.urldb.UrlDb;
import com.jaeksoft.searchlib.crawler.urldb.UrlItem;

public class CrawlThread implements Runnable {

	private Client client;
	private CrawlMaster crawlMaster;
	private Thread thread;
	private long fetchedCount;
	private long deletedCount;
	private long indexedCount;
	private long ignoredCount;
	private UrlItem currentUrlItem;
	private ArrayList<UrlItem> currentUrlList;
	private boolean bAbort;
	private String error;
	private Status status;

	public enum Status {

		NOTSTARTED("Not started"), RUNNING("Running"), ABORTING("Aborting"), ERROR(
				"Error"), FINISHED("Finished"), ABORTED("Aborted");

		private String name;

		private Status(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

	}

	public CrawlThread(Client client, CrawlMaster crawlMaster) {
		this.client = client;
		this.crawlMaster = crawlMaster;
		this.thread = new Thread(this);
		this.thread.start();
		this.fetchedCount = 0;
		this.deletedCount = 0;
		this.indexedCount = 0;
		this.ignoredCount = 0;
		this.currentUrlItem = null;
		this.currentUrlList = null;
		this.bAbort = false;
		this.error = null;
		this.status = Status.NOTSTARTED;
	}

	public void run() {
		setStatus(Status.RUNNING);
		try {
			ArrayList<UrlItem> urlList = null;
			loop: while ((urlList = crawlMaster.getNextUrlList()) != null) {
				setCurrentUrlList(urlList);
				for (UrlItem urlItem : urlList) {
					if (getAbort())
						break loop;
					crawl(client, urlItem);
					crawlMaster.sleepInterval();
				}
				client.reload();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		evaluateFinalStatus();
		thread = null;
	}

	private void evaluateFinalStatus() {
		synchronized (this) {
			if (getError() != null) {
				setStatus(Status.ERROR);
				return;
			}
			if (status == Status.ABORTING) {
				setStatus(Status.ABORTED);
				return;
			}
			setStatus(Status.FINISHED);
		}
	}

	private void setCurrentUrlList(ArrayList<UrlItem> urlList) {
		synchronized (this) {
			currentUrlList = urlList;
		}
	}

	private void crawl(Config config, UrlItem urlItem) {
		synchronized (this) {
			currentUrlItem = urlItem;
		}
		URL url = urlItem.getURL();
		PrefixUrlFilter prefixFilter = config.getPrefixUrlFilter();
		if (url != null)
			if (prefixFilter.findPrefixUrl(url) == null)
				url = null;
		if (url == null) {
			crawlMaster.deleteBadUrl(urlItem.getUrl());
			incDeletedCount();
			return;
		}
		incFetchedCount();
		try {
			Crawl crawl = new Crawl(config, crawlMaster.getUserAgent(), url);
			Crawl.Status crawlStatus = crawl.getStatus();
			if (crawlStatus == Crawl.Status.CRAWLED) {
				client.updateDocument(crawl.getDocument());
				incIndexedCount();
			} else if (crawlStatus == Crawl.Status.NOPARSER)
				incIgnoredCount();
			new UrlDb(client).update(crawl);
		} catch (Crawl.CrawlException e) {
			e.printStackTrace();
		} catch (IOException e) {
			abort();
			setError(e.getMessage());
		} catch (SQLException e) {
			abort();
			setError(e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			abort();
			setError(e.getMessage());
		}
	}

	private void incDeletedCount() {
		synchronized (this) {
			deletedCount++;
		}
	}

	private void incFetchedCount() {
		synchronized (this) {
			fetchedCount++;
		}
	}

	private void incIndexedCount() {
		synchronized (this) {
			indexedCount++;
		}
	}

	private void incIgnoredCount() {
		synchronized (this) {
			ignoredCount++;
		}
	}

	public UrlItem getCurrentUrlItem() {
		synchronized (this) {
			return currentUrlItem;
		}
	}

	public long getFetchedCount() {
		synchronized (this) {
			return fetchedCount;
		}
	}

	public long getDeletedCount() {
		synchronized (this) {
			return deletedCount;
		}
	}

	public long getIndexedCount() {
		synchronized (this) {
			return indexedCount;
		}
	}

	public long getIgnoredCount() {
		synchronized (this) {
			return ignoredCount;
		}
	}

	public long getUrlListSize() {
		synchronized (this) {
			if (currentUrlList == null)
				return 0;
			return currentUrlList.size();
		}
	}

	protected boolean isRunning() {
		synchronized (this) {
			if (thread == null)
				return false;
			return thread.isAlive();
		}
	}

	protected void abort() {
		synchronized (this) {
			if (status == Status.RUNNING)
				setStatus(Status.ABORTING);
			bAbort = true;
		}
	}

	public boolean getAbort() {
		synchronized (this) {
			return bAbort;
		}
	}

	private void setError(String error) {
		synchronized (this) {
			setStatus(Status.ERROR);
			this.error = error;
		}
	}

	public String getError() {
		synchronized (this) {
			return error;
		}
	}

	private void setStatus(Status status) {
		synchronized (this) {
			if (this.status == Status.ERROR)
				return;
			this.status = status;
		}
	}

	public String getStatusName() {
		synchronized (this) {
			if (status == Status.ERROR)
				return status.name + " " + error;
			return status.name;
		}
	}
}