/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2010 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.web.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.user.Role;

public class IndexController extends CommonController {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7590913483471357743L;

	private String release;

	public IndexController() throws SearchLibException {
		super();
		reset();
	}

	@Override
	public void reset() {
		release = null;
	}

	public String getRelease() throws IOException {
		synchronized (this) {
			if (release != null)
				return release;
			release = "Open Search Server";
			InputStream is = null;
			try {
				is = getDesktop().getWebApp().getResourceAsStream("/version");
				if (is != null) {
					Properties properties = new Properties();
					properties.load(is);
					String version = properties.getProperty("VERSION");
					String stage = properties.getProperty("STAGE");
					String revision = properties.getProperty("REVISION");
					String build = properties.getProperty("BUILD");

					release += " v" + version + " - " + stage;
					if (revision != null)
						release += " - rev " + revision;
					if (build != null)
						release += " - build " + build;
				}
			} finally {
				if (is != null)
					is.close();
			}
			return release;
		}
	}

	public String getIndexTitle() throws SearchLibException {
		String indexName = getIndexName();
		if (indexName == null)
			return "Indices";
		return " Index: " + indexName;
	}

	public boolean isQueryRights() throws SearchLibException {
		if (!isLogged() || !isInstanceValid())
			return false;
		if (isNoUserList())
			return true;
		return getLoggedUser().hasAnyRole(getIndexName(), Role.GROUP_INDEX);
	}

	public boolean isCrawlerRights() throws SearchLibException {
		if (!isLogged() || !isInstanceValid())
			return false;
		if (isNoUserList())
			return true;
		return getLoggedUser().hasAnyRole(getIndexName(),
				Role.GROUP_WEB_CRAWLER, Role.GROUP_FILE_CRAWLER);
	}

	public boolean isRuntimeRights() throws SearchLibException {
		if (!isLogged() || !isInstanceValid())
			return false;
		if (isNoUserList())
			return true;
		return getLoggedUser().hasAnyRole(getIndexName(), Role.GROUP_INDEX);
	}

	public boolean isPrivilegeRights() throws SearchLibException {
		if (isAdmin())
			return true;
		if (isNoUserList())
			return true;
		return false;
	}
}
