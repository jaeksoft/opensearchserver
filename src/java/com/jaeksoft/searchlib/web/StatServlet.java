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

import java.io.PrintWriter;
import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;

import com.jaeksoft.searchlib.Client;

public class StatServlet extends AbstractServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6835267443840241748L;

	@Override
	protected void doRequest(ServletTransaction servletTransaction)
			throws ServletException {

		try {
			Client client = Client.getWebAppInstance();

			HttpServletRequest request = servletTransaction.getServletRequest();

			String reload = request.getParameter("reload");
			boolean deleteOld = (request.getParameter("deleteOld") != null);

			if (reload != null)
				client.getIndex().reload(reload, deleteOld);

			PrintWriter writer = servletTransaction.getWriter("UTF-8");
			writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			HashSet<String> classDetail = new HashSet<String>();
			String[] values = request.getParameterValues("details");
			if (values != null)
				for (String value : values)
					classDetail.add(value);
			client.xmlInfo(writer, servletTransaction.getClassDetail(request));
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

}
