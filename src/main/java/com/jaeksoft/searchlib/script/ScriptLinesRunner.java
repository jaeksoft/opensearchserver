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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.util.InfoCallback;
import com.jaeksoft.searchlib.utils.Variables;

public class ScriptLinesRunner extends AbstractScriptRunner {

	private final static SimpleDateFormat FORMAT_ISO8601 = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ssz");

	@JsonInclude(Include.NON_NULL)
	public static class ScriptLineError {

		public final int lineNumber;
		public final String id;
		public final String date;
		public final String error;

		public ScriptLineError(int lineNumber, ScriptLine scriptLine,
				String errorMsg) {
			this.lineNumber = lineNumber;
			this.id = scriptLine.id;
			synchronized (FORMAT_ISO8601) {
				this.date = FORMAT_ISO8601.format(new Date());
			}
			this.error = errorMsg;
		}
	}

	private final List<ScriptLine> scriptLines;
	private final List<ScriptLineError> scriptLineErrors;
	private Iterator<ScriptLine> lineIterator;
	private int lineNumber;

	public ScriptLinesRunner(Config config, Variables variables,
			InfoCallback callback, List<ScriptLine> scriptLines) {
		super(config, variables, callback);
		this.scriptLines = scriptLines;
		this.scriptLineErrors = scriptLines == null ? null
				: new ArrayList<ScriptLineError>(0);
		lineNumber = 0;
	}

	public List<ScriptLineError> getScriptLineErrors() {
		return scriptLineErrors;
	}

	@Override
	protected void beforeRun(final ScriptCommandContext context,
			final Variables variables) throws ScriptException {
		lineIterator = scriptLines.iterator();
	}

	@Override
	protected ScriptLine nextScriptLine(final Variables variables)
			throws ScriptException {
		if (lineIterator == null)
			return null;
		if (!lineIterator.hasNext())
			return null;
		ScriptLine jsonScriptLine = lineIterator.next();
		return new ScriptLine(jsonScriptLine.id, jsonScriptLine.command,
				jsonScriptLine.parameters);
	}

	@Override
	protected void updateScriptLine(final ScriptLine scriptLine,
			final Variables variables, final String errorMsg)
			throws ScriptException {
		if (StringUtils.isEmpty(errorMsg))
			return;
		scriptLineErrors.add(new ScriptLineError(++lineNumber, scriptLine,
				errorMsg));
	}

	@Override
	public void afterRun(final String lastScriptError, final Variables variables)
			throws ScriptException {
	}

}
