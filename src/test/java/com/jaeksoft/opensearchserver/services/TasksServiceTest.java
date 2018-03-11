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
import com.jaeksoft.opensearchserver.model.WebCrawlRecord;
import com.jaeksoft.opensearchserver.model.WebCrawlTaskDefinition;
import com.qwazr.crawler.web.WebCrawlDefinition;
import com.qwazr.utils.LoggerUtils;
import com.qwazr.utils.RandomUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.NotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class TasksServiceTest extends BaseTest {

	private final static Logger LOGGER = LoggerUtils.getLogger(TasksServiceTest.class);

	private TasksService tasksService;

	@Before
	public void setup() throws IOException {
		tasksService = getTasksService();
	}

	private void checkResult(long totalCount, List<TaskRecord> records, int expectedResults,
			TaskRecord... expectedRecords) {
		Assert.assertNotNull(records);
		Assert.assertEquals(expectedResults, totalCount);
		Assert.assertEquals(expectedRecords.length, records.size());
	}

	@Test
	public void emptyList() {
		List<TaskRecord> records = new ArrayList<>();
		checkResult(tasksService.collectAccountTasks(getAccountId(), 0, 25, records), records, 0);
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
	TaskRecord createWebCrawlTask(String indexName, String taskName, String urlCrawl, int maxDepth) throws IOException {
		final IndexesService indexesService = getIndexesService();
		indexesService.createIndex(getAccountId().toString(), indexName);
		final IndexService indexService = indexesService.getIndex(getAccountId().toString(), indexName);

		final WebCrawlRecord webCrawlRecord = WebCrawlRecord.of()
				.name(taskName)
				.crawlDefinition(WebCrawlDefinition.of().setEntryUrl(urlCrawl).setMaxDepth(maxDepth).build())
				.build();

		final UUID indexUuid = UUID.fromString(indexService.getIndexStatus().index_uuid);

		final WebCrawlTaskDefinition webCrawlTask = new WebCrawlTaskDefinition(webCrawlRecord, indexUuid);

		return TaskRecord.of(getAccountId(), webCrawlTask).status(TaskRecord.Status.PAUSED).build();
	}

	@Test
	public void createTask() throws IOException {

		final TaskRecord taskRecord = createWebCrawlTask("index", "test", "http://www.opensearchserver.com", 3);

		// Create a new task
		tasksService.createTask(taskRecord);
		Assert.assertEquals(taskRecord, tasksService.getTask(taskRecord.getTaskId()));

		final List<TaskRecord> records = new ArrayList<>();
		checkResult(tasksService.collectAccountTasks(getAccountId(), 0, 25, records), records, 1, taskRecord);

		Assert.assertEquals(tasksService.getTask(taskRecord.getTaskId()), taskRecord);

		Assert.assertNull(tasksService.getTask(UUID.randomUUID().toString()));

	}

	@Test(expected = NotFoundException.class)
	public void updateUnknownTask() throws IOException {
		tasksService.updateStatus(RandomUtils.alphanumeric(8), TaskRecord.Status.PAUSED);
	}

}
