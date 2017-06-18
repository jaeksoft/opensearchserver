/*
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2008-2017 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.crawler.web.database;

import com.jaeksoft.searchlib.crawler.TargetStatus;

public enum RobotsTxtStatus {

	UNKNOWN(0, "Unknown", TargetStatus.TARGET_DO_NOTHING),

	DISALLOW(1, "Disallow", TargetStatus.TARGET_DELETE),

	ALLOW(2, "Allow", TargetStatus.TARGET_UPDATE),

	NO_ROBOTSTXT(3, "No file", TargetStatus.TARGET_UPDATE),

	ERROR(4, "Error", TargetStatus.TARGET_DO_NOTHING),

	DISABLED(5, "Disabled", TargetStatus.TARGET_UPDATE),

	ALL(99, "All", null);

	public int value;

	public String name;

	public TargetStatus targetStatus;

	RobotsTxtStatus(int value, String name, TargetStatus targetStatus) {
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

	public static RobotsTxtStatus find(int v) {
		for (RobotsTxtStatus status : values())
			if (status.value == v)
				return status;
		return null;
	}

	public static RobotsTxtStatus findByName(String name) {
		for (RobotsTxtStatus status : values())
			if (status.name.equalsIgnoreCase(name))
				return status;
		return null;
	}

	private static String[] names = null;

	public static synchronized String[] getNames() {
		if (names != null)
			return names;
		int i = 0;
		names = new String[values().length];
		for (RobotsTxtStatus status : values())
			names[i++] = status.name;
		return names;
	}

}
