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

public interface CrawlerContext {

	// Script variables
	String ACCOUNT_ID = "accountId";
	String INDEX_NAME = "indexName";
	String CRAWL_UUID = "crawlUuid";
	String TASK_CREATION_TIME = "taskCreationTime";
	String INDEX_SERVICE_URL = "indexServiceUrl";
	String STORE_SERVICE_URL = "storeServiceUrl";

	// Attributes
	//String WEBCRAWLS_SERVICE = "webCrawlsService";
	String SESSION_STORE = "sessionStore";

}
