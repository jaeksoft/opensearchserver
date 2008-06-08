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
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashSet;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServletTransaction {

	private String info;

	private AbstractServlet servlet;

	private HttpServletResponse response;

	private HttpServletRequest request;

	private PrintWriter writer;

	private OutputStream out;

	public ServletTransaction(AbstractServlet servlet,
			HttpServletRequest request, HttpServletResponse response) {
		info = "";
		this.servlet = servlet;
		this.response = response;
		this.request = request;
		writer = null;
		out = null;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public String getInfo() {
		return this.info;
	}

	public HttpServletRequest getServletRequest() {
		return this.request;
	}

	public HttpServletResponse getServletResponse() {
		return this.response;
	}

	public PrintWriter getWriter(String encoding) throws IOException {
		if (out != null)
			throw new IOException("OutputStream delivered before");
		if (writer != null)
			return writer;
		response.setCharacterEncoding(encoding);
		writer = response.getWriter();
		return writer;
	}

	public OutputStream getOutputStream() throws IOException {
		if (writer != null)
			throw new IOException("Writer delivered before");
		if (out != null)
			return out;
		out = response.getOutputStream();
		return out;
	}

	public void forward(String path) throws ServletException {
		RequestDispatcher dispatcher = servlet.getServletContext()
				.getRequestDispatcher(path);
		try {
			dispatcher.forward(request, response);
		} catch (javax.servlet.ServletException e) {
			throw new ServletException(e);
		} catch (IOException e) {
			throw new ServletException(e);
		}
	}

	public HashSet<String> getClassDetail(HttpServletRequest request) {
		HashSet<String> classDetail = new HashSet<String>();
		String[] values = request.getParameterValues("details");
		if (values != null)
			for (String value : values)
				classDetail.add(value);
		return classDetail;
	}

}
