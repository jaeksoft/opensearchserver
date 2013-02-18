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
package com.jaeksoft.searchlib.webservice.update;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.WebServiceException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.schema.FieldValueOriginEnum;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.CommonServices;

public class UpdateImpl extends CommonServices implements SoapUpdate,
		RestUpdate {

	private int updateDocument(Client client, List<Document> updateDocuments)
			throws NoSuchAlgorithmException, IOException, URISyntaxException,
			SearchLibException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		List<IndexDocument> indexDocuments = new ArrayList<IndexDocument>();
		for (Document document : updateDocuments) {
			IndexDocument indexDoc = new IndexDocument(document.lang);
			for (FieldValuePair fieldValue : document.values)
				indexDoc.add(fieldValue.field, new FieldValueItem(
						FieldValueOriginEnum.EXTERNAL, fieldValue.value));
			indexDocuments.add(indexDoc);
		}
		return client.updateDocuments(indexDocuments);
	}

	@Override
	public CommonResult update(String use, String login, String key,
			List<Document> documents) {
		try {
			Client client = getLoggedClient(use, login, key, Role.INDEX_UPDATE);
			ClientFactory.INSTANCE.properties.checkApi();
			int count = updateDocument(client, documents);
			return new CommonResult(true, count + " document(s) updated");
		} catch (SearchLibException e) {
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

	@Override
	public CommonResult updateJSON(String use, String login, String key,
			List<Document> documents) {
		return update(use, login, key, documents);
	}

	@Override
	public CommonResult updateXML(String use, String login, String key,
			List<Document> documents) {
		return update(use, login, key, documents);
	}

}
