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

public class GenericBeforeCrawl extends AbstractEvent {

	@Override
	public boolean run(final EventContext context) throws Exception {

		final String url = context.currentURI.getUri().toString();

		//Do we already have a status for this URL ?
		final UrlRecord urlRecord = context.indexService.getDocument(url);
		// Already known, we do not crawl
		if (urlRecord != null && urlRecord.httpStatus != null && context.crawlUuid.equals(urlRecord.crawlUuid))
			return false;

		context.currentSession.setVariable(URLRECORD_BUILDER, UrlRecord.of(context.currentURI.getUri())
				.crawlUuid(context.crawlUuid)
				.depth(context.currentURI.getDepth()));

		// If there is exclusions and if any exclusion matched we do not crawl
		if (context.currentURI.isInExclusion() != null && context.currentURI.isInExclusion())
			return false;

		// If there is inclusions and if no inclusion matches we do not crawl
		if (context.currentURI.isInInclusion() != null && !context.currentURI.isInInclusion())
			return false;

		return true;
	}
}
