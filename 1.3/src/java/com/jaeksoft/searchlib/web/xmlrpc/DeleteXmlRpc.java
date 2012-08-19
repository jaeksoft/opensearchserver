/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2009 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.xmlrpc;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.apache.http.HttpException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.SearchLibException;

public class DeleteXmlRpc extends AbstractXmlRpc {

	public Map<String, ?> deleteByUniqueKey(String index, String uniqueKey)
			throws SearchLibException, NamingException, IOException,
			URISyntaxException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, HttpException {
		Client client = ClientCatalog.getClient(index);
		if (client.deleteDocument(uniqueKey))
			return newInfoMap("Document deleted");
		return newInfoMap("Nothing to delete");
	}

	public Map<String, ?> deleteByUniqueKeys(String index,
			List<String> uniqueKeys) throws SearchLibException,
			NamingException, IOException, URISyntaxException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		Client client = ClientCatalog.getClient(index);
		int n = client.deleteDocuments(uniqueKeys);
		return newInfoMap(n + " document(s) deleted");
	}
}
