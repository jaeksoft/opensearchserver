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

package com.jaeksoft.searchlib.web.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.util.Clients;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.script.ScriptLine;

@AfterCompose(superclass = true)
public class ScriptingController extends CommonController {

	private String selectedScript;

	public ScriptingController() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() throws SearchLibException {
		selectedScript = null;
	}

	public String[] getScripts() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getScriptManager().getList();
	}

	public List<ScriptLine> getScriptLines() throws SearchLibException,
			IOException {
		Client client = getClient();
		if (client == null)
			return null;
		if (StringUtils.isEmpty(selectedScript))
			return null;
		return client.getScriptManager().getContent(selectedScript);
	}

	public String getApiList() throws UnsupportedEncodingException {
		return getRestApiUrl("/index/{index_name}/script");
	}

	public String getScriptApi() throws UnsupportedEncodingException {
		if (StringUtils.isEmpty(selectedScript))
			return null;
		return getRestApiUrl("/index/{index_name}/script/"
				+ URLEncoder.encode(selectedScript, "UTF-8"));
	}

	@NotifyChange("*")
	public void setSelectedScript(String scriptName) {
		selectedScript = scriptName;
		Clients.resize(component);
	}

	public String getSelectedScript() {
		return selectedScript;
	}

}
