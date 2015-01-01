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

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.ClientCatalogItem;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.web.servlet.ui.UIMessage.Css;
import com.jaeksoft.searchlib.webservice.index.IndexInfo;

import freemarker.template.TemplateException;

@WebServlet(urlPatterns = { "/ui" })
public class WelcomeServlet extends AbstractUIServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7046086366628095949L;

	public final static String TEMPLATE = "index.html";

	public final static String PATH = "/ui";

	@Override
	protected void service(UITransaction transaction) throws IOException,
			TemplateException, SearchLibException {
		if (!transaction.session.isLogged()) {
			transaction.redirectContext(LoginServlet.PATH);
			return;
		}
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
		try {
			if ((s = transaction.request.getParameter("select")) != null) {
				Client client = ClientCatalog.getClient(s);
				if (client == null)
					transaction.session.addMessage(new UIMessage(Css.WARNING,
							"Unknown index."));
			}
		} catch (SearchLibException e) {
			transaction.session.addMessage(new UIMessage(Css.DANGER, e
					.getMessage()));
			Logging.error(e);
		}
		int pageNumber = catalogItems.size() / 15;
		Integer page = transaction.getRequestParameterInteger("page", 0);
		if (page > pageNumber)
			page = pageNumber;
		int start = page * 15;
		int end = start + 15;
		if (end > catalogItems.size())
			end = catalogItems.size();
		List<IndexInfo> indexList = new ArrayList<IndexInfo>(end - start);
		for (int i = start; i < end; i++)
			indexList.add(new IndexInfo(catalogItems.get(i)));
		transaction.variables.put("indexlist", indexList);
		transaction.variables.put("pagenumber", pageNumber);
		transaction.variables.put("page", page);
		transaction.template(TEMPLATE);
	}
}
