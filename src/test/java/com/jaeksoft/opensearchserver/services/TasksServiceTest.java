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
import com.jaeksoft.opensearchserver.model.WebCrawlTaskRecord;
import com.qwazr.crawler.web.WebCrawlDefinition;
import com.qwazr.utils.LoggerUtils;
import com.qwazr.utils.RandomUtils;
import com.qwazr.utils.concurrent.ThreadUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.NotAllowedException;
import javax.ws.rs.core.NoContentException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class TasksServiceTest extends BaseTest {

	private final static Logger LOGGER = LoggerUtils.getLogger(TasksServiceTest.class);

	private TasksService tasksService;

	@Before
	public void setup() throws IOException, URISyntaxException {
		tasksService = getTasksService();
	}

	private void checkResult(int totalCount, List<TaskRecord> records, int expectedResults,
			TaskRecord... expectedRecords) {
		Assert.assertNotNull(records);
		Assert.assertEquals(expectedResults, totalCount);
		Assert.assertEquals(expectedRecords.length, records.size());
	}

	@Test
	public void emptyList() throws IOException {
		List<TaskRecord> records = new ArrayList<>();
		checkResult(tasksService.collectActiveTasks(getAccountSchema(), 0, 25, records), records, 0);
		checkResult(tasksService.getArchivedTasks(getAccountSchema(), 0, 25, records), records, 0);
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
			throws IOException, URISyntaxException {
		final IndexesService indexesService = getIndexesService();
		indexesService.createIndex(getAccountSchema(), indexName);
		final IndexService indexService = indexesService.getIndex(getAccountSchema(), indexName);
		return WebCrawlTaskRecord.of(WebCrawlRecord.of()
				.name(taskName)
				.crawlDefinition(WebCrawlDefinition.of().setEntryUrl(urlCrawl).setMaxDepth(maxDepth).build())
				.build(), UUID.fromString(indexService.getIndexStatus().index_uuid)).build();
	}

	private void waitForAchivedActiveTask(TaskRecord taskRecord) throws IOException {
		// Archive Task, wait until the task in not running
		for (; ; ) {
			try {
				tasksService.archiveActiveTask(getAccountSchema(), taskRecord.getTaskId());
				break;
			} catch (NotAllowedException e) {
				// That's OK
				LOGGER.info("Wait until the task is done");
				ThreadUtils.sleep(5, TimeUnit.SECONDS);
			}
		}
	}

	@Test
	public void createTaskAndArchive() throws IOException, URISyntaxException {

		final WebCrawlTaskRecord taskRecord = createWebCrawlTask("index", "test", "http://www.opensearchserver.com", 3);

		// Save as an active task
		tasksService.saveActiveTask(getAccountSchema(), taskRecord);
		Assert.assertEquals(taskRecord, tasksService.getActiveTask(getAccountSchema(), taskRecord.getTaskId()));

		final List<TaskRecord> records = new ArrayList<>();
		checkResult(tasksService.collectActiveTasks(getAccountSchema(), 0, 25, records), records, 1, taskRecord);

		records.clear();
		checkResult(tasksService.getArchivedTasks(getAccountSchema(), 0, 25, records), records, 0);

		records.clear();
		checkResult(tasksService.collectActiveTasks(getAccountSchema(), 0, 25, taskRecord.crawlUuid, records), records,
				1, taskRecord);

		records.clear();
		checkResult(tasksService.collectActiveTasks(getAccountSchema(), 0, 25, UUID.randomUUID(), records), records, 0);

		waitForAchivedActiveTask(taskRecord);

		Assert.assertEquals(taskRecord, tasksService.getArchivedTask(getAccountSchema(), taskRecord.getTaskId()));
		records.clear();
		checkResult(tasksService.collectActiveTasks(getAccountSchema(), 0, 25, records), records, 0);

		records.clear();
		checkResult(tasksService.getArchivedTasks(getAccountSchema(), 0, 25, records), records, 1, taskRecord);
	}

	@Test(expected = NoContentException.class)
	public void archiveUnkownTask() throws IOException {
		tasksService.archiveActiveTask(getAccountSchema(), RandomUtils.alphanumeric(8));
	}

	@Test(expected = NotAllowedException.class)
	public void saveAlreadyArchived() throws IOException, URISyntaxException {
		final WebCrawlTaskRecord taskRecord = createWebCrawlTask("index", "test", "http://www.opensearchserver.com", 3);
		tasksService.saveActiveTask(getAccountSchema(), taskRecord);
		waitForAchivedActiveTask(taskRecord);
		tasksService.saveActiveTask(getAccountSchema(), taskRecord);
	}

}
