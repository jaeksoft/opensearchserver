/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.web.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.Analyzer;
import com.jaeksoft.searchlib.index.IndexAbstract;
import com.jaeksoft.searchlib.index.IndexGroup;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.schema.SchemaField;

public class ConfigurationController extends CommonController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9015134975380671501L;

	public ConfigurationController() throws SearchLibException {
		super();
	}

	public List<Analyzer> getAnalyzers() throws SearchLibException {
		return getClient().getSchema().getAnalyzerList();
	}

	public List<SchemaField> getFields() throws SearchLibException {
		return getClient().getSchema().getFieldList().getList();
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

	public Map<String, SearchRequest> getRequests() throws SearchLibException {
		return getClient().getSearchRequestMap();
	}

}
