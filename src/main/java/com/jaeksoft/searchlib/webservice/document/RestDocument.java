/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2014 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.webservice.document;

import java.io.InputStream;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import com.jaeksoft.searchlib.webservice.CommonResult;

@Path("/index/{index_name}/document")
public interface RestDocument {

	@PUT
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/")
	public CommonResult update(@Context UriInfo uriInfo,
			@PathParam("index_name") String index,
			@QueryParam("login") String login, @QueryParam("key") String key,
			List<DocumentUpdate> documents);

	@PUT
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/")
	public CommonResult update(@Context UriInfo uriInfo,
			@PathParam("index_name") String index,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("pattern") String pattern,
			@QueryParam("field") List<String> fields,
			@QueryParam("langpos") Integer langPosition,
			@QueryParam("charset") String charset,
			@QueryParam("buffersize") Integer bufferSize,
			InputStream inputStream);

	@DELETE
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/{field_name}/{values:.+}")
	public CommonResult deleteByValue(@Context UriInfo uriInfo,
			@PathParam("index_name") String index,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@PathParam("field_name") String field,
			@PathParam("values") String values);

	@DELETE
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/{field_name}/")
	public CommonResult deleteByValue(@Context UriInfo uriInfo,
			@PathParam("index_name") String index,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@PathParam("field_name") String field, List<String> values);

	@DELETE
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/")
	public CommonResult deleteByQuery(@Context UriInfo uriInfo,
			@PathParam("index_name") String index,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("query") String query);

}
