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
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.request.Request;
import com.jaeksoft.searchlib.result.Result;
import com.jaeksoft.searchlib.util.Context;
import com.jaeksoft.searchlib.util.XmlInfo;

public class Client extends Config implements XmlInfo {

	public Client(File configfile) throws XPathExpressionException,
			DOMException, NamingException, ParserConfigurationException,
			SAXException, IOException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		super(null, configfile, false);
	}

	public Client(File homeDir, File configfile, boolean createIndexIfNotExists)
			throws XPathExpressionException, DOMException,
			ParserConfigurationException, SAXException, IOException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		super(homeDir, configfile, createIndexIfNotExists);
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

	public static Client getWebAppInstance() throws XPathExpressionException,
			DOMException, NamingException, ParserConfigurationException,
			SAXException, IOException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		if (INSTANCE == null) {
			synchronized (Client.class) {
				if (INSTANCE == null)
					INSTANCE = new Client(new File((String) Context
							.get("JaeksoftSearchServer/configfile")));
			}
		}
		return INSTANCE;
	}
}
