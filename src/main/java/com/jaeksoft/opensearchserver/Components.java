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

import com.jaeksoft.opensearchserver.model.TaskRecord;
import com.jaeksoft.opensearchserver.services.IndexesService;
import com.jaeksoft.opensearchserver.services.ProcessingService;
import com.jaeksoft.opensearchserver.services.TasksService;
import com.jaeksoft.opensearchserver.services.WebCrawlProcessingService;
import com.jaeksoft.opensearchserver.services.WebCrawlsService;
import com.qwazr.crawler.web.WebCrawlerManager;
import com.qwazr.crawler.web.WebCrawlerServiceInterface;
import com.qwazr.scheduler.SchedulerManager;
import com.qwazr.scheduler.SchedulerServiceInterface;
import com.qwazr.scripts.ScriptManager;
import com.qwazr.search.index.IndexManager;
import com.qwazr.search.index.IndexServiceInterface;
import com.qwazr.search.index.SchemaSettingsDefinition;
import com.qwazr.store.StoreManager;
import com.qwazr.store.StoreServiceInterface;
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.ObjectMappers;
import org.quartz.SchedulerException;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Components implements Closeable {

	private final static String DEFAULT_SCHEMA = System.getProperty("OSS_SCHEMA", "opensearchserver");

	private final static String WEB_CRAWLS_DIRECTORY = "web_crawls";

	private final Path dataDirectory;

	private final List<Closeable> closing;

	private volatile ExecutorService executorService;

	private volatile ScriptManager scriptManager;
	private volatile SchedulerManager schedulerManager;
	private volatile SchedulerServiceInterface schedulerServiceInterface;

	private volatile IndexManager indexManager;
	private volatile IndexServiceInterface indexService;
	private volatile IndexesService indexesService;

	private volatile WebCrawlerManager webCrawlerManager;
	private volatile WebCrawlerServiceInterface webCrawlerService;
	private volatile WebCrawlsService webCrawlsService;

	private volatile TasksService tasksService;
	private volatile Map<Class<? extends TaskRecord>, ProcessingService> tasksProcessors;
	private volatile WebCrawlProcessingService webCrawlProcessingService;

	private volatile StoreManager storeManager;
	private volatile StoreServiceInterface storeService;

	Components(final Path dataDirectory) {
		closing = new ArrayList<>();
		this.dataDirectory = dataDirectory;
	}

	private synchronized String getAccountSchema() {
		return "local";
	}

	private synchronized ExecutorService getExecutorService() {
		if (executorService == null)
			executorService = Executors.newCachedThreadPool();
		return executorService;
	}

	private synchronized IndexManager getIndexManager() throws IOException {
		if (indexManager == null) {
			final Path indexesDirectory = dataDirectory.resolve(IndexServiceInterface.PATH);
			if (!Files.exists(indexesDirectory))
				Files.createDirectory(indexesDirectory);
			indexManager = new IndexManager(indexesDirectory, getExecutorService());
			closing.add(indexManager);
		}
		return indexManager;
	}

	private synchronized IndexServiceInterface getIndexService() throws IOException {
		if (indexService == null)
			indexService = getIndexManager().getService();
		return indexService;
	}

	synchronized IndexesService getIndexesService() throws IOException {
		if (indexesService == null)
			indexesService = new IndexesService(getIndexService(), DEFAULT_SCHEMA, getSchemaDefinition(DEFAULT_SCHEMA));
		return indexesService;
	}

	synchronized WebCrawlsService getWebCrawlsService() throws IOException {
		if (webCrawlsService == null) {
			final Path webCrawlsDirectory = dataDirectory.resolve(WEB_CRAWLS_DIRECTORY);
			if (!Files.exists(webCrawlsDirectory))
				Files.createDirectory(webCrawlsDirectory);
			webCrawlsService = new WebCrawlsService(getStoreService(), getAccountSchema());
		}
		return webCrawlsService;
	}

	private synchronized WebCrawlProcessingService getWebCrawlProcessingService() throws IOException {
		if (webCrawlProcessingService == null)
			webCrawlProcessingService = new WebCrawlProcessingService(getWebCrawlerService(), getIndexesService());
		return webCrawlProcessingService;

	}

	private synchronized Map<Class<? extends TaskRecord>, ProcessingService> getProcessors() throws IOException {
		if (tasksProcessors == null)
			tasksProcessors = ProcessingService.of().register(getWebCrawlProcessingService()).build();
		return tasksProcessors;
	}

	synchronized TasksService getTasksService() throws IOException {
		if (tasksService == null)
			tasksService = new TasksService(getStoreService(), getAccountSchema(), getProcessors());
		return tasksService;
	}

	private synchronized ScriptManager getScriptManager() {
		if (scriptManager == null)
			scriptManager = new ScriptManager(getExecutorService(), dataDirectory.toFile());
		return scriptManager;
	}

	private synchronized SchedulerManager getSchedulerManager() throws IOException, SchedulerException {
		if (schedulerManager == null) {
			schedulerManager = new SchedulerManager(getExecutorService(), null, getScriptManager(), 50, null);
			closing.add(schedulerManager);
		}
		return schedulerManager;
	}

	private synchronized WebCrawlerManager getWebCrawlerManager() {
		if (webCrawlerManager == null)
			webCrawlerManager = new WebCrawlerManager("localhost", getScriptManager(), getExecutorService());
		return webCrawlerManager;
	}

	synchronized WebCrawlerServiceInterface getWebCrawlerService() {
		if (webCrawlerService == null)
			webCrawlerService = getWebCrawlerManager().getService();
		return webCrawlerService;
	}

	synchronized SchedulerServiceInterface getSchedulerService() throws IOException, SchedulerException {
		if (schedulerServiceInterface == null)
			schedulerServiceInterface = getSchedulerManager().getService();
		return schedulerServiceInterface;
	}

	synchronized SchemaSettingsDefinition getSchemaDefinition(final String schemaName) throws IOException {
		final Path schemaConfig = dataDirectory.resolve(schemaName + ".json");
		return Files.exists(schemaConfig) ?
				ObjectMappers.JSON.readValue(schemaConfig.toFile(), SchemaSettingsDefinition.class) :
				null;
	}

	private synchronized StoreManager getStoreManager() throws IOException {
		if (storeManager == null) {
			final Path storeDirectory = dataDirectory.resolve(StoreServiceInterface.SERVICE_NAME);
			if (!Files.exists(storeDirectory))
				Files.createDirectory(storeDirectory);
			storeManager = new StoreManager(null, getExecutorService(), getScriptManager(), storeDirectory);
		}
		return storeManager;
	}

	synchronized StoreServiceInterface getStoreService() throws IOException {
		if (storeService == null)
			storeService = getStoreManager().getService();
		return storeService;
	}

	@Override
	public void close() {
		// Close components in reverse order
		int i = closing.size();
		while (i > 0)
			IOUtils.closeQuietly(closing.get(--i));
		if (executorService != null) {
			executorService.shutdown();
			executorService = null;
		}
	}
}
