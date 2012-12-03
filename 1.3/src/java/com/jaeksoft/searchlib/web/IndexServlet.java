/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.user.User;
import com.jaeksoft.searchlib.util.DomUtils;

public class IndexServlet extends AbstractServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3855116559376800406L;

	@Override
	protected void doRequest(ServletTransaction transaction)
			throws ServletException {
		try {

			String indexName = transaction.getIndexName();

			User user = transaction.getLoggedUser();
			if (user != null && !user.hasRole(indexName, Role.INDEX_UPDATE))
				throw new SearchLibException("Not permitted");

			Client client = transaction.getClient();
			String ct = transaction.getRequestContentType();
			int bufferSize = transaction.getParameterInteger("bufferSize", 50);
			int updatedCount = 0;
			int deletedCount = 0;
			if (ct != null && ct.toLowerCase().contains("xml")) {
				Node xmlDoc = DomUtils.readXml(
						new StreamSource(transaction.getInputStream()), false);
				updatedCount = client.updateXmlDocuments(xmlDoc, bufferSize,
						null, client.getWebPropertyManager().getProxyHandler(),
						null);
				deletedCount = client.deleteXmlDocuments(xmlDoc, bufferSize,
						null);
			}
			transaction.addXmlResponse("Status", "OK");
			transaction.addXmlResponse("Count", Integer.toString(updatedCount));
			transaction.addXmlResponse("Deleted",
					Integer.toString(deletedCount));
		} catch (IOException e) {
			throw new ServletException(e);
		} catch (XPathExpressionException e) {
			throw new ServletException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new ServletException(e);
		} catch (ParserConfigurationException e) {
			throw new ServletException(e);
		} catch (SAXException e) {
			throw new ServletException(e);
		} catch (URISyntaxException e) {
			throw new ServletException(e);
		} catch (SearchLibException e) {
			throw new ServletException(e);
		} catch (NamingException e) {
			throw new ServletException(e);
		} catch (InstantiationException e) {
			throw new ServletException(e);
		} catch (IllegalAccessException e) {
			throw new ServletException(e);
		} catch (ClassNotFoundException e) {
			throw new ServletException(e);
		} catch (InterruptedException e) {
			throw new ServletException(e);
		}
	}

}
