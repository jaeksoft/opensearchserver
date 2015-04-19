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

package com.jaeksoft.searchlib.crawler.database;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.properties.PropertyItem;
import com.jaeksoft.searchlib.util.properties.PropertyManager;

public class DatabasePropertyManager extends PropertyManager {

	final private PropertyItem<String> defaultLogin;
	final private PropertyItem<String> defaultPassword;

	public DatabasePropertyManager(File file) throws IOException {
		super(file);
		defaultLogin = newStringProperty("defaultLogin", "");
		defaultPassword = newStringProperty("defaultPassword", "");
	}

	public String getDefaultLogin() {
		return defaultLogin.getValue();
	}

	public void setDefaultLogin(String login) throws IOException,
			SearchLibException {
		defaultLogin.setValue(login);
	}

	public void setDefaultPassword(String passwd)
			throws UnsupportedEncodingException, IOException,
			SearchLibException {
		String v = passwd == null ? null : StringUtils.base64encode(passwd);
		defaultPassword.setValue(v == null ? "" : v);
	}

	public String getDefaultPassword() {
		return StringUtils.base64decode(defaultPassword.getValue());
	}

}
