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

public enum IndexStatus {

	NOT_INDEXED(0, "Not indexed", TargetStatus.TARGET_DO_NOTHING),

	INDEXED(1, "Indexed", TargetStatus.TARGET_DO_NOTHING),

	META_NOINDEX(2, "Meta No Index", TargetStatus.TARGET_DELETE),

	INDEX_ERROR(3, "Index error", TargetStatus.TARGET_DO_NOTHING),

	PLUGIN_REJECTED(4, "Rejected", TargetStatus.TARGET_DELETE),

	NOTHING_TO_INDEX(5, "Nothing to index", TargetStatus.TARGET_DO_NOTHING),

	TO_INDEX(6, "To index", TargetStatus.TARGET_UPDATE),

	ALL(99, "All", null);

	final public int value;

	final public String name;

	final public TargetStatus targetStatus;

	private IndexStatus(int value, String name, TargetStatus targetStatus) {
		this.value = value;
		this.name = name;
		this.targetStatus = targetStatus;
	}

	@Override
	public String toString() {
		return name;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return Integer.toString(value);
	}

	public static IndexStatus find(int v) {
		for (IndexStatus status : values())
			if (status.value == v)
				return status;
		return null;
	}

	public static IndexStatus findByName(String name) {
		for (IndexStatus status : values())
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
		for (IndexStatus status : values())
			names[i++] = status.name;
		return names;
	}

}
