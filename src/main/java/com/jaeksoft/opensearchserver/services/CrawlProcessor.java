/*
 * Copyright 2017-2018 Emmanuel Keller / Jaeksoft
 *  <p>
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  <p>
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  <p>
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.jaeksoft.opensearchserver.services;

import com.jaeksoft.opensearchserver.model.CrawlTaskDefinition;
import com.jaeksoft.opensearchserver.model.CrawlTaskInfos;
import com.jaeksoft.opensearchserver.model.TaskInfos;
import com.jaeksoft.opensearchserver.model.TaskRecord;
import com.qwazr.crawler.common.CrawlDefinition;
import com.qwazr.crawler.common.CrawlStatus;
import com.qwazr.crawler.common.CrawlerServiceInterface;
import com.qwazr.server.ServerException;
import com.qwazr.server.client.ErrorWrapper;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Objects;

public abstract class CrawlProcessor<D extends CrawlDefinition, T extends CrawlTaskDefinition<D>, S extends CrawlStatus<D>>
		implements TaskProcessor<S> {

	private final CrawlerServiceInterface<D, S> crawlerService;
	protected final ConfigService configService;
	protected final IndexesService indexesService;
	private final Class<T> crawlTaskDefinitionClass;

	protected CrawlProcessor(final ConfigService configService, final CrawlerServiceInterface<D, S> crawlerService,
			final IndexesService indexesService, final Class<T> crawlTaskDefinitionClass) {
		this.configService = configService;
		this.crawlerService = crawlerService;
		this.indexesService = indexesService;
		this.crawlTaskDefinitionClass = crawlTaskDefinitionClass;
	}

	@Override
	final public S getStatus(final String taskId) {
		return ErrorWrapper.bypass(() -> crawlerService.getSession(taskId), 404);
	}

	@Override
	final public boolean isRunning(final String taskId) {
		final S crawlStatus = getStatus(taskId);
		return crawlStatus != null && crawlStatus.endTime == null;
	}

	@Override
	public void abort(final String taskId) {
		try {
			crawlerService.abortSession(taskId, "User request");
		} catch (WebApplicationException e) {
			if (e.getResponse().getStatus() == 404)
				return;
			throw e;
		} catch (ServerException e) {
			if (e.getStatusCode() == 404)
				return;
			throw e;
		}
	}

	protected T getCrawlTaskDefinition(final TaskRecord taskRecord) {
		return crawlTaskDefinitionClass.cast(taskRecord.getDefinition());
	}

	@Override
	public TaskInfos getTaskInfos(final TaskRecord taskRecord) {
		final T crawlTaskDefinition = getCrawlTaskDefinition(taskRecord);
		final String indexName =
				indexesService.getIndexNameResolver(taskRecord.accountId).get(crawlTaskDefinition.indexUuid);
		if (indexName == null)
			return null;
		Objects.requireNonNull(taskRecord.sessionTimeId, "The sessionTimeId is missing");
		final IndexService indexService = indexesService.getIndex(taskRecord.accountId, indexName);
		return new CrawlTaskInfos(indexService.getCrawlStatusCount(crawlTaskDefinition.id, taskRecord.sessionTimeId),
				indexService.getIndexStatusCount(crawlTaskDefinition.id, taskRecord.sessionTimeId));
	}

	protected abstract D getNextCrawlDefinition(final TaskRecord taskRecord) throws Exception;

	@Override
	final public TaskRecord.Status runSession(final TaskRecord taskRecord) throws Exception {
		final TaskRecord.Status currentStatus = taskRecord.getStatus();
		if (currentStatus != TaskRecord.Status.ACTIVE)
			return currentStatus;
		if (isRunning(taskRecord.getTaskId()))
			return currentStatus;
		try {
			final D crawlDefinition = getNextCrawlDefinition(taskRecord);
			if (crawlDefinition == null)
				return TaskRecord.Status.DONE;
			crawlerService.runSession(taskRecord.getTaskId(), crawlDefinition);
		} catch (WebApplicationException e) {
			if (e.getResponse().getStatus() != Response.Status.CONFLICT.getStatusCode())
				throw e;
		} catch (Exception e) {
			throw e;
		}
		return currentStatus;
	}
}
