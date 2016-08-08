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

import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.jaeksoft.searchlib.script.CommandAbstract;
import com.jaeksoft.searchlib.script.CommandEnum;
import com.jaeksoft.searchlib.script.ScriptCommandContext;
import com.jaeksoft.searchlib.script.ScriptException;
import com.jaeksoft.searchlib.util.RegExpUtils;

public class VarCommands {

	public static class VarNewRegEx extends CommandAbstract {

		public VarNewRegEx() {
			super(CommandEnum.VAR_NEW_REGEX);
		}

		@Override
		public void run(ScriptCommandContext context, String id,
				String... parameters) throws ScriptException {
			checkParameters(3, parameters);
			String text = context.replaceVariables(getParameterString(0));
			Pattern pattern = Pattern.compile(getParameterString(1));
			List<String> groups = RegExpUtils.getGroups(pattern, text);
			if (CollectionUtils.isEmpty(groups))
				return;
			context.addContextVariables(getParameterString(2),
					StringUtils.join(groups, ""));
		}
	}

	public static class VarClear extends CommandAbstract {

		public VarClear() {
			super(CommandEnum.VAR_CLEAR);
		}

		@Override
		public void run(ScriptCommandContext context, String id,
				String... parameters) throws ScriptException {
			checkParameters(1, parameters);
			context.addContextVariables(getParameterString(0), null);
		}
	}

}
