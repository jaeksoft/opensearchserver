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

import com.jaeksoft.searchlib.script.CommandAbstract;
import com.jaeksoft.searchlib.script.CommandEnum;
import com.jaeksoft.searchlib.script.ScriptCommandContext;
import com.jaeksoft.searchlib.script.ScriptException;

public class CssSelector {

	public static class Add extends CommandAbstract {

		public Add() {
			super(CommandEnum.CSS_SELECTOR_ADD);
		}

		@Override
		public void run(ScriptCommandContext context, String id,
				Object... parameters) throws ScriptException {
			checkParameters(1, parameters);
			context.addCssSelector(getParameterString(0));
		}

	}

	public static class Reset extends CommandAbstract {

		public Reset() {
			super(CommandEnum.CSS_SELECTOR_RESET);
		}

		@Override
		public void run(ScriptCommandContext context, String id,
				Object... parameters) throws ScriptException {
			context.resetCssSelector();
		}

	}
}
