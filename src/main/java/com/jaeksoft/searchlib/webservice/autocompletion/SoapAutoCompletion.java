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

package com.jaeksoft.searchlib.webservice.autocompletion;

import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebService;

import com.jaeksoft.searchlib.webservice.CommonResult;

@WebService(name = "AutoCompletion")
public interface SoapAutoCompletion {

	public CommonResult list(@WebParam(name = "index") String index,
			@WebParam(name = "login") String login,
			@WebParam(name = "key") String key);

	public CommonResult set(@WebParam(name = "index") String index,
			@WebParam(name = "login") String login,
			@WebParam(name = "key") String key,
			@WebParam(name = "name") String name,
			@WebParam(name = "fields") List<String> fields,
			@WebParam(name = "rows") Integer rows);

	public CommonResult build(@WebParam(name = "index") String index,
			@WebParam(name = "login") String login,
			@WebParam(name = "key") String key,
			@WebParam(name = "name") String name);

	public AutoCompletionResult query(@WebParam(name = "index") String index,
			@WebParam(name = "login") String login,
			@WebParam(name = "key") String key,
			@WebParam(name = "name") String name,
			@WebParam(name = "prefix") String prefix,
			@WebParam(name = "rows") Integer rows);
}
