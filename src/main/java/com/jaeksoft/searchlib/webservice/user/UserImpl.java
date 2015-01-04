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
package com.jaeksoft.searchlib.webservice.user;

import com.jaeksoft.searchlib.webservice.CommonListResult;
import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.CommonServices;

public class UserImpl extends CommonServices implements RestUser {

	@Override
	public CommonResult createUser(String login, String key, String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CommonResult deleteUser(String login, String key, String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CommonListResult<ResultUser> indexList(String login, String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResultUser getIndex(String login, String key, String name) {
		// TODO Auto-generated method stub
		return null;
	}

}
