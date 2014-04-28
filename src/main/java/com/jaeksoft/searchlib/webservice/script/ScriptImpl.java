/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2014 Emmanuel Keller / Jaeksoft
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
import java.util.Map;

import javax.ws.rs.core.UriInfo;

import org.apache.commons.collections.MapUtils;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.script.ScriptException;
import com.jaeksoft.searchlib.script.ScriptLine;
import com.jaeksoft.searchlib.script.ScriptLinesRunner;
import com.jaeksoft.searchlib.script.ScriptManager;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.util.IOUtils;
import com.jaeksoft.searchlib.util.Variables;
import com.jaeksoft.searchlib.webservice.AbstractDirectoryImpl;
import com.jaeksoft.searchlib.webservice.CommonListResult;
import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.CommonServices;

public class ScriptImpl extends CommonServices implements RestScript {

	private class ScriptDirectoryImpl extends
			AbstractDirectoryImpl<List<ScriptLine>, ScriptManager> {

		@Override
		protected ScriptManager getManager(Client client)
				throws SearchLibException {
			return client.getScriptManager();
		}

		private CommonResult run(List<ScriptLine> scriptLines,
				Variables variables) {
			CommonResult result = new CommonResult(true, null);
			ScriptLinesRunner scriptLinesRunner = null;
			try {
				scriptLinesRunner = new ScriptLinesRunner(client, variables,
						result, scriptLines);
				scriptLinesRunner.run();
				result.addDetail("Script lines",
						scriptLinesRunner.getLineCount());
				result.addDetail("Error lines",
						scriptLinesRunner.getErrorCount());
				result.addDetail("Ignored lines",
						scriptLinesRunner.getIgnoredCount());
				result.addDetail("Updated documents",
						scriptLinesRunner.getUpdatedDocumentCount());
				return new ScriptResult(result,
						scriptLinesRunner.getScriptLineErrors());
			} catch (ScriptException e) {
				throw new CommonServiceException(e);
			} finally {
				IOUtils.close(scriptLinesRunner);
			}
		}

		protected CommonResult run(UriInfo uriInfo, String index, String login,
				String key, List<ScriptLine> scriptLines) {
			try {
				getLoggedClient(uriInfo, index, login, key, Role.SCRIPT_RUN);
				ClientFactory.INSTANCE.properties.checkApi();
				return run(scriptLines, null);
			} catch (InterruptedException e) {
				throw new CommonServiceException(e);
			} catch (IOException e) {
				throw new CommonServiceException(e);
			}
		}

		protected CommonResult run(UriInfo uriInfo, String index, String login,
				String key, String name, Map<String, String> vars) {
			try {
				getLoggedClient(uriInfo, index, login, key, Role.SCRIPT_RUN);
				ClientFactory.INSTANCE.properties.checkApi();
				Variables variables = MapUtils.isEmpty(vars) ? null
						: new Variables(vars);
				return run(get(name), variables);
			} catch (InterruptedException e) {
				throw new CommonServiceException(e);
			} catch (IOException e) {
				throw new CommonServiceException(e);
			} catch (SearchLibException e) {
				throw new CommonServiceException(e);
			}
		}
	}

	@Override
	public CommonResult run(UriInfo uriInfo, String index, String login,
			String key, List<ScriptLine> scriptLines) {
		return new ScriptDirectoryImpl().run(uriInfo, index, login, key,
				scriptLines);

	}

	@Override
	public CommonResult run(UriInfo uriInfo, String index, String login,
			String key, String name) {
		return new ScriptDirectoryImpl().run(uriInfo, index, login, key, name,
				null);
	}

	@Override
	public CommonResult run(UriInfo uriInfo, String index, String login,
			String key, String name, Map<String, String> variables) {
		return new ScriptDirectoryImpl().run(uriInfo, index, login, key, name,
				variables);
	}

	@Override
	public CommonListResult list(UriInfo uriInfo, String index, String login,
			String key) {
		return new ScriptDirectoryImpl().list(uriInfo, index, login, key);
	}

	@Override
	public List<ScriptLine> get(UriInfo uriInfo, String index, String login,
			String key, String name) {
		return new ScriptDirectoryImpl().get(uriInfo, index, login, key, name);
	}

	@Override
	public CommonResult exists(UriInfo uriInfo, String index, String login,
			String key, String name) {
		return new ScriptDirectoryImpl().exists(uriInfo, index, login, key,
				name);
	}

	@Override
	public CommonResult set(UriInfo uriInfo, String index, String login,
			String key, String name, List<ScriptLine> scriptLines) {
		return new ScriptDirectoryImpl().set(uriInfo, index, login, key, name,
				scriptLines);
	}

	@Override
	public CommonResult delete(UriInfo uriInfo, String index, String login,
			String key, String name) {
		return new ScriptDirectoryImpl().delete(uriInfo, index, login, key,
				name);
	}
}
