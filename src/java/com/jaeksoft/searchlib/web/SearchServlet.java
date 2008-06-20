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
import com.jaeksoft.searchlib.render.Render;
import com.jaeksoft.searchlib.render.RenderJsp;
import com.jaeksoft.searchlib.render.RenderXml;
import com.jaeksoft.searchlib.request.Request;
import com.jaeksoft.searchlib.result.Result;

public class SearchServlet extends AbstractServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2241064786260022955L;

	@Override
	protected void doRequest(ServletTransaction servletTransaction)
			throws ServletException {

		try {

			HttpServletRequest request = servletTransaction.getServletRequest();
			Client client = Client.getWebAppInstance();
			Request req = client.getNewRequest(request.getParameter("qt"),
					request);

			Result<?> result = client.getIndex().search(req);

			Render render = null;
			String p;
			if ((p = request.getParameter("render")) != null) {
				if ("jsp".equals(p))
					render = new RenderJsp(request.getParameter("jsp"), result);
			}

			if (render == null)
				render = new RenderXml(result);

			render.render(servletTransaction);

			servletTransaction.setInfo(req.getUrlQueryString());

		} catch (Exception e) {
			throw new ServletException(e);
		}

	}
}
