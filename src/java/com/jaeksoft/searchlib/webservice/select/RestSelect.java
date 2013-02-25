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

package com.jaeksoft.searchlib.webservice.select;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.collapse.CollapseParameters;

@Path("/select")
public interface RestSelect {

	@GET
	@Produces(MediaType.APPLICATION_XML)
	@Path("/search/{index}/xml")
	public SelectResult searchXML(@PathParam("index") String index,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("template") String template,
			@QueryParam("query") String query,
			@QueryParam("start") Integer start,
			@QueryParam("rows") Integer rows,
			@QueryParam("lang") LanguageEnum lang,
			@QueryParam("sort") List<String> sort,
			@QueryParam("filter") List<String> filter);

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/search/{index}/json")
	public SelectResult searchJSON(@PathParam("index") String index,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("template") String template,
			@QueryParam("query") String query,
			@QueryParam("start") Integer start,
			@QueryParam("rows") Integer rows,
			@QueryParam("lang") LanguageEnum lang,
			@QueryParam("sort") List<String> sort,
			@QueryParam("filter") List<String> filter);

	@GET
	@Produces(MediaType.APPLICATION_XML)
	@Path("/searchlog/{index}/xml")
	public SelectResult searchAndLogXML(@PathParam("index") String index,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("template") String template,
			@QueryParam("query") String query,
			@QueryParam("start") Integer start,
			@QueryParam("rows") Integer rows,
			@QueryParam("lang") LanguageEnum lang,
			@QueryParam("sort") List<String> sort,
			@QueryParam("filter") List<String> filter,
			@QueryParam("enableLog") Boolean enableLog,
			@QueryParam("customLog") List<String> customLog);

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/searchlog/{index}/json")
	public SelectResult searchAndLogJSON(@PathParam("index") String index,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("template") String template,
			@QueryParam("query") String query,
			@QueryParam("start") Integer start,
			@QueryParam("rows") Integer rows,
			@QueryParam("lang") LanguageEnum lang,
			@QueryParam("sort") List<String> sort,
			@QueryParam("filter") List<String> filter,
			@QueryParam("enableLog") Boolean enableLog,
			@QueryParam("customLog") List<String> customLog);

	@GET
	@Produces(MediaType.APPLICATION_XML)
	@Path("/fullsearch/{index}/xml")
	public SelectResult fullSearchXML(@PathParam("index") String index,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("template") String template,
			@QueryParam("query") String query,
			@QueryParam("start") Integer start,
			@QueryParam("rows") Integer rows,
			@QueryParam("lang") LanguageEnum lang,
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
			@QueryParam("enableLog") Boolean enableLog,
			@QueryParam("customLog") List<String> customLog);

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/fullsearch/{index}/json")
	public SelectResult fullSearchJSON(@PathParam("index") String index,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("template") String template,
			@QueryParam("query") String query,
			@QueryParam("start") Integer start,
			@QueryParam("rows") Integer rows,
			@QueryParam("lang") LanguageEnum lang,
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
			@QueryParam("enableLog") Boolean enableLog,
			@QueryParam("customLog") List<String> customLog);
}
