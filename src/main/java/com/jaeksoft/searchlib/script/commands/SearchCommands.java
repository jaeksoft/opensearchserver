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

import java.sql.SQLException;
import java.util.regex.Pattern;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.schema.FieldValue;
import com.jaeksoft.searchlib.script.CommandAbstract;
import com.jaeksoft.searchlib.script.CommandEnum;
import com.jaeksoft.searchlib.script.ScriptCommandContext;
import com.jaeksoft.searchlib.script.ScriptException;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.utils.Variables;

public class SearchCommands {

	public static class SearchTemplate extends CommandAbstract {

		public SearchTemplate() {
			super(CommandEnum.SEARCH_TEMPLATE);
		}

		protected SearchTemplate(CommandEnum command) {
			super(command);
		}

		public final static Pattern PARAM_SQL_BY_DOC = Pattern.compile(
				"sql_by_doc\\(\\[([^\\]]*)\\]\\)", Pattern.CASE_INSENSITIVE);

		@Override
		public void run(ScriptCommandContext context, String id,
				String... parameters) throws ScriptException {
			checkParameters(2, parameters);
			String template = parameters[0];
			String query = parameters[1];
			String sqlByDoc = findPatternFunction(2, PARAM_SQL_BY_DOC);
			Client client = (Client) context.getConfig();
			Variables searchVariables = new Variables();
			Variables documentVariables = new Variables();
			context.addVariables(searchVariables, documentVariables);
			try {
				AbstractRequest request = client.getNewRequest(template);
				if (!(request instanceof AbstractSearchRequest))
					throw new ScriptException("Wrong type of search request: "
							+ request.getNameType());
				AbstractSearchRequest searchRequest = (AbstractSearchRequest) request;
				if (!query.isEmpty())
					searchRequest.setQueryString(query);
				AbstractResultSearch result = (AbstractResultSearch) client
						.request(searchRequest);
				searchVariables.put("search:numfound",
						Integer.toString(result.getNumFound()));
				int pos = searchRequest.getStart();
				if (!StringUtils.isEmpty(sqlByDoc)) {
					for (ResultDocument document : result) {
						documentVariables.clear();
						for (FieldValue fieldValue : document.getReturnFields()
								.values()) {
							if (fieldValue.getValuesCount() == 0)
								continue;
							documentVariables.put(StringUtils.fastConcat(
									"search:", fieldValue.getName()),
									fieldValue.getValueArray()[0].getValue());
						}
						documentVariables.put("search:score",
								Float.toString(result.getScore(pos++)));
						context.executeSqlUpdate(context
								.replaceVariables(sqlByDoc));
					}
				}
			} catch (SearchLibException e) {
				throw new ScriptException(e);
			} catch (SQLException e) {
				throw new ScriptException(e);
			} finally {
				context.removeVariables(searchVariables, documentVariables);
			}
		}
	}

}
