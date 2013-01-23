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

package com.jaeksoft.searchlib.webservice.action;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.jaeksoft.searchlib.webservice.CommonResult;

@WebService
@Path("/action")
public interface Action {

	@WebResult(name = "action")
	public CommonResult action(@WebParam(name = "use") String use,
			@WebParam(name = "login") String login,
			@WebParam(name = "key") String key,
			@WebParam(name = "optimize") Boolean optimize,
			@WebParam(name = "reload") Boolean reload,
			@WebParam(name = "online") Boolean online,
			@WebParam(name = "offline") Boolean offline,
			@WebParam(name = "readonly") Boolean readonly,
			@WebParam(name = "readwrite") Boolean readwrite);

	@GET
	@Produces("application/xml")
	@Path("/{index}/xml")
	public CommonResult actionXML(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("command") ActionEnum action);

	@GET
	@Produces("application/json")
	@Path("/{index}/json")
	public CommonResult actionJSON(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("command") ActionEnum action);
}
