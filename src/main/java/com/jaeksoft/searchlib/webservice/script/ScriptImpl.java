/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2013 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.webservice.script;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.script.JsonScript;
import com.jaeksoft.searchlib.script.JsonScript.JsonScriptLine;
import com.jaeksoft.searchlib.script.ScriptException;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.CommonServices;

public class ScriptImpl extends CommonServices implements RestScript {

	@Override
	public CommonResult script(String use, String login, String key,
			List<JsonScriptLine> scriptLines) {
		JsonScript jsonScript = null;
		try {
			Client client = getLoggedClient(use, login, key, Role.SCRIPT_RUN);
			ClientFactory.INSTANCE.properties.checkApi();
			CommonResult result = new CommonResult(true, null);
			jsonScript = new JsonScript(client, null, result, scriptLines);
			jsonScript.run();
			result.addDetail("Script lines", jsonScript.getLineCount());
			result.addDetail("Error lines", jsonScript.getErrorCount());
			result.addDetail("Ignored lines", jsonScript.getIgnoredCount());
			return new ScriptResult(result, jsonScript.getScriptLineResults());
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		} catch (ScriptException e) {
			throw new CommonServiceException(e);
		} finally {
			if (jsonScript != null)
				IOUtils.closeQuietly(jsonScript);
		}

	}
}
