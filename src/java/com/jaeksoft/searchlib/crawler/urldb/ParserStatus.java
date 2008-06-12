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

public enum ParserStatus {

	NOT_PARSED(0, "Not parsed"), PARSED(1, "Parsed"), NOPARSER(1, "No parser"), PARSER_ERROR(
			2, "Parser Error"), ALL(99, "All");

	public int value;

	public String name;

	private ParserStatus(int value, String name) {
		this.value = value;
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	public static ParserStatus find(int v) {
		for (ParserStatus status : values())
			if (status.value == v)
				return status;
		return null;
	}

}
