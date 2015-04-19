/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014 Emmanuel Keller / Jaeksoft
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

import java.util.Map;
import java.util.TreeMap;

import com.jaeksoft.searchlib.util.StringUtils;

public class AuthUserCache {

	private final Map<String, AuthPluginInterface.User> users = new TreeMap<String, AuthPluginInterface.User>();
	private final long expirationTime;

	/**
	 * Create a User cache
	 * 
	 * @param expirationTime
	 *            How many time a record is valid (in seconds)
	 */
	AuthUserCache(int expirationTime) {
		this.expirationTime = expirationTime * 1000;
	}

	private String getUserIdDomain(String username, String domain) {
		return StringUtils.fastConcat(username, '@', domain);
	}

	/**
	 * Add a user to the cache
	 * 
	 * @param user
	 */
	void add(String username, String domain, AuthPluginInterface.User user) {
		users.put(getUserIdDomain(username, domain), user);
	}

	/**
	 * Retrieve the non-expired user.
	 * 
	 * @param userId
	 * @return
	 */
	AuthPluginInterface.User get(String username, String domain) {
		AuthPluginInterface.User user = users.get(getUserIdDomain(username,
				domain));
		if (user == null)
			return null;
		if (user.creationTime + expirationTime < System.currentTimeMillis())
			return null;
		return user;
	}

	final static AuthUserCache INSTANCE = new AuthUserCache(300);
}
