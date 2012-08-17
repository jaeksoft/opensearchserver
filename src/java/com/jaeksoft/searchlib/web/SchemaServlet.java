/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web;

import java.io.IOException;

import javax.naming.NamingException;
import javax.xml.transform.TransformerConfigurationException;

import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.ClientCatalogItem;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.schema.SchemaFieldList;
import com.jaeksoft.searchlib.template.TemplateAbstract;
import com.jaeksoft.searchlib.template.TemplateList;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.user.User;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.web.controller.PushEvent;

public class SchemaServlet extends AbstractServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2904843916773570688L;

	private boolean getSchema(User user, ServletTransaction transaction)
			throws TransformerConfigurationException, SAXException,
			IOException, SearchLibException, NamingException {

		if (user != null
				&& !user.hasRole(transaction.getIndexName(), Role.INDEX_SCHEMA))
			throw new SearchLibException("Not permitted");

		Client client = transaction.getClient();
		Schema schema = client.getSchema();

		transaction.setResponseContentType("text/xml");
		XmlWriter xmlWriter = new XmlWriter(transaction.getWriter("UTF-8"),
				"UTF-8");
		xmlWriter.startElement("response");
		schema.writeXmlConfig(xmlWriter);
		xmlWriter.endElement();
		return true;
	}

	public static void saveSchema(Client client, Schema schema)
			throws SearchLibException {
		client.saveConfig();
		schema.recompileAnalyzers();
		PushEvent.SCHEMA_CHANGED.publish(client);
	}

	private boolean setField(User user, ServletTransaction transaction)
			throws SearchLibException, NamingException {

		if (user != null
				&& !user.hasRole(transaction.getIndexName(), Role.INDEX_SCHEMA))
			throw new SearchLibException("Not permitted");

		Client client = transaction.getClient();
		Schema schema = client.getSchema();
		String defaultField = transaction.getParameterString("field.default");
		String uniqueField = transaction.getParameterString("field.unique");
		SchemaField schemaField = SchemaField.fromHttpRequest(transaction);
		transaction.addXmlResponse("Info", "field '" + schemaField.getName()
				+ "' added/updated");
		schema.getFieldList().put(schemaField);
		if (defaultField != null) {
			if (defaultField.equalsIgnoreCase("yes")) {
				schema.getFieldList().setDefaultField(
						transaction.getParameterString("field.name"));
			}
		}
		if (uniqueField != null) {
			if (uniqueField.equalsIgnoreCase("yes")) {
				schema.getFieldList().setUniqueField(
						transaction.getParameterString("field.name"));
			}
		}
		saveSchema(client, schema);
		return true;
	}

	private boolean deleteField(User user, ServletTransaction transaction)
			throws SearchLibException, NamingException {

		if (user != null
				&& !user.hasRole(transaction.getIndexName(), Role.INDEX_SCHEMA))
			throw new SearchLibException("Not permitted");

		Client client = transaction.getClient();
		Schema schema = client.getSchema();
		String name = transaction.getParameterString("field.name");
		SchemaFieldList sfl = schema.getFieldList();
		SchemaField field = sfl.get(name);
		if (field == null)
			return false;
		sfl.remove(field.getName());
		saveSchema(client, schema);
		transaction.addXmlResponse("Info", "field '" + name + "' removed");
		return true;
	}

	private boolean createIndex(User user, ServletTransaction transaction)
			throws SearchLibException, IOException {
		String indexName = transaction.getParameterString("index.name");
		TemplateAbstract template = TemplateList.findTemplate(transaction
				.getParameterString("index.template"));
		ClientCatalog.createIndex(null, indexName, template);
		transaction.addXmlResponse("Info", "Index created: " + indexName);
		return true;
	}

	private boolean deleteIndex(User user, ServletTransaction transaction)
			throws SearchLibException, IOException, NamingException {
		String indexName = transaction.getParameterString("index.name");
		String indexDeleteName = transaction
				.getParameterString("index.delete.name");
		if (indexName == null || indexDeleteName == null)
			return false;
		if (!indexName.equals(indexDeleteName))
			throw new SearchLibException(
					"parameters index.name and index.delete.name do not match");
		ClientCatalog.eraseIndex(user, indexName);
		transaction.addXmlResponse("Info", "Index deleted: " + indexName);
		return true;
	}

	private boolean indexList(User user, ServletTransaction transaction)
			throws SearchLibException, TransformerConfigurationException,
			SAXException, IOException {
		transaction.setResponseContentType("text/xml");
		XmlWriter xmlWriter = new XmlWriter(transaction.getWriter("UTF-8"),
				"UTF-8");
		xmlWriter.startElement("response");
		for (ClientCatalogItem catalogItem : ClientCatalog
				.getClientCatalog(user)) {
			xmlWriter.startElement("index", "name", catalogItem.getIndexName());
			xmlWriter.endElement();
		}
		xmlWriter.endElement();
		return true;
	}

	@Override
	protected void doRequest(ServletTransaction transaction)
			throws ServletException {
		try {
			User user = transaction.getLoggedUser();
			String cmd = transaction.getParameterString("cmd");
			boolean done = false;
			if ("setfield".equalsIgnoreCase(cmd)) {
				done = setField(user, transaction);
				transaction.addXmlResponse("Status", "OK");
			} else if ("deletefield".equalsIgnoreCase(cmd)) {
				done = deleteField(user, transaction);
				transaction.addXmlResponse("Status", "OK");
			} else if ("getschema".equalsIgnoreCase(cmd))
				done = getSchema(user, transaction);
			else if ("createindex".equalsIgnoreCase(cmd)) {
				done = createIndex(user, transaction);
				transaction.addXmlResponse("Status", "OK");
			} else if ("deleteindex".equalsIgnoreCase(cmd)) {
				done = deleteIndex(user, transaction);
				transaction.addXmlResponse("Status", "OK");
			} else if ("indexlist".equalsIgnoreCase(cmd)) {
				done = indexList(user, transaction);
			}
			if (!done)
				transaction.addXmlResponse("Info", "Nothing to do");
		} catch (SearchLibException e) {
			throw new ServletException(e);
		} catch (NamingException e) {
			throw new ServletException(e);
		} catch (TransformerConfigurationException e) {
			throw new ServletException(e);
		} catch (SAXException e) {
			throw new ServletException(e);
		} catch (IOException e) {
			throw new ServletException(e);
		} catch (InterruptedException e) {
			throw new ServletException(e);
		}
	}
}
