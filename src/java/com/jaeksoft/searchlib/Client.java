/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.naming.NamingException;

import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.request.Request;
import com.jaeksoft.searchlib.result.Result;
import com.jaeksoft.searchlib.util.Context;
import com.jaeksoft.searchlib.util.XmlInfo;

public class Client extends Config implements XmlInfo {

	public Client(File homeDir, File configfile, boolean createIndexIfNotExists)
			throws SearchLibException {
		super(homeDir, configfile, createIndexIfNotExists);
	}

	public Client(File configfile, boolean createIndexIfNotExists)
			throws SearchLibException {
		this(null, configfile, createIndexIfNotExists);
	}

	public Client(File configfile) throws SearchLibException {
		this(null, configfile, false);
	}

	public void updateDocument(IndexDocument document)
			throws NoSuchAlgorithmException, IOException {
		getIndex().updateDocument(getSchema(), document, false);
	}

	public void reload() throws IOException {
		getIndex().reload(null, true);
	}

	public Result<?> search(Request request) throws IOException {
		return getIndex().search(request);
	}

	private static volatile Client INSTANCE;

	public static Client getWebAppInstance() throws SearchLibException,
			NamingException {
		if (INSTANCE == null) {
			synchronized (Client.class) {
				String contextPath = "java:comp/env/JaeksoftSearchServer/configfile";
				if (INSTANCE == null)
					INSTANCE = new Client(new File((String) Context
							.get(contextPath)), true);
			}
		}
		return INSTANCE;
	}

	public class Test {

	}
}
