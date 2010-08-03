/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
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

package com.jaeksoft.searchlib.analysis;

public enum ClassPropertyEnum {

	CLASS(false, "Class", "The name of the class"),

	SCOPE(false, "Scope", "The scope of the class"),

	FILE(true, "List", "The name of the list"),

	MIN_GRAM(true, "Min gram size", "The smallest n-gram to generate"),

	MAX_GRAM(true, "Max gram size", "The largest n-gram to generate"),

	SIDE(true, "Edge side",
			"Specifies which side of the input the n-gram should be generated from"),

	TOKEN_SEPARATOR(true, "Token separator",
			"The string to use when joining adjacent tokens to form a shingle"),

	MAX_SHINGLE_SIZE(true, "Max shingle size",
			"Set the max shingle size (default: 2)"),

	MIN_SHINGLE_SIZE(true, "Min shingle size",
			"Set the min shingle size (default: 1)");

	public final static String[] BOOLEAN_LIST = { Boolean.TRUE.toString(),
			Boolean.FALSE.toString() };

	private boolean isUser;

	private String label;

	private String info;

	private ClassPropertyEnum(boolean isUser, String label, String info) {
		this.isUser = isUser;
		this.label = label;
		this.info = info;
	}

	/**
	 * Returns a string used for XML attribute storage
	 * 
	 * @return
	 */
	public String getAttribute() {
		return name().toLowerCase();
	}

	/**
	 * Return true if the properties is a user property
	 * 
	 * @return
	 */
	public boolean isUser() {
		return isUser;
	}

	/**
	 * 
	 * @return the literal labe of the property
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @return the information
	 */
	public String getInfo() {
		return info;
	}

}
