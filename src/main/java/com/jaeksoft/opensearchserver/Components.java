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
import com.jaeksoft.opensearchserver.services.AccountsService;
import com.jaeksoft.opensearchserver.services.ConfigService;
import com.jaeksoft.opensearchserver.services.IndexesService;
import com.jaeksoft.opensearchserver.services.JobService;
import com.jaeksoft.opensearchserver.services.PermissionsService;
import com.jaeksoft.opensearchserver.services.SearchService;
import com.jaeksoft.opensearchserver.services.TaskExecutionService;
import com.jaeksoft.opensearchserver.services.TaskProcessor;
import com.jaeksoft.opensearchserver.services.TasksService;
import com.jaeksoft.opensearchserver.services.TemplatesService;
import com.jaeksoft.opensearchserver.services.UsersService;
import com.jaeksoft.opensearchserver.services.WebCrawlProcessor;
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
import com.qwazr.utils.concurrent.SupplierEx;

import javax.ws.rs.InternalServerErrorException;
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
	private volatile TemplatesService templatesService;
	private volatile SearchService searchService;

	private volatile WebCrawlerManager webCrawlerManager;
	private volatile WebCrawlerServiceInterface webCrawlerService;
	private volatile WebCrawlsService webCrawlsService;

	private volatile TasksService tasksService;
	private volatile Map<String, TaskProcessor> tasksProcessors;
	private volatile WebCrawlProcessor webCrawlProcessor;
	private volatile TaskExecutionService taskExecutionService;

	private volatile StoreManager storeManager;
	private volatile StoreServiceInterface storeService;

	private volatile TableManager tableManager;
	private volatile TableServiceInterface tableService;
	private volatile UsersService usersService;
	private volatile AccountsService accountsService;
	private volatile PermissionsService permissionsService;

	private volatile JobService jobService;

	Components(final Path dataDirectory) {
		this.closing = new ArrayList<>();
		this.dataDirectory = dataDirectory;
		CrawlerComponents.setLocalComponents(this);
	}

	private static <T> T bypass(SupplierEx<T, Exception> supplierEx) {
		try {
			return supplierEx.get();
		} catch (Exception e) {
			throw new InternalServerErrorException(e);
		}
	}

	private synchronized ExecutorService getExecutorService() {
		if (executorService == null)
			executorService = Executors.newCachedThreadPool();
		return executorService;
	}

	private synchronized ConfigService getConfigService() {
		if (configService == null)
			configService = bypass(() -> new ConfigService(this.dataDirectory.resolve(
					System.getProperty("com.opensearchserver.config", "config.properties"))));
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

	private synchronized IndexManager getIndexManager() {
		if (indexManager == null) {
			indexManager = bypass(() -> {
				final Path indexesDirectory = dataDirectory.resolve(IndexServiceInterface.PATH);
				if (!Files.exists(indexesDirectory))
					Files.createDirectory(indexesDirectory);
				return new IndexManager(indexesDirectory, getExecutorService());
			});
			closing.add(indexManager);
		}
		return indexManager;
	}

	private synchronized IndexServiceInterface getIndexService() {
		if (indexService == null) {
			if (getConfigService().getIndexServiceUri() != null)
				indexService = new IndexSingleClient(RemoteService.of(getConfigService().getIndexServiceUri()).build());
			else
				indexService = getIndexManager().getService();
		}
		return indexService;
	}

	public synchronized IndexesService getIndexesService() {
		if (indexesService == null)
			indexesService = new IndexesService(getIndexService());
		return indexesService;
	}

	public synchronized TemplatesService getTemplatesService() {
		if (templatesService == null) {
			templatesService = new TemplatesService(getStoreService());
			closing.add(templatesService);
		}
		return templatesService;
	}

	public synchronized SearchService getSearchService() {
		if (searchService == null)
			searchService = new SearchService();
		return searchService;
	}

	public synchronized WebCrawlsService getWebCrawlsService() {
		if (webCrawlsService == null)
			webCrawlsService = new WebCrawlsService(getStoreService());
		return webCrawlsService;
	}

	private synchronized WebCrawlProcessor getWebCrawlProcessor() {
		if (webCrawlProcessor == null)
			webCrawlProcessor = new WebCrawlProcessor(getConfigService(), getWebCrawlerService(), getIndexesService());
		return webCrawlProcessor;

	}

	private synchronized Map<String, TaskProcessor> getProcessors() {
		if (tasksProcessors == null)
			tasksProcessors = TaskProcessor.of().register(getWebCrawlProcessor()).build();
		return tasksProcessors;
	}

	public synchronized TasksService getTasksService() {
		if (tasksService == null)
			tasksService = bypass(() -> new TasksService(getTableService(), getTaskExecutionService()));
		return tasksService;
	}

	public synchronized TaskExecutionService getTaskExecutionService() {
		if (taskExecutionService == null)
			taskExecutionService = bypass(() -> new TaskExecutionService(getTableService(), getProcessors()));
		return taskExecutionService;
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

	protected synchronized WebCrawlerServiceInterface getWebCrawlerService() {
		if (webCrawlerService == null) {
			if (getConfigService().getCrawlerServiceUri() != null)
				webCrawlerService =
						new WebCrawlerSingleClient(RemoteService.of(getConfigService().getCrawlerServiceUri()).build());
			else
				webCrawlerService = getWebCrawlerManager().getService();
		}
		return webCrawlerService;
	}

	private synchronized StoreManager getStoreManager() {
		if (storeManager == null) {
			storeManager = bypass(() -> {
				final Path storeDirectory = dataDirectory.resolve(StoreServiceInterface.SERVICE_NAME);
				if (!Files.exists(storeDirectory))
					Files.createDirectory(storeDirectory);
				return new StoreManager(getExecutorService(), getScriptManager(), storeDirectory);
			});
			closing.add(storeManager);
		}
		return storeManager;
	}

	synchronized StoreServiceInterface getStoreService() {
		if (storeService == null) {
			if (getConfigService().getStoreServiceUri() != null)
				storeService = new StoreSingleClient(RemoteService.of(getConfigService().getStoreServiceUri()).build());
			else
				storeService = getStoreManager().getService();
		}
		return storeService;
	}

	private synchronized TableManager getTableManager() {
		if (tableManager == null) {
			try {
				tableManager = new TableManager(getExecutorService(), TableManager.checkTablesDirectory(dataDirectory));
			} catch (IOException e) {
				throw new InternalServerErrorException(e);
			}
			Runtime.getRuntime().addShutdownHook(new Thread(Tables::closeAll));
		}
		return tableManager;
	}

	private synchronized TableServiceInterface getTableService() {
		if (tableService == null) {
			if (getConfigService().getTableServiceUrl() != null)
				tableService = new TableSingleClient(RemoteService.of(getConfigService().getTableServiceUrl()).build());
			else
				tableService = getTableManager().getService();
		}
		return tableService;
	}

	public synchronized UsersService getUsersService() {
		if (usersService == null)
			usersService = bypass(() -> new UsersService(getConfigService(), getTableService()));
		return usersService;
	}

	public synchronized PermissionsService getPermissionsService() {
		if (permissionsService == null) {
			try {
				permissionsService = new PermissionsService(getTableService());
			} catch (NoSuchMethodException | URISyntaxException e) {
				throw new InternalServerErrorException(e);
			}
		}
		return permissionsService;
	}

	public synchronized AccountsService getAccountsService() {
		if (accountsService == null)
			accountsService = bypass(() -> new AccountsService(getTableService()));
		return accountsService;
	}

	public synchronized JobService getJobService() {
		if (jobService == null) {
			jobService =
					new JobService(getConfigService(), getAccountsService(), getTasksService(), getIndexesService(),
							getTemplatesService(), getTaskExecutionService());
			closing.add(jobService);
		}
		return jobService;
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

		Tables.closeAll();

		// Set the singletons back to null
		scriptManager = null;

		indexManager = null;
		indexService = null;
		indexesService = null;

		webCrawlerManager = null;
		webCrawlerService = null;
		webCrawlsService = null;

		tasksService = null;
		taskExecutionService = null;
		tasksProcessors = null;
		webCrawlProcessor = null;

		storeManager = null;
		storeService = null;
	}

}
