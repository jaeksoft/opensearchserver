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

import javax.jws.WebParam;
import javax.jws.WebService;

import com.jaeksoft.searchlib.webservice.CommonResult;

@WebService(name = "FileCrawler")
public interface SoapFileCrawler {

	public CommonResult runOnce(@WebParam(name = "use") String use,
			@WebParam(name = "login") String login,
			@WebParam(name = "key") String key);

	public CommonResult runForever(@WebParam(name = "use") String use,
			@WebParam(name = "login") String login,
			@WebParam(name = "key") String key);

	public CommonResult stop(@WebParam(name = "use") String use,
			@WebParam(name = "login") String login,
			@WebParam(name = "key") String key);

	public CommonResult status(@WebParam(name = "use") String use,
			@WebParam(name = "login") String login,
			@WebParam(name = "key") String key);

	public CommonResult injectLocalFileRepository(
			@WebParam(name = "use") String use,
			@WebParam(name = "login") String login,
			@WebParam(name = "key") String key,
			@WebParam(name = "path") String filePath,
			@WebParam(name = "ignoreHiddenFile") Boolean ignoreHiddenFile,
			@WebParam(name = "includeSubDirectory") Boolean withSubDirectory,
			@WebParam(name = "enabled") Boolean enabled,
			@WebParam(name = "delay") int delay);

	public CommonResult removeLocalFileRepository(
			@WebParam(name = "use") String use,
			@WebParam(name = "login") String login,
			@WebParam(name = "key") String key,
			@WebParam(name = "path") String path);

	public CommonResult injectSmbRepository(@WebParam(name = "use") String use,
			@WebParam(name = "login") String login,
			@WebParam(name = "key") String key,
			@WebParam(name = "path") String path,
			@WebParam(name = "ignoreHiddenFile") Boolean ignoreHiddenFile,
			@WebParam(name = "includeSubDirectory") Boolean withSubDirectory,
			@WebParam(name = "enabled") Boolean enabled,
			@WebParam(name = "delay") int delay,
			@WebParam(name = "username") String username,
			@WebParam(name = "password") String password,
			@WebParam(name = "domain") String domain,
			@WebParam(name = "host") String host);

	public CommonResult removeSmbRepository(@WebParam(name = "use") String use,
			@WebParam(name = "login") String login,
			@WebParam(name = "key") String key,
			@WebParam(name = "path") String path,
			@WebParam(name = "username") String username,
			@WebParam(name = "domain") String domain,
			@WebParam(name = "host") String host);

	public CommonResult injectFtpRepository(@WebParam(name = "use") String use,
			@WebParam(name = "login") String login,
			@WebParam(name = "key") String key,
			@WebParam(name = "path") String filePath,
			@WebParam(name = "ignoreHiddenFile") Boolean ignoreHiddenFile,
			@WebParam(name = "includeSubDirectory") Boolean withSubDirectory,
			@WebParam(name = "enabled") Boolean enabled,
			@WebParam(name = "delay") int delay,
			@WebParam(name = "username") String username,
			@WebParam(name = "password") String password,
			@WebParam(name = "domain") String domain,
			@WebParam(name = "host") String host,
			@WebParam(name = "ssl") boolean ssl);

	public CommonResult removeFtpRepository(@WebParam(name = "use") String use,
			@WebParam(name = "login") String login,
			@WebParam(name = "key") String key,
			@WebParam(name = "path") String path,
			@WebParam(name = "username") String username,
			@WebParam(name = "host") String host,
			@WebParam(name = "ssl") boolean ssl);

}
