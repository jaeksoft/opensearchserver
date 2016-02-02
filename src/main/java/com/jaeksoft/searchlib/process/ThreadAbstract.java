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
import java.util.concurrent.atomic.AtomicBoolean;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.scheduler.TaskLog;
import com.jaeksoft.searchlib.streamlimiter.LimitException;
import com.jaeksoft.searchlib.util.InfoCallback;
import com.jaeksoft.searchlib.util.ThreadUtils;
import com.jaeksoft.searchlib.web.StartStopListener.ShutdownWaitInterface;

public abstract class ThreadAbstract<T extends ThreadAbstract<T>> implements Runnable, InfoCallback {

	private final Config config;

	private final ThreadMasterAbstract<?, T> threadMaster;

	private final ThreadItem<?, T> threadItem;

	private volatile String info;

	private volatile AtomicBoolean abort;

	private volatile Thread thread;

	private volatile long startTime;

	private volatile long idleTime;

	private volatile long endTime;

	private volatile Exception exception;

	protected final InfoCallback infoCallback;

	protected final TaskLog taskLog;

	protected ThreadAbstract(Config config, ThreadMasterAbstract<?, T> threadMaster, ThreadItem<?, T> threadItem,
			InfoCallback infoCallback) {
		this.config = config;
		this.threadItem = threadItem;
		this.threadMaster = threadMaster;
		this.infoCallback = infoCallback;
		taskLog = infoCallback != null && infoCallback instanceof TaskLog ? (TaskLog) infoCallback : null;
		exception = null;
		abort = new AtomicBoolean(false);
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
		return info;
	}

	@Override
	public void setInfo(String info) {
		this.info = info;
	}

	public boolean isAborted() {
		if (!abort.get() && taskLog != null)
			if (taskLog.isAbortRequested())
				abort.set(true);
		return abort.get();
	}

	public void abort() {
		abort.set(true);
	}

	public Exception getException() {
		return exception;
	}

	protected void setException(Exception e) {
		this.exception = e;
	}

	public boolean isAborting() {
		return isRunning() && isAborted();

	}

	private class StartInterface extends ShutdownWaitInterface {

		@Override
		public boolean done() {
			if (getStartTime() != 0 || getEndTime() != 0)
				return true;
			State state = getThreadState();
			if (state == State.NEW)
				return false;
			return state == State.RUNNABLE;
		}

	}

	private class EndInterface extends ShutdownWaitInterface {

		@Override
		public boolean done() {
			if (getStartTime() == 0)
				return true;
			if (getEndTime() != 0)
				return true;
			return getThreadState() == State.TERMINATED;
		}

	}

	public boolean waitForStart(long secTimeOut) {
		return ThreadUtils.waitUntil(secTimeOut, new StartInterface());
	}

	public boolean waitForEnd(long secTimeOut) {
		return ThreadUtils.waitUntil(secTimeOut, new EndInterface());
	}

	protected void sleepMs(long ms) throws InterruptedException {
		while (ms > 0 && !isAborted()) {
			Thread.sleep(1000);
			ms -= 1000;
		}
	}

	protected void sleepSec(int sec) throws InterruptedException {
		if (sec == 0)
			return;
		sleepMs(sec * 1000);
	}

	protected void idle() {
		idleTime = System.currentTimeMillis();
	}

	protected boolean isIdleTimeExhausted(long seconds) {
		if (seconds == 0)
			return false;
		long timeElapsed = (System.currentTimeMillis() - idleTime) / 1000;
		return timeElapsed > seconds;
	}

	public String getCurrentMethod() {
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
		return element.getClassName() + '.' + element.getMethodName() + " (" + element.getLineNumber() + ")";
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
		exception = null;
		abort.set(false);
		info = null;
		endTime = 0;
		startTime = System.currentTimeMillis();
		thread = Thread.currentThread();
		thread.setName(getThreadName());
		idleTime = startTime;
	}

	final private void initEnd() {
		thread = null;
		endTime = System.currentTimeMillis();
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
		Thread td = thread;
		if (td == null)
			return false;
		switch (td.getState()) {
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
	}

	final public State getThreadState() {
		Thread td = thread;
		if (td == null)
			return null;
		return td.getState();
	}

	final public String getThreadStatus() {
		Thread td = thread;
		StringBuilder sb = new StringBuilder();
		sb.append(this.hashCode());
		if (td == null)
			return sb.toString();
		sb.append('/');
		sb.append(td.hashCode());
		sb.append(' ');
		sb.append(td.getState().toString());
		return sb.toString();
	}

	protected ThreadItem<?, T> getThreadItem() {
		return threadItem;
	}

	final public void execute(int secTimeOut) {
		startTime = 0;
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
		return startTime;
	}

	public long getDuration() {
		long st = startTime;
		long et = endTime;
		if (st == 0)
			return 0;
		if (et != 0)
			return et - st;
		return System.currentTimeMillis() - st;
	}

	public long getEndTime() {
		return endTime;

	}

	@Override
	public String toString() {
		return super.toString() + " " + getClass().getSimpleName();
	}
}
