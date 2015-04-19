/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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

public abstract class ThreadItem<I extends ThreadItem<I, T>, T extends ThreadAbstract<T>>
		implements Comparable<I> {

	protected final ThreadMasterAbstract<?, T> threadMaster;

	protected T lastThread = null;

	protected ThreadItem(ThreadMasterAbstract<?, T> threadMaster) {
		this.threadMaster = threadMaster;
	}

	/**
	 * @return the crawlThread
	 */
	public T getLastThread() {
		return lastThread;
	}

	/**
	 * @param crawlThread
	 *            the crawlThread to set
	 */
	public void setLastThread(T lastThread) {
		this.lastThread = lastThread;
	}

	protected void copyTo(I item) {
		item.lastThread = this.lastThread;
	}

	public boolean isThread() {
		if (threadMaster == null)
			return false;
		return threadMaster.isThread(this);
	}

}
