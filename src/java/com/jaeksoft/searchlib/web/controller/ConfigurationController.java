/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.web.controller;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.IndexAbstract;
import com.jaeksoft.searchlib.index.IndexGroup;

public class ConfigurationController extends CommonController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9015134975380671501L;

	public ConfigurationController() throws SearchLibException {
		super();
	}

	@Override
	public void reset() {
	}

	public List<IndexAbstract> getIndices() throws SearchLibException,
			NamingException {
		List<IndexAbstract> list = new ArrayList<IndexAbstract>();
		IndexAbstract index = getClient().getIndex();
		if (index instanceof IndexGroup) {
			for (IndexAbstract idx : ((IndexGroup) index).getIndices())
				list.add(idx);
		} else
			list.add(index);
		return list;
	}

}
