/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012-2013 Emmanuel Keller / Jaeksoft
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

import com.jaeksoft.searchlib.Logging;

public class FunctionTimer {

	private static class ExecutionInfo {

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

	public static class ExecutionToken {

		private final String name;
		private final String[] texts;
		private final long startTime;
		private long duration;

		private ExecutionToken(final String name, final String... texts) {
			this.name = name;
			this.texts = texts;
			this.startTime = System.currentTimeMillis();
		}

		public final void end(String... texts) {
			duration = System.currentTimeMillis() - startTime;
			if (!Logging.isDebug)
				return;
			System.out.println(StringUtils.fastConcatCharSequence(name, " ",
					StringUtils.fastConcat(this.texts), " (",
					Long.toString(duration), "ms) ",
					StringUtils.fastConcat(texts)));
		}
	}

	final public static ExecutionToken newExecutionToken(final String name,
			final String... text) {
		return new ExecutionToken(name, text);
	}

}
