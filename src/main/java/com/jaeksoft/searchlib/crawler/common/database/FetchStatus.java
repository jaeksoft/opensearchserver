/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.common.database;

import com.jaeksoft.searchlib.crawler.TargetStatus;

public enum FetchStatus {

	FETCH_FIRST(12, "Fetch first", TargetStatus.TARGET_DO_NOTHING),

	UN_FETCHED(0, "Unfetched", TargetStatus.TARGET_DO_NOTHING),

	FETCHED(1, "Fetched", TargetStatus.TARGET_UPDATE),

	GONE(2, "Gone", TargetStatus.TARGET_DELETE),

	REDIR_TEMP(3, "Temporary redirect", TargetStatus.TARGET_DELETE),

	REDIR_PERM(4, "Permanent redirect", TargetStatus.TARGET_DELETE),

	ERROR(5, "Error", TargetStatus.TARGET_DO_NOTHING),

	HTTP_ERROR(6, "HTTP Error", TargetStatus.TARGET_DELETE),

	NOT_ALLOWED(7, "Not allowed", TargetStatus.TARGET_DELETE),

	SIZE_EXCEED(8, "Size exceed", TargetStatus.TARGET_DELETE),

	URL_ERROR(9, "Url error", TargetStatus.TARGET_DELETE),

	NOT_IN_INCLUSION_LIST(10, "Not in inclusion list",
			TargetStatus.TARGET_DELETE),

	BLOCKED_BY_EXCLUSION_LIST(11, "Blocked by exclusion list",
			TargetStatus.TARGET_DELETE),

	ALL(99, "All", null);

	public int value;

	public String name;

	public TargetStatus targetStatus;

	private FetchStatus(int value, String name, TargetStatus targetStatus) {
		this.value = value;
		this.name = name;
		this.targetStatus = targetStatus;
	}

	@Override
	public String toString() {
		return name;
	}

	public String getValue() {
		return Integer.toString(value);
	}

	public static FetchStatus find(int v) {
		for (FetchStatus status : values())
			if (status.value == v)
				return status;
		return null;
	}

	public static FetchStatus findByName(String name) {
		for (FetchStatus status : values())
			if (status.name.equalsIgnoreCase(name))
				return status;
		return null;
	}

	private static String[] names = null;

	public final static String[] getNames() {
		if (names != null)
			return names;
		int i = 0;
		names = new String[values().length];
		for (FetchStatus status : values())
			names[i++] = status.name;
		return names;
	}

	public String getName() {
		return name;
	}

}
