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
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import com.jaeksoft.searchlib.query.QueryUtils;
import com.jaeksoft.searchlib.renderer.Renderer;

public interface AuthPluginInterface {

	public static class User {

		public final static User EMPTY = new User(null, null, null);

		public final String userId;
		public final String username;
		public final String password;
		public final Set<String> usernames;

		public User(String userId, String username, String password,
				Object... userNames) {
			usernames = new TreeSet<String>();
			this.username = username;
			addUsername(username);
			this.password = password;
			this.userId = userId;
			for (Object objectName : userNames)
				if (objectName != null)
					addUsername(objectName.toString());
		}

		protected void addUsername(String username) {
			if (username == null)
				return;
			if (username.length() == 0)
				return;
			usernames.add(username);
		}

		public final static void usernamesToFilterQuery(User user,
				StringBuilder sbQuery) {
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
				sbQuery.append(QueryUtils.escapeQuery(username));
				sbQuery.append('"');
			}
			sbQuery.append(')');
		}

	}

	public String[] authGetGroups(Renderer renderer, User user)
			throws IOException;

	public User getUser(Renderer renderer, User sessionUser,
			HttpServletRequest request) throws IOException;

}
