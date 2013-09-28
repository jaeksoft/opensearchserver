/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.util;

import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

import com.jaeksoft.pojodbc.Transaction;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.crawler.database.DatabaseCrawlSql.SqlUpdateMode;

public class DatabaseUtils {

	public final static String PRIMARY_KEY_VARIABLE_NAME = "$PK";

	public final static String ERROR_VARIABLE_NAME = "$ERR";

	final private static String toIdList(List<String> pkList, boolean quote) {
		StringBuffer sb = new StringBuffer();
		boolean b = false;
		for (String uk : pkList) {
			if (b)
				sb.append(',');
			else
				b = true;
			if (quote) {
				sb.append('\'');
				sb.append(uk.replace("'", "''"));
				sb.append('\'');
			} else
				sb.append(uk);
		}
		return sb.toString();
	}

	public final static String escapeSqlChar(String error) {
		if (error == null)
			return "null";
		StringBuffer sb = new StringBuffer();
		sb.append('\'');
		sb.append(StringEscapeUtils.escapeEcmaScript(error));
		sb.append('\'');
		return sb.toString();
	}

	final public static void update(Transaction transaction, String pk,
			String error, SqlUpdateMode sqlUpdateMode, String sqlUpdate)
			throws SQLException {
		if (sqlUpdateMode != SqlUpdateMode.ONE_CALL_PER_PRIMARY_KEY)
			return;
		String sql = sqlUpdate.replace(PRIMARY_KEY_VARIABLE_NAME, pk);
		error = escapeSqlChar(error);
		sql = sql.replace(ERROR_VARIABLE_NAME, error);
		transaction.update(sql);
		transaction.commit();
		// Logging.info("SQL UPDATE: " + sql);
	}

	final public static void update(Transaction transaction,
			List<String> pkList, String error, SqlUpdateMode sqlUpdateMode,
			String sqlUpdate) throws SQLException {
		if (sqlUpdateMode == SqlUpdateMode.NO_CALL)
			return;
		String lastSql = null;
		error = escapeSqlChar(error);
		if (sqlUpdateMode == SqlUpdateMode.ONE_CALL_PER_PRIMARY_KEY) {
			for (String uk : pkList) {
				lastSql = sqlUpdate.replace(PRIMARY_KEY_VARIABLE_NAME, uk);
				lastSql = lastSql.replace(ERROR_VARIABLE_NAME, error);
				transaction.update(lastSql);
			}
		} else if (sqlUpdateMode == SqlUpdateMode.PRIMARY_KEY_LIST) {
			lastSql = sqlUpdate.replace(PRIMARY_KEY_VARIABLE_NAME,
					toIdList(pkList, false));
			lastSql = lastSql.replace(ERROR_VARIABLE_NAME, error);
			transaction.update(lastSql);
		} else if (sqlUpdateMode == SqlUpdateMode.PRIMARY_KEY_CHAR_LIST) {
			lastSql = sqlUpdate.replace(PRIMARY_KEY_VARIABLE_NAME,
					toIdList(pkList, true));
			lastSql = lastSql.replace(ERROR_VARIABLE_NAME, error);
			transaction.update(lastSql);
		}
		Logging.info("SQL UPDATE: " + lastSql);
		transaction.commit();
	}
}
