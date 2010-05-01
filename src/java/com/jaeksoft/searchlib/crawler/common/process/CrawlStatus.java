/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.crawler.common.process;

public enum CrawlStatus {

	NOT_RUNNING("Not running"), STARTING("Starting"), EXTRACTING_URLLIST(
			"Extracting url list"), EXTRACTING_HOSTLIST("Extracting host list"), CRAWL(
			"Crawling"), INDEXATION("Indexation"), OPTMIZING_INDEX(
			"Optimizing index"), PUBLISH_INDEX("Publishing index"), EXTRACTING_URLS(
			"Extracting urls"), WAITING("Waiting"), ERROR("Error"), ABORTED(
			"Aborted"), COMPLETE("Complete"), DELETE_REMOVED(
			"Delete removed files");

	public String name;

	private CrawlStatus(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

}
