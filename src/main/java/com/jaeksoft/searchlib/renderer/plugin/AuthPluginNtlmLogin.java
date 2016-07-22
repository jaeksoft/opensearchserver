/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014-2015 Emmanuel Keller / Jaeksoft
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
		return getUser(renderer, request.getParameter("username"),
				request.getParameter("password"));
	}

	@Override
	public User getUser(Renderer renderer, String username, String password)
			throws IOException {
		if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password))
			throw new AuthException("Username or password is empty");
		if (StringUtils.isEmpty(renderer.getAuthServer()))
			throw new AuthException(
					"No auth server given, check the parameters of the renderer");

		ActiveDirectory activeDirectory = null;
		try {
			String domain = renderer.getAuthDomain();
			String authServer = renderer.getAuthServer();

			User user = AuthUserCache.INSTANCE.get(username, domain);
			if (user != null)
				return user;

			NtlmPasswordAuthentication ntlmAuth = getNtlmAuth(renderer,
					username, password);
			UniAddress dc = UniAddress.getByName(authServer, true);
			SmbSession.logon(dc, ntlmAuth);

			activeDirectory = new ActiveDirectory(renderer.getAuthServer(),
					ntlmAuth.getUsername(), ntlmAuth.getPassword(), domain);

			NamingEnumeration<SearchResult> result = activeDirectory
					.findUser(username);
			Attributes attrs = ActiveDirectory.getAttributes(result);
			if (attrs == null)
				throw new AuthException("No user found: " + username);

			String userId = ActiveDirectory.getObjectSID(attrs);
			List<ADGroup> groups = new ArrayList<ADGroup>();
			activeDirectory.findUserGroups(attrs, groups);
			String dnUser = ActiveDirectory.getStringAttribute(attrs,
					"DistinguishedName");
			if (!StringUtils.isEmpty(dnUser))
				activeDirectory.findUserGroup(dnUser, groups);

			Logging.info("USER authenticated: " + user);

			user = new User(userId.toLowerCase(), username.toLowerCase(),
					password, ActiveDirectory.toArray(groups, "everyone"),
					ActiveDirectory.getDisplayString(domain, username));
			AuthUserCache.INSTANCE.add(username, domain, user);
			return user;

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
		} catch (NamingException e) {
			Logging.warn(e);
			throw new AuthException("LDAP error (NamingException) : "
					+ e.getMessage());
		} finally {
			IOUtils.close(activeDirectory);
		}
	}

	public static void main(String[] args) throws NamingException,
			UnknownHostException, SmbException {

		ActiveDirectory activeDirectory = null;
		try {
			String server = args[0];
			String domain = args[1];
			String authUser = args[2];
			String password = args[3];
			String username = args[4];

			NtlmPasswordAuthentication ntlmAuth = new NtlmPasswordAuthentication(
					domain, authUser, password);
			UniAddress dc = UniAddress.getByName(server, true);
			SmbSession.logon(dc, ntlmAuth);

			activeDirectory = new ActiveDirectory(server, authUser, password,
					domain);

			NamingEnumeration<SearchResult> result = activeDirectory
					.findUser(username);

			Attributes attrs = ActiveDirectory.getAttributes(result);
			if (attrs == null) {
				System.out.println("no user found");
				return;
			}
			String userId = ActiveDirectory.getObjectSID(attrs);
			System.out.println("SID: " + userId);
			List<ADGroup> groups = new ArrayList<ADGroup>();
			activeDirectory.findUserGroups(attrs, groups);

			String dnUser = ActiveDirectory.getStringAttribute(attrs,
					"DistinguishedName");
			System.out.println(dnUser);
			if (!StringUtils.isEmpty(dnUser))
				activeDirectory.findUserGroup(dnUser, groups);

			String[] groupArray = ActiveDirectory.toArray(groups, "everyone");
			System.out.println(new User(userId, username, password, groupArray,
					ActiveDirectory.getDisplayString(domain, username)));
			for (String group : groupArray)
				System.out.println(group);

		} finally {
			if (activeDirectory != null)
				activeDirectory.close();
		}
	}
}
