/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2009 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.xmlrpc;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.schema.FieldValueItem;

public class UpdateXmlRpc extends AbstractXmlRpc {

	public Map<String, FieldValueItem> updateDocument(String index,
			LanguageEnum lang, Map<String, FieldValueItem> document)
			throws SearchLibException, NamingException,
			NoSuchAlgorithmException, IOException, URISyntaxException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		Client client = ClientCatalog.getClient(index);
		IndexDocument indexDocument = new IndexDocument(lang);
		indexDocument.add(document);
		client.updateDocument(indexDocument);
		return newInfoMap("Updated");
	}

	public Map<String, FieldValueItem> updateDocuments(String index,
			LanguageEnum lang, List<Map<String, FieldValueItem>> documents)
			throws SearchLibException, NamingException,
			NoSuchAlgorithmException, IOException, URISyntaxException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		Client client = ClientCatalog.getClient(index);
		List<IndexDocument> indexDocuments = new ArrayList<IndexDocument>(
				documents.size());
		for (Map<String, FieldValueItem> document : documents) {
			IndexDocument indexDocument = new IndexDocument(lang);
			indexDocument.add(document);
			indexDocuments.add(indexDocument);
		}
		int n = client.updateDocuments(indexDocuments);
		return newInfoMap(n + " document(s) indexed");
	}
}
