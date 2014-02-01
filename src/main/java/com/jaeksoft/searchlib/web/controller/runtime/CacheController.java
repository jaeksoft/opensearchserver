/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

import java.util.ArrayList;
import java.util.List;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.cache.LRUCache;
import com.jaeksoft.searchlib.index.IndexAbstract;
import com.jaeksoft.searchlib.index.IndexSingle;
import com.jaeksoft.searchlib.web.controller.CommonController;

@AfterCompose(superclass = true)
public class CacheController extends CommonController {

	private transient List<LRUCache<?, ?>> cacheList;

	public CacheController() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() {
		cacheList = null;
	}

	public IndexSingle getIndexSingle() throws SearchLibException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return null;
			IndexAbstract indexAbstract = client.getIndexAbstract();
			if (indexAbstract == null)
				return null;
			return (IndexSingle) indexAbstract;
		}
	}

	public List<LRUCache<?, ?>> getCacheList() throws SearchLibException {
		synchronized (this) {
			if (cacheList != null)
				return cacheList;
			IndexSingle index = getIndexSingle();
			if (index == null)
				return null;
			cacheList = new ArrayList<LRUCache<?, ?>>(3);
			cacheList.add(index.getSearchCache());
			cacheList.add(index.getFilterCache());
			cacheList.add(index.getFieldCache());
			cacheList.add(index.getTermVectorCache());
			return cacheList;
		}

	}

	@Command
	@NotifyChange("*")
	public void doFlush(@BindingParam("cache") LRUCache<?, ?> cache)
			throws SearchLibException {
		synchronized (this) {
			cache.clear();
		}
	}

	@Command
	@NotifyChange("*")
	public void onSave() throws SearchLibException {
		synchronized (this) {
			getClient().saveConfig();
		}
	}

}
