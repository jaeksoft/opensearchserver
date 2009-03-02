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

import java.util.Iterator;

import javax.naming.NamingException;

import org.zkoss.zk.ui.Page;
import org.zkoss.zkplus.databind.DataBinder;
import org.zkoss.zul.Window;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;

public class CommonController extends Window {

	private Client client;

	/**
	 * 
	 */
	private static final long serialVersionUID = -3581269068713587866L;

	public CommonController() throws SearchLibException {
		super();
		client = null;
	}

	public Client getClient() throws SearchLibException {
		synchronized (this) {
			if (client != null)
				return client;
			try {
				return Client.getWebAppInstance();
			} catch (NamingException e) {
				return null;
			}
		}
	}

	protected void reloadDesktop() {
		Iterator<?> it = getDesktop().getPages().iterator();
		while (it.hasNext()) {
			Page page = (Page) it.next();
			DataBinder binder = (DataBinder) page.getVariable("binder");
			binder.loadAll();
		}
	}

	protected void reloadPage() {
		DataBinder binder = (DataBinder) getPage().getVariable("binder");
		binder.loadAll();
	}

}
