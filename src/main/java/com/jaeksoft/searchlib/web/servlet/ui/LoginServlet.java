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

import javax.servlet.annotation.WebServlet;

import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.user.User;
import com.jaeksoft.searchlib.web.servlet.ui.UIMessage.Css;

import freemarker.template.TemplateException;

@WebServlet(urlPatterns = { "/ui/login" })
public class LoginServlet extends AbstractUIServlet {

	private static final long serialVersionUID = 7046086366628095949L;

	public static final String TEMPLATE = "login.html";

	public static final String PATH = "/ui/login";

	@Override
	protected void service(UITransaction transaction)
			throws SearchLibException, InterruptedException, IOException,
			TemplateException {
		if (ClientCatalog.getUserList().isEmpty()
				|| transaction.session.getLoggedUser() != null) {
			transaction.redirectContext(WelcomeServlet.PATH);
			return;
		}
		if ("post".equalsIgnoreCase(transaction.request.getMethod())) {
			String login = transaction.request.getParameter("login");
			String password = transaction.request.getParameter("password");
			User user = ClientCatalog.authenticate(login, password);
			if (user == null) {
				Thread.sleep(2000);
				transaction.session.addMessage(new UIMessage(Css.WARNING,
						"Authentication failed"));
				transaction.template(TEMPLATE);
				return;
			}
			transaction.session.setLoggedUser(user);
			transaction.redirectContext(WelcomeServlet.PATH);
			return;
		}
		transaction.template(TEMPLATE);
	}
}
