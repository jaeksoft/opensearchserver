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

package com.jaeksoft.searchlib.webservice.learner;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.jaeksoft.searchlib.webservice.CommonResult;

@Path("/learner")
public interface RestLearner {

	@GET
	@Produces(MediaType.APPLICATION_XML)
	@Path("/classify/{index}/xml")
	public LearnerResult classifyXML(@PathParam("index") String index,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("name") String name, @QueryParam("text") String text);

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/classify/{index}/json")
	public LearnerResult classifyJSON(@PathParam("index") String index,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("name") String name, @QueryParam("text") String text);

	@DELETE
	@Produces(MediaType.APPLICATION_XML)
	@Path("/reset/{index}/xml")
	public CommonResult resetXML(@PathParam("index") String index,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("name") String name);

	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/reset/{index}/json")
	public CommonResult resetJSON(@PathParam("index") String index,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("name") String name);

	@GET
	@Produces(MediaType.APPLICATION_XML)
	@Path("/learn/{index}/xml")
	public CommonResult learnXML(@PathParam("index") String index,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("name") String name);

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/learn/{index}/json")
	public CommonResult learnJSON(@PathParam("index") String index,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("name") String name);
}
