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

import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.collapse.CollapseParameters;

@WebService
public interface Select extends CommonServices {

	@WebResult(name = "search")
	public SelectResult search(
			@WebParam(name = "q") String q,
			@WebParam(name = "qt") String qt,
			@WebParam(name = "use") String use,
			@WebParam(name = "login") String login,
			@WebParam(name = "key") String key,
			@WebParam(name = "start") int start,
			@WebParam(name = "rows") int rows,
			@WebParam(name = "lang") LanguageEnum lang,
			@WebParam(name = "collapseField") String collapseField,
			@WebParam(name = "collapseMax") int collapseMax,
			@WebParam(name = "collapseMode") CollapseParameters.Mode collapseMode,
			@WebParam(name = "collapseType") CollapseParameters.Type collapseType,
			@WebParam(name = "fq") List<String> filterQuery,
			@WebParam(name = "fqn") List<String> filterQueryNegetive,
			@WebParam(name = "sort") List<String> sort,
			@WebParam(name = "rf") List<String> returnField,
			@WebParam(name = "withDocs") boolean withDocs,
			@WebParam(name = "hl") List<String> highlight,
			@WebParam(name = "facet") List<String> facet,
			@WebParam(name = "facetcollapse") List<String> facetCollapse,
			@WebParam(name = "facetmulti") List<String> facetMulti,
			@WebParam(name = "facetmulticollapse") List<String> facetMultiCollapse,
			@WebParam(name = "mlt") boolean moreLikeThis,
			@WebParam(name = "mltdocquery") String mltDocQuery,
			@WebParam(name = "mltminwordlen") int mltMinWordLen,
			@WebParam(name = "mltmaxwordlen") int mltMaxwordlen,
			@WebParam(name = "mltmindocfreq") int mltMinDocFeq,
			@WebParam(name = "mltmintermfreq") int mltMinTermFreq,
			@WebParam(name = "mltstopwords") String mltStopWords,
			@WebParam(name = "customlog") List<String> customLogs,
			@WebParam(name = "log") boolean log,
			@WebParam(name = "delete") boolean delete);

}
