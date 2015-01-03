/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2015 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.servlet;

import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;

import org.apache.cxf.jaxrs.servlet.CXFNonSpringJaxrsServlet;

@WebServlet(loadOnStartup = 1, urlPatterns = { "/services/rest/*", "/rest/*" }, initParams = { @WebInitParam(name = "javax.ws.rs.Application", value = "com.jaeksoft.searchlib.webservice.RestApplication") })
public class RestServlet extends CXFNonSpringJaxrsServlet {

	private static final long serialVersionUID = 4673061148192337149L;

}
