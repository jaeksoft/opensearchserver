/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.scheduler;

import java.io.IOException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.util.Variables;

public abstract class TaskAbstract {

	/**
	 * The name of the task
	 * 
	 * @return the name
	 */
	public abstract String getName();

	/**
	 * List of the properties
	 * 
	 * @return an array containing the definition of the properties
	 */
	public abstract TaskPropertyDef[] getPropertyList();

	public boolean isProperty() {
		TaskPropertyDef[] props = getPropertyList();
		if (props == null)
			return false;
		return props.length > 0;
	}

	/**
	 * The possible values for a property.
	 * 
         * @param config
	 * @param propertyDef
	 * @param properties
	 * @return an array with the possible value name
	 */
	public abstract String[] getPropertyValues(Config config,
			TaskPropertyDef propertyDef, TaskProperties properties)
			throws SearchLibException;

	/**
	 * Implements the task execution
	 * 
	 * @param client
	 * @param properties
	 * @param variables
	 * @param taskLog
	 * @throws SearchLibException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public abstract void execute(Client client, TaskProperties properties,
			Variables variables, TaskLog taskLog) throws SearchLibException,
			IOException, InterruptedException;

	/**
	 * Returns the default value of the property
	 * 
	 * @param config
	 * @param propertyDef
	 * @return
	 */
	public abstract String getDefaultValue(Config config,
			TaskPropertyDef propertyDef);

	/**
	 * Find a propertyDef from its configName
	 * 
	 * @param configName
	 * @return
	 */
	public TaskPropertyDef findPropertyByConfigName(String configName) {
		TaskPropertyDef[] propDefs = getPropertyList();
		if (propDefs == null)
			return null;
		for (TaskPropertyDef propDef : propDefs)
			if (propDef.configName.equalsIgnoreCase(configName))
				return propDef;
		return null;
	}

}
