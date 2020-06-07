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

import com.qwazr.crawler.web.WebCrawlerManager;
import com.qwazr.crawler.web.WebCrawlerServiceInterface;
import com.qwazr.crawler.web.WebCrawlerSingleClient;
import com.qwazr.library.AbstractLibrary;
import com.qwazr.library.freemarker.FreeMarkerTool;
import com.qwazr.scripts.ScriptManager;
import com.qwazr.search.index.IndexManager;
import com.qwazr.search.index.IndexServiceInterface;
import com.qwazr.search.index.IndexSingleClient;
import com.qwazr.server.InFileSessionPersistenceManager;
import com.qwazr.server.RemoteService;
import com.qwazr.utils.LoggerUtils;
import com.qwazr.utils.concurrent.ConsumerEx;
import com.qwazr.utils.concurrent.SupplierEx;
import io.undertow.servlet.api.SessionPersistenceManager;

import javax.annotation.concurrent.ThreadSafe;
import javax.ws.rs.InternalServerErrorException;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Components implements Closeable {

    private final static Logger LOGGER = LoggerUtils.getLogger(Components.class);

    private final Path dataDirectory;

    private final List<AtomicProvider<?>> providers = new ArrayList<>();
    private final List<AtomicProvider<?>> providersWithAutoCloseableValue = new ArrayList<>();

    private final AtomicProvider<ExecutorService> executorService = new AtomicProvider<>();

    private final AtomicProvider<ConfigService> configService = new AtomicProvider<>();

    private final AtomicProvider<FreeMarkerTool> freemarkerTool = new AtomicProvider<>();

    private final AtomicProvider<ScriptManager> scriptManager = new AtomicProvider<>();

    private final AtomicProvider<IndexManager> indexManager = new AtomicProvider<>();
    private final AtomicProvider<IndexServiceInterface> indexService = new AtomicProvider<>();

    private final AtomicProvider<WebCrawlerManager> webCrawlerManager = new AtomicProvider<>();
    private final AtomicProvider<WebCrawlerServiceInterface> webCrawlerService = new AtomicProvider<>();

    private final AtomicProvider<SessionPersistenceManager> sessionPersistenceManager = new AtomicProvider<>();

    Components(final Path dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    /**
     * Manage to provide a singleton
     *
     * @param <T>
     */
    @ThreadSafe
    private class AtomicProvider<T> implements AutoCloseable {

        private volatile T value;

        private AtomicProvider() {
            providers.add(this);
            this.value = null;
        }

        T get(final SupplierEx<T, Exception> defaultSupplier) {
            if (value != null)
                return value;
            synchronized (this) {
                if (value != null)
                    return value;
                try {
                    value = defaultSupplier.get();
                    if (value instanceof AutoCloseable)
                        providersWithAutoCloseableValue.add(this);
                    if (value instanceof AbstractLibrary)
                        ((AbstractLibrary) value).load();
                }
                catch (Exception e) {
                    throw new InternalServerErrorException("Cannot create the component", e);
                }
                return value;
            }
        }

        void ifPresent(final boolean silentlyLogException, final ConsumerEx<T, Exception> consumer) {
            synchronized (this) {
                try {
                    if (value != null)
                        consumer.accept(value);
                }
                catch (Exception e) {
                    final String error = "Error while consuming the component: " + value;
                    if (silentlyLogException)
                        LOGGER.log(Level.WARNING, error, e);
                    else
                        throw new InternalServerErrorException(error, e);
                }
            }
        }

        @Override
        public void close() {
            synchronized (this) {
                if (value != null && value instanceof AutoCloseable) {
                    try {
                        ((AutoCloseable) value).close();
                    }
                    catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Error while closing the component: " + value, e);
                    }
                    value = null;
                }
            }
        }
    }

    private Path createDataSubDirectoryIfNotExists(final String resolve) throws IOException {
        final Path directory = dataDirectory.resolve(resolve);
        if (!Files.exists(directory))
            Files.createDirectory(directory);
        return directory;
    }

    private ExecutorService getExecutorService() {
        return executorService.get(Executors::newCachedThreadPool);
    }

    public ConfigService getConfigService() {
        return configService.get(() -> new ConfigService(
            dataDirectory.resolve(System.getProperty("com.opensearchserver.config", "config.properties"))));
    }

    public FreeMarkerTool getFreemarkerTool() {
        return freemarkerTool.get(() -> FreeMarkerTool.of()
            .defaultContentType("text/html")
            .defaultEncoding("UTF-8")
            .templateLoader(FreeMarkerTool.Loader.Type.resource, "com/jaeksoft/opensearchserver/front/templates/")
            .build());
    }

    private IndexManager getIndexManager() {
        return indexManager.get(() -> new IndexManager(createDataSubDirectoryIfNotExists(IndexServiceInterface.PATH),
            getExecutorService()));
    }

    public IndexServiceInterface getIndexService() {
        return indexService.get(() -> {
            if (getConfigService().getIndexServiceUri() != null)
                return new IndexSingleClient(RemoteService.of(getConfigService().getIndexServiceUri()).build());
            else
                return getIndexManager().getService();
        });
    }

    private ScriptManager getScriptManager() {
        return scriptManager.get(() -> new ScriptManager(getExecutorService(), dataDirectory));
    }

    private WebCrawlerManager getWebCrawlerManager() {
        return webCrawlerManager.get(
            () -> new WebCrawlerManager("localhost", getScriptManager(), getExecutorService()));
    }

    protected WebCrawlerServiceInterface getWebCrawlerService() {
        return webCrawlerService.get(() -> {
            if (getConfigService().getCrawlerServiceUri() != null)
                return new WebCrawlerSingleClient(RemoteService.of(getConfigService().getCrawlerServiceUri()).build());
            else
                return getWebCrawlerManager().getService();
        });
    }

    public SessionPersistenceManager getSessionPersistenceManager() {
        return sessionPersistenceManager.get(
            () -> new InFileSessionPersistenceManager(createDataSubDirectoryIfNotExists("web-sessions")));
    }

    @Override
    public synchronized void close() {

        // First we shutdown the executorService
        executorService.ifPresent(true, ExecutorService::shutdown);

        // Then we close components in reverse order
        int i = providersWithAutoCloseableValue.size();
        while (i > 0)
            providersWithAutoCloseableValue.get(--i).close();

        // Let's wait 5 minutes for all threads to be done
        executorService.ifPresent(true, executor -> executor.awaitTermination(5, TimeUnit.MINUTES));

        // Second chance shutdown
        executorService.ifPresent(true, ExecutorService::shutdownNow);

        // Closing/resetting every providers
        providers.forEach(AtomicProvider::close);

    }

}
