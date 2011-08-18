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

package com.jaeksoft.searchlib.scheduler;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;

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
	 * @return an array containing the name of the properties
	 */
	public abstract String[] getPropertyList();

	public boolean isProperty() {
		String[] props = getPropertyList();
		if (props == null)
			return false;
		return props.length > 0;
	}

	/**
	 * The possible values for a property.
	 * 
	 * @param client
	 * @param property
	 * @return an array with the possible value name
	 */
	public abstract String[] getPropertyValues(Config config, String property)
			throws SearchLibException;

	/**
	 * Implements the task execution
	 * 
	 * @param client
	 * @param properties
	 * @throws SearchLibException
	 */
	public abstract void execute(Client client, TaskProperties properties)
			throws SearchLibException;

	/**
	 * The width in col of the input text
	 * 
	 * @param config
	 * @param name
	 * @return
	 */
	public abstract int getPropertyCols(Config config, String name);

}
