/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2013 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 *
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/
package com.jaeksoft.searchlib.webservice.crawler;

import com.jaeksoft.searchlib.crawler.common.process.CrawlMasterAbstract;
import com.jaeksoft.searchlib.webservice.CommonResult;

public class CrawlerUtils {

	public enum InfoStatus {
		STARTED, STARTING, STOPPED, STOPPING;
	}

	public enum CrawlerActionEnum {

		START("start"),

		STOP("stop"),

		STATUS("status"),

		EMTPY("empty");

		private String name;

		public String getName() {
			return name;
		}

		private CrawlerActionEnum(String name) {
			this.name = name;

		}
	}

	public static CommonResult crawlerAction(
			CrawlMasterAbstract<?, ?> crawlMaster, int timeOut,
			boolean runOnce, CrawlerActionEnum action) {
		if (CrawlerActionEnum.STOP.name().equalsIgnoreCase(action.name())) {
			crawlMaster.abort();
			if (crawlMaster.waitForEnd(timeOut))
				return new CommonResult(true, InfoStatus.STOPPED.name());
			else
				return new CommonResult(true, InfoStatus.STOPPING.name());
		} else if (CrawlerActionEnum.START.name().equalsIgnoreCase(
				action.name())) {
			crawlMaster.start(runOnce);
			if (crawlMaster.waitForStart(timeOut))
				return new CommonResult(true, InfoStatus.STARTED.name());
			else
				return new CommonResult(true, InfoStatus.STARTING.name());
		} else if (CrawlerActionEnum.STATUS.name().equalsIgnoreCase(
				action.name())) {
			if (crawlMaster.isAborting())
				return new CommonResult(true, InfoStatus.STOPPING.name());
			else if (crawlMaster.isRunning())
				return new CommonResult(true, InfoStatus.STARTED.name());
			else
				return new CommonResult(true, InfoStatus.STOPPED.name());
		} else
			return new CommonResult(true, CrawlerActionEnum.EMTPY.name());

	}
}
