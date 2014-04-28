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
package com.jaeksoft.searchlib.webservice.index;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import com.jaeksoft.searchlib.template.TemplateList;
import com.jaeksoft.searchlib.webservice.CommonResult;

@Path("/index")
public interface RestIndex {

	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/{index_name}/template/{template}")
	public CommonResult createIndex(@Context UriInfo uriInfo,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@PathParam("index_name") String name,
			@PathParam("template") TemplateList template,
			@QueryParam("remote_uri") String remoteURI);

	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/{index_name}")
	public CommonResult createIndex(@Context UriInfo uriInfo,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@PathParam("index_name") String name,
			@QueryParam("remote_uri") String remoteURI);

	@DELETE
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/{index_name}")
	public CommonResult deleteIndex(@Context UriInfo uriInfo,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@PathParam("index_name") String name);

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/")
	public ResultIndexList indexList(@Context UriInfo uriInfo,
			@QueryParam("login") String login, @QueryParam("key") String key);

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/{index_name}")
	public CommonResult indexExists(@Context UriInfo uriInfo,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@PathParam("index_name") String name);

}
