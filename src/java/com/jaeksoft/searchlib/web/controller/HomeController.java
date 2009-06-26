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

import java.io.File;

import javax.naming.NamingException;

import org.zkoss.zul.Image;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;

import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.SearchLibException;

public class HomeController extends CommonController implements
		ListitemRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2896471240596574094L;

	public HomeController() throws SearchLibException, NamingException {
		super();
	}

	public File[] getClients() throws SearchLibException {
		return ClientCatalog.getClientCatalog();
	}

	public void setClient(File file) throws SearchLibException, NamingException {
		setAttribute(ScopeAttribute.CURRENT_CLIENT, ClientCatalog
				.getClient(file.getName()));
		reloadPage();
	}

	public void render(Listitem item, Object data) throws Exception {
		File indexDirectory = (File) data;
		new Listcell(indexDirectory.getName()).setParent(item);
		Listcell listcell = new Listcell();
		Image image = new Image("/images/action_delete.png");
		image.addForward(null, this, "onValueRemove", indexDirectory);
		image.setParent(listcell);
		listcell.setParent(item);
	}
}
