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

package com.jaeksoft.searchlib.webservice.delete;

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.jaeksoft.searchlib.webservice.CommonResult;

@Path("/delete")
public interface RestDelete {

	@DELETE
	@Produces(MediaType.APPLICATION_XML)
	@Path("/delete/value/{index}/xml")
	public CommonResult deleteByValueXML(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("field") String field,
			@QueryParam("value") List<String> values);

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/delete/value/{index}/json")
	public CommonResult deleteByValueJSON(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("field") String field,
			@QueryParam("value") List<String> values);

	@POST
	@Produces(MediaType.APPLICATION_XML)
	@Path("/delete/query/{index}/xml")
	public CommonResult deleteByQueryXML(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("query") String query);

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/delete/query/{index}/json")
	public CommonResult deleteByQueryJSON(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("query") String query);
}
