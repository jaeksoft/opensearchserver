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

package com.jaeksoft.searchlib.crawler.database;

import java.util.ArrayList;
import java.util.List;

public enum DatabaseDriverNames {

	DERBY("org.apache.derby.jdbc.EmbeddedDriver"),

	DB2("COM.ibm.db2.jdbc.app.DB2Driver"),

	FILEMAKER("com.filemaker.jdbc.Driver"),

	HSQLDB("org.hsqldb.jdbcDriver"),

	INGRES("com.ingres.jdbc.IngresDriver"),

	MYSQL("com.mysql.jdbc.Driver"),

	ORACLE("oracle.jdbc.driver.OracleDriver"),

	POSTGRESQL("org.postgresql.Driver"),

	SQLITE("org.sqlite.JDBC"),

	SQLSERVER("com.microsoft.jdbc.sqlserver.SQLServerDriver"),

	SYBASE("com.sybase.jdbc2.jdbc.SybDriver");

	private String name;

	private DatabaseDriverNames(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	public static List<DatabaseDriverNames> getAvailableList() {
		List<DatabaseDriverNames> list = new ArrayList<DatabaseDriverNames>();
		for (DatabaseDriverNames ddn : values()) {
			try {
				Class.forName(ddn.name, false, null);
				list.add(ddn);
			} catch (ClassNotFoundException e) {
			}
		}
		return list;
	}
}
