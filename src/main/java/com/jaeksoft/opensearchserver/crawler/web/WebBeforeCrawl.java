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

package com.jaeksoft.opensearchserver.crawler.web;

import com.jaeksoft.opensearchserver.model.UrlRecord;

import java.net.URI;

public class WebBeforeCrawl extends WebAbstractEvent {

	@Override
	public boolean run(final EventContext context) throws Exception {

		final URI uri = context.currentCrawl.getUri();
		final String url = uri.toString();

		//Do we already have a status for this URL ?
		final UrlRecord urlRecord = context.indexService.getDocument(url);
		// Already known, we do not crawl
		if (urlRecord != null && urlRecord.httpStatus != null && context.crawlUuid.equals(urlRecord.getCrawlUuid()))
			return false;

		// If there is exclusions and if any exclusion matched we do not crawl
		if (context.currentCrawl.isInExclusion() != null && context.currentCrawl.isInExclusion())
			return false;

		// If there is inclusions and if no inclusion matches we do not crawl
		if (context.currentCrawl.isInInclusion() != null && !context.currentCrawl.isInInclusion())
			return false;

		return true;
	}
}
