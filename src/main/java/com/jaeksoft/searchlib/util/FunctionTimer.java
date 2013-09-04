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

package com.jaeksoft.searchlib.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;

public class FunctionTimer {

	public static FunctionTimer INSTANCE = new FunctionTimer();

	private class ExecutionInfo {

		private final String name;
		private long totalTime;
		private long callCount;

		public ExecutionInfo(ExecutionToken executionToken) {
			this.name = executionToken.name;
			callCount = 0;
			add(executionToken);
		}

		public final void add(ExecutionToken executionToken) {
			callCount++;
			totalTime += executionToken.duration;
		}

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append(name);
			sb.append(" - count: ");
			sb.append(callCount);
			sb.append(" - total time: ");
			sb.append(totalTime);
			sb.append(" - avg. time: ");
			sb.append(totalTime / callCount);
			return sb.toString();
		}

	}

	public class ExecutionToken {

		private final String name;
		private final long startTime;
		private long duration;

		private ExecutionToken(String name) {
			this.name = name;
			this.startTime = System.currentTimeMillis();
		}

		public final void end() {
			duration = System.currentTimeMillis() - startTime;
			endExecutionToken(this);
		}
	}

	private final SimpleLock lock = new SimpleLock();
	private final Map<String, ExecutionInfo> map;

	private FunctionTimer() {
		map = new TreeMap<String, ExecutionInfo>();
	}

	public ExecutionToken newExecutionToken(String name) {
		return new ExecutionToken(name);
	}

	private void endExecutionToken(ExecutionToken executionToken) {
		lock.rl.lock();
		try {
			ExecutionInfo executionInfo = map.get(executionToken.name);
			if (executionInfo == null) {
				map.put(executionToken.name, new ExecutionInfo(executionToken));
			} else
				executionInfo.add(executionToken);
		} finally {
			lock.rl.unlock();
		}
	}

	public void reset() {
		lock.rl.lock();
		try {
			map.clear();
		} finally {
			lock.rl.unlock();
		}
	}

	@Override
	public String toString() {
		StringWriter sw = null;
		PrintWriter pw = null;
		lock.rl.lock();
		try {
			sw = new StringWriter();
			pw = new PrintWriter(sw);
			for (ExecutionInfo info : map.values())
				pw.println(info);
			return sw.toString();
		} finally {
			lock.rl.unlock();
			if (pw != null)
				IOUtils.closeQuietly(pw);
			if (sw != null)
				IOUtils.closeQuietly(sw);
		}
	}
}
