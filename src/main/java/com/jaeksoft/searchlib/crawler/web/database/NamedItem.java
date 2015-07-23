/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2015 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.web.database;

import java.util.Date;

import com.jaeksoft.searchlib.crawler.common.database.FetchStatus;
import com.jaeksoft.searchlib.crawler.web.database.HostUrlList.ListType;

public class NamedItem {

	final protected String name;
	final protected long count;
	final public Selection selection;

	public NamedItem(String name) {
		this(name, 0, null);
	}

	public NamedItem(String name, long count, Selection selection) {
		this.name = name;
		this.count = count;
		this.selection = selection;
	}

	public String getName() {
		return name;
	}

	public long getCount() {
		return count;
	}

	public String getNameAndCount() {
		StringBuilder sb = new StringBuilder();
		sb.append(name);
		sb.append(" (");
		sb.append(count);
		sb.append(")");
		return sb.toString();
	}

	public static class Selection {

		public final ListType listType;

		public final Date beforeDate;

		public final Date afterDate;

		public final FetchStatus fetchStatus;

		public Selection(ListType listType, FetchStatus fetchStatus,
				Date beforeDate, Date afterDate) {
			this.listType = listType;
			this.fetchStatus = fetchStatus;
			this.beforeDate = beforeDate;
			this.afterDate = afterDate;
		}
	}
}
