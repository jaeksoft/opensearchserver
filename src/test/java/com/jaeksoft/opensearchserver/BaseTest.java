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

package com.jaeksoft.opensearchserver;

import com.jaeksoft.opensearchserver.services.IndexesService;
import com.jaeksoft.opensearchserver.services.TasksService;
import com.jaeksoft.opensearchserver.services.WebCrawlsService;
import org.junit.After;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class BaseTest {

	private Components components;
	private final UUID accountId = UUID.randomUUID();

	private synchronized Components getComponents() throws IOException {
		if (components == null) {
			final Path dataDirectory = Files.createTempDirectory("BaseTest");
			Files.copy(Paths.get("config.properties"), dataDirectory.resolve("config.properties"));
			components = new Components(dataDirectory);
		}
		return components;
	}

	protected UUID getAccountId() {
		return accountId;
	}

	protected WebCrawlsService getWebCrawlsService() throws IOException {
		return getComponents().getWebCrawlsService();
	}

	protected TasksService getTasksService() throws IOException {
		return getComponents().getTasksService();
	}

	protected IndexesService getIndexesService() throws IOException {
		return getComponents().getIndexesService();
	}

	@After
	public synchronized void cleanup() {
		if (components != null) {
			components.close();
			components = null;
		}
	}

}
