/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2012 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.webservice.crawler.filecrawler;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.jaeksoft.searchlib.webservice.CommonResult;

@Path("/crawler/file")
public interface RestFileCrawler {

	@GET
	@Produces("application/xml")
	@Path("/run/once/{index}/xml")
	public CommonResult runOnceXML(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key);

	@GET
	@Produces("application/json")
	@Path("/run/once/{index}/json")
	public CommonResult runOnceJSON(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key);

	@GET
	@Produces("application/xml")
	@Path("/run/forever/{index}/xml")
	public CommonResult runForeverXML(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key);

	@GET
	@Produces("application/json")
	@Path("/run/forever/{index}/json")
	public CommonResult runForeverJSON(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key);

	@GET
	@Produces("application/xml")
	@Path("/stop/{index}/xml")
	public CommonResult stopXML(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key);

	@GET
	@Produces("application/json")
	@Path("/stop/{index}/json")
	public CommonResult stopJSON(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key);

	@GET
	@Produces("application/xml")
	@Path("/status/{index}/xml")
	public CommonResult statusXML(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key);

	@GET
	@Produces("application/json")
	@Path("/status/{index}/json")
	public CommonResult statusJSON(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key);

	@GET
	@Produces("application/xml")
	@Path("/repository/inject/localfile/{index}/xml")
	public CommonResult injectLocalFileRepositoryXML(
			@PathParam("index") String use, @QueryParam("login") String login,
			@QueryParam("key") String key, @QueryParam("path") String filePath,
			@QueryParam("ignoreHiddenFile") Boolean ignoreHiddenFile,
			@QueryParam("includeSubDirectory") Boolean withSubDirectory,
			@QueryParam("enabled") Boolean enabled,
			@QueryParam("delay") int delay);

	@GET
	@Produces("application/json")
	@Path("/repository/inject/localfile/{index}/json")
	public CommonResult injectLocalFileRepositoryJSON(
			@PathParam("index") String use, @QueryParam("login") String login,
			@QueryParam("key") String key, @QueryParam("path") String filePath,
			@QueryParam("ignoreHiddenFile") Boolean ignoreHiddenFile,
			@QueryParam("includeSubDirectory") Boolean withSubDirectory,
			@QueryParam("enabled") Boolean enabled,
			@QueryParam("delay") int delay);

	@GET
	@Produces("application/xml")
	@Path("/repository/remove/localfile/{index}/xml")
	public CommonResult removeLocalFileRepositoryXML(
			@PathParam("index") String use, @QueryParam("login") String login,
			@QueryParam("key") String key, @QueryParam("path") String path);

	@GET
	@Produces("application/json")
	@Path("/repository/remove/localfile/{index}/json")
	public CommonResult removeLocalFileRepositoryJSON(
			@PathParam("index") String use, @QueryParam("login") String login,
			@QueryParam("key") String key, @QueryParam("path") String path);

	@GET
	@Produces("application/xml")
	@Path("/repository/inject/smb/{index}/xml")
	public CommonResult injectSmbRepositoryXML(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("path") String path,
			@QueryParam("ignoreHiddenFile") Boolean ignoreHiddenFile,
			@QueryParam("includeSubDirectory") Boolean withSubDirectory,
			@QueryParam("enabled") Boolean enabled,
			@QueryParam("delay") int delay,
			@QueryParam("username") String username,
			@QueryParam("password") String password,
			@QueryParam("domain") String domain, @QueryParam("host") String host);

	@GET
	@Produces("application/json")
	@Path("/repository/inject/smb/{index}/json")
	public CommonResult injectSmbRepositoryJSON(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("path") String path,
			@QueryParam("ignoreHiddenFile") Boolean ignoreHiddenFile,
			@QueryParam("includeSubDirectory") Boolean withSubDirectory,
			@QueryParam("enabled") Boolean enabled,
			@QueryParam("delay") int delay,
			@QueryParam("username") String username,
			@QueryParam("password") String password,
			@QueryParam("domain") String domain, @QueryParam("host") String host);

	@GET
	@Produces("application/xml")
	@Path("/repository/remove/smb/{index}/xml")
	public CommonResult removeSmbRepositoryXML(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("path") String path,
			@QueryParam("username") String username,
			@QueryParam("domain") String domain, @QueryParam("host") String host);

	@GET
	@Produces("application/json")
	@Path("/repository/remove/smb/{index}/json")
	public CommonResult removeSmbRepositoryJSON(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("path") String path,
			@QueryParam("username") String username,
			@QueryParam("domain") String domain, @QueryParam("host") String host);

	@GET
	@Produces("application/xml")
	@Path("/repository/inject/ftp/{index}/xml")
	public CommonResult injectFtpRepositoryXML(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("path") String path,
			@QueryParam("ignoreHiddenFile") Boolean ignoreHiddenFile,
			@QueryParam("includeSubDirectory") Boolean withSubDirectory,
			@QueryParam("enabled") Boolean enabled,
			@QueryParam("delay") int delay,
			@QueryParam("username") String username,
			@QueryParam("password") String password,
			@QueryParam("domain") String domain,
			@QueryParam("host") String host, @QueryParam("ssl") boolean ssl);

	@GET
	@Produces("application/json")
	@Path("/repository/inject/ftp/{index}/json")
	public CommonResult injectFtpRepositoryJSON(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("path") String path,
			@QueryParam("ignoreHiddenFile") Boolean ignoreHiddenFile,
			@QueryParam("includeSubDirectory") Boolean withSubDirectory,
			@QueryParam("enabled") Boolean enabled,
			@QueryParam("delay") int delay,
			@QueryParam("username") String username,
			@QueryParam("password") String password,
			@QueryParam("domain") String domain,
			@QueryParam("host") String host, @QueryParam("ssl") boolean ssl);

	@GET
	@Produces("application/xml")
	@Path("/repository/remove/ftp/{index}/xml")
	public CommonResult removeFtpRepositoryXML(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("path") String path,
			@QueryParam("username") String username,
			@QueryParam("host") String host, @QueryParam("ssl") boolean ssl);

	@GET
	@Produces("application/json")
	@Path("/repository/remove/ftp/{index}/json")
	public CommonResult removeFtpRepositoryJSON(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("path") String path,
			@QueryParam("username") String username,
			@QueryParam("host") String host, @QueryParam("ssl") boolean ssl);

}
