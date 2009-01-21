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

package com.jaeksoft.searchlib.util;

import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class DaemonThread implements Runnable {

	final private static Logger logger = Logger.getLogger(DaemonThread.class
			.getCanonicalName());

	public enum Status {

		NOTSTARTED("Not started"), RUNNING("Running"), SLEEPING("Sleeping"), ABORTING(
				"Aborting"), ERROR("Error"), FINISHED("Finished"), ABORTED(
				"Aborted");

		private String name;

		private Status(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	private Status status;
	private Thread thread;
	private boolean abort;
	private String error;

	protected DaemonThread() {
		reset();
	}

	private void reset() {
		status = Status.NOTSTARTED;
		abort = false;
		thread = null;
		error = null;
	}

	public boolean start() {
		synchronized (this) {
			if (isRunning())
				return false;
			reset();
			thread = new Thread(this);
			thread.setDaemon(true);
			thread.start();
			return true;
		}
	}

	public boolean isRunning() {
		synchronized (this) {
			if (thread == null)
				return false;
			return thread.isAlive();
		}
	}

	public void abort() {
		synchronized (this) {
			if (status == Status.RUNNING)
				setStatus(Status.ABORTING);
			abort = true;
		}
	}

	public boolean isAborting() {
		synchronized (this) {
			return abort && isRunning();
		}
	}

	public boolean getAbort() {
		synchronized (this) {
			return abort;
		}
	}

	protected void setError(String error) {
		synchronized (this) {
			setStatus(Status.ERROR);
			this.error = error;
		}
	}

	protected void setError(Exception e) {
		synchronized (this) {
			setStatus(Status.ERROR);
			this.error = e.getMessage();
		}
	}

	public String getError() {
		synchronized (this) {
			return error;
		}
	}

	protected void setStatus(Status status) {
		synchronized (this) {
			if (this.status == Status.ERROR)
				return;
			this.status = status;
		}
	}

	public String getStatusName() {
		synchronized (this) {
			StringBuffer sb = new StringBuffer(status.name);
			if (status == Status.ERROR) {
				sb.append(' ');
				sb.append(error);
			}
			return sb.toString();
		}
	}

	public String getThreadStatus() {
		synchronized (this) {
			if (thread != null)
				return thread.getState().toString();
			return "";
		}
	}

	private void evaluateFinalStatus() {
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

	public abstract void runner() throws Exception;

	final public void run() {
		abort = false;
		setStatus(Status.RUNNING);
		try {
			runner();
		} catch (Exception e) {
			setError(e.getMessage());
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
		evaluateFinalStatus();
	}

	protected void sleepMs(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	protected void sleepSec(int sec) {
		if (sec == 0)
			return;
		setStatus(Status.SLEEPING);
		while (!abort && sec-- > 0)
			sleepMs(1000);
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
		return element.getClassName() + '.' + element.getMethodName() + " ("
				+ element.getLineNumber() + ")";
	}
}
