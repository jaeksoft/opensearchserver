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

import org.apache.log4j.Logger;

public abstract class DaemonThread implements Runnable {

	final private static Logger logger = Logger.getLogger(DaemonThread.class);

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

	private Status status;
	private Thread thread;
	private boolean abort;
	private String error;
	private boolean perpetual;
	private int sleepInterval;

	protected DaemonThread(boolean perpetual, int sleepInterval) {
		reset();
		this.perpetual = perpetual;
		this.sleepInterval = sleepInterval;
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
			if (status == Status.ERROR)
				return status.name + " " + error;
			return status.name;
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
		do {
			setStatus(Status.RUNNING);
			try {
				runner();
			} catch (Exception e) {
				setError(e.getMessage());
				logger.error(e.getMessage(), e);
			}
			if (perpetual)
				sleepSec(sleepInterval);
		} while (perpetual && !abort);
		evaluateFinalStatus();
	}

	protected void sleepMs(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			logger.error(e);
		}
	}

	protected void sleepSec(int sec) {
		if (sec == 0)
			return;
		while (!abort && sec-- > 0)
			sleepMs(1000);
	}

}
