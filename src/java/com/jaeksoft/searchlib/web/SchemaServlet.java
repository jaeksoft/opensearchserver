/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web;

import java.io.IOException;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.TransformerConfigurationException;

import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.schema.SchemaFieldList;
import com.jaeksoft.searchlib.template.TemplateAbstract;
import com.jaeksoft.searchlib.template.TemplateList;
import com.jaeksoft.searchlib.util.XmlWriter;

public class SchemaServlet extends AbstractServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2904843916773570688L;

	private boolean getSchema(HttpServletRequest request,
			ServletTransaction transaction)
			throws TransformerConfigurationException, SAXException,
			IOException, SearchLibException, NamingException {

		Client client = ClientCatalog.getClient(request);
		Schema schema = client.getSchema();

		transaction.getServletResponse().setContentType("text/xml");
		XmlWriter xmlWriter = new XmlWriter(transaction.getWriter("UTF-8"),
				"UTF-8");
		schema.writeXmlConfig(xmlWriter);
		return true;
	}

	private boolean setField(HttpServletRequest request,
			ServletTransaction transaction) throws SearchLibException,
			NamingException {
		Client client = ClientCatalog.getClient(request);
		Schema schema = client.getSchema();

		SchemaField schemaField = SchemaField.fromHttpRequest(request);
		transaction.addXmlResponse("Info", "field '" + schemaField.getName()
				+ "' added/updated");
		return schema.getFieldList().addOrSet(schemaField);
	}

	private boolean deleteField(HttpServletRequest request,
			ServletTransaction transaction) throws SearchLibException,
			NamingException {
		Client client = ClientCatalog.getClient(request);
		Schema schema = client.getSchema();

		String name = request.getParameter("field.name");
		SchemaFieldList sfl = schema.getFieldList();
		SchemaField field = sfl.get(name);
		if (field == null)
			return false;
		sfl.remove(field);
		transaction.addXmlResponse("Info", "field '" + name + "' removed");
		return true;
	}

	private boolean createIndex(HttpServletRequest request,
			ServletTransaction transaction) throws SearchLibException,
			IOException {
		String indexName = request.getParameter("index.name");
		TemplateAbstract template = TemplateList.findTemplate(request
				.getParameter("index.template"));
		ClientCatalog.createIndex(null, indexName, template);
		transaction.addXmlResponse("Info", "Index created: " + indexName);
		return true;
	}

	@Override
	protected void doRequest(ServletTransaction transaction)
			throws ServletException {
		try {
			HttpServletRequest request = transaction.getServletRequest();
			String cmd = request.getParameter("cmd");
			boolean done = false;
			if ("setfield".equalsIgnoreCase(cmd)) {
				done = setField(request, transaction);
				transaction.addXmlResponse("Status", "OK");
			} else if ("deletefield".equalsIgnoreCase(cmd)) {
				done = deleteField(request, transaction);
				transaction.addXmlResponse("Status", "OK");
			} else if ("getschema".equalsIgnoreCase(cmd))
				done = getSchema(request, transaction);
			else if ("createindex".equalsIgnoreCase(cmd)) {
				done = createIndex(request, transaction);
				transaction.addXmlResponse("Status", "OK");
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
		}
	}
}
