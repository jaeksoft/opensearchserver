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

import com.jaeksoft.opensearchserver.BaseTest;
import com.jaeksoft.opensearchserver.model.TaskRecord;
import com.jaeksoft.opensearchserver.model.UrlRecord;
import com.jaeksoft.opensearchserver.model.WebCrawlRecord;
import com.jaeksoft.opensearchserver.model.WebCrawlTaskRecord;
import com.qwazr.crawler.web.WebCrawlDefinition;
import com.qwazr.search.annotations.AnnotatedIndexService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.NotAllowedException;
import javax.ws.rs.core.NoContentException;
import java.io.IOException;
import java.util.UUID;

public class TasksServiceTest extends BaseTest {

	private TasksService tasksService;

	@Before
	public void setup() throws IOException {
		tasksService = getTasksService();
	}

	private void checkResult(StoreService.RecordsResult results, int expectedResults, TaskRecord... records) {
		Assert.assertNotNull(results);
		Assert.assertEquals(expectedResults, results.getTotalCount());
		Assert.assertEquals(records.length, results.getRecords().size());
	}

	@Test
	public void emptyList() throws IOException {
		checkResult(tasksService.getActiveTasks(0, 25), 0);
		checkResult(tasksService.getArchivedTasks(0, 25), 0);
	}

	/**
	 * Create a new WebCrawlTaskRecord
	 *
	 * @param indexName
	 * @param taskName
	 * @param urlCrawl
	 * @param maxDepth
	 * @return
	 * @throws IOException
	 */
	WebCrawlTaskRecord createWebCrawlTask(String indexName, String taskName, String urlCrawl, int maxDepth)
			throws IOException {
		final IndexesService indexesService = getIndexesService();
		indexesService.createIndex(indexName);
		final AnnotatedIndexService<UrlRecord> indexService = indexesService.getIndex(indexName);
		return WebCrawlTaskRecord.of(WebCrawlRecord.of()
				.name(taskName)
				.crawlDefinition(WebCrawlDefinition.of().setEntryUrl(urlCrawl).setMaxDepth(maxDepth).build())
				.build(), UUID.fromString(indexService.getIndexStatus().index_uuid)).build();
	}

	@Test
	public void createTaskAndArchive() throws IOException {

		final WebCrawlTaskRecord taskRecord =
				createWebCrawlTask("index", "test", "https://www.opensearchserver.com", 3);

		// Save as an active task
		tasksService.saveActiveTask(taskRecord);
		Assert.assertEquals(taskRecord, tasksService.getActiveTask(taskRecord.getUuid()));
		checkResult(tasksService.getActiveTasks(0, 25), 1, taskRecord);
		checkResult(tasksService.getArchivedTasks(0, 25), 0);

		// Archive Task
		tasksService.archiveActiveTask(taskRecord.getUuid());
		Assert.assertEquals(taskRecord, tasksService.getArchivedTask(taskRecord.getUuid()));
		checkResult(tasksService.getActiveTasks(0, 25), 0);
		checkResult(tasksService.getArchivedTasks(0, 25), 1, taskRecord);
	}

	@Test(expected = NoContentException.class)
	public void archiveUnkownTask() throws IOException {
		tasksService.archiveActiveTask(UUID.randomUUID());
	}

	@Test(expected = NotAllowedException.class)
	public void saveAlreadyArchived() throws IOException {
		final WebCrawlTaskRecord taskRecord =
				createWebCrawlTask("index", "test", "https://www.opensearchserver.com", 3);
		tasksService.saveActiveTask(taskRecord);
		tasksService.archiveActiveTask(taskRecord.getUuid());
		tasksService.saveActiveTask(taskRecord);
	}

}
