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

import com.jaeksoft.opensearchserver.crawler.CrawlerComponents;
import com.jaeksoft.opensearchserver.model.TaskRecord;
import com.jaeksoft.opensearchserver.services.AccountsService;
import com.jaeksoft.opensearchserver.services.ConfigService;
import com.jaeksoft.opensearchserver.services.IndexesService;
import com.jaeksoft.opensearchserver.services.PermissionsService;
import com.jaeksoft.opensearchserver.services.ProcessingService;
import com.jaeksoft.opensearchserver.services.TasksService;
import com.jaeksoft.opensearchserver.services.UsersService;
import com.jaeksoft.opensearchserver.services.WebCrawlProcessingService;
import com.jaeksoft.opensearchserver.services.WebCrawlsService;
import com.qwazr.crawler.web.WebCrawlerManager;
import com.qwazr.crawler.web.WebCrawlerServiceInterface;
import com.qwazr.crawler.web.WebCrawlerSingleClient;
import com.qwazr.database.TableManager;
import com.qwazr.database.TableServiceInterface;
import com.qwazr.database.TableSingleClient;
import com.qwazr.database.store.Tables;
import com.qwazr.library.freemarker.FreeMarkerTool;
import com.qwazr.scripts.ScriptManager;
import com.qwazr.search.index.IndexManager;
import com.qwazr.search.index.IndexServiceInterface;
import com.qwazr.search.index.IndexSingleClient;
import com.qwazr.server.RemoteService;
import com.qwazr.store.StoreManager;
import com.qwazr.store.StoreServiceInterface;
import com.qwazr.store.StoreSingleClient;
import com.qwazr.utils.ExceptionUtils;
import com.qwazr.utils.IOUtils;

