/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2011 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer.  If not, see <http://www.gnu.org/licenses/>.
 **/
package com.jaeksoft.searchlib.web.controller.crawler.web;

import java.io.IOException;

import org.zkoss.zul.Textbox;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.common.database.PropertyManager;
import com.jaeksoft.searchlib.crawler.web.database.WebPropertyManager;
import com.jaeksoft.searchlib.web.controller.CommonController;

/**
 * @author Naveen
 * 
 */
public class ProxyController extends CommonController {

	private static final long serialVersionUID = 6562366244469411878L;
	private Textbox proxyHost;
	private Textbox proxyPort;

	public ProxyController() throws SearchLibException {
		super();
		reset();
	}

	@Override
	protected void reset() throws SearchLibException {
		proxyHost = null;
		proxyPort = null;
	}

	public PropertyManager getProperties() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getWebPropertyManager();
	}

	public void onSave() throws SearchLibException, IOException {
		proxyHost = (Textbox) getFellow("proxyHost");
		proxyPort = (Textbox) getFellow("proxyPort");
		WebPropertyManager webPropertyManager = getClient()
				.getWebPropertyManager();
		webPropertyManager.getProxyHost().setValue(proxyHost.getText());
		webPropertyManager.getProxyPort().setValue(
				Integer.parseInt(proxyPort.getText()));

	}
}
