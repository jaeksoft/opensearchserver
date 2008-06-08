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

package com.jaeksoft.searchlib.crawler.urldb;

public enum UrlStatus {
	UN_FETCHED(0, "Unfetched"), FETCHED(1, "Fetched"), GONE(2, "Gone"), REDIR_TEMP(
			3, "Temporary redirect"), REDIR_PERM(4, "Permanent redirect"), ERROR(
			5, "Error"), IGNORED(6, "Ignored"), NOPARSER(7, "No parser");

	private int value;

	private String name;

	private UrlStatus(int v, String name) {
		this.value = v;
		this.name = name;
	}

	public int getValue() {
		return this.value;
	}

	public String getValueString() {
		return Integer.toString(value);
	}

	public String getName() {
		return name;
	}

	public static UrlStatus findByValue(String value) {
		return findByValue(Integer.parseInt(value));
	}

	public static UrlStatus findByValue(int value) {
		for (UrlStatus status : values())
			if (status.value == value)
				return status;
		return null;
	}

	public static UrlStatus findByName(String name) {
		for (UrlStatus status : values())
			if (status.name.equals(name))
				return status;
		return null;
	}

}
