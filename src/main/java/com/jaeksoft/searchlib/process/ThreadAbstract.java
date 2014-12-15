/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2014 Emmanuel Keller / Jaeksoft
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
import com.jaeksoft.searchlib.util.ThreadUtils;
import com.jaeksoft.searchlib.web.StartStopListener.ShutdownWaitInterface;

public abstract class ThreadAbstract<T extends ThreadAbstract<T>> implements
		Runnable, InfoCallback {

	final private ReadWriteLock rwl = new ReadWriteLock();

	private final Config config;

	private final ThreadMasterAbstract<?, T> threadMaster;

	private final ThreadItem<?, T> threadItem;

	private volatile String info;

	private volatile boolean abort;

	private volatile Thread thread;

	private volatile long startTime;

	private volatile long idleTime;

	private volatile long endTime;

	private volatile Exception exception;

	protected ThreadAbstract(Config config,
			ThreadMasterAbstract<?, T> threadMaster, ThreadItem<?, T> threadItem) {
		this.config = config;
		this.threadItem = threadItem;
		this.threadMaster = threadMaster;
		exception = null;
		abort = false;
		thread = null;
		info = null;
		startTime = 0;
		idleTime = 0;
		endTime = 0;
	}

	public Config getConfig() {
		return config;
	}

	@Override
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

	private class StartInterface extends ShutdownWaitInterface {

		@Override
		public boolean done() {
			return getStartTime() != 0;
		}

	}

	private class EndInterface extends ShutdownWaitInterface {

		@Override
		public boolean done() {
			return !isRunning();
		}

	}

	public boolean waitForStart(long secTimeOut) {
		return ThreadUtils.waitUntil(secTimeOut, new StartInterface());
	}

	public boolean waitForEnd(long secTimeOut) {
		return ThreadUtils.waitUntil(secTimeOut, new EndInterface());
	}

	protected void sleepMs(long ms) {
		try {
			while (ms > 0 && !isAborted()) {
				Thread.sleep(1000);
				ms -= 1000;
			}
		} catch (InterruptedException e) {
			Logging.warn(e.getMessage(), e);
		}
	}

	protected void sleepSec(int sec) {
		if (sec == 0)
			return;
		sleepMs(sec * 1000);
	}

	protected void idle() {
		rwl.w.lock();
		try {
			idleTime = System.currentTimeMillis();
		} finally {
			rwl.w.unlock();
		}
	}

	protected boolean isIdleTimeExhausted(long seconds) {
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

	protected abstract void runner() throws Exception;

	public abstract void release();

	final private String getThreadName() {
		StringBuilder sb = new StringBuilder();
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

	final private void initStart() {
		rwl.w.lock();
		try {
			exception = null;
			abort = false;
			info = null;
			endTime = 0;
			startTime = System.currentTimeMillis();
			thread = Thread.currentThread();
			thread.setName(getThreadName());
			idleTime = startTime;
		} finally {
			rwl.w.unlock();
		}
	}

	final private void initEnd() {
		rwl.w.lock();
		try {
			thread = null;
			endTime = System.currentTimeMillis();
		} finally {
			rwl.w.unlock();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	final public void run() {
		initStart();
		try {
			runner();
		} catch (Exception e) {
			setException(e);
			setInfo(e.getMessage());
			if (!(e instanceof LimitException))
				Logging.error(e.getMessage(), e);
		} finally {
			initEnd();
			if (threadMaster != null) {
				threadMaster.remove((T) this);
				synchronized (threadMaster) {
					threadMaster.notify();
				}
			}
		}
		release();
	}

	final public boolean isRunning() {
		rwl.r.lock();
		try {
			if (thread == null)
				return false;
			switch (thread.getState()) {
			case NEW:
				return false;
			case BLOCKED:
				return true;
			case RUNNABLE:
				return true;
			case TERMINATED:
				return false;
			case TIMED_WAITING:
				return true;
			case WAITING:
				return true;
			}
			return false;
		} finally {
			rwl.r.unlock();
		}
	}

	final public State getThreadState() {
		rwl.r.lock();
		try {
			if (thread == null)
				return null;
			return thread.getState();
		} finally {
			rwl.r.unlock();
		}
	}

	final public String getThreadStatus() {
		rwl.r.lock();
		try {
			StringBuilder sb = new StringBuilder();
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

	protected ThreadItem<?, T> getThreadItem() {
		return threadItem;
	}

	final public void execute(int secTimeOut) {
		rwl.w.lock();
		try {
			startTime = 0;
		} finally {
			rwl.w.unlock();
		}
		config.getThreadPool().execute(this);
		waitForStart(secTimeOut);
	}

	protected ThreadMasterAbstract<?, T> getThreadMaster() {
		return threadMaster;
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

	public long getEndTime() {
		rwl.r.lock();
		try {
			return endTime;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public String toString() {
		return super.toString() + " " + getClass().getSimpleName();
	}
}
