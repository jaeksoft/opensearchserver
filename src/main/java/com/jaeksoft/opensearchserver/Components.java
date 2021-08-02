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

import com.qwazr.search.index.IndexManager;
import com.qwazr.search.index.IndexServiceInterface;
import com.qwazr.search.index.IndexSingleClient;
import com.qwazr.server.RemoteService;
import com.qwazr.utils.LoggerUtils;
import com.qwazr.utils.concurrent.ConsumerEx;
import com.qwazr.utils.concurrent.SupplierEx;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.ThreadSafe;
import javax.ws.rs.InternalServerErrorException;

public class Components implements Closeable {

    public final static String INDEX_SERVICE_ATTRIBUTE = "indexService";

    private final static Logger LOGGER = LoggerUtils.getLogger(Components.class);

    private final Path dataDirectory;

    private final List<AtomicProvider<?>> providers = new ArrayList<>();
    private final LinkedList<AtomicProvider<?>> providersWithAutoCloseableValue = new LinkedList<>();

    private final AtomicProvider<ExecutorService> executorService = new AtomicProvider<>();

    private final AtomicProvider<ConfigService> configService = new AtomicProvider<>();

    private final AtomicProvider<IndexManager> indexManager = new AtomicProvider<>();
    private final AtomicProvider<IndexServiceInterface> indexService = new AtomicProvider<>();

    private final AtomicProvider<GraphQLService> graphqlService = new AtomicProvider<>();

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
                        providersWithAutoCloseableValue.addFirst(this);
                } catch (Exception e) {
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
                } catch (Exception e) {
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
                    } catch (Exception e) {
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

    public GraphQLService getGraphQLService() {
        return graphqlService.get(() -> new GraphQLService(getIndexService()));
    }

    @Override
    public synchronized void close() {

        // Then we close components in reverse order
        providersWithAutoCloseableValue.forEach(AtomicProvider::close);
        providersWithAutoCloseableValue.clear();

        // Closing/resetting every providers
        providers.forEach(AtomicProvider::close);
        providers.clear();

    }

}
