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

package com.jaeksoft.searchlib.webservice.query.namedEntity;

import java.io.IOException;

import javax.ws.rs.core.UriInfo;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.request.NamedEntityExtractionRequest;
import com.jaeksoft.searchlib.request.RequestTypeEnum;
import com.jaeksoft.searchlib.result.ResultNamedEntityExtraction;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.query.CommonQuery;
import com.jaeksoft.searchlib.webservice.query.QueryTemplateResultList;

public class NamedEntityImpl extends CommonQuery implements RestNamedEntity {

	@Override
	public QueryTemplateResultList namedEntityTemplateList(UriInfo uriInfo,
			String index, String login, String key) {
		return super.queryTemplateList(uriInfo, index, login, key,
				RequestTypeEnum.NamedEntityExtractionRequest);
	}

	@Override
	public NamedEntityTemplateResult namedEntityTemplateGet(UriInfo uriInfo,
			String index, String login, String key, String template) {
		NamedEntityExtractionRequest request = (NamedEntityExtractionRequest) super
				.queryTemplateGet(uriInfo, index, login, key, template,
						RequestTypeEnum.NamedEntityExtractionRequest);
		return new NamedEntityTemplateResult(request);
	}

	@Override
	public NamedEntityResult namedEntityTemplate(UriInfo uriInfo, String index,
			String login, String key, String template, NamedEntityQuery query) {
		try {
			NamedEntityExtractionRequest request = (NamedEntityExtractionRequest) super
					.queryTemplateGet(uriInfo, index, login, key, template,
							RequestTypeEnum.NamedEntityExtractionRequest);
			if (query != null)
				query.apply(request);
			return new NamedEntityResult(
					(ResultNamedEntityExtraction) client.request(request));
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public NamedEntityResult namedEntityTemplate(UriInfo uriInfo, String index,
			String login, String key, String template, String text) {
		try {
			NamedEntityExtractionRequest request = (NamedEntityExtractionRequest) super
					.queryTemplateGet(uriInfo, index, login, key, template,
							RequestTypeEnum.NamedEntityExtractionRequest);
			if (text != null)
				request.setText(text);
			return new NamedEntityResult(
					(ResultNamedEntityExtraction) client.request(request));
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public CommonResult namedEntityTemplateSet(UriInfo uriInfo, String index,
			String login, String key, String template, NamedEntityQuery query) {
		Client client = getLoggedClient(uriInfo, index, login, key,
				Role.INDEX_UPDATE);
		NamedEntityExtractionRequest request = new NamedEntityExtractionRequest(
				client);
		return queryTemplateSet(client, index, login, key, template, query,
				request);
	}

	@Override
	public NamedEntityResult namedEntitySearch(UriInfo uriInfo, String index,
			String login, String key, NamedEntityQuery query) {
		try {
			Client client = getLoggedClientAnyRole(uriInfo, index, login, key,
					Role.GROUP_INDEX);
			ClientFactory.INSTANCE.properties.checkApi();
			NamedEntityExtractionRequest request = new NamedEntityExtractionRequest(
					client);
			if (query != null)
				query.apply(request);
			return new NamedEntityResult(
					(ResultNamedEntityExtraction) client.request(request));
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public CommonResult namedEntityTemplateDelete(UriInfo uriInfo,
			String index, String login, String key, String template) {
		return queryTemplateDelete(uriInfo, index, login, key, template,
				RequestTypeEnum.NamedEntityExtractionRequest);
	}

}
