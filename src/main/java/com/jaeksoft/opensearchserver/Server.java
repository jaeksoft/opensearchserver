/*
 * Copyright 2017-2018 Emmanuel Keller / Jaeksoft
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

import com.jaeksoft.opensearchserver.front.HomeServlet;
import com.jaeksoft.opensearchserver.front.LogoutServlet;
import com.jaeksoft.opensearchserver.front.SigninServlet;
import com.jaeksoft.opensearchserver.front.accounts.AccountsServlet;
import com.jaeksoft.opensearchserver.front.admin.AdminAccountsServlet;
import com.jaeksoft.opensearchserver.front.admin.AdminUsersServlet;
import com.qwazr.server.GenericServer;
import com.qwazr.server.GenericServerBuilder;
import com.qwazr.server.configuration.ServerConfiguration;
import com.qwazr.utils.ExceptionUtils;
import com.qwazr.webapps.WebappManager;

import java.io.IOException;

public class Server extends Components {

	private final GenericServer server;

	private Server(final ServerConfiguration configuration) throws IOException {
		super(configuration.dataDirectory.toPath());

		final GenericServerBuilder serverBuilder = GenericServer.of(configuration);
		final WebappManager.Builder webAppBuilder = WebappManager.of(serverBuilder, serverBuilder.getWebAppContext())
				.registerDefaultFaviconServlet()
				.registerWebjars()
				.registerStaticServlet("/s/*", "com.jaeksoft.opensearchserver.front.statics")
				.registerJavaServlet(HomeServlet.class, () -> new HomeServlet(this))
				.registerJavaServlet(SigninServlet.class, () -> new SigninServlet(this))
				.registerJavaServlet(LogoutServlet.class, LogoutServlet::new)
				.registerJavaServlet(AccountsServlet.class, () -> new AccountsServlet(this))
				.registerJavaServlet(AdminUsersServlet.class, () -> new AdminUsersServlet(this))
				.registerJavaServlet(AdminAccountsServlet.class, () -> new AdminAccountsServlet(this));
		serverBuilder.identityManagerProvider(realm -> ExceptionUtils.bypass(this::getUsersService));
		webAppBuilder.build();
		server = serverBuilder.build();
	}

	@Override
	public void close() {
		server.stopAll();
		super.close();
	}

	private static volatile Server instance;

	public static Server getInstance() {
		return instance;
	}

	public static synchronized void main(final String... args) throws Exception {
		if (instance != null)
			throw new Exception("The instance has already be started");
		instance = new Server(new ServerConfiguration(args));
		instance.server.start(true);
	}

	public static synchronized void stop() {
		if (instance == null)
			return;
		instance.close();
		instance = null;
	}

}