/*
 * Copyright 2017-2020 Emmanuel Keller / Jaeksoft
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

import com.qwazr.server.ApplicationBuilder;
import com.qwazr.server.CorsFilter;
import com.qwazr.server.GenericServer;
import com.qwazr.server.GenericServerBuilder;
import com.qwazr.server.RestApplication;
import com.qwazr.server.configuration.ServerConfiguration;
import com.qwazr.webapps.WebappManager;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

import static com.qwazr.webapps.WebappManager.DEFAULT_EXPIRATION_TIME;

public class Server extends Components {

    private final GenericServer server;

    private Server(final ServerConfiguration configuration) throws IOException {
        super(configuration.dataDirectory);

        final GenericServerBuilder serverBuilder = GenericServer.of(configuration)
            .webAppAccessLogger(Logger.getLogger("com.qwazr.AccessLogs"))
            .sessionPersistenceManager(getSessionPersistenceManager());
        ;
        final WebappManager.Builder webAppBuilder = WebappManager.of(serverBuilder, serverBuilder.getWebAppContext())
            .registerDefaultFaviconServlet()
            .registerWebjars()
            .registerStaticServlet("/s/*", "com.jaeksoft.opensearchserver.front.statics")
            .registerJavaServlet(AppServlet.class, () -> new AppServlet(this))
            .registerJaxRsResources(ApplicationBuilder.of("/ws/*")
                .classes(RestApplication.WithAuth.JSON_CLASSES)
                .singletons(getIndexService(), new CorsFilter()));
        if (!getConfigService().isProduction())
            webAppBuilder.registerStaticServlet("/jsx/*", Path.of("src", "main", "jsx"), DEFAULT_EXPIRATION_TIME);

        webAppBuilder.build();
        server = serverBuilder.build();
    }

    @Override
    public void close() {
        server.close();
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
