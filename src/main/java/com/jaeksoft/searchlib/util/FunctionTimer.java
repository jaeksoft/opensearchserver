/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012-2014 Emmanuel Keller / Jaeksoft
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

import java.util.Map;
import java.util.TreeMap;

import com.jaeksoft.searchlib.Logging;

public class FunctionTimer {

	public enum Mode {
		OFF, TIME_ONLY, FULL
	};

	private static double globalTotalTime = 0;
	private static double fullTotalTime = 0;

	public final static Mode MODE = Mode.TIME_ONLY;

	private static class ExecutionInfo {

		private final String name;
		private long totalTime;
		private long callCount;

		private ExecutionInfo(String name) {
			this.name = name;
			callCount = 0;
		}

		private final void add(final ExecutionTokenImpl executionToken) {
			synchronized (this) {
				callCount++;
				totalTime += executionToken.duration;
			}
		}

		@Override
		final public String toString() {
			StringBuilder sb = new StringBuilder();
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

	public interface ExecutionToken {
		public void end(final String... texts);
	}

	private static class ExecutionTokenDisableImpl implements ExecutionToken {

		@Override
		final public void end(final String... texts) {
		}

		private final static ExecutionToken DISABLED = new ExecutionTokenDisableImpl();
	}

	private static class ExecutionTokenImpl implements ExecutionToken {

		private final String name;
		private final String[] texts;
		private final long startTime;
		private long duration;
		private final ExecutionInfo executionInfo;

		private ExecutionTokenImpl(final ExecutionInfo executionInfo,
				final String name, final String... texts) {
			this.executionInfo = executionInfo;
			this.name = name;
			this.texts = texts;
			this.startTime = System.currentTimeMillis();
		}

		@Override
		public final void end(final String... texts) {
			duration = System.currentTimeMillis() - startTime;
			fullTotalTime += duration;
			if (executionInfo != null)
				executionInfo.add(this);
			if (!Logging.isDebug)
				return;
			System.out.println(StringUtils.fastConcatCharSequence(name, " ",
					StringUtils.fastConcat(this.texts), " (",
					Long.toString(duration), "ms) ",
					StringUtils.fastConcat(texts)));
		}
	}

	private final static Map<String, ExecutionInfo> ExecutionInfos;
	static {
		ExecutionInfos = MODE == Mode.FULL ? new TreeMap<String, ExecutionInfo>()
				: null;
	}

	final public static ExecutionToken newExecutionToken(final String name,
			final String... text) {
		ExecutionInfo executionInfo = null;
		switch (MODE) {
		case OFF:
			return ExecutionTokenDisableImpl.DISABLED;
		case FULL:
			synchronized (ExecutionInfos) {
				executionInfo = ExecutionInfos.get(name);
				if (executionInfo == null) {
					executionInfo = new ExecutionInfo(name);
					ExecutionInfos.put(name, executionInfo);
				}
			}
			break;
		default:
			break;
		}
		return new ExecutionTokenImpl(executionInfo, name, text);
	}

	final public static void dumpExecutionInfo(final boolean reset) {
		switch (MODE) {
		case FULL:
			synchronized (ExecutionInfos) {
				System.out.println("EXECUTIONS INFO DUMP");
				float totalTime = 0;
				for (ExecutionInfo executionInfo : ExecutionInfos.values()) {
					System.out.println(executionInfo);
					totalTime += executionInfo.totalTime;
				}
				System.out.println(StringUtils.fastConcat("Total time: ",
						Float.toString(totalTime / 1000), " sec"));
				if (reset)
					ExecutionInfos.clear();
			}
		case OFF:
			break;
		case TIME_ONLY:
			globalTotalTime += (double) fullTotalTime / 1000;
			System.out.println(StringUtils.fastConcat("Total time: ",
					Double.toString(fullTotalTime / 1000))
					+ " / " + Double.toString(globalTotalTime));
			break;
		}
		if (reset)
			fullTotalTime = 0;
	}
}
