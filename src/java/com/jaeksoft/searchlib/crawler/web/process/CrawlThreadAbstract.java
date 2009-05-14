/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
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

package com.jaeksoft.searchlib.crawler.web.process;

import java.lang.Thread.State;
import java.util.concurrent.ExecutorService;

public abstract class CrawlThreadAbstract implements Runnable {

	private CrawlStatus status;

	private String info;

	private volatile long statusTime;

	private volatile boolean abort;

	private Thread thread;

	private volatile boolean running;

	protected CrawlThreadAbstract() {
		setStatus(CrawlStatus.NOT_RUNNING);
		abort = false;
		thread = null;
		info = null;
		running = false;
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

	protected void sleepMs(long ms) {
		try {
			setStatus(CrawlStatus.WAITING);
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	protected void sleepSec(int sec) {
		if (sec == 0)
			return;
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

	public abstract void complete();

	final public void run() {
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
		complete();
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

	protected State getThreadState() {
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

	final public void start(ExecutorService threadPool) {
		running = true;
		threadPool.execute(this);
	}
}
