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

package com.jaeksoft.searchlib.webservice.select;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.collapse.CollapseParameters;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.user.Role;

public class SearchImpl extends CommonSelect implements RestSearch, SoapSearch {

	@Override
	public SelectResult search(String index, String login, String key,
			String template, String query, Integer start, Integer rows,
			LanguageEnum lang, OperatorEnum operator, String collapseField,
			Integer collapseMax, CollapseParameters.Mode collapseMode,
			CollapseParameters.Type collapseType, List<String> filter,
			List<String> negativeFilter, List<String> sort,
			List<String> returnedField, List<String> snippetField,
			List<String> facet, List<String> facetCollapse,
			List<String> facetMulti, List<String> facetMultiCollapse,
			List<String> filterParams, List<String> joinParams,
			Boolean enableLog, List<String> customLog) {
		try {
			Client client = getLoggedClient(index, login, key, Role.INDEX_QUERY);
			ClientFactory.INSTANCE.properties.checkApi();
			AbstractSearchRequest searchRequest = getSearchRequest(client,
					template, query, start, rows, lang, operator,
					collapseField, collapseMax, collapseMode, collapseType,
					filter, negativeFilter, sort, returnedField, snippetField,
					facet, facetCollapse, facetMulti, facetMultiCollapse,
					filterParams, joinParams, enableLog, customLog);
			return new SelectResult(
					(AbstractResultSearch) client.request(searchRequest));
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		} catch (SyntaxError e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		} catch (InstantiationException e) {
			throw new CommonServiceException(e);
		} catch (IllegalAccessException e) {
			throw new CommonServiceException(e);
		} catch (ClassNotFoundException e) {
			throw new CommonServiceException(e);
		} catch (ParseException e) {
			throw new CommonServiceException(e);
		} catch (URISyntaxException e) {
			throw new CommonServiceException(e);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public SelectResult searchPost(String index, String login, String key,
			String template, String query, Integer start, Integer rows,
			LanguageEnum lang, OperatorEnum operator, String collapseField,
			Integer collapseMax, CollapseParameters.Mode collapseMode,
			CollapseParameters.Type collapseType, List<String> filter,
			List<String> negativeFilter, List<String> sort,
			List<String> returnedField, List<String> snippetField,
			List<String> facet, List<String> facetCollapse,
			List<String> facetMulti, List<String> facetMultiCollapse,
			List<String> filterParams, List<String> joinParams,
			Boolean enableLog, List<String> customLog) {
		return search(index, login, key, template, query, start, rows, lang,
				operator, collapseField, collapseMax, collapseMode,
				collapseType, filter, negativeFilter, sort, returnedField,
				snippetField, facet, facetCollapse, facetMulti,
				facetMultiCollapse, filterParams, joinParams, enableLog,
				customLog);

	}
}
