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

import com.jaeksoft.searchlib.webservice.CommonListResult;
import com.jaeksoft.searchlib.webservice.CommonResult;

public class AutoCompletionImpl extends AutoCompletionCommon implements
		SoapAutoCompletion, RestAutoCompletion {

	@Override
	public CommonListResult list(String index, String login, String key) {
		return super.list(index, login, key);
	}

	@Override
	public AutoCompletionResult query(String index, String login, String key,
			String name, String prefix, Integer rows) {
		return super.query(index, login, key, name, prefix, rows);
	}

	@Override
	public AutoCompletionResult queryPost(String index, String login,
			String key, String name, String prefix, Integer rows) {
		return super.query(index, login, key, name, prefix, rows);
	}

	@Override
	public CommonResult build(String index, String login, String key,
			String name) {
		return super.build(index, login, key, name);
	}

	@Override
	public CommonResult set(String index, String login, String key,
			String name, List<String> fields, Integer rows) {
		if ((fields == null || fields.size() == 0) && rows == null)
			return super.build(index, login, key, name);
		else
			return super.set(index, login, key, name, fields, rows);
	}

	@Override
	public CommonResult delete(String index, String login, String key,
			String name) {
		return super.delete(index, login, key, name);
	}

}
