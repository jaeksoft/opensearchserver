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
import java.net.UnknownHostException;

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
import com.jaeksoft.searchlib.renderer.RendererException.NoUserException;
import com.jaeksoft.searchlib.util.StringUtils;

public class AuthPluginNtlm implements AuthPluginInterface {

	private static final NtlmPasswordAuthentication getNtlmAuth(
			Renderer renderer, String username, String password) {
		return new NtlmPasswordAuthentication(renderer.getAuthDomain(),
				username == null ? renderer.getAuthUsername() : username,
				password == null ? renderer.getAuthPassword() : password);
	}

	@Override
	/*
	 * The userId must be an SID (non-Javadoc)
	 * 
	 * @see
	 * com.jaeksoft.searchlib.renderer.plugin.AuthPluginInterface#authGetGroups
	 * (com.jaeksoft.searchlib.renderer.Renderer, java.lang.String)
	 */
	public String[] authGetGroups(Renderer renderer, User user)
			throws IOException {
		if (user == null)
			throw new NoUserException("No USER given");
		if (user.userId == null)
			throw new NoUserException("No user SID ");
		SID[] sids = null;
		try {
			NtlmPasswordAuthentication ntlmAuth = getNtlmAuth(renderer,
					user.password != null ? user.username : null, user.password);
			String authServer = renderer.getAuthServer();
			SID sid = new SID(user.userId);
			sid.resolve(authServer, ntlmAuth);
			sids = sid.getGroupMemberSids(authServer, ntlmAuth,
					SID.SID_FLAG_RESOLVE_SIDS);
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
		if (sids == null)
			return null;
		String[] groups = new String[sids.length];
		int i = 0;
		for (SID gsid : sids)
			groups[i++] = gsid.toDisplayString();
		return groups;
	}

	@Override
	public User getUser(Renderer renderer, HttpServletRequest request)
			throws IOException {
		String userId = null;
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		if (username != null && password != null) {
			try {
				NtlmPasswordAuthentication ntlmAuth = getNtlmAuth(renderer,
						username, password);
				UniAddress dc = UniAddress.getByName(renderer.getAuthServer(),
						true);
				SmbSession.logon(dc, ntlmAuth);
				Logging.warn("Authentication success " + ntlmAuth.getName());
				SID serverSid = SID.getServerSid(renderer.getAuthServer(),
						ntlmAuth);
				SID userSid = null;
				if (StringUtils.isNumeric(username))
					userSid = new SID(serverSid, Integer.parseInt(username));
				else
					userSid = new SID(serverSid, SID.SID_TYPE_USER,
							serverSid.getDomainName(), username, false);
				if (userSid != null) {
					userSid.resolve(renderer.getAuthServer(), ntlmAuth);
					userId = userSid.toString();
				}
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
				throw new AuthException(
						"Authentication error (SmbException) : "
								+ e.getMessage());
			}
		}
		return new User(request, userId, username, password);
	}
}
