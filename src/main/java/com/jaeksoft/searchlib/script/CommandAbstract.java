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

package com.jaeksoft.searchlib.script;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.jaeksoft.searchlib.util.RegExpUtils;

public abstract class CommandAbstract {

	private final CommandEnum commandEnum;

	private String[] parameters;

	protected CommandAbstract(CommandEnum commandEnum) {
		this.commandEnum = commandEnum;
		this.parameters = null;
	}

	protected void checkParameters(int count, String... parameters)
			throws ScriptException {
		this.parameters = parameters;
		if (count == 0)
			return;
		if (parameters == null)
			throwError("Missing parameters");
		if (parameters.length < count)
			throwError("Missing parameter(s)");
		for (int i = 0; i < count; i++)
			if (parameters[i] == null)
				throwError("The parameter is empty: " + (i + 1));
	}

	protected String findPatternFunction(int from, Pattern pattern) {
		String functionParam = null;
		for (int i = from; i < getParameterCount(); i++) {
			String param = getParameterString(i);
			if (StringUtils.isEmpty(param))
				continue;
			Matcher matcher = RegExpUtils.matcher(pattern, param);
			if (matcher.find())
				if (matcher.groupCount() > 0)
					functionParam = matcher.group(1);
		}
		return functionParam;
	}

	protected int getParameterCount() {
		return parameters.length;
	}

	protected String getParameterString(int pos) {
		if (pos >= parameters.length)
			return null;
		String p = parameters[pos];
		if (p == null)
			return null;
		return p;
	}

	protected Integer getParameterInt(int pos) {
		String s = getParameterString(pos);
		if (s == null)
			return null;
		if (s.length() == 0)
			return null;
		return new Integer(s);
	}

	protected void throwError(String message) throws ScriptException {
		throw new ScriptException("Command " + commandEnum.name() + " - "
				+ message);
	}

	public abstract void run(ScriptCommandContext context, String id,
			String... parameters) throws ScriptException;
}
