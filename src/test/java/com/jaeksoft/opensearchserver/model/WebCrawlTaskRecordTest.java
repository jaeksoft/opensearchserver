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

package com.jaeksoft.opensearchserver.model;

import com.jaeksoft.opensearchserver.services.WebCrawlsServiceTest;
import com.qwazr.utils.ObjectMappers;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class WebCrawlTaskRecordTest {

	@Test
	public void serializationDeserializationTest() throws IOException {
		final WebCrawlRecord taskRecord1 = WebCrawlsServiceTest.createNewCrawlRecord();
		final String taskRecordString = ObjectMappers.JSON.writeValueAsString(taskRecord1);
		final WebCrawlRecord taskRecord2 = ObjectMappers.JSON.readValue(taskRecordString, WebCrawlRecord.class);
		Assert.assertEquals(taskRecord1, taskRecord2);
	}
}
