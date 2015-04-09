/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2015 Emmanuel Keller / Jaeksoft
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

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.scheduler.TaskAbstract;
import com.jaeksoft.searchlib.scheduler.TaskLog;
import com.jaeksoft.searchlib.scheduler.TaskProperties;
import com.jaeksoft.searchlib.scheduler.TaskPropertyDef;
import com.jaeksoft.searchlib.scheduler.TaskPropertyType;
import com.jaeksoft.searchlib.script.ScriptException;
import com.jaeksoft.searchlib.script.ScriptLinesRunner;
import com.jaeksoft.searchlib.util.IOUtils;
import com.jaeksoft.searchlib.util.Variables;

public class TaskRunScript extends TaskAbstract {

	final private TaskPropertyDef propScriptName = new TaskPropertyDef(
			TaskPropertyType.comboBox, "Script name", "Script name", null, 50);

	final private TaskPropertyDef propCrawlVariables = new TaskPropertyDef(
			TaskPropertyType.multilineTextBox, "crawl variables",
			"crawl variables", "The name of the variables item", 50, 5);

	final private TaskPropertyDef[] taskPropertyDefs = { propScriptName,
			propCrawlVariables };

	@Override
	public String getName() {
		return "Run script";
	}

	@Override
	public TaskPropertyDef[] getPropertyList() {
		return taskPropertyDefs;
	}

	@Override
	public String[] getPropertyValues(Config config,
			TaskPropertyDef propertyDef, TaskProperties taskProperties)
			throws SearchLibException {
		if (propertyDef == propScriptName)
			return config.getScriptManager().getList();
		return null;
	}

	@Override
	public String getDefaultValue(Config config, TaskPropertyDef propertyDef) {
		return null;
	}

	@Override
	public void execute(Client client, TaskProperties properties,
			Variables variables, TaskLog taskLog) throws SearchLibException,
			IOException {
		String scriptName = properties.getValue(propScriptName);
		String vars = properties.getValue(propCrawlVariables);
		if (!StringUtils.isEmpty(vars)) {
			variables = new Variables(variables);
			variables.putProperties(vars);
		}
		if (scriptName == null)
			throw new SearchLibException("The script name is missing");
		ScriptLinesRunner scriptLinesRunner = null;
		try {
			scriptLinesRunner = new ScriptLinesRunner(client, variables,
					taskLog, client.getScriptManager().getContent(scriptName));
			scriptLinesRunner.run();
		} catch (ScriptException e) {
			throw new SearchLibException(e);
		} finally {
			IOUtils.close(scriptLinesRunner);
		}
	}
}
