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

import java.io.File;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.parser.Parser;
import com.jaeksoft.searchlib.script.CommandAbstract;
import com.jaeksoft.searchlib.script.CommandEnum;
import com.jaeksoft.searchlib.script.ScriptCommandContext;
import com.jaeksoft.searchlib.script.ScriptException;

public class ParserCommands {

	public static class ParserMerge extends CommandAbstract {

		public ParserMerge() {
			super(CommandEnum.PARSER_MERGE);
		}

		@Override
		public void run(ScriptCommandContext context, String id,
				String... parameters) throws ScriptException {
			checkParameters(3, parameters);
			try {
				String parserName = getParameterString(0);
				Parser parser = context.getConfig().getParserSelector()
						.getNewParserByName(parserName);
				if (parser == null)
					throw new ScriptException("Parser not found: " + parserName);
				String dirPath = getParameterString(1);
				File fileDir = new File(dirPath);
				if (!fileDir.exists())
					throw new ScriptException("Directory not found: " + dirPath);
				if (!fileDir.isDirectory())
					throw new ScriptException("The path is not a directory: "
							+ dirPath);
				File destFile = new File(getParameterString(2));
				parser.mergeFiles(fileDir, destFile);
			} catch (SearchLibException e) {
				throw new ScriptException(e);
			}

		}
	}

}
