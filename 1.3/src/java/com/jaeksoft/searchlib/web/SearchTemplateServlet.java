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

package com.jaeksoft.searchlib.web;

import java.io.IOException;

import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.snippet.SnippetField;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.user.User;

public class SearchTemplateServlet extends AbstractServlet {

	private static final long serialVersionUID = -6279193437936726711L;

	@Override
	protected void doRequest(ServletTransaction transaction)
			throws ServletException {

		boolean done = false;
		try {
			User user = transaction.getLoggedUser();
			if (user != null
					&& !user.hasRole(transaction.getIndexName(),
							Role.INDEX_QUERY))
				throw new SearchLibException("Not permitted");
			String cmd = transaction.getParameterString("cmd");
			if (cmd.equalsIgnoreCase("create")) {
				done = createTemplate(user, transaction);
				transaction.addXmlResponse("Status", "OK");
			}
			if (cmd.equalsIgnoreCase("delete")) {
				done = deletTemplate(user, transaction);
			}
			if (cmd.equalsIgnoreCase("setreturnfield")) {
				done = setReturnField(user, transaction);
			}
			if (cmd.equalsIgnoreCase("setsnippetfield")) {
				done = setSnippetField(user, transaction);
			}
			if (!done)
				transaction.addXmlResponse("Info", "Nothing to do");

		} catch (SearchLibException e) {
			throw new ServletException(e);
		} catch (InterruptedException e) {
			throw new ServletException(e);
		} catch (NamingException e) {
			throw new ServletException(e);
		} catch (XPathExpressionException e) {
			throw new ServletException(e);
		} catch (DOMException e) {
			throw new ServletException(e);
		} catch (ParserConfigurationException e) {
			throw new ServletException(e);
		} catch (SAXException e) {
			throw new ServletException(e);
		} catch (IOException e) {
			throw new ServletException(e);
		} catch (ParseException e) {
			throw new ServletException(e);
		} catch (InstantiationException e) {
			throw new ServletException(e);
		} catch (IllegalAccessException e) {
			throw new ServletException(e);
		} catch (ClassNotFoundException e) {
			throw new ServletException(e);
		}

	}

	private boolean setSnippetField(User user, ServletTransaction transaction)
			throws SearchLibException, NamingException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {

		String searchTemplate = transaction.getParameterString("qt.name");
		String maxSnippetSize = transaction
				.getParameterString("qt.maxSnippetSize");
		String tag = transaction.getParameterString("qt.tag");
		String snippetField = transaction.getParameterString("snippetfield");
		String fragmenter = transaction.getParameterString("qt.fragmenter");
		Client client = transaction.getClient();
		if (client.getRequestMap().get(searchTemplate) != null) {
			SearchRequest request = (SearchRequest) client.getRequestMap().get(
					searchTemplate);
			if (snippetField != null) {
				request.getSnippetFieldList().add(
						new SnippetField(snippetField));
				SnippetField snippetFieldParameter = request
						.getSnippetFieldList().get(snippetField);
				if (maxSnippetSize != null && !maxSnippetSize.equals(""))
					snippetFieldParameter.setMaxSnippetSize(Integer
							.parseInt(maxSnippetSize));
				if (tag != null && !tag.equals(""))
					snippetFieldParameter.setTag(tag);
				if (fragmenter != null && !fragmenter.equals(""))
					snippetFieldParameter.setFragmenter(fragmenter);
				client.getRequestMap().put(request);
				client.saveRequests();
				transaction.addXmlResponse("Status", "OK");
			}
		} else {
			transaction.addXmlResponse("Info", "SearchTemplate Not Found");

		}
		return true;

	}

	private boolean setReturnField(User user, ServletTransaction transaction)
			throws SearchLibException, NamingException {
		String searchTemplate = transaction.getParameterString("qt.name");
		String returnField = transaction.getParameterString("returnfield");
		Client client = transaction.getClient();
		if (client.getRequestMap().get(searchTemplate) != null) {
			SearchRequest request = (SearchRequest) client.getRequestMap().get(
					searchTemplate);
			request.addReturnField(returnField);
			client.getRequestMap().put(request);
			client.saveRequests();
			transaction.addXmlResponse("Status", "OK");
		} else {
			transaction.addXmlResponse("Info", "SearchTemplate Not Found");

		}
		return true;

	}

	private boolean deletTemplate(User user, ServletTransaction transaction)
			throws InterruptedException, SearchLibException, NamingException {
		String searchTemplate = transaction.getParameterString("qt.name");
		Client client = transaction.getClient();
		if (client.getRequestMap().get(searchTemplate) != null) {
			client.getRequestMap().remove(searchTemplate);
			client.saveRequests();
			transaction.addXmlResponse("Status", "OK");
		} else {
			transaction.addXmlResponse("Info", "SearchTemplate Not Found");

		}
		return true;
	}

	private boolean createTemplate(User user, ServletTransaction transaction)
			throws InterruptedException, SearchLibException, NamingException,
			ParserConfigurationException, SAXException, IOException,
			XPathExpressionException, DOMException, ParseException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		Client client = transaction.getClient();
		SearchRequest request = new SearchRequest(client);
		if (transaction.getParameterString("qt.name") != null) {
			String searchTemplate = transaction.getParameterString("qt.name");
			request.setRequestName(searchTemplate);
		}
		if (transaction.getParameterString("qt.query") != null) {
			String queryPattern = transaction.getParameterString("qt.query");
			request.setPatternQuery(queryPattern);
		}
		if (transaction.getParameterString("qt.operator") != null) {
			String defaultOperator = transaction
					.getParameterString("qt.operator");
			request.setDefaultOperator(defaultOperator);
		}
		if (transaction.getParameterString("qt.rows") != null) {
			int rows = Integer.parseInt(transaction
					.getParameterString("qt.rows"));
			request.setRows(rows);
		}
		if (transaction.getParameterString("qt.slop") != null) {
			int phraseSlop = Integer.parseInt(transaction
					.getParameterString("qt.slop"));
			request.setPhraseSlop(phraseSlop);
		}
		if (transaction.getParameterString("qt.lang") != null) {
			request.setLang(LanguageEnum.valueOf(transaction
					.getParameterString("qt.lang")));
		}

		client.getRequestMap().put(request);
		client.saveRequests();
		return true;
	}
}
