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

import com.jaeksoft.opensearchserver.services.IndexService;
import com.jaeksoft.opensearchserver.services.WebCrawlsService;
import com.qwazr.crawler.common.CrawlSession;
import com.qwazr.crawler.web.CurrentURI;
import com.qwazr.crawler.web.driver.DriverInterface;
import com.qwazr.scripts.ScriptInterface;
import com.qwazr.utils.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.UUID;

public abstract class AbstractEvent implements ScriptInterface, CrawlerContext {

	public abstract boolean run(final EventContext context) throws Exception;

	@Override
	final public boolean run(final Map<String, ?> variables) throws Exception {
		return run(new EventContext(variables));
	}

	class EventContext {

		final CrawlSession currentSession;
		final CurrentURI currentURI;
		final DriverInterface driver;
		final UUID crawlUuid;

		public final IndexService indexService;
		public final WebCrawlsService webCrawlsService;
		public final IndexQueue indexQueue;

		private EventContext(final Map<String, ?> variables) {

			currentSession = (CrawlSession) variables.get("session");
			currentURI = (CurrentURI) variables.get("current");
			driver = (DriverInterface) variables.get("driver");

			indexService = (IndexService) variables.get(INDEX_SERVICE);
			webCrawlsService = (WebCrawlsService) variables.get(WEBCRAWLS_SERVICE);
			indexQueue = (IndexQueue) variables.get(INDEX_QUEUE);
			crawlUuid = UUID.fromString(currentSession.getVariable(AbstractEvent.CRAWL_UUID).toString());
		}

		String getSchemaName() {
			return (String) currentSession.getVariable(AbstractEvent.SCHEMA_NAME);
		}

		String getIndexName() {
			return (String) currentSession.getVariable(AbstractEvent.INDEX_NAME);
		}

		URL getIndexServiceUrl() throws MalformedURLException {
			return toUrl(currentSession.getVariable(AbstractEvent.INDEX_SERVICE_URL));
		}

		URL getStoreServiceUrl() throws MalformedURLException {
			return toUrl(currentSession.getVariable(AbstractEvent.STORE_SERVICE_URL));
		}

	}

	private static URL toUrl(final Object url) throws MalformedURLException {
		final String urlString = url.toString();
		return StringUtils.isBlank(urlString) ? null : new URL(urlString);
	}
}
