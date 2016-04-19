/**
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2011-2013 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 **/
package com.jaeksoft.searchlib.webservice.crawler;

import com.jaeksoft.searchlib.crawler.common.process.CrawlMasterAbstract;
import com.jaeksoft.searchlib.web.WebCrawlerServlet.InfoStatus;
import com.jaeksoft.searchlib.webservice.CommonResult;

public class CrawlerUtils {

	public static CommonResult stop(CrawlMasterAbstract<?, ?> crawlMaster) {
		crawlMaster.abort();
		return new CommonResult(true, InfoStatus.STOPPING.name());
	}

	public static CommonResult runForever(CrawlMasterAbstract<?, ?> crawlMaster) {
		crawlMaster.start(false);
		return new CommonResult(true, InfoStatus.STARTING.name());

	}

	public static CommonResult runOnce(CrawlMasterAbstract<?, ?> crawlMaster) {
		crawlMaster.start(true);
		return new CommonResult(true, InfoStatus.STARTING.name());
	}

	public static String infoStatus(CrawlMasterAbstract<?, ?> crawlMaster) {
		if (crawlMaster.isAborting())
			return InfoStatus.STOPPING.name();
		else if (crawlMaster.isRunning())
			return InfoStatus.STARTED.name();
		else
			return InfoStatus.STOPPED.name();
	}

	public static CommonResult status(CrawlMasterAbstract<?, ?> crawlMaster) {
			return new CommonResult(true, infoStatus(crawlMaster));
	}
}
