/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013-2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.webservice.query.document;

import java.io.IOException;

import javax.ws.rs.core.UriInfo;

import org.apache.commons.collections.CollectionUtils;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.request.DocumentsRequest;
import com.jaeksoft.searchlib.request.RequestTypeEnum;
import com.jaeksoft.searchlib.result.ResultDocuments;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.query.CommonQuery;
import com.jaeksoft.searchlib.webservice.query.QueryTemplateResultList;

public class DocumentsImpl extends CommonQuery implements RestDocuments {

	@Override
	public QueryTemplateResultList documentsTemplateList(UriInfo uriInfo,
			String index, String login, String key) {
		return super.queryTemplateList(uriInfo, index, login, key,
				RequestTypeEnum.DocumentsRequest);
	}

	@Override
	public DocumentsTemplateResult documentsTemplateGet(UriInfo uriInfo,
			String index, String login, String key, String template) {
		DocumentsRequest request = (DocumentsRequest) super.queryTemplateGet(
				uriInfo, index, login, key, template,
				RequestTypeEnum.DocumentsRequest);
		return new DocumentsTemplateResult(request);
	}

	@Override
	public DocumentsResult documentsTemplate(UriInfo uriInfo, String index,
			String login, String key, String template, DocumentsQuery query) {
		try {
			DocumentsRequest request = (DocumentsRequest) super
					.queryTemplateGet(uriInfo, index, login, key, template,
							RequestTypeEnum.DocumentsRequest);
			if (query != null)
				query.apply(request);
			return new DocumentsResult(
					(ResultDocuments) client.request(request), false);
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public CommonResult documentsTemplateSet(UriInfo uriInfo, String index,
			String login, String key, String template, DocumentsQuery query) {
		Client client = getLoggedClient(uriInfo, index, login, key,
				Role.INDEX_UPDATE);
		DocumentsRequest request = new DocumentsRequest(client);
		return queryTemplateSet(client, index, login, key, template, query,
				request);
	}

	@Override
	public DocumentsResult documentsSearch(UriInfo uriInfo, String index,
			String login, String key, DocumentsQuery query) {
		try {
			Client client = getLoggedClientAnyRole(uriInfo, index, login, key,
					Role.GROUP_INDEX);
			ClientFactory.INSTANCE.properties.checkApi();
			DocumentsRequest request = new DocumentsRequest(client);
			if (query != null)
				query.apply(request);
			return new DocumentsResult(
					(ResultDocuments) client.request(request),
					CollectionUtils.isEmpty(query.returnedFields));
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public CommonResult documentsTemplateDelete(UriInfo uriInfo, String index,
			String login, String key, String template) {
		return queryTemplateDelete(uriInfo, index, login, key, template,
				RequestTypeEnum.DocumentsRequest);
	}

}
