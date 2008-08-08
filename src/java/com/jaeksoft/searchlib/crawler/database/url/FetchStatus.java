/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.crawler.database.url;

public enum FetchStatus {

	UN_FETCHED(0, "Unfetched"), FETCHED(1, "Fetched"), GONE(2, "Gone"), REDIR_TEMP(
			3, "Temporary redirect"), REDIR_PERM(4, "Permanent redirect"), ERROR(
			5, "Error"), HTTP_ERROR(6, "HTTP Error"), ALL(99, "All");

	public int value;

	public String name;

	private FetchStatus(int value, String name) {
		this.value = value;
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	public static FetchStatus find(int v) {
		for (FetchStatus status : values())
			if (status.value == v)
				return status;
		return null;
	}

}
