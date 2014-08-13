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
import java.net.UnknownHostException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;
import javax.servlet.http.HttpServletRequest;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SID;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbException;

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

	/*
	 * The userId must be an SID (non-Javadoc)
	 * 
	 * @see
	 * com.jaeksoft.searchlib.renderer.plugin.AuthPluginInterface#authGetGroups
	 * (com.jaeksoft.searchlib.renderer.Renderer, java.lang.String)
	 */
	private String[] getGroups(Renderer renderer, String sidString)
			throws IOException {
		SID[] sids = null;
		try {
			NtlmPasswordAuthentication ntlmAuth = getNtlmAuth(renderer, null,
					null);
			String authServer = renderer.getAuthServer();
			SID sid = new SID(sidString);
			sid.resolve(authServer, ntlmAuth);
			sids = sid.getGroupMemberSids(authServer, ntlmAuth,
					SID.SID_FLAG_RESOLVE_SIDS);
			return getGroups(sids, authServer, ntlmAuth);
		} catch (SmbAuthException sae) {
			Logging.warn(sae);
			throw new AuthException("SmbAuthException : " + sae.getMessage());
		} catch (UnknownHostException e) {
			Logging.warn(e);
			throw new AuthException("UnknownHostException : " + e.getMessage());
		} catch (SmbException e) {
			Logging.warn(e);
			throw new AuthException("SmbException : " + e.getMessage());
		}
	}

	@Override
	public User getUser(Renderer renderer, HttpServletRequest request)
			throws IOException {
		String remoteUser = request.getRemoteUser();
		String userId = remoteUser;
		Principal principal = request.getUserPrincipal();
		String username = principal != null ? principal.getName() : remoteUser;
		ActiveDirectory activeDirectory = null;
		try {
			String domain = renderer.getAuthDomain();
			NtlmPasswordAuthentication ntlmAuth = getNtlmAuth(renderer, null,
					null);
			activeDirectory = new ActiveDirectory(ntlmAuth.getUsername(),
					ntlmAuth.getPassword(), ntlmAuth.getDomain());

			NamingEnumeration<SearchResult> result = activeDirectory
					.findUser(username);
			Attributes attrs = ActiveDirectory.getAttributes(result);
			if (attrs == null)
				throw new AuthException("No user found");
			userId = ActiveDirectory.getObjectSID(attrs);
			List<ADGroup> groups = new ArrayList<ADGroup>();
			activeDirectory.findUserGroups(attrs, groups);
			return new User(userId, username, null,
					ActiveDirectory.toArray(groups),
					ActiveDirectory.getDisplayString(domain, username));
		} catch (NamingException e) {
			Logging.warn(e);
			throw new AuthException("LDAP error (NamingException) : "
					+ e.getMessage());
		} finally {
			IOUtils.close(activeDirectory);
		}
	}
}
