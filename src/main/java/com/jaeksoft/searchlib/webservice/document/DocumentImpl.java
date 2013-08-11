/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2013 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.webservice.document;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.request.SearchPatternRequest;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.CommonServices;

public class DocumentImpl extends CommonServices implements SoapDocument,
		RestDocument {

	@Override
	public CommonResult deleteByQuery(String use, String login, String key,
			String query) {
		try {
			Client client = getLoggedClient(use, login, key, Role.INDEX_UPDATE);
			ClientFactory.INSTANCE.properties.checkApi();
			AbstractSearchRequest request = new SearchPatternRequest(client);
			request.setQueryString(query);
			int count = client.deleteDocuments(request);
			return new CommonResult(true, count + " document(s) deleted");
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public CommonResult deleteByValue(String use, String login, String key,
			String field, List<String> values) {
		try {
			Client client = getLoggedClient(use, login, key, Role.INDEX_UPDATE);
			ClientFactory.INSTANCE.properties.checkApi();
			int count = client.deleteDocuments(field, values);
			return new CommonResult(true, count + " document(s) deleted");
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		}
	}

	private int updateDocument(Client client, List<DocumentUpdate> documents)
			throws NoSuchAlgorithmException, IOException, URISyntaxException,
			SearchLibException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		if (documents == null || documents.size() == 0)
			throw new CommonServiceException("No documents");
		List<IndexDocument> indexDocuments = new ArrayList<IndexDocument>(0);
		for (DocumentUpdate document : documents) {
			IndexDocument indexDoc = new IndexDocument(document.lang);
			if (document.values != null)
				for (DocumentUpdate.Values values : document.values)
					if (values.value != null)
						for (DocumentUpdate.Value value : values.value)
							indexDoc.add(values.field, value.content,
									value.boost != null ? value.boost
											: values.boost);
			if (document.value != null)
				for (DocumentUpdate.Value value : document.value)
					indexDoc.add(value.field, value.content, value.boost);
			indexDocuments.add(indexDoc);
		}
		return client.updateDocuments(indexDocuments);
	}

	@Override
	public CommonResult update(String use, String login, String key,
			List<DocumentUpdate> documents) {
		try {
			Client client = getLoggedClient(use, login, key, Role.INDEX_UPDATE);
			ClientFactory.INSTANCE.properties.checkApi();
			int count = updateDocument(client, documents);
			return new CommonResult(true, count + " document(s) updated");
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		} catch (URISyntaxException e) {
			throw new CommonServiceException(e);
		} catch (InstantiationException e) {
			throw new CommonServiceException(e);
		} catch (IllegalAccessException e) {
			throw new CommonServiceException(e);
		} catch (ClassNotFoundException e) {
			throw new CommonServiceException(e);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		}
	}

}
