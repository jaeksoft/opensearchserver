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
package com.jaeksoft.searchlib.webservice.crawler.webcrawler;

import java.net.URL;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.jaeksoft.searchlib.webservice.CommonResult;

@Path("/crawler/web")
public interface RestWebCrawler {

	@GET
	@Produces(MediaType.APPLICATION_XML)
	@Path("/run/once/{index}/xml")
	public CommonResult runOnceXML(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key);

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/run/once/{index}/json")
	public CommonResult runOnceJSON(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key);

	@GET
	@Produces(MediaType.APPLICATION_XML)
	@Path("/run/forever/{index}/xml")
	public CommonResult runForeverXML(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key);

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/run/forever/{index}/json")
	public CommonResult runForeverJSON(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key);

	@GET
	@Produces(MediaType.APPLICATION_XML)
	@Path("/stop/{index}/xml")
	public CommonResult stopXML(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key);

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/stop/{index}/json")
	public CommonResult stopJSON(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key);

	@GET
	@Produces(MediaType.APPLICATION_XML)
	@Path("/status/{index}/xml")
	public CommonResult statusXML(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key);

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/status/{index}/json")
	public CommonResult statusJSON(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key);

	@POST
	@Produces(MediaType.APPLICATION_XML)
	@Path("/patterns/inclusion/inject/{index}/xml")
	public CommonResult injectPatternsInclusionXML(
			@PathParam("index") String use, @QueryParam("login") String login,
			@QueryParam("key") String key,
			@QueryParam("deleteall") Boolean deleteAll,
			@QueryParam("inject") List<String> injectList);

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/patterns/inclusion/inject/{index}/json")
	public CommonResult injectPatternsInclusionJSON(
			@PathParam("index") String use, @QueryParam("login") String login,
			@QueryParam("key") String key,
			@QueryParam("deleteall") Boolean deleteAll,
			@QueryParam("inject") List<String> injectList);

	@POST
	@Produces(MediaType.APPLICATION_XML)
	@Path("/patterns/exclusion/inject/{index}/xml")
	public CommonResult injectPatternsExclusionXML(
			@PathParam("index") String use, @QueryParam("login") String login,
			@QueryParam("key") String key,
			@QueryParam("deleteall") Boolean deleteAll,
			@QueryParam("inject") List<String> injectList);

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/patterns/exclusion/inject/{index}/json")
	public CommonResult injectPatternsExclusionJSON(
			@PathParam("index") String use, @QueryParam("login") String login,
			@QueryParam("key") String key,
			@QueryParam("deleteall") Boolean deleteAll,
			@QueryParam("inject") List<String> injectList);

	@GET
	@Produces("image/png")
	@Path("/screenshot")
	public byte[] captureScreenshotAPI(@QueryParam("login") String login,
			@QueryParam("key") String key, @QueryParam("url") URL url,
			@QueryParam("width") Integer browserWidth,
			@QueryParam("height") Integer browserHeight,
			@QueryParam("reduction") Integer reductionPercent,
			@QueryParam("visible") Boolean visiblePartOnly,
			@QueryParam("wait") Integer wait);

	@POST
	@Produces(MediaType.APPLICATION_XML)
	@Path("/screenshot/capture/{index}/xml")
	public CommonResult captureScreenshotXML(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("url") URL url);

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/screenshot/capture/{index}/json")
	public CommonResult captureScreenshotJSON(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("url") URL url);

	@GET
	@Produces(MediaType.APPLICATION_XML)
	@Path("/screenshot/check/{index}/xml")
	public CommonResult checkScreenshotXML(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("url") URL url);

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/screenshot/check/{index}/json")
	public CommonResult checkScreenshotJSON(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("url") URL url);

	@GET
	@Produces(MediaType.APPLICATION_XML)
	@Path("/crawl/{index}/xml")
	public CommonResult crawlXML(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("url") URL url);

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/crawl/{index}/json")
	public CommonResult crawlJSON(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("url") URL url);
}
