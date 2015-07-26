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
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;
import javax.servlet.http.HttpServletRequest;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SID;

import org.apache.commons.lang3.StringUtils;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.renderer.Renderer;
import com.jaeksoft.searchlib.renderer.RendererException.AuthException;
import com.jaeksoft.searchlib.util.ActiveDirectory;
import com.jaeksoft.searchlib.util.ActiveDirectory.ADGroup;
import com.jaeksoft.searchlib.util.IOUtils;

public class AuthPluginNtlm implements AuthPluginInterface {

	protected NtlmPasswordAuthentication getNtlmAuth(Renderer renderer,
			String username, String password) {
		return new NtlmPasswordAuthentication(renderer.getAuthDomain(),
				username == null ? renderer.getAuthUsername() : username,
				password == null ? renderer.getAuthPassword() : password);
	}

	protected String[] getGroups(SID[] sids, String authServer,
			NtlmPasswordAuthentication ntlmAuth) throws IOException {
		if (sids == null)
			return null;
		String[] groups = new String[sids.length];
		SID.resolveSids(authServer, ntlmAuth, sids);
		int i = 0;
		for (SID gsid : sids)
			groups[i++] = gsid.toDisplayString();
		return groups;
	}

	@Override
	public User getUser(Renderer renderer, HttpServletRequest request)
			throws IOException {
		String remoteUser = request.getRemoteUser();
		if (remoteUser == null)
			remoteUser = request.getHeader("X-OSS-REMOTE-USER");
		return getUser(renderer, remoteUser, null);
	}

	@Override
	public User getUser(Renderer renderer, String remoteUser,
			String ignoredPassword) throws IOException {
		ActiveDirectory activeDirectory = null;
		if (StringUtils.isEmpty(remoteUser))
			throw new AuthException("No user");
		int i = remoteUser.indexOf('@');
		if (i != -1)
			remoteUser = remoteUser.substring(0, i);
		i = remoteUser.indexOf('\\');
		if (i != -1)
			remoteUser = remoteUser.substring(i + 1);
		try {
			String domain = renderer.getAuthDomain();

			User user = AuthUserCache.INSTANCE.get(remoteUser, domain);
			if (user != null)
				return user;

			NtlmPasswordAuthentication ntlmAuth = getNtlmAuth(renderer, null,
					null);
			activeDirectory = new ActiveDirectory(renderer.getAuthServer(),
					ntlmAuth.getUsername(), ntlmAuth.getPassword(),
					ntlmAuth.getDomain());

			NamingEnumeration<SearchResult> result = activeDirectory
					.findUser(remoteUser);
			Attributes attrs = ActiveDirectory.getAttributes(result);
			if (attrs == null)
				throw new AuthException("No user found: " + remoteUser);
			String userId = ActiveDirectory.getObjectSID(attrs);
			List<ADGroup> groups = new ArrayList<ADGroup>();
			activeDirectory.findUserGroups(attrs, groups);
			String dnUser = ActiveDirectory.getStringAttribute(attrs,
					"DistinguishedName");
			if (!StringUtils.isEmpty(dnUser))
				activeDirectory.findUserGroup(dnUser, groups);
			user = new User(userId, remoteUser, null, ActiveDirectory.toArray(
					groups, "everyone"), ActiveDirectory.getDisplayString(
					domain, remoteUser));

			Logging.info("USER authenticated: " + user + " DN=" + dnUser);

			AuthUserCache.INSTANCE.add(remoteUser, domain, user);
			return user;
		} catch (NamingException e) {
			Logging.warn(e);
			throw new AuthException("LDAP error (NamingException) : "
					+ e.getMessage());
		} finally {
			IOUtils.close(activeDirectory);
		}
	}
}
