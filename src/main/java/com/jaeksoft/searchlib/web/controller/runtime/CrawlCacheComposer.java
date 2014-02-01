/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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
import org.zkoss.zul.Messagebox;

import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.cache.CrawlCacheManager;
import com.jaeksoft.searchlib.crawler.cache.CrawlCacheProviderEnum;
import com.jaeksoft.searchlib.web.controller.AlertController;
import com.jaeksoft.searchlib.web.controller.CommonController;

@AfterCompose(superclass = true)
public class CrawlCacheComposer extends CommonController {

	public CrawlCacheComposer() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() throws SearchLibException {
	}

	public CrawlCacheManager getCrawlCacheManager() throws SearchLibException,
			IOException {
		return ClientCatalog.getCrawlCacheManager();
	}

	public CrawlCacheProviderEnum[] getCrawlCacheProviderList() {
		return CrawlCacheProviderEnum.values();
	}

	private void flush(boolean expiration) throws SearchLibException,
			IOException, InterruptedException {
		CrawlCacheManager manager = ClientCatalog.getCrawlCacheManager();
		if (manager == null)
			return;
		long count = manager.flushCache(expiration);
		reload();
		new AlertController(count + " content(s) deleted.");
	}

	public boolean isEnabled() throws SearchLibException {
		CrawlCacheManager manager = ClientCatalog.getCrawlCacheManager();
		if (manager == null)
			return false;
		return manager.isEnabled();
	}

	public void setEnabled(boolean b) throws SearchLibException,
			InterruptedException {
		CrawlCacheManager manager = ClientCatalog.getCrawlCacheManager();
		if (manager == null)
			return;
		try {
			manager.setEnabled(b);
		} catch (Exception e) {
			reload();
			new AlertController(e.getMessage(), Messagebox.ERROR);
		}
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

}
