/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
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

import java.util.ArrayList;
import java.util.List;

import org.zkoss.zk.ui.Executions;

import com.jaeksoft.searchlib.Logging;

public enum DatabaseDriverNames {

	DERBY("org.apache.derby.jdbc.EmbeddedDriver"),

	DB2("COM.ibm.db2.jdbc.app.DB2Driver"),

	FILEMAKER("com.filemaker.jdbc.Driver"),

	HSQLDB("org.hsqldb.jdbcDriver"),

	INGRES("com.ingres.jdbc.IngresDriver"),

	JTDS("net.sourceforge.jtds.jdbc.Driver"),

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

	private static String[] dbDriverList = null;

	/**
	 * @return the list of available driver classes
	 */
	public static synchronized String[] getAvailableList(ClassLoader classLoader) {
		if (dbDriverList != null)
			return dbDriverList;
		List<String> list = new ArrayList<String>();
		for (DatabaseDriverNames ddn : values()) {
			try {
				Class.forName(ddn.name, false, classLoader);
				list.add(ddn.name);
			} catch (Exception e) {
				Logging.warn(e);
			} catch (LinkageError e) {
				Logging.warn(e);
			}
		}
		dbDriverList = new String[list.size()];
		list.toArray(dbDriverList);
		return dbDriverList;
	}

	public static synchronized String[] getAvailableList() {
		return getAvailableList(Executions.getCurrent().getDesktop().getWebApp().getClass().getClassLoader());
	}
}
