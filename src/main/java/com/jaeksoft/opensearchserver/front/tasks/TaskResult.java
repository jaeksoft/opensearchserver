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

package com.jaeksoft.opensearchserver.front.tasks;

import com.jaeksoft.opensearchserver.model.CrawlTaskRecord;
import com.jaeksoft.opensearchserver.model.TaskRecord;
import com.jaeksoft.opensearchserver.model.WebCrawlRecord;
import com.jaeksoft.opensearchserver.model.WebCrawlTaskRecord;
import com.jaeksoft.opensearchserver.services.IndexesService;
import com.jaeksoft.opensearchserver.services.WebCrawlsService;
import com.qwazr.utils.ExceptionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

public class TaskResult {

	private final TaskRecord task;

	private final String indexName;

	private final String crawlName;

	private TaskResult(final TaskRecord task, final String indexName, final String crawlName) {
		this.task = task;
		this.indexName = indexName;
		this.crawlName = crawlName;
	}

	public static Builder of(final IndexesService indexesService, final WebCrawlsService webCrawlsService) {
		return new Builder(indexesService, webCrawlsService);
	}

	public static class Builder {

		private final Map<UUID, String> indexNameResolver;
		private final Map<String, String> crawlResolver;
		private final WebCrawlsService webCrawlsService;

		private volatile List<TaskResult> results;

		Builder(final IndexesService indexesService, final WebCrawlsService webCrawlsService) {
			indexNameResolver = indexesService.getIndexNameResolver();
			if (webCrawlsService != null) {
				this.webCrawlsService = webCrawlsService;
				this.crawlResolver = new HashMap<>();
			} else {
				this.webCrawlsService = null;
				this.crawlResolver = null;
			}
		}

		public void add(TaskRecord taskRecord) {
			if (results == null)
				results = new ArrayList<>();
			String indexName = null;
			String crawlName = null;
			if (taskRecord instanceof CrawlTaskRecord) {
				final CrawlTaskRecord crawlTask = (CrawlTaskRecord) taskRecord;
				indexName = indexNameResolver.get(crawlTask.indexUuid);
				if (taskRecord instanceof WebCrawlTaskRecord) {
					if (webCrawlsService != null && crawlResolver != null) {
						crawlName = crawlResolver.computeIfAbsent(taskRecord.taskId, taskId -> {
							final WebCrawlRecord webCrawlRecord =
									ExceptionUtils.bypass(() -> webCrawlsService.read(crawlTask.crawlUuid));
							return webCrawlRecord == null ? null : webCrawlRecord.getName();
						});
					}
				}
			}
			results.add(new TaskResult(taskRecord, indexName, crawlName));
		}

		public List<TaskResult> build() {
			return results == null ? Collections.emptyList() : results;
		}
	}
}
