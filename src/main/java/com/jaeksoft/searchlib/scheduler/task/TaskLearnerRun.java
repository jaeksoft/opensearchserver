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

package com.jaeksoft.searchlib.scheduler.task;

import java.util.ArrayList;
import java.util.List;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.learning.Learner;
import com.jaeksoft.searchlib.learning.LearnerManager;
import com.jaeksoft.searchlib.scheduler.TaskAbstract;
import com.jaeksoft.searchlib.scheduler.TaskLog;
import com.jaeksoft.searchlib.scheduler.TaskProperties;
import com.jaeksoft.searchlib.scheduler.TaskPropertyDef;
import com.jaeksoft.searchlib.scheduler.TaskPropertyType;
import com.jaeksoft.searchlib.util.Variables;

public class TaskLearnerRun extends TaskAbstract {

	final private TaskPropertyDef propLearnerName = new TaskPropertyDef(
			TaskPropertyType.comboBox, "learner name", "learner name",
			"The name of the learner item", 50);

	final private TaskPropertyDef[] taskPropertyDefs = { propLearnerName };

	private String learnerName;

	@Override
	public String getName() {
		return "Learner - run";
	}

	@Override
	public TaskPropertyDef[] getPropertyList() {
		return taskPropertyDefs;
	}

	public void setManualLearn(String learnerName) {
		this.learnerName = learnerName;
	}

	@Override
	public String[] getPropertyValues(Config config,
			TaskPropertyDef propertyDef, TaskProperties taskProperties)
			throws SearchLibException {
		if (propertyDef == propLearnerName) {
			Learner[] learners = config.getLearnerManager().getArray();
			List<String> nameList = new ArrayList<String>(0);
			for (Learner learner : learners)
				nameList.add(learner.getName());
			if (nameList.size() == 0)
				return null;
			return nameList.toArray(new String[nameList.size()]);
		}
		return null;
	}

	@Override
	public String getDefaultValue(Config config, TaskPropertyDef propertyDef) {
		return null;
	}

	@Override
	public void execute(Client client, TaskProperties properties,
			Variables variables, TaskLog taskLog) throws SearchLibException {
		LearnerManager learnerManager = client.getLearnerManager();
		if (learnerName == null)
			learnerName = properties.getValue(propLearnerName);
		if (learnerName == null) {
			taskLog.setInfo("No learner name");
			return;
		}
		learnerManager.learn(learnerName, taskLog);
	}

}
