/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014-2015 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.web.servlet.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.annotation.WebServlet;

import com.jaeksoft.searchlib.ClientCatalogItem;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.webservice.WebServiceEnum;
import com.jaeksoft.searchlib.webservice.index.IndexInfo;
import com.jaeksoft.searchlib.webservice.index.RestIndex;
import com.jaeksoft.searchlib.webservice.index.ResultIndexList;

import freemarker.template.TemplateException;

@WebServlet(urlPatterns = { "/ui/indices" })
public class IndicesServlet extends AbstractUIServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7046086366628095949L;

	public final static String TEMPLATE = "indices.ftl";

	public final static String PATH = "/ui/indices";

	@Override
	protected void service(UITransaction transaction) throws IOException,
			TemplateException, SearchLibException {

		RestIndex restIndex = WebServiceEnum.Index.getNewInstance();
		ResultIndexList resultIndexList = restIndex.indexList(
				transaction.getUserLogin(), transaction.getUserApiKey(), true);

		String s;
		if ((s = transaction.request.getParameter("info")) != null) {
			ClientCatalogItem catalogItem = new ClientCatalogItem(s);
			catalogItem.computeInfos();
			resultIndexList.indexMap.put(s, new IndexInfo(catalogItem));
		}
		List<IndexInfo> indexInfoList = new ArrayList<IndexInfo>(
				resultIndexList.indexMap.size());
		indexInfoList.addAll(resultIndexList.indexMap.values());
		UIPagination pagination = new UIPagination(transaction, "page", 15,
				indexInfoList);
		transaction.variables.put("indexlist",
				indexInfoList.subList(pagination.start, pagination.end));
		transaction.variables.put("pagination", pagination);
		transaction.variables.put("selectedIndex",
				transaction.request.getParameter("select"));
		transaction.template(TEMPLATE);
	}
}
