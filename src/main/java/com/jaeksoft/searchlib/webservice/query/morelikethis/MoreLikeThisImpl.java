/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.webservice.query.morelikethis;

import java.io.IOException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.request.MoreLikeThisRequest;
import com.jaeksoft.searchlib.request.RequestTypeEnum;
import com.jaeksoft.searchlib.result.ResultMoreLikeThis;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.query.CommonQuery;
import com.jaeksoft.searchlib.webservice.query.QueryTemplateResultList;
import com.jaeksoft.searchlib.webservice.query.document.DocumentsResult;

public class MoreLikeThisImpl extends CommonQuery implements RestMoreLikeThis,
		SoapMoreLikeThis {

	@Override
	public QueryTemplateResultList moreLikeThisTemplateList(String index,
			String login, String key) {
		return super.queryTemplateList(index, login, key,
				RequestTypeEnum.MoreLikeThisRequest);
	}

	@Override
	public MoreLikeThisTemplateResult moreLikeThisTemplateGet(String index,
			String login, String key, String template) {
		MoreLikeThisRequest request = (MoreLikeThisRequest) super
				.queryTemplateGet(index, login, key, template,
						RequestTypeEnum.MoreLikeThisRequest);
		return new MoreLikeThisTemplateResult(request);
	}

	@Override
	public CommonResult moreLikeThisTemplateSet(String index, String login,
			String key, String template, MoreLikeThisQuery query) {
		Client client = getLoggedClient(index, login, key, Role.INDEX_UPDATE);
		MoreLikeThisRequest request = new MoreLikeThisRequest(client);
		return queryTemplateSet(client, index, login, key, template, query,
				request);
	}

	@Override
	public DocumentsResult moreLikeThisTemplate(String index, String login,
			String key, String template, MoreLikeThisQuery query) {
		try {
			MoreLikeThisRequest request = (MoreLikeThisRequest) super
					.queryTemplateGet(index, login, key, template,
							RequestTypeEnum.MoreLikeThisRequest);
			if (query != null)
				query.apply(request);
			return new DocumentsResult(
					(ResultMoreLikeThis) client.request(request));
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public DocumentsResult moreLikeThis(String index, String login, String key,
			MoreLikeThisQuery query) {
		try {
			Client client = getLoggedClientAnyRole(index, login, key,
					Role.GROUP_INDEX);
			ClientFactory.INSTANCE.properties.checkApi();
			MoreLikeThisRequest request = new MoreLikeThisRequest(client);
			if (query != null)
				query.apply(request);
			return new DocumentsResult(
					(ResultMoreLikeThis) client.request(request));
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public CommonResult moreLikeThisTemplateDelete(String index, String login,
			String key, String template) {
		return queryTemplateDelete(index, login, key, template,
				RequestTypeEnum.MoreLikeThisRequest);
	}
}
