/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.database;

public enum IsolationLevelEnum {

	TRANSACTION_NONE(java.sql.Connection.TRANSACTION_NONE),

	TRANSACTION_READ_COMMITTED(java.sql.Connection.TRANSACTION_READ_COMMITTED),

	TRANSACTION_READ_UNCOMMITTED(
			java.sql.Connection.TRANSACTION_READ_UNCOMMITTED),

	TRANSACTION_REPEATABLE_READ(java.sql.Connection.TRANSACTION_REPEATABLE_READ),

	TRANSACTION_SERIALIZABLE(java.sql.Connection.TRANSACTION_SERIALIZABLE);

	protected final int value;

	private IsolationLevelEnum(int value) {
		this.value = value;
	}

	public static IsolationLevelEnum find(String label) {
		if (label == null)
			return TRANSACTION_NONE;
		for (IsolationLevelEnum iso : values())
			if (iso.name().equals(label))
				return iso;
		return TRANSACTION_NONE;
	}

}
