/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014 Emmanuel Keller / Jaeksoft
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
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.annotation.WebServlet;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.ClientCatalogItem;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.web.servlet.ui.UIMessage.Css;

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
		Set<ClientCatalogItem> catalogItems = transaction.session.getAttribute(
				UISession.Attributes.CLIENT_CATALOG, Set.class);
		if (catalogItems == null) {
			TreeSet<String> indexList = new TreeSet<String>();
			ClientCatalog.populateClientName(
					transaction.session.getLoggedUser(), indexList, null);
			catalogItems = new TreeSet<ClientCatalogItem>();
			for (String indexName : indexList)
				catalogItems.add(new ClientCatalogItem(indexName));
			transaction.session.setAttribute(
					UISession.Attributes.CLIENT_CATALOG, catalogItems);
		}
		String s;
		if ((s = transaction.request.getParameter("info")) != null) {
			ClientCatalogItem catalogItem = new ClientCatalogItem(s);
			catalogItems.remove(catalogItem);
			catalogItem.computeInfos();
			catalogItems.add(catalogItem);
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
		transaction.variables.put("indexlist", catalogItems);
		transaction.template(TEMPLATE);
	}
}
