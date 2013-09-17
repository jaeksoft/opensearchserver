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
package com.jaeksoft.searchlib.collapse;

import com.jaeksoft.searchlib.index.FieldCacheIndex;

public class CollapseFunction {

	static abstract class FunctionExecutor {

		abstract String execute(FieldCacheIndex stringIndex, int doc,
				int[] collapsedDocs);
	}

	static class FunctionMinimum extends FunctionExecutor {

		@Override
		final String execute(FieldCacheIndex stringIndex, int doc,
				int[] collapsedDocs) {
			String value = null;
			int min = stringIndex.order[doc];
			for (int id : collapsedDocs) {
				int pos = stringIndex.order[id];
				if (pos < min) {
					min = pos;
					value = stringIndex.lookup[pos];
				}
			}
			return value;
		}
	}

	static class FunctionMaximum extends FunctionExecutor {

		@Override
		final String execute(FieldCacheIndex stringIndex, int doc,
				int[] collapsedDocs) {
			String value = null;
			int max = stringIndex.order[doc];
			for (int id : collapsedDocs) {
				int pos = stringIndex.order[id];
				if (pos > max) {
					max = pos;
					value = stringIndex.lookup[pos];
				}
			}
			return value;
		}
	}

	static class FunctionConcat extends FunctionExecutor {

		@Override
		String execute(FieldCacheIndex stringIndex, int doc, int[] collapsedDocs) {
			StringBuffer sb = new StringBuffer();
			sb.append(stringIndex.lookup[stringIndex.order[doc]]);
			for (int id : collapsedDocs) {
				sb.append('|');
				sb.append(stringIndex.lookup[stringIndex.order[id]]);
			}
			return sb.toString();
		}
	}

	static class FunctionCount extends FunctionExecutor {

		@Override
		String execute(FieldCacheIndex stringIndex, int doc, int[] collapsedDocs) {
			return Integer.toString(collapsedDocs.length + 1);
		}
	}

}
