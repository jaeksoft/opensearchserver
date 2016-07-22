/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2015 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.renderer.plugin;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.filter.TermFilter;
import com.jaeksoft.searchlib.renderer.Renderer;
import com.jaeksoft.searchlib.renderer.RendererException.AuthException;
import com.jaeksoft.searchlib.renderer.plugin.AuthRendererTokens.AuthToken;
import com.jaeksoft.searchlib.request.SearchFieldRequest;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.webservice.query.search.SearchFieldQuery.SearchField.Mode;
import com.jaeksoft.searchlib.webservice.query.search.SearchQueryAbstract.OperatorEnum;

public class AuthPluginIndexLogin implements AuthPluginInterface {

	@Override
	public User getUser(Renderer renderer, HttpServletRequest request)
			throws IOException {
		String token = request.getParameter("token");
		if (!StringUtils.isEmpty(token)) {
			AuthToken authToken = renderer.getTokens().getToken(token);
			if (authToken == null)
				throw new AuthException("Invalid authentication token");
			return getUser(renderer, authToken.login, authToken.password);
		}
		return getUser(renderer, request.getParameter("username"),
				request.getParameter("password"));
	}

	private final static String USERNAME_FIELD = "username";
	private final static String PASSWORD_FIELD = "password";
	private final static String GROUPS_FIELD = "groups";

	@Override
	public User getUser(Renderer renderer, String username, String password)
			throws IOException {
		if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password))
			throw new AuthException("Username or password is empty");
		String authIndex = renderer.getAuthIndex();
		if (StringUtils.isEmpty(authIndex))
			throw new AuthException(
					"No auth index given, check the parameters of the renderer");
		try {
			Client authClient = ClientCatalog.getClient(authIndex);
			if (authClient == null)
				throw new AuthException("No auth index found: " + authIndex);

			SearchFieldRequest searchFieldRequest = new SearchFieldRequest(
					authClient);
			searchFieldRequest.setDefaultOperator(OperatorEnum.AND);
			searchFieldRequest.addSearchField(PASSWORD_FIELD, Mode.TERM, 1.0d,
					1.0d, null, null);
			searchFieldRequest.setQueryString(PASSWORD_FIELD, password);
			TermFilter tf = new TermFilter();
			tf.setField(USERNAME_FIELD);
			tf.setTerm(username);
			searchFieldRequest.getFilterList().add(tf);
			searchFieldRequest.setStart(0);
			searchFieldRequest.setRows(1);
			searchFieldRequest.addReturnField(GROUPS_FIELD);
			AbstractResultSearch<?> result = (AbstractResultSearch<?>) authClient
					.request(searchFieldRequest);
			if (result == null || result.getNumFound() == 0)
				throw new AuthException("Authentication failed.");
			ResultDocument document = result.getDocument(0);
			List<FieldValueItem> values = document.getValues(GROUPS_FIELD);
			String[] groups;
			if (values != null) {
				groups = new String[values.size()];
				int i = 0;
				for (FieldValueItem value : values)
					groups[i++] = value.value;
			} else
				groups = null;
			User user = new User(username.toLowerCase(), username, password,
					groups);
			Logging.info("USER authenticated: " + user);
			return user;
		} catch (SearchLibException e) {
			Logging.warn(e);
			throw new AuthException(
					"Authentication error (SearchLibException) : "
							+ e.getMessage());
		}
	}
}
