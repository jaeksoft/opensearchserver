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

import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Textbox;

import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.user.User;

public class LoginController extends CommonController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2918395323961861472L;

	public LoginController() throws SearchLibException {
		super();
		reset();
	}

	public void onLogin(Event event) throws WrongValueException,
			SearchLibException, InterruptedException {
		Textbox teLogin = (Textbox) getFellow("login");
		Textbox tePassword = (Textbox) getFellow("password");
		User user = ClientCatalog.authenticate(teLogin.getValue(), tePassword
				.getValue());
		if (user == null) {
			Thread.sleep(2000);
			new AlertController("Authentication failed");
			return;
		}
		setAttribute(ScopeAttribute.LOGGED_USER, user);
		reloadDesktop();
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}
}
