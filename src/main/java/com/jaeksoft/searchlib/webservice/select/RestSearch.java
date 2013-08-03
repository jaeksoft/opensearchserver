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

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.collapse.CollapseParameters;

@Path("/index/{index_name}/search/{template_name}")
public interface RestSearch {

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public SelectResult searchPost(@PathParam("index_name") String index,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@PathParam("template_name") String template,
			@FormParam("query") String query,
			@FormParam("start") Integer start, @FormParam("rows") Integer rows,
			@FormParam("lang") LanguageEnum lang,
			@FormParam("operator") OperatorEnum operator,
			@FormParam("collapseField") String collapseField,
			@FormParam("collapseMax") Integer collapseMax,
			@FormParam("collapseMode") CollapseParameters.Mode collapseMode,
			@FormParam("collapseType") CollapseParameters.Type collapseType,
			@FormParam("filter") List<String> filter,
			@FormParam("negativeFilter") List<String> negativeFilter,
			@FormParam("sort") List<String> sort,
			@FormParam("returnedField") List<String> returnedField,
			@FormParam("snippetField") List<String> snippetField,
			@FormParam("facet") List<String> facet,
			@FormParam("facetCollapse") List<String> facetCollapse,
			@FormParam("facetMulti") List<String> facetMulti,
			@FormParam("facetMultiCollapse") List<String> facetMultiCollapse,
			@FormParam("filterParam") List<String> filterParams,
			@FormParam("joinParam") List<String> joinParams,
			@FormParam("enableLog") Boolean enableLog,
			@FormParam("customLog") List<String> customLog);

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public SelectResult search(@PathParam("index_name") String index,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@PathParam("template_name") String template,
			@QueryParam("query") String query,
			@QueryParam("start") Integer start,
			@QueryParam("rows") Integer rows,
			@QueryParam("lang") LanguageEnum lang,
			@QueryParam("operator") OperatorEnum operator,
			@QueryParam("collapseField") String collapseField,
			@QueryParam("collapseMax") Integer collapseMax,
			@QueryParam("collapseMode") CollapseParameters.Mode collapseMode,
			@QueryParam("collapseType") CollapseParameters.Type collapseType,
			@QueryParam("filter") List<String> filter,
			@QueryParam("negativeFilter") List<String> negativeFilter,
			@QueryParam("sort") List<String> sort,
			@QueryParam("returnedField") List<String> returnedField,
			@QueryParam("snippetField") List<String> snippetField,
			@QueryParam("facet") List<String> facet,
			@QueryParam("facetCollapse") List<String> facetCollapse,
			@QueryParam("facetMulti") List<String> facetMulti,
			@QueryParam("facetMultiCollapse") List<String> facetMultiCollapse,
			@QueryParam("filterParam") List<String> filterParams,
			@QueryParam("joinParam") List<String> joinParams,
			@QueryParam("enableLog") Boolean enableLog,
			@QueryParam("customLog") List<String> customLog);

}
