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

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;
import javax.servlet.http.HttpServletRequest;

import jcifs.UniAddress;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SID;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbException;
import jcifs.smb.SmbSession;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.renderer.Renderer;
import com.jaeksoft.searchlib.renderer.RendererException.AuthException;
import com.jaeksoft.searchlib.util.ActiveDirectory;
import com.jaeksoft.searchlib.util.ActiveDirectory.ADGroup;
import com.jaeksoft.searchlib.util.IOUtils;
import com.jaeksoft.searchlib.util.StringUtils;

public class AuthPluginNtlmLogin extends AuthPluginNtlm {

	protected String[] getGroups(Collection<String> sidCollection,
			String authServer, NtlmPasswordAuthentication ntlmAuth)
			throws IOException {
		SID[] sids = new SID[sidCollection.size()];
		int i = 0;
		for (String sid : sidCollection)
			sids[i++] = new SID(sid);
		return getGroups(sids, authServer, ntlmAuth);
	}

	@Override
	public User getUser(Renderer renderer, HttpServletRequest request)
			throws IOException {

		String username = request.getParameter("username");
		String password = request.getParameter("password");
		if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password))
			throw new AuthException("Username or password is empty");
		if (StringUtils.isEmpty(renderer.getAuthServer()))
			throw new AuthException(
					"No auth server given, check the parameters of the renderer");
		// For debug
		/*
		 * if (true) return new User("userId", username, password, new String[]
		 * { "Guest" });
		 */
		ActiveDirectory activeDirectory = null;
		try {
			String domain = renderer.getAuthDomain();
			NtlmPasswordAuthentication ntlmAuth = getNtlmAuth(renderer,
					username, password);
			UniAddress dc = UniAddress
					.getByName(renderer.getAuthServer(), true);
			SmbSession.logon(dc, ntlmAuth);

			activeDirectory = new ActiveDirectory(username, password, domain);

			NamingEnumeration<SearchResult> result = activeDirectory
					.findUser(username);
			Attributes attrs = ActiveDirectory.getAttributes(result);
			if (attrs == null)
				throw new AuthException("No user found");

			String userId = ActiveDirectory.getObjectSID(attrs);
			List<ADGroup> groups = new ArrayList<ADGroup>();
			activeDirectory.findUserGroups(attrs, groups);
			for (ADGroup group : groups)
				Logging.warn("GROUP FOUND: " + group.cn);
			return new User(userId, username, password,
					ActiveDirectory.toArray(groups),
					ActiveDirectory.getDisplayString(domain, username));

		} catch (SmbAuthException e) {
			Logging.warn(e);
			throw new AuthException(
					"Authentication error (SmbAuthException) : "
							+ e.getMessage());
		} catch (UnknownHostException e) {
			Logging.warn(e);
			throw new AuthException(
					"Authentication error (UnknownHostException) : "
							+ e.getMessage());
		} catch (SmbException e) {
			Logging.warn(e);
			throw new AuthException("Authentication error (SmbException) : "
					+ e.getMessage());
		} catch (NamingException e) {
			Logging.warn(e);
			throw new AuthException("LDAP error (NamingException) : "
					+ e.getMessage());
		} finally {
			IOUtils.close(activeDirectory);
		}
	}
}
