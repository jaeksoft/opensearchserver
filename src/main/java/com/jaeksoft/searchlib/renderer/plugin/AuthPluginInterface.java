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

import com.jaeksoft.searchlib.renderer.Renderer;
import com.jaeksoft.searchlib.util.StringUtils;

public interface AuthPluginInterface {

	public static class User {

		public final long creationTime;
		public final String userId;
		public final String username;
		public final String password;
		public final Set<String> usernames;
		public final Set<String> groups;

		public User(String userId, String username, String password,
				String[] groups, Object... userNames) {
			this.creationTime = System.currentTimeMillis();
			this.usernames = new TreeSet<String>();
			this.groups = new TreeSet<String>();
			this.username = username;
			addUsername(username);
			this.password = password;
			this.userId = userId;
			for (Object objectName : userNames)
				if (objectName != null)
					addUsername(objectName.toString());
			if (groups != null)
				for (String group : groups)
					if (group != null)
						this.groups.add(group);
		}

		protected void addUsername(String username) {
			if (username == null)
				return;
			if (username.length() == 0)
				return;
			usernames.add(username);
		}

		@Override
		public String toString() {
			return StringUtils.fastConcat(userId, " - ", username, " ",
					usernames, " groups: ",
					groups == null ? '0' : groups.size());
		}

	}

	public User getUser(Renderer renderer, HttpServletRequest request)
			throws IOException;

}
