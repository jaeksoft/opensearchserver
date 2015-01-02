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
import java.util.TreeSet;

import javax.servlet.annotation.WebServlet;

import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.ClientCatalogItem;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.webservice.index.IndexInfo;

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
		@SuppressWarnings("unchecked")
		List<ClientCatalogItem> catalogItems = transaction.session
				.getAttribute(UISession.Attributes.CLIENT_CATALOG, List.class);
		if (transaction.getRequestParameterBoolean("refresh", false))
			catalogItems = null;
		if (catalogItems == null) {
			TreeSet<String> indexList = new TreeSet<String>();
			ClientCatalog.populateClientName(
					transaction.session.getLoggedUser(), indexList, null);
			catalogItems = new ArrayList<ClientCatalogItem>(indexList.size());
			for (String indexName : indexList)
				catalogItems.add(new ClientCatalogItem(indexName));
			transaction.session.setAttribute(
					UISession.Attributes.CLIENT_CATALOG, catalogItems);
		}
		String s;
		if ((s = transaction.request.getParameter("info")) != null) {
			ClientCatalogItem catalogItem = new ClientCatalogItem(s);
			int i = catalogItems.indexOf(catalogItem);
			if (i >= 0) {
				catalogItems.set(i, catalogItem);
				catalogItem.computeInfos();
			}
		}
		UIPagination pagination = new UIPagination(transaction, "page", 15,
				catalogItems);
		List<IndexInfo> indexList = pagination.getNewPageList();
		for (int i = pagination.start; i < pagination.end; i++)
			indexList.add(new IndexInfo(catalogItems.get(i)));
		transaction.variables.put("indexlist", indexList);
		transaction.variables.put("pagination", pagination);
		transaction.variables.put("selectedIndex",
				transaction.request.getParameter("select"));
		transaction.template(TEMPLATE);
	}
}
