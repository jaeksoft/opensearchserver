/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
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
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zul.Button;
import org.zkoss.zul.Textbox;

import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.user.User;

public class LoginComposer extends CommonComposer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2918395323961861472L;

	Textbox login;

	Textbox password;

	Button submit;

	public LoginComposer() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() throws SearchLibException {
	}

	public void onClick$submit() throws WrongValueException,
			SearchLibException, InterruptedException {
		login();
	}

	public void onOK$login() throws WrongValueException, SearchLibException,
			InterruptedException {
		login();
	}

	public void onOK$password() throws WrongValueException, SearchLibException,
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

		setAttribute(ScopeAttribute.LOGGED_USER, user);
		Executions.sendRedirect("/");
	}

}
