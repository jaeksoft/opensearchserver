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

import com.jaeksoft.opensearchserver.model.CrawlStatus;
import com.jaeksoft.opensearchserver.model.UrlRecord;
import com.jaeksoft.opensearchserver.services.IndexService;
import com.qwazr.utils.FileUtils;
import com.qwazr.utils.HashUtils;
import com.qwazr.utils.ObjectMappers;
import org.apache.commons.collections4.trie.PatriciaTrie;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class SessionStore implements Closeable {

	public final UUID crawlUuid;
	public final Long taskCreationTime;
	private final Path tempDirectory;
	private final Map<String, UUID> uris;
	private final PatriciaTrie<Integer> newLinks;

	public SessionStore(final UUID crawlUuid, final Long taskCreationTime) throws IOException {
		this.crawlUuid = crawlUuid;
		this.taskCreationTime = taskCreationTime;
		tempDirectory = Files.createTempDirectory("oss-session-store");
		uris = new LinkedHashMap<>();
		newLinks = new PatriciaTrie<>();

	}

	private File getFile(UUID uuid) {
		return tempDirectory.resolve(uuid.toString() + ".json").toFile();
	}

	public void saveCrawl(final URI uri, final UrlRecord urlRecord) throws IOException {
		final String uriString = uri.toString();
		final UUID uuid = uris.computeIfAbsent(uriString, u -> HashUtils.newTimeBasedUUID());
		newLinks.remove(uriString);
		ObjectMappers.JSON.writeValue(getFile(uuid), urlRecord);
	}

	public void saveNewLinks(final Collection<URI> links, int depth) {
		if (newLinks == null)
			return;
		links.forEach(uri -> {
			final String uriString = uri.toString();
			if (!uris.containsKey(uriString))
				newLinks.putIfAbsent(uriString, depth);
		});
	}

	private void indexNewLink(final IndexService indexService, final IndexQueue indexQueue, final String link,
			final Integer depth) throws Exception {
		if (indexService.isAlreadyCrawled(link, crawlUuid, taskCreationTime))
			return;
		final URI uri = URI.create(link);
		final UrlRecord.Builder linkBuilder = UrlRecord.of(uri)
				.crawlStatus(CrawlStatus.UNKNOWN)
				.crawlUuid(crawlUuid)
				.taskCreationTime(taskCreationTime)
				.depth(depth);
		if (indexService.exists(link))
			indexQueue.update(uri, linkBuilder.build());
		else
			indexQueue.post(uri,
					linkBuilder.hostAndUrlStore(null).lastModificationTime(System.currentTimeMillis()).build());
	}

	public void index(final URL indexServiceUrl, final String accountId, final String indexName)
			throws IOException, URISyntaxException, InterruptedException {
		final IndexService indexService = CrawlerComponents.getIndexService(indexServiceUrl, accountId, indexName);
		try (final IndexQueue indexQueue = new IndexQueue(indexService, 20, 100, 60)) {
			for (UUID uuid : uris.values()) {
				final UrlRecord urlRecord = ObjectMappers.JSON.readValue(getFile(uuid), UrlRecord.class);
				indexQueue.post(URI.create(urlRecord.url), urlRecord);
			}
			newLinks.forEach((link, depth) -> {
				try {
					indexNewLink(indexService, indexQueue, link, depth);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});
		}
	}

	@Override
	public void close() throws IOException {
		FileUtils.deleteDirectory(tempDirectory);
	}

}
