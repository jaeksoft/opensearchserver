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
package com.jaeksoft.searchlib.webservice.user;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response.Status;

import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.user.User;
import com.jaeksoft.searchlib.web.controller.ScopeAttribute;
import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.CommonServices;

public class UserImpl extends CommonServices implements RestUser {

	private CommonResult bologin(HttpServletRequest request, String login,
			String password) {
		try {
			User user = ClientCatalog.authenticate(login, password);
			if (user == null) {
				Thread.sleep(2000);
				throw new CommonServiceException(Status.FORBIDDEN,
						"Authentication failed");
			}
			request.getSession().setAttribute(
					ScopeAttribute.LOGGED_USER.name(), user);
			CommonResult cr = new CommonResult(true, "Welcome " + login);
			cr.addDetail("key", user.getApiKey());
			return cr;
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public CommonResult bologin_post(HttpServletRequest request, String login,
			String password) {
		return bologin(request, login, password);
	}

	@Override
	public CommonResult bologin_get(HttpServletRequest request, String login,
			String password) {
		return bologin(request, login, password);
	}

	@Override
	public CommonResult bologout(HttpServletRequest request) {
		request.getSession().removeAttribute(ScopeAttribute.LOGGED_USER.name());
		return new CommonResult(true, "Goodbye !");
	}
}
