/*
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2017 Emmanuel Keller / Jaeksoft
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
 */
package com.jaeksoft.searchlib.webservice.document;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.request.DocumentsRequest;
import com.jaeksoft.searchlib.request.SearchPatternRequest;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.schema.SchemaFieldList;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.CommonServices;
import com.jaeksoft.searchlib.webservice.query.document.IndexDocumentResult;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.core.Response.Status;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DocumentImpl extends CommonServices implements RestDocument {

	public final static String UPDATED_COUNT = "updatedCount";
	public final static String DELETED_COUNT = "deletedCount";

	@Override
	public CommonResult deleteByQuery(String index, String login, String key, String query, String template) {
		try {
			boolean bQuery = !StringUtils.isEmpty(query);
			boolean bSearchTemplate = !StringUtils.isEmpty(template);
			if (!bQuery && !bSearchTemplate)
				throw new CommonServiceException("Missing parameter: query or template");
			Client client = getLoggedClient(index, login, key, Role.INDEX_UPDATE);
			ClientFactory.INSTANCE.properties.checkApi();
			AbstractRequest request = bSearchTemplate ? client.getNewRequest(template) : new SearchPatternRequest(
					client);
			if (bQuery && request instanceof AbstractSearchRequest)
				((AbstractSearchRequest) request).setQueryString(query);
			long count = client.deleteDocuments(request);
			return new CommonResult(true, count + " document(s) deleted").addDetail(DELETED_COUNT, count);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public CommonResult deleteByValue(String index, String login, String key, String field, List<String> values) {
		try {
			Client client = getLoggedClient(index, login, key, Role.INDEX_UPDATE);
			ClientFactory.INSTANCE.properties.checkApi();
			SchemaFieldList schemaFieldList = client.getSchema().getFieldList();
			SchemaField schemaField = field != null ? schemaFieldList.get(field) : schemaFieldList.getUniqueField();
			if (schemaField == null)
				throw new CommonServiceException(Status.NOT_FOUND, "Field not found: " + field);
			int count = 0;
			if (!CollectionUtils.isEmpty(values))
				count = client.deleteDocuments(new DocumentsRequest(client, schemaField.getName(), values, false));
			return new CommonResult(true, count + " document(s) deleted by " + schemaField.getName()).addDetail(
					DELETED_COUNT, count);
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public CommonResult deleteByValue(String index, String login, String key, String field, String values) {
		String[] valueArray = StringUtils.split(values, '/');
		return deleteByValue(index, login, key, field, Arrays.asList(valueArray));
	}

	private int updateDocument(Client client, List<DocumentUpdate> documents)
			throws NoSuchAlgorithmException, IOException, URISyntaxException, SearchLibException,
			InstantiationException, IllegalAccessException, ClassNotFoundException {
		if (documents == null || documents.size() == 0)
			throw new CommonServiceException(Status.NO_CONTENT, "No documents");
		List<IndexDocument> indexDocuments = new ArrayList<IndexDocument>(0);
		for (DocumentUpdate document : documents) {
			IndexDocument indexDocument = DocumentUpdate.getIndexDocument(document);
			if (indexDocument != null)
				indexDocuments.add(indexDocument);
		}
		return client.updateDocuments(indexDocuments);
	}

	@Override
	public CommonResult update(String index, String login, String key, List<DocumentUpdate> documents) {
		try {
			Client client = getLoggedClient(index, login, key, Role.INDEX_UPDATE);
			ClientFactory.INSTANCE.properties.checkApi();
			int count = updateDocument(client, documents);
			return new CommonResult(true, count + " document(s) updated").addDetail(UPDATED_COUNT, count);
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

	@Override
	public CommonResult updateRaw(String index, String login, String key, List<IndexDocumentResult> indexDocuments) {
		try {
			Client client = getLoggedClient(index, login, key, Role.INDEX_UPDATE);
			ClientFactory.INSTANCE.properties.checkApi();
			if (indexDocuments == null || indexDocuments.size() == 0)
				throw new CommonServiceException(Status.NO_CONTENT, "No documents");
			int count = client.updateIndexDocuments(indexDocuments);
			return new CommonResult(true, count + " document(s) updated").addDetail(UPDATED_COUNT, count);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public CommonResult update(String index, String login, String key, String capturePattern, List<String> fields,
			Integer langPosition, String charset, Integer bufferSize, InputStream inputStream) {
		try {
			Client client = getLoggedClient(index, login, key, Role.INDEX_UPDATE);
			ClientFactory.INSTANCE.properties.checkApi();
			CommonResult result = new CommonResult(true, null);
			StreamSource streamSource = new StreamSource(inputStream);
			int count = client.updateTextDocuments(streamSource, charset, bufferSize, capturePattern, langPosition,
					fields, result);
			return result.addDetail(UPDATED_COUNT, count);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new CommonServiceException(e);
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		} catch (URISyntaxException e) {
			throw new CommonServiceException(e);
		} catch (InstantiationException e) {
			throw new CommonServiceException(e);
		} catch (IllegalAccessException e) {
			throw new CommonServiceException(e);
		} catch (ClassNotFoundException e) {
			throw new CommonServiceException(e);
		}
	}

}
