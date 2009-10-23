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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Iterator;

import org.apache.http.HttpException;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Page;
import org.zkoss.zkplus.databind.DataBinder;
import org.zkoss.zul.Window;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;

public abstract class CommonController extends Window {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3581269068713587866L;

	public CommonController() throws SearchLibException {
		super();
		reset();
	}

	protected Object getAttribute(ScopeAttribute scopeAttribute) {
		return scopeAttribute.get(this);
	}

	protected void setAttribute(ScopeAttribute scopeAttribute, Object value) {
		scopeAttribute.set(this, value);
	}

	public Client getClient() throws SearchLibException {
		return (Client) getAttribute(ScopeAttribute.CURRENT_CLIENT);
	}

	public void setClient(Client client) {
		setAttribute(ScopeAttribute.CURRENT_CLIENT, client);
	}

	public boolean isInstanceValid() throws SearchLibException {
		return getClient() != null;
	}

	public boolean isInstanceNotValid() throws SearchLibException {
		return getClient() == null;
	}

	protected void reloadDesktop() {
		Iterator<?> it = getDesktop().getPages().iterator();
		while (it.hasNext()) {
			Page page = (Page) it.next();
			Component component = page.getFirstRoot();
			if (component != null && component instanceof CommonController) {
				CommonController cc = (CommonController) component;
				cc.reset();
				cc.reloadPage();
			} else {
				DataBinder binder = (DataBinder) page.getVariable("binder");
				if (binder != null)
					binder.loadAll();
			}
		}
	}

	public void reloadComponent(String compId) {
		DataBinder binder = (DataBinder) getPage().getVariable("binder");
		if (binder != null)
			binder.loadComponent(getFellow(compId));
	}

	public abstract void reset();

	public void reloadPage() {
		DataBinder binder = (DataBinder) getPage().getVariable("binder");
		if (binder != null)
			binder.loadAll();
	}

	public void onReload() throws IOException, URISyntaxException,
			SearchLibException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, HttpException {
		reloadPage();
	}

	public LanguageEnum[] getLanguageEnum() {
		return LanguageEnum.values();
	}

}
