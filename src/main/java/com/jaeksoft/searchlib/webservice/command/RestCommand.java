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

package com.jaeksoft.searchlib.webservice.command;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.jaeksoft.searchlib.webservice.CommonResult;

@Path("/command")
public interface RestCommand {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/optimize/{index}/json")
	public CommonResult optimizeJSON(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key);

	@GET
	@Produces(MediaType.APPLICATION_XML)
	@Path("/optimize/{index}/xml")
	public CommonResult optimizeXML(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key);

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/online/{index}/json")
	public CommonResult onlineJSON(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key);

	@GET
	@Produces(MediaType.APPLICATION_XML)
	@Path("/online/{index}/xml")
	public CommonResult onlineXML(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key);

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/offline/{index}/json")
	public CommonResult offlineJSON(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key);

	@GET
	@Produces(MediaType.APPLICATION_XML)
	@Path("/offline/{index}/xml")
	public CommonResult offlineXML(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key);

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/reload/{index}/json")
	public CommonResult reloadJSON(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key);

	@GET
	@Produces(MediaType.APPLICATION_XML)
	@Path("/reload/{index}/xml")
	public CommonResult reloadXML(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key);

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/truncate/{index}/json")
	public CommonResult truncateJSON(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key);

	@GET
	@Produces(MediaType.APPLICATION_XML)
	@Path("/truncate/{index}/xml")
	public CommonResult truncateXML(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key);

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/merge/{index}/json")
	public CommonResult mergeJSON(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("index") String index);

	@GET
	@Produces(MediaType.APPLICATION_XML)
	@Path("/merge/{index}/xml")
	public CommonResult mergeXML(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("index") String index);

}
