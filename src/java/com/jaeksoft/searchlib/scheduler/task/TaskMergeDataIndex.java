/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012-2013 Emmanuel Keller / Jaeksoft
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

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.ClientCatalogItem;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.scheduler.TaskAbstract;
import com.jaeksoft.searchlib.scheduler.TaskLog;
import com.jaeksoft.searchlib.scheduler.TaskProperties;
import com.jaeksoft.searchlib.scheduler.TaskPropertyDef;
import com.jaeksoft.searchlib.scheduler.TaskPropertyType;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.user.User;
import com.jaeksoft.searchlib.util.StringUtils;

public class TaskMergeDataIndex extends TaskAbstract {

	final protected TaskPropertyDef propSourceIndex = new TaskPropertyDef(
			TaskPropertyType.comboBox, "Index source", "Index source",
			"Select the external index which will be merged", 100);

	final protected TaskPropertyDef propLogin = new TaskPropertyDef(
			TaskPropertyType.textBox, "Login", "Login",
			"The login to the external index", 20);

	final protected TaskPropertyDef propApiKey = new TaskPropertyDef(
			TaskPropertyType.password, "API Key", "API Key",
			"The API Key to connect to the external index", 50);

	final private TaskPropertyDef[] taskPropertyDefs = { propSourceIndex,
			propLogin, propApiKey };

	@Override
	public String getName() {
		return "Merge index";
	}

	@Override
	public TaskPropertyDef[] getPropertyList() {
		return taskPropertyDefs;
	}

	protected void populateSourceIndexValues(Config config, List<String> values)
			throws SearchLibException {
		for (ClientCatalogItem item : ClientCatalog.getClientCatalog(null)) {
			String v = item.getIndexName();
			if (!v.equals(config.getIndexName()))
				values.add(v);
		}
	}

	@Override
	public String[] getPropertyValues(Config config, TaskPropertyDef propertyDef)
			throws SearchLibException {
		List<String> values = new ArrayList<String>(0);
		if (propertyDef == propSourceIndex)
			populateSourceIndexValues(config, values);

		return StringUtils.toStringArray(values, false);
	}

	@Override
	public String getDefaultValue(Config config, TaskPropertyDef propertyDef) {
		return null;
	}

	public void setValues(TaskProperties properties, String index,
			String login, String apiKey) {
		if (index != null)
			properties.setValue(propSourceIndex, index);
		if (login != null)
			properties.setValue(propLogin, login);
		if (apiKey != null)
			properties.setValue(propApiKey, apiKey);
	}

	@Override
	public void execute(Client client, TaskProperties properties,
			TaskLog taskLog) throws SearchLibException {

		try {

			String index = properties.getValue(propSourceIndex);
			String login = properties.getValue(propLogin);
			String apiKey = properties.getValue(propApiKey);

			if (!ClientCatalog.getUserList().isEmpty()) {
				User user = ClientCatalog.authenticateKey(login, apiKey);
				if (user == null)
					throw new SearchLibException("Authentication failed");
				if (!user.hasAnyRole(index, Role.GROUP_INDEX))
					throw new SearchLibException("Not enough right");
			}
			Client sourceClient = ClientCatalog.getClient(index);
			if (sourceClient == null)
				throw new SearchLibException("Client not found: " + index);

			client.mergeData(sourceClient);

		} catch (NamingException e) {
			throw new SearchLibException(e);
		}
	}
}
