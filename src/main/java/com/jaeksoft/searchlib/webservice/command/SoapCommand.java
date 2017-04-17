/**
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2011-2013 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.webservice.command;

import com.jaeksoft.searchlib.webservice.CommonResult;

import javax.jws.WebParam;
import javax.jws.WebService;

@WebService(name = "Command")
public interface SoapCommand {

	public CommonResult reload(@WebParam(name = "use") String use, @WebParam(name = "login") String login,
			@WebParam(name = "key") String key);

	public CommonResult online(@WebParam(name = "use") String use, @WebParam(name = "login") String login,
			@WebParam(name = "key") String key);

	public CommonResult offline(@WebParam(name = "use") String use, @WebParam(name = "login") String login,
			@WebParam(name = "key") String key);

	public CommonResult truncate(@WebParam(name = "use") String use, @WebParam(name = "login") String login,
			@WebParam(name = "key") String key);

	public CommonResult merge(@WebParam(name = "use") String use, @WebParam(name = "login") String login,
			@WebParam(name = "key") String key, @WebParam(name = "index") String index);

}
