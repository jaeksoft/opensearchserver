/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C)2011-2012 Emmanuel Keller / Jaeksoft
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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.WrongValueException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.api.Api;
import com.jaeksoft.searchlib.api.ApiManager;
import com.jaeksoft.searchlib.api.OpenSearchApi;
import com.jaeksoft.searchlib.api.OpenSearchTypes;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.web.SearchServlet;
import com.jaeksoft.searchlib.web.controller.AlertController;
import com.jaeksoft.searchlib.web.controller.CommonController;

public class OpenSearchController extends CommonController {

	private OpenSearchTypes fieldType;
	private String searchTemplate, currentField;
	private OpenSearchFields currentOpenSearchField;
	private List<OpenSearchApi> openSearchApiList;

	enum OpenSearchFields {
		TITLE, DESCRIPTION, URL;
	}

	public OpenSearchController() throws SearchLibException {
		super();
		reload();
		load();
	}

	@Override
	protected void reset() throws SearchLibException {

	}

	public void load() throws SearchLibException {
		Client client;
		try {
			openSearchApiList = new ArrayList<OpenSearchApi>();
			client = getClient();
			if (client == null)
				return;
			ApiManager apiManager = client.getApiManager();
			if (apiManager.isAvailable()) {
				if (apiManager.getFieldValue("opensearch") != null)
					searchTemplate = apiManager.getFieldValue("opensearch");
				openSearchApiList = apiManager
						.getOpenSearchFieldList("opensearch");
			}
		} catch (TransformerConfigurationException e) {
			throw new SearchLibException(e);
		} catch (XPathExpressionException e) {
			throw new SearchLibException(e);
		} catch (ParserConfigurationException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		}
	}

	@Command
	@NotifyChange("*")
	public void onDelete(@BindingParam("openSearchField") String openSearchField)
			throws SearchLibException {
		for (Iterator<OpenSearchApi> iter = openSearchApiList.iterator(); iter
				.hasNext();) {
			OpenSearchApi api = iter.next();
			if (api.getOpenSearchField().equalsIgnoreCase(openSearchField)) {
				iter.remove();
			}
		}
	}

	public Set<String> getRequestList() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getRequestMap().getNameList();
	}

	@Command
	@NotifyChange("*")
	public void onAdd() throws SearchLibException, UnsupportedEncodingException {
		Boolean isAlreadyAdded = false;
		if (openSearchApiList != null && openSearchApiList.size() != 0) {
			for (Iterator<OpenSearchApi> iter = openSearchApiList.iterator(); iter
					.hasNext();) {
				OpenSearchApi api = iter.next();
				if (api.getOpenSearchField().equalsIgnoreCase(
						currentOpenSearchField.name())) {
					isAlreadyAdded = true;
					break;
				}
			}
		}
		if (isAlreadyAdded != null && !isAlreadyAdded) {
			openSearchApiList.add(new OpenSearchApi(currentField,
					currentOpenSearchField.name()));
		}
	}

	public List<OpenSearchApi> getList() {
		return openSearchApiList;
	}

	public List<String> getFieldList() throws SearchLibException,
			InterruptedException {
		if (fieldType == null)
			return null;
		Client client = getClient();
		if (client == null)
			return null;
		if (searchTemplate == null)
			new AlertController("Please Select an Query Template");
		AbstractSearchRequest request = (AbstractSearchRequest) client
				.getRequestMap().get(searchTemplate);
		if (request == null)
			return null;
		List<String> nameList = new ArrayList<String>();
		nameList.add(null);
		if (fieldType == OpenSearchTypes.FIELD)
			request.getReturnFieldList().toNameList(nameList);
		else if (fieldType == OpenSearchTypes.SNIPPET)
			request.getSnippetFieldList().toNameList(nameList);
		return nameList;
	}

	@Command
	@NotifyChange("*")
	public void onSave() throws WrongValueException,
			TransformerConfigurationException, XPathExpressionException,
			IOException, SAXException, ParserConfigurationException,
			SearchLibException {
		Client client = getClient();
		if (client == null)
			return;
		ApiManager apiManager = client.getApiManager();
		apiManager.createNewApi(new Api("opensearch", searchTemplate,
				openSearchApiList));
	}

	public String getRequestApiCall() throws SearchLibException,
			TransformerConfigurationException, WrongValueException,
			IOException, SAXException, XPathExpressionException,
			ParserConfigurationException {
		Client client = getClient();
		if (client == null)
			return null;
		StringBuilder sb = SearchServlet.getOpenSearchApiUrl(getBaseUrl(),
				"/opensearch", client, getLoggedUser());
		return sb.toString();

	}

	public OpenSearchTypes[] getFields() {
		return OpenSearchTypes.values();
	}

	public OpenSearchFields[] getOpenSearchFields() {
		return OpenSearchFields.values();
	}

	public OpenSearchTypes getFieldType() {
		return fieldType;
	}

	@NotifyChange("*")
	public void setFieldType(OpenSearchTypes fieldType) {
		this.fieldType = fieldType;
	}

	public String getSearchTemplate() {
		return searchTemplate;
	}

	public void setSearchTemplate(String searchTemplate) {
		this.searchTemplate = searchTemplate;
	}

	public String getCurrentField() {
		return currentField;
	}

	public void setCurrentField(String currentField) {
		this.currentField = currentField;
	}

	public OpenSearchFields getCurrentOpenSearchField() {
		return currentOpenSearchField;
	}

	public void setCurrentOpenSearchField(
			OpenSearchFields currentOpenSearchField) {
		this.currentOpenSearchField = currentOpenSearchField;
	}

}
