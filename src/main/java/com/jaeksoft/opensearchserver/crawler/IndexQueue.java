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

package com.jaeksoft.opensearchserver.crawler;

import com.jaeksoft.opensearchserver.model.UrlRecord;
import com.jaeksoft.opensearchserver.services.IndexService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class IndexQueue {

	private final IndexService indexService;

	private final int bufferSize;

	private final List<UrlRecord> buffer;

	IndexQueue(final IndexService indexService, final int bufferSize) {
		this.indexService = indexService;
		this.bufferSize = bufferSize;
		this.buffer = new ArrayList<>(bufferSize);
	}

	void post(UrlRecord urlRecord) throws IOException, InterruptedException {
		synchronized (buffer) {
			buffer.add(urlRecord);
			if (buffer.size() >= bufferSize)
				flush();
		}
	}

	void flush() throws IOException, InterruptedException {
		synchronized (buffer) {
			if (buffer.isEmpty())
				return;
			indexService.postDocuments(buffer);
			buffer.clear();
		}
	}
}
