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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jaeksoft.searchlib.util.Timer;

public abstract class AbstractServlet extends HttpServlet {

	final private static Logger log = Logger.getLogger(AbstractServlet.class
			.getCanonicalName());

	protected abstract void doRequest(ServletTransaction transaction)
			throws ServletException;

	private void doRequest(HttpServletRequest request,
			HttpServletResponse response) {

		ServletTransaction transaction = new ServletTransaction(this, request,
				response);

		String p;
		if ((p = request.getParameter("log")) != null)
			log.setLevel(Level.parse(p.toUpperCase()));

		Timer timer = new Timer();

		try {
			doRequest(transaction);
			timer.end();
			log.info(this.getClass().getSimpleName()
					+ (transaction.getInfo() == null ? "" : " "
							+ transaction.getInfo()) + " " + timer.duration());
		} catch (Exception e) {
			try {
				response.sendError(500, e.getMessage());
				e.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			log.severe(e.getMessage());
		}
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) {
		doRequest(request, response);
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) {
		doRequest(request, response);
	}

	@Override
	protected void doPut(HttpServletRequest request,
			HttpServletResponse response) {
		doRequest(request, response);
	}

}
