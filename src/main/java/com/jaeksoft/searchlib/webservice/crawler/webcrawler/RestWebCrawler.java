/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2014 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.webservice.crawler.webcrawler;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.jaeksoft.searchlib.webservice.CommonListResult;
import com.jaeksoft.searchlib.webservice.CommonResult;

@Path("/index/{index_name}/crawler/web")
public interface RestWebCrawler {

	@PUT
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/run")
	public CommonResult run(@PathParam("index_name") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("once") boolean once);

	@DELETE
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/run")
	public CommonResult stop(@PathParam("index_name") String use,
			@QueryParam("login") String login, @QueryParam("key") String key);

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/run")
	public CommonResult status(@PathParam("index_name") String use,
			@QueryParam("login") String login, @QueryParam("key") String key);

	@PUT
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/patterns/inclusion")
	public CommonResult injectPatternsInclusion(
			@PathParam("index_name") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("replace") boolean replaceAll, List<String> injectList);

	@DELETE
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/patterns/inclusion")
	public CommonResult deletePatternsInclusion(
			@PathParam("index_name") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			List<String> deleteList);

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/patterns/inclusion")
	public CommonListResult<String> extractPatternsInclusion(
			@PathParam("index_name") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("starts_with") String startsWith);

	@PUT
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/patterns/exclusion")
	public CommonResult injectPatternsExclusion(
			@PathParam("index_name") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("replace") boolean replaceAll, List<String> injectList);

	@DELETE
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/patterns/exclusion")
	public CommonResult deletePatternsExclusion(
			@PathParam("index_name") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			List<String> deleteList);

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/patterns/exclusion")
	public CommonListResult<String> extractPatternsExclusion(
			@PathParam("index_name") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("starts_with") String startsWith);

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/crawl")
	public CommonResult crawl(@PathParam("index_name") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("url") String url,
			@QueryParam("returnData") Boolean returnData);

	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/crawl")
	public CommonResult crawlPost(@PathParam("index_name") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@FormParam("url") String url,
			@FormParam("returnData") Boolean returnData);

	@PUT
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/urls")
	public CommonResult injectUrls(@PathParam("index_name") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("replace") boolean replaceAll, List<String> urls);

}
