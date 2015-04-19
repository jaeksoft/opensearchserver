/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2015 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.webservice.replication;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.jaeksoft.searchlib.webservice.CommonResult;

@Path("/index/{index_name}/replication")
public interface RestReplication {

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/")
	public CommonResult replicationGet(@QueryParam("login") String login,
			@QueryParam("key") String key,
			@PathParam("index_name") String index_name,
			@QueryParam("name") String replication_name);

	@PUT
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/")
	public CommonResult replicationSet(@QueryParam("login") String login,
			@QueryParam("key") String key,
			@PathParam("index_name") String index_name,
			ReplicationResult replication);

	@PUT
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/run")
	public CommonResult replicationRun(@QueryParam("login") String login,
			@QueryParam("key") String key,
			@PathParam("index_name") String index_name,
			@QueryParam("name") String replication_name);

	@DELETE
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/")
	public CommonResult replicationDelete(@QueryParam("login") String login,
			@QueryParam("key") String key,
			@PathParam("index_name") String index_name,
			@QueryParam("name") String replication_name);

}
