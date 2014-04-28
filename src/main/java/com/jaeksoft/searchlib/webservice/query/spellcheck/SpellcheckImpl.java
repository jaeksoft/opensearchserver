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

package com.jaeksoft.searchlib.webservice.query.spellcheck;

import java.io.IOException;

import javax.ws.rs.core.UriInfo;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.request.RequestTypeEnum;
import com.jaeksoft.searchlib.request.SpellCheckRequest;
import com.jaeksoft.searchlib.result.ResultSpellCheck;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.query.CommonQuery;
import com.jaeksoft.searchlib.webservice.query.QueryTemplateResultList;

public class SpellcheckImpl extends CommonQuery implements RestSpellCheck {

	@Override
	public QueryTemplateResultList spellcheckTemplateList(UriInfo uriInfo,
			String index, String login, String key) {
		return super.queryTemplateList(uriInfo, index, login, key,
				RequestTypeEnum.SpellCheckRequest);
	}

	@Override
	public SpellcheckResult spellcheck(UriInfo uriInfo, String index,
			String login, String key, String template, LanguageEnum lang,
			String query) {
		try {
			Client client = getLoggedClient(uriInfo, index, login, key,
					Role.INDEX_QUERY);
			ClientFactory.INSTANCE.properties.checkApi();
			SpellCheckRequest spellCheckRequest = (SpellCheckRequest) getRequest(
					client, template, SpellCheckRequest.class);
			if (spellCheckRequest == null)
				throw new CommonServiceException("Request " + template
						+ " not found");
			if (query != null && query.length() > 0)
				spellCheckRequest.setQueryString(query);
			if (lang != null)
				spellCheckRequest.setLang(lang);
			return new SpellcheckResult(
					(ResultSpellCheck) client.request(spellCheckRequest), query);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public SpellcheckResult spellcheckPost(UriInfo uriInfo, String index,
			String login, String key, String template, LanguageEnum lang,
			String query) {
		return spellcheck(uriInfo, index, login, key, template, lang, query);
	}

	@Override
	public CommonResult spellcheckTemplateDelete(UriInfo uriInfo, String index,
			String login, String key, String template) {
		return queryTemplateDelete(uriInfo, index, login, key, template,
				RequestTypeEnum.SpellCheckRequest);
	}
}
