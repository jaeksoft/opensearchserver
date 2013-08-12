/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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

import javax.servlet.ServletConfig;
import javax.xml.ws.Endpoint;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.jaxrs.servlet.CXFNonSpringJaxrsServlet;

import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.webservice.WebServiceEnum;

public class ServicesServlet extends CXFNonSpringJaxrsServlet {

	public class ThreadedLoad implements Runnable {

		public ThreadedLoad() {
			new Thread(ClientCatalog.getThreadGroup(), this).start();
		}

		@Override
		public void run() {
			for (WebServiceEnum webServiceEnum : WebServiceEnum.values()) {
				try {
					if (webServiceEnum.defaultPath != null)
						Endpoint.publish(webServiceEnum.defaultPath,
								webServiceEnum.getNewInstance());
				} catch (InstantiationException e) {
					Logging.error(e);
				} catch (IllegalAccessException e) {
					Logging.error(e);
				}
			}
		}

	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 2295475119745093594L;

	@Override
	public void loadBus(ServletConfig servletConfig) {
		super.loadBus(servletConfig);

		Bus bus = getBus();
		BusFactory.setDefaultBus(bus);

		if (!ClientFactory.INSTANCE.getSoapActive().isValue())
			return;

		new ThreadedLoad();

	}
}
