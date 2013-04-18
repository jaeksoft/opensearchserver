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

package com.jaeksoft.searchlib.renderer.plugin;

import java.io.IOException;
import java.security.Principal;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import com.jaeksoft.searchlib.renderer.Renderer;

public interface AuthPluginInterface {

	public static class User {

		public final Principal principal;
		public final String remoteUser;
		public final String userId;
		public final Set<String> usernames;

		public User(HttpServletRequest request, String userId,
				Object... objectNames) {
			usernames = new TreeSet<String>();
			principal = request.getUserPrincipal();
			if (principal != null)
				addUsername(principal.getName());
			remoteUser = request.getRemoteUser();
			addUsername(remoteUser);
			this.userId = userId != null ? userId : remoteUser;
			for (Object objectName : objectNames)
				if (objectName != null)
					addUsername(objectName.toString());
		}

		protected void addUsername(String username) {
			if (username == null)
				return;
			if (username.length() == 0)
				return;
			System.out.println("AUTH USERNAME: " + username);
			usernames.add(username);
		}

		public final static void usernamesToFilterQuery(User user,
				StringBuffer sbQuery) {
			if (user == null || user.usernames.size() == 0) {
				sbQuery.append("\"\"");
				return;
			}
			sbQuery.append('(');
			boolean bOr = false;
			for (String username : user.usernames) {
				if (bOr)
					sbQuery.append(" OR ");
				else
					bOr = true;
				sbQuery.append('"');
				sbQuery.append(username);
				sbQuery.append('"');
			}
			sbQuery.append(')');
		}
	}

	public String[] authGetGroups(Renderer renderer, User user)
			throws IOException;

	public User getUser(HttpServletRequest request) throws IOException;
}
