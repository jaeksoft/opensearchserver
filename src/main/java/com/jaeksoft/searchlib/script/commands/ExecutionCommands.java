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

package com.jaeksoft.searchlib.script.commands;

import java.util.ArrayList;
import java.util.List;

import com.jaeksoft.searchlib.script.CommandAbstract;
import com.jaeksoft.searchlib.script.CommandEnum;
import com.jaeksoft.searchlib.script.ScriptCommandContext;
import com.jaeksoft.searchlib.script.ScriptCommandContext.OnError;
import com.jaeksoft.searchlib.script.ScriptException;
import com.jaeksoft.searchlib.util.ThreadUtils;

public class ExecutionCommands {

	public static class SleepCommand extends CommandAbstract {

		public SleepCommand() {
			super(CommandEnum.SLEEP);
		}

		@Override
		public void run(ScriptCommandContext context, String id,
				String... parameters) throws ScriptException {
			checkParameters(1, parameters);
			ThreadUtils.waitUntil(Integer.parseInt(parameters[0].toString()),
					context.taskLog);
		}
	}

	public static class OnErrorCommand extends CommandAbstract {

		public OnErrorCommand() {
			super(CommandEnum.ON_ERROR);
		}

		@Override
		public void run(ScriptCommandContext context, String id,
				String... parameters) throws ScriptException {
			checkParameters(1, parameters);
			OnError onError = OnError.valueOf(getParameterString(0));
			switch (onError) {
			case FAILURE:
			case RESUME:
				context.setOnError(onError, null);
				break;
			case NEXT_COMMAND:
				checkParameters(2, parameters);
				List<CommandEnum> commands = new ArrayList<CommandEnum>(0);
				for (int i = 1; i < parameters.length; i++) {
					String p = parameters[i];
					if (p == null)
						continue;
					p = p.trim();
					if (p.length() == 0)
						continue;
					commands.add(CommandEnum.find(p));
				}
				context.setOnError(onError, commands);
				break;
			}
		}
	}
}
