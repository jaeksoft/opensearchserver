/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.web;

import javax.servlet.http.HttpServletRequest;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.index.ReaderInterface;
import com.jaeksoft.searchlib.render.RenderObject;
import com.jaeksoft.searchlib.request.Request;
import com.jaeksoft.searchlib.result.DocumentResult;

public class DocumentServlet extends AbstractServlet {

	private static final long serialVersionUID = 2241064786260022955L;

	@Override
	protected void doRequest(ServletTransaction servletTransaction)
			throws ServletException {

		try {
			Client client = Client.getWebAppInstance();

			HttpServletRequest request = servletTransaction.getServletRequest();
			Request req = client
					.getNewRequest(request.getParameter("qt"), null);

			ReaderInterface reader = null;

			String p;

			if ((p = request.getParameter("search")) != null) {
				reader = client.getIndex().get(p);
				req.setReader(reader);
			}

			if ((p = request.getParameter("q")) != null)
				req.setQueryString(p);

			if ((p = request.getParameter("lang")) != null)
				req.setLang(p);

			if ((p = request.getParameter("forceLocal")) != null)
				req.setForceLocal(true);

			String[] docIds = request.getParameterValues("docId");
			if (docIds != null)
				for (String docId : docIds)
					req.addDocId(reader, Integer.parseInt(docId));

			DocumentResult result = client.getIndex().documents(req);

			new RenderObject(result).render(servletTransaction);

			servletTransaction.setInfo(req.getUrlQueryString());

		} catch (Exception e) {
			throw new ServletException(e);
		}

	}
}
