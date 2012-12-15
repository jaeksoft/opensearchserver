/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Button;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.user.User;

public class LoginComposer extends SelectorComposer<Window> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2918395323961861472L;

	@Wire("#login")
	Textbox login;

	@Wire("#password")
	Textbox password;

	@Wire("#submit")
	Button submit;

	@WireVariable
	private Session session;

	public LoginComposer() throws SearchLibException {
		super();
	}

	@Listen("onClick = #submit; onOK = #password; onOK= #login")
	public void onSubmit() throws WrongValueException, SearchLibException,
			InterruptedException {
		login();
	}

	private void login() throws WrongValueException, SearchLibException,
			InterruptedException {
		User user = ClientCatalog.authenticate(login.getValue(),
				password.getValue());
		if (user == null) {
			Thread.sleep(2000);
			new AlertController("Authentication failed");
			return;
		}

		session.setAttribute(ScopeAttribute.LOGGED_USER.name(), user);
		Executions.sendRedirect("/");
	}
}
