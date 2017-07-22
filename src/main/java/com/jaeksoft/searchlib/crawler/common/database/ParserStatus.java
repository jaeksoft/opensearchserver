/*
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2017 Emmanuel Keller / Jaeksoft
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
 */

package com.jaeksoft.searchlib.crawler.common.database;

import com.jaeksoft.searchlib.crawler.TargetStatus;

public enum ParserStatus {

	NOT_PARSED(0, "Not parsed", TargetStatus.TARGET_DO_NOTHING),

	PARSED(1, "Parsed", TargetStatus.TARGET_UPDATE),

	PARSER_ERROR(2, "Parser Error", TargetStatus.TARGET_DO_NOTHING),

	NOPARSER(3, "No parser", TargetStatus.TARGET_DELETE),

	PARSED_NON_CANONICAL(4, "Parsed non-canonical", TargetStatus.TARGET_DELETE),

	ALL(99, "All", null);

	final public int value;

	final public String name;

	final public TargetStatus targetStatus;

	private ParserStatus(int value, String name, TargetStatus targetStatus) {
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

	public static ParserStatus find(int v) {
		for (ParserStatus status : values())
			if (status.value == v)
				return status;
		return null;
	}

	public static ParserStatus findByName(String name) {
		for (ParserStatus status : values())
			if (status.name.equalsIgnoreCase(name))
				return status;
		return null;
	}

	private static String[] names = null;

	public final static synchronized String[] getNames() {
		if (names != null)
			return names;
		int i = 0;
		names = new String[values().length];
		for (ParserStatus status : values())
			names[i++] = status.name;
		return names;
	}

}
