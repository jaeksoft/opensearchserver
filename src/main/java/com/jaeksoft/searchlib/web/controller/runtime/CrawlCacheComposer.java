/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012-2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.controller.runtime;

import java.io.IOException;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.cache.CrawlCacheManager;
import com.jaeksoft.searchlib.crawler.cache.CrawlCacheProviderEnum;
import com.jaeksoft.searchlib.web.controller.AlertController;
import com.jaeksoft.searchlib.web.controller.CommonController;

@AfterCompose(superclass = true)
public class CrawlCacheComposer extends CommonController {

	public enum CrawlScope {
		Global, Index;
	}

	private CrawlCacheManager crawlCacheManager = null;

	private CrawlScope crawlScope = CrawlScope.Global;

	public CrawlCacheComposer() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() throws SearchLibException {
		crawlScope = CrawlScope.Global;
		crawlCacheManager = CrawlCacheManager.getGlobalInstance();
	}

	private void setCrawlCacheManager() throws SearchLibException {
		switch (crawlScope) {
		default:
			crawlCacheManager = CrawlCacheManager.getGlobalInstance();
			break;
		case Index:
			crawlCacheManager = getClient().getCrawlCacheManager();
			break;
		}
	}

	public CrawlCacheManager getCrawlCacheManager() throws SearchLibException {
		if (crawlCacheManager == null)
			setCrawlCacheManager();
		return crawlCacheManager;
	}

	public CrawlCacheProviderEnum[] getCrawlCacheProviderList() {
		return CrawlCacheProviderEnum.values();
	}

	private void flush(boolean expiration) throws SearchLibException,
			IOException, InterruptedException {
		if (crawlCacheManager == null)
			return;
		long count = crawlCacheManager.flushCache(expiration);
		reload();
		new AlertController(count + " content(s) deleted.");
	}

	@Command
	public void onFlushAll() throws SearchLibException, IOException,
			InterruptedException {
		flush(false);
	}

	@Command
	public void onFlushExpire() throws SearchLibException, IOException,
			InterruptedException {
		flush(true);
	}

	/**
	 * @return the crawlScope
	 */
	public CrawlScope getCrawlScope() {
		return crawlScope;
	}

	/**
	 * @param crawlScope
	 *            the crawlScope to set
	 * @throws SearchLibException
	 */
	public void setCrawlScope(CrawlScope crawlScope) throws SearchLibException {
		this.crawlScope = crawlScope;
		setCrawlCacheManager();
	}

	public CrawlScope[] getCrawlScopes() {
		return CrawlScope.values();
	}
}
