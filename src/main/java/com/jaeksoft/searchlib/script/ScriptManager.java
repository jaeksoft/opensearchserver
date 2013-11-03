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

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.jaeksoft.searchlib.analysis.stopwords.AbstractDirectoryManager;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.script.ScriptLine.XmlScript;

public class ScriptManager extends AbstractDirectoryManager<List<ScriptLine>> {

	public ScriptManager(Config config, File file) {
		super(config, file);
	}

	@Override
	protected List<ScriptLine> getContent(File file) throws IOException {
		ObjectMapper xmlMapper = new XmlMapper();
		XmlScript xmlScript = xmlMapper.readValue(file, XmlScript.class);
		return xmlScript.scriptLines;
	}

	@Override
	protected void saveContent(File file, List<ScriptLine> scriptLines)
			throws IOException {
		ObjectMapper xmlMapper = new XmlMapper();
		xmlMapper.writeValue(file, new XmlScript(scriptLines));
	}

}
