/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.web.controller;

import java.io.File;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;

public class IndexController extends CommonController {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7590913483471357743L;

	public IndexController() throws SearchLibException {
		super();
	}

	public boolean isInstanceValid() throws SearchLibException {
		return getClient() != null;
	}

	public boolean isInstanceNotValid() throws SearchLibException {
		return getClient() == null;
	}

	public void onEnterConfig() throws SearchLibException {
		Client.getFileInstance(new File(getConfigPath()));
		reloadDesktop();
	}

	public void setConfigPath(String configPath) {
		setAttribute("configPath", configPath);
	}

	public String getConfigPath() {
		return (String) getAttribute("configPath");
	}

}
