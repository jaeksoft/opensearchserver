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

package com.jaeksoft.searchlib.scheduler.task;

import java.sql.SQLException;

import org.apache.commons.io.IOUtils;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.database.DatabaseCrawlSql;
import com.jaeksoft.searchlib.crawler.database.DatabaseCrawlSql.SqlUpdateMode;
import com.jaeksoft.searchlib.crawler.database.DatabaseDriverNames;
import com.jaeksoft.searchlib.crawler.database.IsolationLevelEnum;
import com.jaeksoft.searchlib.scheduler.TaskAbstract;
import com.jaeksoft.searchlib.scheduler.TaskLog;
import com.jaeksoft.searchlib.scheduler.TaskProperties;
import com.jaeksoft.searchlib.scheduler.TaskPropertyDef;
import com.jaeksoft.searchlib.scheduler.TaskPropertyType;
import com.jaeksoft.searchlib.script.DatabaseScript;
import com.jaeksoft.searchlib.script.ScriptException;

public class TaskDatabaseScript extends TaskAbstract {

	final private TaskPropertyDef propDatabaseDriver = new TaskPropertyDef(
			TaskPropertyType.comboBox, "Database driver", "Database driver",
			null, 30);

	final private TaskPropertyDef propDatabaseURL = new TaskPropertyDef(
			TaskPropertyType.comboBox, "JDBC URL", "JDBC URL", null, 50);

	final private TaskPropertyDef propDatabaseUsername = new TaskPropertyDef(
			TaskPropertyType.textBox, "Database user name",
			"Database username", null, 20);

	final private TaskPropertyDef propDatabasePassword = new TaskPropertyDef(
			TaskPropertyType.password, "Database password",
			"Database password", null, 20);

	final private TaskPropertyDef propIsolationLevel = new TaskPropertyDef(
			TaskPropertyType.comboBox, "Isolation Level", "Isolation Level",
			"Select the right isolation level", 20);

	final private TaskPropertyDef propSelectSQLVariables = new TaskPropertyDef(
			TaskPropertyType.multilineTextBox, "SQL Select for variables",
			"SQL Select", "The SQL statement to retrieve the variables", 70, 5);

	final private TaskPropertyDef propVariablesNameColumn = new TaskPropertyDef(
			TaskPropertyType.textBox, "Variable name column",
			"Variable name column",
			"The label of the column which contains the name of the variable",
			20);

	final private TaskPropertyDef propVariablesValueColumn = new TaskPropertyDef(
			TaskPropertyType.textBox,
			"Variable value column",
			"Variable value column",
			"The label of the columns which contains the value of the variable",
			20);

	final private TaskPropertyDef propSelectSQL = new TaskPropertyDef(
			TaskPropertyType.multilineTextBox, "SQL Select for script",
			"SQL Select", "The SQL statement to retrieve the list of commands",
			70, 5);

	final private TaskPropertyDef propUpdateSQL = new TaskPropertyDef(
			TaskPropertyType.multilineTextBox, "SQL Update", "SQL Update",
			"The SQ statement called when the command has been executed", 70, 5);

	final private TaskPropertyDef propUpdateMethod = new TaskPropertyDef(
			TaskPropertyType.comboBox, "Update method", "Update method",
			"Select the right update method", 20);

	final private TaskPropertyDef[] taskPropertyDefs = { propDatabaseDriver,
			propDatabaseURL, propDatabaseUsername, propDatabasePassword,
			propIsolationLevel, propSelectSQL, propUpdateSQL, propUpdateMethod,
			propSelectSQLVariables, propVariablesNameColumn,
			propVariablesValueColumn };

	@Override
	public String getName() {
		return "Database script";
	}

	@Override
	public TaskPropertyDef[] getPropertyList() {
		return taskPropertyDefs;
	}

	@Override
	public String[] getPropertyValues(Config config, TaskPropertyDef propertyDef)
			throws SearchLibException {
		if (propertyDef == propDatabaseDriver) {
			return DatabaseDriverNames.getAvailableList();
		} else if (propertyDef == propIsolationLevel) {
			return IsolationLevelEnum.getNameList();
		} else if (propertyDef == propUpdateMethod) {
			return DatabaseCrawlSql.SqlUpdateMode.getNameList();
		}
		return null;
	}

	@Override
	public String getDefaultValue(Config config, TaskPropertyDef propertyDef) {
		if (propertyDef == propUpdateMethod)
			return DatabaseCrawlSql.SqlUpdateMode.NO_CALL.name();
		else if (propertyDef == propIsolationLevel)
			return IsolationLevelEnum.TRANSACTION_NONE.name();
		return null;
	}

	@Override
	public void execute(Client client, TaskProperties properties,
			TaskLog taskLog) throws SearchLibException {
		String dbDriver = properties.getValue(propDatabaseDriver);
		String dbURL = properties.getValue(propDatabaseURL);
		String dbUsername = properties.getValue(propDatabaseUsername);
		String dbPassword = properties.getValue(propDatabasePassword);
		String sqlSelectVar = properties.getValue(propSelectSQLVariables);
		String sqlVariableName = properties.getValue(propVariablesNameColumn);
		String sqlVariableValue = properties.getValue(propVariablesValueColumn);
		String sqlSelect = properties.getValue(propSelectSQL);
		String sqlUpdate = properties.getValue(propUpdateSQL);
		IsolationLevelEnum isolationLevelEnum = IsolationLevelEnum
				.find(properties.getValue(propIsolationLevel));
		SqlUpdateMode sqlUpdateMode = SqlUpdateMode.find(properties
				.getValue(propUpdateMethod));
		DatabaseScript dbScript = null;
		try {
			dbScript = new DatabaseScript(client, dbDriver, dbURL, dbUsername,
					dbPassword, isolationLevelEnum, sqlSelectVar,
					sqlVariableName, sqlVariableValue, sqlSelect, sqlUpdate,
					sqlUpdateMode, taskLog);
			dbScript.run();
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
			throw new SearchLibException(e);
		} catch (SQLException e) {
			throw new SearchLibException(e);
		} catch (ScriptException e) {
			throw new SearchLibException(e);
		} finally {
			if (dbScript != null)
				IOUtils.closeQuietly(dbScript);
		}
	}
}
