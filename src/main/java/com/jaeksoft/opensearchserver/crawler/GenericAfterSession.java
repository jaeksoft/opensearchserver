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

public class GenericAfterSession extends AbstractEvent {

	@Override
	public boolean run(final EventContext context) throws Exception {

		final IndexService indexService =
				CrawlerComponents.getIndexService(context.getIndexServiceUrl(), context.getSchemaName(),
						context.getIndexName());

		context.currentSession.setVariable(INDEX_SERVICE, indexService);
		context.currentSession.setVariable(INDEX_QUEUE, new IndexQueue(indexService, 100));

		context.currentSession.setVariable(WEBCRAWLS_SERVICE,
				CrawlerComponents.getWebCrawlsService(context.getStoreServiceUrl(), context.getSchemaName()));

		return true;
	}

}
