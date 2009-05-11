/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.crawler.web.database;

public enum RobotsTxtStatus {

	UNKNOWN(0, "Unknown"), DISALLOW(1, "Disallow"), ALLOW(2, "Allow"), NO_ROBOTSTXT(
			3, "No file"), ERROR(4, "Error"), ALL(99, "All");

	public int value;

	public String name;

	private RobotsTxtStatus(int value, String name) {
		this.value = value;
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	public static RobotsTxtStatus find(int v) {
		for (RobotsTxtStatus status : values())
			if (status.value == v)
				return status;
		return null;
	}

}
