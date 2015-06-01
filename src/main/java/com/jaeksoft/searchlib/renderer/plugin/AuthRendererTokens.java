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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.jaeksoft.searchlib.util.ReadWriteLock;

public class AuthRendererTokens {

	private final ReadWriteLock rwl = new ReadWriteLock();

	private final HashMap<String, AuthToken> tokens;

	public AuthRendererTokens() {
		tokens = new HashMap<String, AuthToken>();
	}

	public String generateToken(String login, String password) {
		checkExpiration();
		rwl.w.lock();
		try {
			String uuid = UUID.randomUUID().toString();
			tokens.put(uuid, new AuthToken(login, password));
			return uuid;
		} finally {
			rwl.w.unlock();
		}
	}

	AuthToken getToken(String uuid) {
		rwl.r.lock();
		try {
			AuthToken token = tokens.get(uuid);
			if (token != null && !token.hasExpired(System.currentTimeMillis()))
				return token;
			return null;
		} finally {
			rwl.r.unlock();
			expire(uuid);
		}
	}

	void expire(String uuid) {
		rwl.w.lock();
		try {
			tokens.remove(uuid);
		} finally {
			rwl.w.unlock();
		}
	}

	private void checkExpiration() {
		rwl.w.lock();
		try {
			List<String> uuidsToDelete = new ArrayList<String>();
			long currentTime = System.currentTimeMillis();
			for (Map.Entry<String, AuthToken> entry : tokens.entrySet())
				if (entry.getValue().hasExpired(currentTime))
					uuidsToDelete.add(entry.getKey());
			for (String uuid : uuidsToDelete)
				tokens.remove(uuid);
		} finally {
			rwl.w.unlock();
		}
	}

	class AuthToken {

		private final long expirationTime;

		final String login;
		final String password;

		private AuthToken(String login, String password) {
			this.login = login;
			this.password = password;
			this.expirationTime = System.currentTimeMillis() + 1000 * 60 * 15;
		}

		boolean hasExpired(long currentTime) {
			return currentTime > expirationTime;
		}
	}
}
