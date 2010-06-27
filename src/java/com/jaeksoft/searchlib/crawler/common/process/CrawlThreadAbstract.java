/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2010 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.common.process;

import java.lang.Thread.State;
import java.util.Date;

import com.jaeksoft.searchlib.config.Config;

public abstract class CrawlThreadAbstract implements Runnable {

	protected Config config;

	private CrawlMasterAbstract crawlMaster;

	private CrawlStatus status;

	private String info;

	private volatile long statusTime;

	private volatile boolean abort;

	private Thread thread;

	private volatile boolean running;

	protected CrawlStatistics currentStats;

	private Date startTime;

	protected CrawlThreadAbstract(Config config, CrawlMasterAbstract crawlMaster) {
		this.config = config;
		this.crawlMaster = crawlMaster;
		currentStats = null;
		setStatus(CrawlStatus.NOT_RUNNING);
		abort = false;
		thread = null;
		info = null;
		running = false;
	}

	public Config getConfig() {
		return config;
	}

	public CrawlStatus getStatus() {
		synchronized (this) {
			return status;
		}
	}

	public String getInfo() {
		synchronized (this) {
			return info;
		}
	}

	public String getStatusInfo() {
		synchronized (this) {
			StringBuffer sb = new StringBuffer();
			sb.append(status);
			if (info != null) {
				sb.append(' ');
				sb.append('(');
				sb.append(info);
				sb.append(')');
			}
			return sb.toString();
		}
	}

	public long getStatusTimeElapsed() {
		return (System.currentTimeMillis() - statusTime) / 1000;
	}

	public boolean getCrawlTimeOutExhausted(int seconds) {
		synchronized (this) {
			if (getStatus() != CrawlStatus.CRAWL)
				return false;
			return getStatusTimeElapsed() > seconds;
		}
	}

	public void setStatus(CrawlStatus status) {
		synchronized (this) {
			this.status = status;
			this.statusTime = System.currentTimeMillis();
		}
	}

	protected void setInfo(String info) {
		synchronized (this) {
			this.info = info;
		}
	}

	public boolean isAbort() {
		return abort;
	}

	public void abort() {
		abort = true;
	}

	public boolean isAborting() {
		return isRunning() && isAbort();
	}

	public void waitForEnd() {
		while (isRunning())
			sleepMs(100);
	}

	protected void sleepMs(long ms) {
		sleepMs(ms, true);
	}

	protected void sleepMs(long ms, boolean withStatus) {

		try {
			if (withStatus)
				setStatus(CrawlStatus.WAITING);
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	protected void sleepSec(int sec) {
		sleepSec(sec, true);
	}

	protected void sleepSec(int sec, boolean withStatus) {
		if (sec == 0)
			return;
		if (withStatus)
			setStatus(CrawlStatus.WAITING);
		while (!abort && sec-- > 0)
			sleepMs(1000);
	}

	public String getCurrentMethod() {
		synchronized (this) {
			if (thread == null)
				return "No thread";
			StackTraceElement[] ste = thread.getStackTrace();
			if (ste == null)
				return "No stack";
			if (ste.length == 0)
				return "Empty stack";
			StackTraceElement element = ste[0];
			for (StackTraceElement e : ste) {
				if (e.getClassName().contains("jaeksoft")) {
					element = e;
					break;
				}
			}
			return element.getClassName() + '.' + element.getMethodName()
					+ " (" + element.getLineNumber() + ")";
		}
	}

	public abstract void runner() throws Exception;

	public abstract void release();

	final public void run() {
		startTime = new Date();
		abort = false;
		setThread(Thread.currentThread());
		setStatus(CrawlStatus.STARTING);
		try {
			runner();
		} catch (Exception e) {
			e.printStackTrace();
			setStatus(CrawlStatus.ERROR);
			setInfo(e.getMessage());
		}
		if (getStatus() != CrawlStatus.ERROR) {
			if (isAbort())
				setStatus(CrawlStatus.ABORTED);
			else
				setStatus(CrawlStatus.COMPLETE);
		}
		setThread(null);
		if (crawlMaster != null) {
			crawlMaster.remove(this);
			synchronized (crawlMaster) {
				crawlMaster.notify();
			}
		}
		release();
		running = false;
	}

	private void setThread(Thread thread) {
		synchronized (this) {
			this.thread = thread;
		}
	}

	public boolean isRunning() {
		return running;
	}

	public State getThreadState() {
		synchronized (this) {
			if (thread == null)
				return null;
			return thread.getState();
		}
	}

	public String getThreadStatus() {
		synchronized (this) {
			StringBuffer sb = new StringBuffer();
			sb.append(this.hashCode());
			if (thread == null)
				return sb.toString();
			sb.append('/');
			sb.append(thread.hashCode());
			sb.append(' ');
			sb.append(thread.getState().toString());
			return sb.toString();
		}
	}

	final protected void execute() {
		running = true;
		config.getThreadPool().execute(this);
	}

	protected CrawlMasterAbstract getCrawlMaster() {
		return crawlMaster;
	}

	public String getDebugInfo() {
		synchronized (this) {
			StringBuffer sb = new StringBuffer();
			sb.append(getThreadStatus());
			sb.append(' ');
			sb.append(getCurrentInfo());
			return sb.toString();
		}
	}

	protected abstract String getCurrentInfo();

	public CrawlStatistics getCurrentStatistics() {
		return currentStats;
	}

	/**
	 * @return the startTime
	 */
	public Date getStartTime() {
		return startTime;
	}

}
