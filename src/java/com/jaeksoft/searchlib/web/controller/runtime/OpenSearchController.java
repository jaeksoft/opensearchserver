/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C)2011 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.web.controller.runtime;

import java.io.IOException;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zul.api.Combobox;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.api.ApiManager;
import com.jaeksoft.searchlib.api.OpenSearchApi;
import com.jaeksoft.searchlib.web.controller.CommonController;


public class OpenSearchController extends CommonController {
	/**
	 * 
	 */
	private static final long serialVersionUID = -899171123101262091L;
	public Combobox searchRequest;

	public OpenSearchController() throws SearchLibException {
		super();

	}

	public Set<String> getRequestList() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getSearchRequestMap().getNameList();
	}

	public void onViewOpenSearch() throws SearchLibException,
			TransformerConfigurationException, WrongValueException,
			IOException, SAXException, XPathExpressionException,
			ParserConfigurationException {
		searchRequest = (Combobox) getFellow("searchRequest");
		Client client = getClient();
		ApiManager apiManager = client.getApiManager();
		apiManager.createNewApi(new OpenSearchApi(searchRequest.getText()));
	}

	@Override
	protected void reset() throws SearchLibException {

	}

}
