/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
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

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;

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
		Client client = getClient();
		if (client != null)
			return " Index: " + client.getIndexDirectory().getName();
		return "Indices";
	}

	public void onLogout() {
		for (ScopeAttribute attr : ScopeAttribute.values())
			setAttribute(attr, null);
		resetDesktop();
	}

}
