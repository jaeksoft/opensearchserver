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

import javax.jws.WebService;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.jaeksoft.searchlib.webservice.CommonResult;

@WebService(name = "Command")
@Path("/command")
public interface RestCommand {

	@GET
	@Produces("application/json")
	@Path("/optimize/{index}/json")
	public CommonResult optimizeJSON(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key);

	@GET
	@Produces("application/xml")
	@Path("/optimize/{index}/xml")
	public CommonResult optimizeXML(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key);

	@GET
	@Produces("application/json")
	@Path("/online/{index}/json")
	public CommonResult onlineJSON(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key);

	@GET
	@Produces("application/xml")
	@Path("/online/{index}/xml")
	public CommonResult onlineXML(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key);

	@GET
	@Produces("application/json")
	@Path("/offline/{index}/json")
	public CommonResult offlineJSON(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key);

	@GET
	@Produces("application/xml")
	@Path("/offline/{index}/xml")
	public CommonResult offlineXML(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key);

	@GET
	@Produces("application/json")
	@Path("/reload/{index}/json")
	public CommonResult reloadJSON(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key);

	@GET
	@Produces("application/xml")
	@Path("/reload/{index}/xml")
	public CommonResult reloadXML(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key);
}
