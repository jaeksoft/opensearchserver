/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

	INDEXED(1, "Indexed", TargetStatus.TARGET_UPDATE),

	META_NOINDEX(2, "Meta No Index", TargetStatus.TARGET_DELETE),

	INDEX_ERROR(3, "Index error", TargetStatus.TARGET_DO_NOTHING),

	PLUGIN_REJECTED(4, "Rejected", TargetStatus.TARGET_DELETE),

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

	public String getValue() {
		return Integer.toString(value);
	}

	public static IndexStatus find(int v) {
		for (IndexStatus status : values())
			if (status.value == v)
				return status;
		return null;
	}

}
