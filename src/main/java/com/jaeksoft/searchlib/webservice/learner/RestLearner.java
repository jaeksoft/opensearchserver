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

package com.jaeksoft.searchlib.webservice.learner;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.learner.LearnerImpl.LearnerMode;

@Path("/index/{index_name}/learner/{learner_name}")
public interface RestLearner {

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public LearnerResult classify(@Context UriInfo uriInfo,
			@PathParam("index_name") String index_name,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@PathParam("learner_name") String learner_name,
			@QueryParam("max_rank") int max_rank,
			@QueryParam("min_score") double min_score,
			@QueryParam("mode") LearnerMode mode,
			@QueryParam("text") String text);

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public LearnerResult classifyPost(@Context UriInfo uriInfo,
			@PathParam("index_name") String index_name,
			@FormParam("login") String login, @FormParam("key") String key,
			@PathParam("learner_name") String learner_name,
			@FormParam("max_rank") int max_rank,
			@FormParam("min_score") double min_score,
			@FormParam("mode") LearnerMode mode, @FormParam("text") String text);

	@PUT
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CommonResult learn(@Context UriInfo uriInfo,
			@PathParam("index_name") String index,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@PathParam("learner_name") String name);

}
