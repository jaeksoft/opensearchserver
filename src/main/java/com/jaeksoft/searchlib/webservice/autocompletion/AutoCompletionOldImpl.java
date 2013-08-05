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

import com.jaeksoft.searchlib.webservice.CommonResult;

public class AutoCompletionOldImpl extends AutoCompletionCommon implements
		RestAutoCompletionOld {

	@Override
	public CommonResult setXML(String index, String login, String key,
			String name, List<String> fields, Integer rows) {
		return set(index, login, key, name, fields, rows);
	}

	@Override
	public CommonResult setJSON(String index, String login, String key,
			String name, List<String> fields, Integer rows) {
		return set(index, login, key, name, fields, rows);
	}

	@Override
	public CommonResult buildXML(String index, String login, String key,
			String name) {
		return build(index, login, key, name);
	}

	@Override
	public CommonResult buildJSON(String index, String login, String key,
			String name) {
		return build(index, login, key, name);
	}

	@Override
	public AutoCompletionResult queryXML(String index, String login,
			String key, String name, String prefix, Integer rows) {
		return query(index, login, key, name, prefix, rows);
	}

	@Override
	public AutoCompletionResult queryJSON(String index, String login,
			String key, String name, String prefix, Integer rows) {
		return query(index, login, key, name, prefix, rows);
	}
}
