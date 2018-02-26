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

import com.jaeksoft.opensearchserver.front.Message;

public enum CrawlStatus {

	UNKNOWN(0, "Not yet crawled", Message.Css.info),
	BEFORE_CRAWL(1, "Before crawl", Message.Css.danger),
	EXCLUSION_MATCH(2, "Matches exclusion", Message.Css.warning),
	INCLUSION_MISS(3, "Missed inclusion", Message.Css.warning),
	ROBOTS_TXT_DENY(4, "Denied by Robots.txt", Message.Css.warning),
	REDIRECTION(8, "Redirected", Message.Css.info),
	ERROR(9, "In error", Message.Css.danger),
	CRAWLED(10, "Crawled", Message.Css.success);

	public final int code;

	public final String message;

	public final Message.Css css;

	CrawlStatus(int code, String message, Message.Css css) {
		this.code = code;
		this.message = message;
		this.css = css;
	}

	public int getCode() {
		return code;
	}

	public Message.Css getCss() {
		return css;
	}

	@Override
	public String toString() {
		return message;
	}

	public static boolean isUnknown(final Integer crawlStatus) {
		return crawlStatus == null || crawlStatus == UNKNOWN.code;
	}
}
