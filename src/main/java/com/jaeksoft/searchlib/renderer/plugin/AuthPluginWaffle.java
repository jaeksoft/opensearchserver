/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013-2014 Emmanuel Keller / Jaeksoft
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
import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.realm.GenericPrincipal;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.renderer.Renderer;
import com.jaeksoft.searchlib.renderer.RendererException.AuthException;

public class AuthPluginWaffle implements AuthPluginInterface {

	@Override
	public User getUser(Renderer renderer, HttpServletRequest request)
			throws IOException {

		final Principal userPrincipal = request.getUserPrincipal();
		if (userPrincipal == null)
			throw new AuthException("No authenticated user");
		final String remoteUser = userPrincipal.getName();
		String domain = renderer.getAuthDomain();

		User user = AuthUserCache.INSTANCE.get(remoteUser, domain);
		if (user != null)
			return user;

		GenericPrincipal genericPrincipal = (GenericPrincipal) userPrincipal;
		final String[] roles = genericPrincipal.getRoles();
		if (roles != null)
			for (int i = 0; i < roles.length; i++)
				roles[i] = roles[i].toLowerCase();

		user = new User(remoteUser, request.getRemoteUser(), null, roles);

		Logging.info("USER authenticated: " + user);

		AuthUserCache.INSTANCE.add(remoteUser, domain, user);
		return user;
	}

	@Override
	public User getUser(Renderer renderer, String login, String password)
			throws IOException {
		throw new IOException("Not implemented");
	}
}
