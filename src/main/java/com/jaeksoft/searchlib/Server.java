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
package com.jaeksoft.searchlib;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletInfo;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import com.opensearchserver.utils.StringUtils;
import com.jaeksoft.searchlib.web.StartStopListener;
import com.jaeksoft.searchlib.web.servlet.RestServlet;
import com.jaeksoft.searchlib.web.servlet.ServletArray;

public class Server implements HttpHandler {

	private final HttpHandler defaultHandler;
	private final HttpHandler resourceHandler;

	private final String[] resourcesExact = { "/favicon.ico" };
	private final String[] resourcesPrefix = { "/js", "/css", "/images",
			"/fonts" };

	private Server(final HttpHandler defaultHandler) {
		// Default servlet handler
		this.defaultHandler = defaultHandler;
		// Load from resource
		this.resourceHandler = Handlers.resource(new ClassPathResourceManager(
				Server.class.getClassLoader(), ""));
	}

	@Override
	final public void handleRequest(final HttpServerExchange exchange)
			throws Exception {
		final String path = exchange.getRelativePath();
		if (path == null) {
			defaultHandler.handleRequest(exchange);
			return;
		}
		for (String s : resourcesExact) {
			if (path.equals(s)) {
				resourceHandler.handleRequest(exchange);
				return;
			}
		}
		for (String s : resourcesPrefix) {
			if (path.startsWith(s)) {
				resourceHandler.handleRequest(exchange);
				return;
			}
		}
		defaultHandler.handleRequest(exchange);
	}

	final public static void registerServlet(DeploymentInfo deployInfo,
			Class<? extends HttpServlet> httpServletClass) {
		WebServlet webServlet = httpServletClass
				.getAnnotation(WebServlet.class);
		if (webServlet == null) {
			Logging.warn("Unable to load the servlet. The WebServlet annotation is missing: "
					+ httpServletClass.getName());
			return;
		}
		String name = webServlet.name();
		if (StringUtils.isEmpty(name))
			name = httpServletClass.getName();
		ServletInfo servletInfo = Servlets.servlet(name, httpServletClass);
		WebInitParam[] initParams = webServlet.initParams();
		if (initParams != null)
			for (WebInitParam initParam : initParams)
				servletInfo.addInitParam(initParam.name(), initParam.value());
		servletInfo.setLoadOnStartup(webServlet.loadOnStartup());
		String[] urlPatterns = webServlet.urlPatterns();
		if (urlPatterns == null) {
			Logging.warn("Unable to load the servlet. The urlPatterns array is missing: "
					+ httpServletClass.getName());
			return;
		}
		for (String urlPattern : urlPatterns)
			servletInfo.addMapping(urlPattern);
		deployInfo.addServlet(servletInfo);
	}

	public static void main(final String[] args) throws ServletException {

		DeploymentInfo deployInfo = Servlets.deployment()
				.setClassLoader(Server.class.getClassLoader())
				.setContextPath("/").setDeploymentName("oss.war")
				.setIgnoreFlush(true)
				.addServletExtension(new StartStopListener());

		// Apache CXF
		registerServlet(deployInfo, RestServlet.class);

		// UI & Rest V1
		ServletArray.register(deployInfo);

		DeploymentManager manager = Servlets.defaultContainer().addDeployment(
				deployInfo);

		manager.deploy();
		Server server = new Server(manager.start());

		Undertow undertow = Undertow.builder()
				.addHttpListener(9090, "localhost").setHandler(server).build();
		undertow.start();
	}

}
