/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2012 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.webservice;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;
import javax.xml.ws.WebServiceException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.schema.FieldValueOriginEnum;

public class UpdateImpl extends CommonServicesImpl implements Update {

	private int updateDocCount;

	@Override
	public CommonResult update(String use, String login, String key,
			List<Document> updateDocuments) {
		try {
			ClientFactory.INSTANCE.properties.checkApi();
			if (isLogged(use, login, key)) {
				Client client = ClientCatalog.getClient(use);
				updateDocCount = updateDocument(client, updateDocuments);
				return new CommonResult(true, "Updated " + updateDocCount
						+ " Document");
			} else
				throw new WebServiceException("Bad Credential");
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		} catch (NamingException e) {
			throw new WebServiceException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new WebServiceException(e);
		} catch (IOException e) {
			throw new WebServiceException(e);
		} catch (URISyntaxException e) {
			throw new WebServiceException(e);
		} catch (InstantiationException e) {
			throw new WebServiceException(e);
		} catch (IllegalAccessException e) {
			throw new WebServiceException(e);
		} catch (ClassNotFoundException e) {
			throw new WebServiceException(e);
		} catch (InterruptedException e) {
			throw new WebServiceException(e);
		}
	}

	private int updateDocument(Client client, List<Document> updateDocuments)
			throws NoSuchAlgorithmException, IOException, URISyntaxException,
			SearchLibException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		int count = 0;
		List<IndexDocument> indexDocuments = new ArrayList<IndexDocument>();
		for (Document document : updateDocuments) {
			IndexDocument indexDoc = getIndexDocument(document.lang);
			for (UpdateFieldList fields : document.fields) {
				indexDoc.add(fields.name, new FieldValueItem(
						FieldValueOriginEnum.EXTERNAL, fields.value));
			}
			indexDocuments.add(indexDoc);
			count++;
		}
		client.updateDocuments(indexDocuments);
		return count;
	}

	private IndexDocument getIndexDocument(LanguageEnum lang) {
		synchronized (this) {
			return new IndexDocument(lang);
		}
	}
}
