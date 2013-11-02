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
package com.jaeksoft.searchlib.webservice.script;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.jaeksoft.searchlib.script.ScriptLine;
import com.jaeksoft.searchlib.webservice.CommonListResult;
import com.jaeksoft.searchlib.webservice.CommonResult;

@Path("/index/{index_name}/script")
public interface RestScript {

	@POST
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/")
	public CommonResult script(@PathParam("index_name") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			List<ScriptLine> scriptLines);

	@GET
	@Path("/")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CommonListResult list(@PathParam("index_name") String index,
			@QueryParam("login") String login, @QueryParam("key") String key);

	@GET
	@Path("/{script_name}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public List<ScriptLine> get(@PathParam("index_name") String index,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@PathParam("script_name") String name);

	@HEAD
	@Path("/{script_name}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CommonResult exists(@PathParam("index_name") String index,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@PathParam("script_name") String name);

	@PUT
	@Path("/{script_name}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CommonResult set(@PathParam("index_name") String index,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@PathParam("script_name") String name, List<ScriptLine> scriptLines);

	@DELETE
	@Path("/{script_name}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CommonResult delete(@PathParam("index_name") String index,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@PathParam("script_name") String name);
}
