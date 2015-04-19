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

package com.jaeksoft.searchlib.webservice.query.spellcheck;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.query.QueryTemplateResultList;

@Path("/index/{index_name}/spellcheck")
public interface RestSpellCheck {

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/")
	public QueryTemplateResultList spellcheckTemplateList(
			@PathParam("index_name") String index,
			@QueryParam("login") String login, @QueryParam("key") String key);

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/{template_name}")
	public SpellcheckResult spellcheck(@PathParam("index_name") String index,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@PathParam("template_name") String template,
			@QueryParam("language") LanguageEnum lang,
			@QueryParam("query") String query);

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/{template_name}")
	public SpellcheckResult spellcheckPost(
			@PathParam("index_name") String index,
			@FormParam("login") String login, @FormParam("key") String key,
			@PathParam("template_name") String template,
			@FormParam("language") LanguageEnum lang,
			@FormParam("query") String query);

	@DELETE
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/{template_name}")
	public CommonResult spellcheckTemplateDelete(
			@PathParam("index_name") String index,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@PathParam("template_name") String template);

}
