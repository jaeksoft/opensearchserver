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
package com.jaeksoft.searchlib.webservice.monitor;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

@WebService
@Path("/monitor/")
public interface Monitor {

	@WebResult(name = "monitor")
	public MonitorResult monitor(
			@QueryParam("login") @WebParam(name = "login") String login,
			@QueryParam("key") @WebParam(name = "key") String key);

	@GET
	@Produces("application/xml")
	@Path("/getMonitorXML/")
	public MonitorResult getMonitorXML(@QueryParam("login") String login,
			@QueryParam("key") String key);

	@GET
	@Produces("application/json")
	@Path("/getMonitorJSON/")
	public MonitorResult getMonitorJSON(@QueryParam("login") String login,
			@QueryParam("key") String key);

}
