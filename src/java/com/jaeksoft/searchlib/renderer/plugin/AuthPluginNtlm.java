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

import javax.servlet.http.HttpServletRequest;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SID;
import jcifs.smb.SmbAuthException;

import com.jaeksoft.searchlib.renderer.Renderer;

public class AuthPluginNtlm implements AuthPluginInterface {

	@Override
	/*
	 * The userId must be an SID (non-Javadoc)
	 * 
	 * @see
	 * com.jaeksoft.searchlib.renderer.plugin.AuthPluginInterface#authGetGroups
	 * (com.jaeksoft.searchlib.renderer.Renderer, java.lang.String)
	 */
	public String[] authGetGroups(Renderer renderer, String userId)
			throws IOException {
		if (userId == null)
			throw new IOException("No SID given");
		SID[] sids = null;
		try {
			NtlmPasswordAuthentication ntlmAuth = new NtlmPasswordAuthentication(
					renderer.getAuthDomain(), renderer.getAuthUsername(),
					renderer.getAuthPassword());
			String authServer = renderer.getAuthServer();
			SID sid = new SID(userId);
			sid.resolve(authServer, ntlmAuth);
			sids = sid.getGroupMemberSids(authServer, ntlmAuth,
					SID.SID_FLAG_RESOLVE_SIDS);
		} catch (SmbAuthException e) {
			throw new IOException("SMB authentication failed. Domain: "
					+ renderer.getAuthDomain() + " User: "
					+ renderer.getAuthUsername(), e);
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
	public String getUserId(HttpServletRequest request) throws IOException {
		if (request.getUserPrincipal() != null)
			return request.getUserPrincipal().getName();
		return request.getRemoteUser();
	}

}