import java.io.Closeable;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Components implements Closeable {

	private final Path dataDirectory;

	private final List<Closeable> closing;

	private volatile ExecutorService executorService;

	private volatile ConfigService configService;

	private volatile FreeMarkerTool freemarkerTool;

	private volatile ScriptManager scriptManager;

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

	private volatile TableManager tableManager;
	private volatile TableServiceInterface tableService;
	private volatile UsersService usersService;
	private volatile AccountsService accountsService;
	private volatile PermissionsService permissionsService;

	Components(final Path dataDirectory) {
		this.closing = new ArrayList<>();
		this.dataDirectory = dataDirectory;
		CrawlerComponents.setLocalComponents(this);
	}

	private synchronized ExecutorService getExecutorService() {
		if (executorService == null)
			executorService = Executors.newCachedThreadPool();
		return executorService;
	}

	private synchronized ConfigService getConfigService() throws IOException, URISyntaxException {
		if (configService == null)
			configService = new ConfigService(
					this.dataDirectory.resolve(System.getProperty("com.opensearchserver.config", "config.properties")));
		return configService;
	}

	public synchronized FreeMarkerTool getFreemarkerTool() {
		if (freemarkerTool == null) {
			freemarkerTool = FreeMarkerTool.of()
					.defaultContentType("text/html")
					.defaultEncoding("UTF-8")
					.templateLoader(FreeMarkerTool.Loader.Type.resource,
							"com/jaeksoft/opensearchserver/front/templates/")
					.build();
			freemarkerTool.load();
			closing.add(freemarkerTool);
		}
		return freemarkerTool;
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

	private synchronized IndexServiceInterface getIndexService() throws IOException, URISyntaxException {
		if (indexService == null) {
			if (getConfigService().getIndexServiceUri() != null)
				indexService = new IndexSingleClient(RemoteService.of(getConfigService().getIndexServiceUri()).build());
			else
				indexService = getIndexManager().getService();
		}
		return indexService;
	}

	public synchronized IndexesService getIndexesService() throws IOException, URISyntaxException {
		if (indexesService == null)
			indexesService = new IndexesService(getIndexService());
		return indexesService;
	}

	public synchronized WebCrawlsService getWebCrawlsService() throws IOException, URISyntaxException {
		if (webCrawlsService == null)
			webCrawlsService = new WebCrawlsService(getStoreService());
		return webCrawlsService;
	}

	private synchronized WebCrawlProcessingService getWebCrawlProcessingService()
			throws IOException, URISyntaxException {
		if (webCrawlProcessingService == null)
			webCrawlProcessingService =
					new WebCrawlProcessingService(getConfigService(), getWebCrawlerService(), getIndexesService());
		return webCrawlProcessingService;

	}

	private synchronized Map<Class<? extends TaskRecord>, ProcessingService> getProcessors()
			throws IOException, URISyntaxException {
		if (tasksProcessors == null)
			tasksProcessors = ProcessingService.of().register(getWebCrawlProcessingService()).build();
		return tasksProcessors;
	}

	public synchronized TasksService getTasksService() throws IOException, URISyntaxException {
		if (tasksService == null)
			tasksService = new TasksService(getStoreService(), getProcessors());
		return tasksService;
	}

	private synchronized ScriptManager getScriptManager() {
		if (scriptManager == null)
			scriptManager = new ScriptManager(getExecutorService(), dataDirectory.toFile());
		return scriptManager;
	}

	private synchronized WebCrawlerManager getWebCrawlerManager() {
		if (webCrawlerManager == null)
			webCrawlerManager = new WebCrawlerManager("localhost", getScriptManager(), getExecutorService());
		return webCrawlerManager;
	}

	synchronized WebCrawlerServiceInterface getWebCrawlerService() throws IOException, URISyntaxException {
		if (webCrawlerService == null) {
			if (getConfigService().getCrawlerServiceUri() != null)
				webCrawlerService =
						new WebCrawlerSingleClient(RemoteService.of(getConfigService().getCrawlerServiceUri()).build());
			else
				webCrawlerService = getWebCrawlerManager().getService();
		}
		return webCrawlerService;
	}

	private synchronized StoreManager getStoreManager() throws IOException {
		if (storeManager == null) {
			final Path storeDirectory = dataDirectory.resolve(StoreServiceInterface.SERVICE_NAME);
			if (!Files.exists(storeDirectory))
				Files.createDirectory(storeDirectory);
			storeManager = new StoreManager(getExecutorService(), getScriptManager(), storeDirectory);
		}
		return storeManager;
	}

	synchronized StoreServiceInterface getStoreService() throws IOException, URISyntaxException {
		if (storeService == null) {
			if (getConfigService().getStoreServiceUri() != null)
				storeService = new StoreSingleClient(RemoteService.of(getConfigService().getStoreServiceUri()).build());
			else
				storeService = getStoreManager().getService();
		}
		return storeService;
	}

	private synchronized TableManager getTableManager() throws IOException {
		if (tableManager == null) {
			tableManager = new TableManager(getExecutorService(), TableManager.checkTablesDirectory(dataDirectory));
			Runtime.getRuntime().addShutdownHook(new Thread(Tables::closeAll));
		}
		return tableManager;
	}

	private synchronized TableServiceInterface getTableService() throws IOException, URISyntaxException {
		if (tableService == null) {
			if (getConfigService().getTableServiceUrl() != null)
				tableService = new TableSingleClient(RemoteService.of(getConfigService().getTableServiceUrl()).build());
			else
				tableService = getTableManager().getService();
		}
		return tableService;
	}

	public synchronized UsersService getUsersService() throws IOException, NoSuchMethodException, URISyntaxException {
		if (usersService == null)
			usersService = new UsersService(getConfigService(), getTableService());
		return usersService;
	}

	public synchronized PermissionsService getPermissionsService()
			throws IOException, NoSuchMethodException, URISyntaxException {
		if (permissionsService == null)
			permissionsService = new PermissionsService(getTableService());
		return permissionsService;
	}

	public synchronized AccountsService getAccountsService()
			throws IOException, NoSuchMethodException, URISyntaxException {
		if (accountsService == null)
			accountsService = new AccountsService(getTableService());
		return accountsService;
	}

	@Override
	public synchronized void close() {

		// First we shutdown the executorService
		if (executorService != null)
			executorService.shutdown();

		// Then we close components in reverse order
		int i = closing.size();
		while (i > 0)
			IOUtils.closeQuietly(closing.get(--i));

		// Let's wait for all threads to be done
		if (executorService != null) {
			ExceptionUtils.bypass(() -> executorService.awaitTermination(1, TimeUnit.MINUTES));
			executorService.shutdownNow();
			executorService = null;
		}

		// Set the singletons back to null
		scriptManager = null;

		indexManager = null;
		indexService = null;
		indexesService = null;

		webCrawlerManager = null;
		webCrawlerService = null;
		webCrawlsService = null;

		tasksService = null;
		tasksProcessors = null;
		webCrawlProcessingService = null;

		storeManager = null;
		storeService = null;
	}

}
