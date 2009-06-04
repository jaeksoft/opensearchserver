/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
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

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.basket.BasketKey;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.parser.Parser;
import com.jaeksoft.searchlib.parser.ParserSelector;

public class BasketServlet extends AbstractServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2935590386041415940L;

	@Override
	protected void doRequest(ServletTransaction transaction)
			throws ServletException {
		try {
			Client client = Client.getWebAppInstance();
			HttpServletRequest request = transaction.getServletRequest();
			String contentType = request.getContentType();
			if (contentType == null)
				throw new ServletException("Unkown content type: "
						+ contentType);
			ParserSelector parserSelector = client.getParserSelector();
			Parser parser = parserSelector.getParserFromMimeType(contentType);
			if (parser == null)
				throw new ServletException("No parser found");

			parser.parseContent(request.getInputStream());
			IndexDocument basketDocument = parser.getIndexDocument();
			BasketKey key = client.getBasketCache().put(basketDocument);
			transaction.addXmlResponse("Status", "OK");
			transaction.addXmlResponse("Key", key.toString());
		} catch (IOException e) {
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
		}
	}
}
