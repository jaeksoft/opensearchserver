/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.process;

import java.lang.Thread.State;
import java.util.Date;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.streamlimiter.LimitException;
import com.jaeksoft.searchlib.util.InfoCallback;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.web.StartStopListener;

public abstract class ThreadAbstract implements Runnable, InfoCallback {

	final private ReadWriteLock rwl = new ReadWriteLock();

	private volatile Config config;

	private volatile ThreadMasterAbstract threadMaster;

	private volatile String info;

	private volatile boolean abort;

	private volatile Thread thread;

	private volatile boolean running;

	private volatile long startTime;

	private volatile long idleTime;

	private volatile long endTime;

	private volatile Exception exception;

	protected ThreadAbstract(Config config, ThreadMasterAbstract threadMaster) {
		this.config = config;
		this.threadMaster = threadMaster;
		exception = null;
		abort = false;
		thread = null;
		info = null;
		running = false;
		startTime = 0;
		idleTime = 0;
		endTime = 0;
	}

	public Config getConfig() {
		rwl.r.lock();
		try {
			return config;
		} finally {
			rwl.r.unlock();
		}
	}

	public String getInfo() {
		rwl.r.lock();
		try {
			return info;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public void setInfo(String info) {
		rwl.w.lock();
		try {
			this.info = info;
		} finally {
			rwl.w.unlock();
		}
	}

	public boolean isAborted() {
		if (StartStopListener.isShutdown())
			abort();
		rwl.r.lock();
		try {
			return abort;
		} finally {
			rwl.r.unlock();
		}
	}

	public void abort() {
		rwl.w.lock();
		try {
			abort = true;
		} finally {
			rwl.w.unlock();
		}
	}

	public Exception getException() {
		rwl.r.lock();
		try {
			return exception;
		} finally {
			rwl.r.unlock();
		}
	}

	protected void setException(Exception e) {
		rwl.w.lock();
		try {
			this.exception = e;
		} finally {
			rwl.w.unlock();
		}
	}

	public boolean isAborting() {
		return isRunning() && isAborted();

	}

	public boolean waitForStart(int secTimeOut) {
		long finalTime = System.currentTimeMillis() + secTimeOut * 1000;
		while (getStartTime() == 0) {
			if (secTimeOut != 0)
				if (System.currentTimeMillis() > finalTime)
					return false;
			sleepMs(100);
		}
		return true;
	}

	public boolean waitForEnd(int secTimeOut) {
		long finalTime = System.currentTimeMillis() + secTimeOut * 1000;
		while (isRunning()) {
			if (isAborted())
				return false;
			if (secTimeOut != 0)
				if (System.currentTimeMillis() > finalTime)
					return false;
			sleepMs(100);
		}
		return true;
	}

	protected void sleepMs(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			Logging.warn(e.getMessage(), e);
		}
	}

	protected void sleepSec(int sec) {
		if (sec == 0)
			return;
		long finalTime = System.currentTimeMillis() + sec * 1000;
		while (System.currentTimeMillis() < finalTime)
			sleepMs(500);
	}

	protected void idle() {
		rwl.w.lock();
		try {
			idleTime = System.currentTimeMillis();
		} finally {
			rwl.w.unlock();
		}
	}

	protected boolean isIdleTimeExhausted(int seconds) {
		rwl.r.lock();
		try {
			if (seconds == 0)
				return false;
			long timeElapsed = (System.currentTimeMillis() - idleTime) / 1000;
			return timeElapsed > seconds;
		} finally {
			rwl.r.unlock();
		}

	}

	public String getCurrentMethod() {
		rwl.r.lock();
		try {
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
		} finally {
			rwl.r.unlock();
		}
	}

	public abstract void runner() throws Exception;

	public abstract void release();

	final private String getThreadName() {
		StringBuffer sb = new StringBuffer();
		if (config != null) {
			sb.append("Index: ");
			sb.append(config.getIndexName());
			sb.append(' ');
		}
		sb.append(getClass().getSimpleName());
		sb.append(' ');
		sb.append(new Date(startTime));
		return sb.toString();
	}

	@Override
	final public void run() {
		rwl.w.lock();
		try {
			startTime = System.currentTimeMillis();
			idleTime = startTime;
			abort = false;
			thread = Thread.currentThread();
			thread.setName(getThreadName());
		} finally {
			rwl.w.unlock();
		}
		try {
			runner();
		} catch (Exception e) {
			setException(e);
			setInfo(e.getMessage());
			if (!(e instanceof LimitException))
				Logging.error(e.getMessage(), e);
		}
		if (threadMaster != null) {
			threadMaster.remove(this);
			synchronized (threadMaster) {
				threadMaster.notify();
			}
		}
		rwl.w.lock();
		try {
			thread = null;
			running = false;
			endTime = System.currentTimeMillis();
		} finally {
			rwl.w.unlock();
		}
		release();
	}

	public boolean isRunning() {
		rwl.r.lock();
		try {
			return running;
		} finally {
			rwl.r.unlock();
		}
	}

	public State getThreadState() {
		rwl.r.lock();
		try {
			if (thread == null)
				return null;
			return thread.getState();
		} finally {
			rwl.r.unlock();
		}
	}

	public String getThreadStatus() {
		rwl.r.lock();
		try {
			StringBuffer sb = new StringBuffer();
			sb.append(this.hashCode());
			if (thread == null)
				return sb.toString();
			sb.append('/');
			sb.append(thread.hashCode());
			sb.append(' ');
			sb.append(thread.getState().toString());
			return sb.toString();
		} finally {
			rwl.r.unlock();
		}
	}

	final public void execute() {
		rwl.w.lock();
		try {
			running = true;
			config.getThreadPool().execute(this);
		} finally {
			rwl.w.unlock();
		}
	}

	final protected ThreadMasterAbstract getThreadMaster() {
		rwl.r.lock();
		try {
			return threadMaster;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @return the startTime
	 */
	public long getStartTime() {
		rwl.r.lock();
		try {
			return startTime;
		} finally {
			rwl.r.unlock();
		}
	}

	public long getDuration() {
		rwl.r.lock();
		try {
			if (startTime == 0)
				return 0;
			if (endTime != 0)
				return endTime - startTime;
			return System.currentTimeMillis() - startTime;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public String toString() {
		return super.toString() + " " + getClass().getSimpleName();
	}
}
