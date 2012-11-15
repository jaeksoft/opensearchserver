/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.controller.runtime;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.List;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.logreport.ErrorParserLogger;
import com.jaeksoft.searchlib.web.controller.CommonController;

public class ThreadsController extends CommonController {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8629877998029844801L;

	private List<ThreadInfo> threadList = null;

	public static class ThreadInfo {

		private final String name;

		private final String location;

		private final State state;

		public ThreadInfo(Thread thread) {
			this.name = thread.getName();
			StackTraceElement[] elements = thread.getStackTrace();
			String l = ErrorParserLogger.getLocation(elements);
			if (l == null)
				l = ErrorParserLogger.getFirstLocation(elements);
			this.location = l;
			this.state = thread.getState();
		}

		public String getName() {
			return name;
		}

		public String getLocation() {
			return location;
		}

		public State getState() {
			return state;
		}
	}

	public ThreadsController() throws SearchLibException {
		super();
	}

	public List<ThreadInfo> getList() throws SearchLibException {
		if (threadList != null)
			return threadList;
		ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
		ThreadGroup parentGroup;
		while ((parentGroup = rootGroup.getParent()) != null) {
			rootGroup = parentGroup;
		}
		Thread[] threads = new Thread[rootGroup.activeCount()];
		for (;;) {
			int l = rootGroup.enumerate(threads);
			if (l == threads.length)
				break;
			threads = new Thread[l];
		}
		threadList = new ArrayList<ThreadInfo>(threads.length);
		for (Thread thread : threads) {
			threadList.add(new ThreadInfo(thread));
		}
		return threadList;
	}

	@Override
	protected void reset() throws SearchLibException {
		threadList = null;
	}

}
