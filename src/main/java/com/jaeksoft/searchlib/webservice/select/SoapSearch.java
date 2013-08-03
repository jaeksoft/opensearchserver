/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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

import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebService;

import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.collapse.CollapseParameters;

@WebService(name = "Search")
public interface SoapSearch {

	public SelectResult search(
			@WebParam(name = "index") String index,
			@WebParam(name = "login") String login,
			@WebParam(name = "key") String key,
			@WebParam(name = "template") String template,
			@WebParam(name = "query") String query,
			@WebParam(name = "start") Integer start,
			@WebParam(name = "rows") Integer rows,
			@WebParam(name = "lang") LanguageEnum lang,
			@WebParam(name = "operator") OperatorEnum operator,
			@WebParam(name = "collapseField") String collapseField,
			@WebParam(name = "collapseMax") Integer collapseMax,
			@WebParam(name = "collapseMode") CollapseParameters.Mode collapseMode,
			@WebParam(name = "collapseType") CollapseParameters.Type collapseType,
			@WebParam(name = "filter") List<String> filter,
			@WebParam(name = "negativeFilter") List<String> negativeFilter,
			@WebParam(name = "sort") List<String> sort,
			@WebParam(name = "returnedField") List<String> returnedField,
			@WebParam(name = "snippetField") List<String> snippetField,
			@WebParam(name = "facet") List<String> facet,
			@WebParam(name = "facetCollapse") List<String> facetCollapse,
			@WebParam(name = "facetMulti") List<String> facetMulti,
			@WebParam(name = "facetMultiCollapse") List<String> facetMultiCollapse,
			@WebParam(name = "filterParam") List<String> filterParams,
			@WebParam(name = "joinParam") List<String> joinParams,
			@WebParam(name = "enableLog") Boolean enableLog,
			@WebParam(name = "customLog") List<String> customLog);
}
