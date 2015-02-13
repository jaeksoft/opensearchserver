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

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.renderer.Renderer;
import com.jaeksoft.searchlib.renderer.RendererException.AuthException;

public class AuthPluginHttpHeader implements AuthPluginInterface {

	@Override
	public User getUser(Renderer renderer, HttpServletRequest request)
			throws IOException {

		String remoteUser = request.getRemoteUser();
		if (remoteUser == null)
			remoteUser = request.getHeader("X-OSS-REMOTE-USER");
		if (StringUtils.isEmpty(remoteUser))
			throw new AuthException("No user");
		String[] groups = null;
		String remoteGroups = request.getHeader("X-OSS-REMOTE-GROUPS");
		if (remoteGroups != null)
			groups = StringUtils.split(remoteGroups, ',');
		User user = new User(remoteUser, remoteUser, null, groups);
		Logging.info("USER authenticated: " + user.userId + " Groups count: "
				+ (groups == null ? 0 : groups.length));
		return user;
	}

	@Override
	public User getUser(Renderer renderer, String login, String password)
			throws IOException {
		throw new IOException("Not implemented");
	}

}
