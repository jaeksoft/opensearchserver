/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.scheduler;

import java.util.Date;

import com.jaeksoft.searchlib.util.ReadWriteLock;

public class ExecutionAbstract {

	private ReadWriteLock rwl = new ReadWriteLock();

	private boolean active;

	private boolean running;

	private Date lastExecution;

	protected ExecutionAbstract() {
		lastExecution = null;
		running = false;
		active = false;
	}

	/**
	 * @param active
	 *            the active to set
	 */
	public void setActive(boolean active) {
		rwl.w.lock();
		try {
			this.active = active;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the active
	 */
	public boolean isActive() {
		rwl.r.lock();
		try {
			return active;
		} finally {
			rwl.r.unlock();
		}
	}

	public boolean isRunning() {
		rwl.r.lock();
		try {
			return running;
		} finally {
			rwl.r.unlock();
		}
	}

	protected void runningEnd() {
		rwl.w.lock();
		try {
			running = false;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the last execution date
	 */
	public Date getLastExecution() {
		rwl.r.lock();
		try {
			return lastExecution;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * Set the last execution date to the current time
	 */
	protected void setRunningNow() {
		rwl.w.lock();
		try {
			running = true;
			lastExecution = new Date();
		} finally {
			rwl.w.unlock();
		}
	}

	public boolean waitForStart(int secTimeOut) throws InterruptedException {
		long timeOut = System.currentTimeMillis() + secTimeOut * 1000;
		while (System.currentTimeMillis() < timeOut) {
			if (getLastExecution() != null)
				return true;
			Thread.sleep(500);
		}
		return false;
	}

}
