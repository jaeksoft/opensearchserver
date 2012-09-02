/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2011 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.controller;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.IndexAbstract;

public class ConfigurationController extends CommonController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9015134975380671501L;

	public ConfigurationController() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() throws SearchLibException {
	}

	public List<IndexAbstract> getIndices() throws SearchLibException,
			NamingException {
		Client client = getClient();
		if (client == null)
			return null;
		List<IndexAbstract> list = new ArrayList<IndexAbstract>();
		IndexAbstract index = client.getIndexAbstract();
		list.add(index);
		return list;
	}

}
