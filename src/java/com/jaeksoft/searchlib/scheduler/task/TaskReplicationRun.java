/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2012 Emmanuel Keller / Jaeksoft
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

import java.util.List;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.replication.ReplicationItem;
import com.jaeksoft.searchlib.replication.ReplicationList;
import com.jaeksoft.searchlib.replication.ReplicationMaster;
import com.jaeksoft.searchlib.scheduler.TaskAbstract;
import com.jaeksoft.searchlib.scheduler.TaskLog;
import com.jaeksoft.searchlib.scheduler.TaskProperties;
import com.jaeksoft.searchlib.scheduler.TaskPropertyDef;
import com.jaeksoft.searchlib.scheduler.TaskPropertyType;

public class TaskReplicationRun extends TaskAbstract {

	final private TaskPropertyDef propReplicationName = new TaskPropertyDef(
			TaskPropertyType.comboBox, "replication name", 50);

	final private TaskPropertyDef[] taskPropertyDefs = { propReplicationName };

	@Override
	public String getName() {
		return "Replication - run";
	}

	@Override
	public TaskPropertyDef[] getPropertyList() {
		return taskPropertyDefs;
	}

	@Override
	public String[] getPropertyValues(Config config, TaskPropertyDef propertyDef)
			throws SearchLibException {
		ReplicationList replicationList = config.getReplicationList();
		List<String> nameList = replicationList.getNameList();
		if (nameList == null)
			return null;
		String[] values = new String[nameList.size()];
		for (int i = 0; i < values.length; i++)
			values[i] = nameList.get(i);
		return values;
	}

	@Override
	public String getDefaultValue(Config config, TaskPropertyDef propertyDef) {
		return null;
	}

	@Override
	public void execute(Client client, TaskProperties properties,
			TaskLog taskLog) throws SearchLibException {
		ReplicationMaster replicationMaster = client.getReplicationMaster();
		ReplicationList replicationList = client.getReplicationList();
		String replicationName = properties.getValue(propReplicationName);
		if (replicationName == null) {
			taskLog.setInfo("No replication name");
			return;
		}
		ReplicationItem replicationItem = replicationList.get(replicationName);
		if (replicationItem == null) {
			taskLog.setInfo("Replication name not found: " + replicationName);
			return;
		}
		try {
			replicationMaster.execute(client, replicationItem, true, taskLog);
		} catch (InterruptedException e) {
			throw new SearchLibException(e);
		}
	}
}
