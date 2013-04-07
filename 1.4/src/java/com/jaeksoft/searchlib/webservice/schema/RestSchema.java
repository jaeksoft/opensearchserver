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
package com.jaeksoft.searchlib.webservice.schema;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.jaeksoft.searchlib.webservice.CommonResult;

@Path("/schema")
public interface RestSchema {

	@POST
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	@Path("/{index}/field/set/xml")
	public CommonResult setFieldXML(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			SchemaFieldRecord schemaFieldRecord);

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{index}/field/set/xml")
	public CommonResult setFieldJSON(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			SchemaFieldRecord schemaFieldRecord);

	@DELETE
	@Produces(MediaType.APPLICATION_XML)
	@Path("/{index}/field/delete/xml")
	public CommonResult deleteFieldXML(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("field") String field);

	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{index}/field/delete/json")
	public CommonResult deleteFieldJSON(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("field") String field);

	@POST
	@Produces(MediaType.APPLICATION_XML)
	@Path("/{index}/field/setdefault/xml")
	public CommonResult setDefaultFieldXML(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("field") String field);

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{index}/field/setdefault/json")
	public CommonResult setDefaultFieldJSON(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("field") String field);

	@POST
	@Produces(MediaType.APPLICATION_XML)
	@Path("/{index}/field/setunique/xml")
	public CommonResult setUniqueFieldXML(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("field") String field);

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{index}/field/setunique/json")
	public CommonResult setUniqueFieldJSON(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("field") String field);

	@GET
	@Produces(MediaType.APPLICATION_XML)
	@Path("/{index}/field/list/xml")
	public ResultFieldList getFieldListXML(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key);

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{index}/field/list/json")
	public ResultFieldList getFieldListJSON(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key);
}
