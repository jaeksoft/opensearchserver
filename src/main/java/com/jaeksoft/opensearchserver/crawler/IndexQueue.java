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
import com.qwazr.utils.LoggerUtils;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class IndexQueue implements Closeable {

	private final static Logger LOGGER = LoggerUtils.getLogger(IndexQueue.class);

	private final IndexService indexService;

	private final int postBufferSize;

	private final int updateBufferSize;

	private final int secondsFlushPeriod;

	private volatile long nextFlush;

	private final Map<URI, UrlRecord> postBuffer;

	private final Map<URI, UrlRecord> updateBuffer;

	public IndexQueue(final IndexService indexService, final int postBufferSize, final int updateBufferSize,
			final int secondsFlushPeriod) {
		this.indexService = indexService;
		this.postBufferSize = postBufferSize;
		this.updateBufferSize = updateBufferSize;
		this.secondsFlushPeriod = secondsFlushPeriod;
		this.postBuffer = new LinkedHashMap<>(postBufferSize);
		this.updateBuffer = new LinkedHashMap<>(updateBufferSize);
		this.nextFlush = computeNextFlush();
	}

	private long computeNextFlush() {
		return System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(secondsFlushPeriod);
	}

	private boolean shouldBeFlushed() {
		return postBuffer.size() >= postBufferSize || updateBuffer.size() >= updateBufferSize ||
				System.currentTimeMillis() > nextFlush;

	}

	public boolean contains(final URI uri) {
		synchronized (postBuffer) {
			synchronized (updateBuffer) {
				return updateBuffer.containsKey(uri) || postBuffer.containsKey(uri);
			}
		}
	}

	public void post(final URI uri, final UrlRecord urlRecord) throws IOException, InterruptedException {
		LOGGER.info(() -> "Post: " + urlRecord.url + " - " + urlRecord.crawlStatus + " - " + urlRecord.depth);
		synchronized (postBuffer) {
			synchronized (updateBuffer) {
				if (updateBuffer.remove(uri) != null)
					LOGGER.warning("Remove update: " + uri);
				postBuffer.put(uri, urlRecord);
				if (shouldBeFlushed())
					flush();
			}
		}
	}

	public void update(final URI uri, final UrlRecord urlRecord) throws IOException, InterruptedException {
		LOGGER.info(() -> "Update: " + urlRecord.url + " - " + urlRecord.crawlStatus + " - " + urlRecord.depth);
		synchronized (updateBuffer) {
			synchronized (postBuffer) {
				if (postBuffer.remove(uri) != null)
					LOGGER.warning("Remove post: " + uri);
				updateBuffer.put(uri, urlRecord);
				if (shouldBeFlushed())
					flush();
			}
		}
	}

	private void flushPostBuffer() throws IOException, InterruptedException {
		if (postBuffer.isEmpty())
			return;
		indexService.postDocuments(postBuffer.values());
		postBuffer.clear();
	}

	private void flushUpdateBuffer() throws IOException, InterruptedException {
		if (updateBuffer.isEmpty())
			return;
		indexService.updateDocumentsValues(updateBuffer.values());
		updateBuffer.clear();
	}

	void flush() throws IOException, InterruptedException {
		synchronized (postBuffer) {
			synchronized (updateBuffer) {
				flushUpdateBuffer();
				flushPostBuffer();
				nextFlush = computeNextFlush();
			}
		}
	}

	@Override
	public void close() throws IOException {
		try {
			flush();
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
	}
}
