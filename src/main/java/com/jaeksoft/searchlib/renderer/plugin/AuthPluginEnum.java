/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013-2015 Emmanuel Keller / Jaeksoft
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

public enum AuthPluginEnum {

	NO_AUTH("No authentication", null, false),

	HTTP_HEADER("HTTP Header", AuthPluginHttpHeader.class, false),

	NTLM("NTLM", AuthPluginNtlm.class, false),

	NTLM_LOGIN("NTLM with login", AuthPluginNtlmLogin.class, true),

	INDEX_LOGIN("INDEX with login", AuthPluginIndexLogin.class, true),

	WAFFLE("WAFFLE/SSO", AuthPluginWaffle.class, false);

	public final String label;

	public final Class<? extends AuthPluginInterface> authPluginClass;

	private final boolean isLogout;

	private AuthPluginEnum(String label,
			Class<? extends AuthPluginInterface> authPluginClass,
			boolean isLogout) {
		this.label = label;
		this.authPluginClass = authPluginClass;
		this.isLogout = isLogout;
	}

	@Override
	public String toString() {
		return label;
	}

	public String getClassName() {
		if (authPluginClass == null)
			return null;
		return authPluginClass.getName();
	}

	public boolean isLogout() {
		return isLogout;
	}

	public static AuthPluginEnum find(String nameOrClass) {
		if (nameOrClass == null || nameOrClass.length() == 0)
			return NO_AUTH;
		for (AuthPluginEnum type : values()) {
			if (type.name().equalsIgnoreCase(nameOrClass))
				return type;
			if (type.authPluginClass != null)
				if (nameOrClass
						.equalsIgnoreCase(type.authPluginClass.getName()))
					return type;
			if (nameOrClass.equalsIgnoreCase(type.label))
				return type;
		}
		return null;
	}

}
