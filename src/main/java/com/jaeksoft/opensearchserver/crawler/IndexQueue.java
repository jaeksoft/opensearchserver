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
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

public class IndexQueue {

	private final IndexService indexService;

	private final int bufferSize;

	private final Map<URI, UrlRecord> postBuffer;

	private final Map<URI, UrlRecord> updateBuffer;

	public IndexQueue(final IndexService indexService, final int bufferSize) {
		this.indexService = indexService;
		this.bufferSize = bufferSize;
		this.postBuffer = new LinkedHashMap<>(bufferSize);
		this.updateBuffer = new LinkedHashMap<>(bufferSize);
	}

	public void post(final URI uri, final UrlRecord urlRecord) throws IOException, InterruptedException {
		synchronized (postBuffer) {
			postBuffer.putIfAbsent(uri, urlRecord);
			if (postBuffer.size() >= bufferSize)
				flushPostBuffer();
		}
	}

	private void flushPostBuffer() throws IOException, InterruptedException {
		synchronized (postBuffer) {
			if (postBuffer.isEmpty())
				return;
			indexService.postDocuments(postBuffer.values());
			postBuffer.clear();
		}
	}

	public void update(final URI uri, final UrlRecord urlRecord) throws IOException, InterruptedException {
		synchronized (updateBuffer) {
			updateBuffer.putIfAbsent(uri, urlRecord);
			if (updateBuffer.size() >= bufferSize)
				flushUpdateBuffer();
		}
	}

	private void flushUpdateBuffer() throws IOException, InterruptedException {
		synchronized (updateBuffer) {
			if (updateBuffer.isEmpty())
				return;
			indexService.updateDocumentsValues(updateBuffer.values());
			updateBuffer.clear();
		}
	}

	public void flush() throws IOException, InterruptedException {
		flushUpdateBuffer();
		flushPostBuffer();
	}
}
