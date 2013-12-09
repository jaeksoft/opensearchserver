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

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.jaeksoft.searchlib.util.Variables;

@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@JsonInclude(Include.NON_EMPTY)
public class ScriptLine {

	final public String id;
	final public String command;
	final public String[] parameters;

	public ScriptLine() {
		id = null;
		command = null;
		parameters = null;
	}

	public ScriptLine(final String id, final String command,
			final String[] parameters) {
		this.id = id;
		this.command = command;
		this.parameters = parameters;
	}

	public ScriptLine(final String id, final String command,
			final String[] parameters, Variables variables) {
		this.id = id;
		this.command = command;
		if (parameters != null)
			for (int i = 0; i < parameters.length; i++)
				parameters[i] = variables.replace(parameters[i]);
		this.parameters = parameters;
	}

	@XmlTransient
	public String getId() {
		return id;
	}

	@XmlTransient
	public String getCommand() {
		return command;
	}

	@XmlTransient
	public String[] getParameters() {
		return parameters;
	}

	public static class XmlScript {

		public final List<ScriptLine> scriptLines;

		public XmlScript(List<ScriptLine> scriptLines) {
			this.scriptLines = scriptLines;
		}

		public XmlScript() {
			this(null);
		}
	}
}
