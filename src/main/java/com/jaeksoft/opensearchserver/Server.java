/*
 * Copyright 2017 Emmanuel Keller / Jaeksoft
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jaeksoft.opensearchserver;

import com.jaeksoft.opensearchserver.front.IndexServlet;
import com.qwazr.library.freemarker.FreeMarkerTool;
import com.qwazr.server.GenericServer;
import com.qwazr.server.GenericServerBuilder;
import com.qwazr.server.configuration.ServerConfiguration;
import com.qwazr.webapps.WebappManager;

import javax.management.JMException;
import javax.servlet.ServletException;
import java.io.IOException;

public class Server {

	private final GenericServer server;

	private Server(final ServerConfiguration configuration)
			throws IOException, ServletException, ReflectiveOperationException, JMException {

		final FreeMarkerTool freemarker = FreeMarkerTool.of()
				.defaultContentType("text/html")
				.defaultEncoding("UTF-8")
				.templateLoader(FreeMarkerTool.Loader.Type.resource, "com/jaeksoft/opensearchserver/front/templates/")
				.build();
		freemarker.load();

		final GenericServerBuilder serverBuilder = GenericServer.of(configuration);
		final WebappManager.Builder webAppBuilder = WebappManager.of(serverBuilder, serverBuilder.getWebAppContext())
				.registerDefaultFaviconServlet()
				.registerWebjars()
				.registerStaticServlet("/s/*", "com.jaeksoft.opensearchserver.front.statics")
				.registerJavaServlet(IndexServlet.class, () -> new IndexServlet(freemarker));
		webAppBuilder.build();

		server = serverBuilder.build();
		server.start(true);
	}

	private static volatile Server instance;

	public static Server getInstance() {
		return instance;
	}

	public static synchronized void main(final String... args) throws Exception {
		if (instance != null)
			throw new Exception("The instance has already be started");
		instance = new Server(new ServerConfiguration(args));
	}

	public static synchronized void stop() {
		if (instance == null)
			return;
		instance.server.stopAll();
		instance = null;
	}
}