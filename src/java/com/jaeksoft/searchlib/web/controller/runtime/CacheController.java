/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2011 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.controller.runtime;

import java.util.ArrayList;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.cache.LRUCache;
import com.jaeksoft.searchlib.index.IndexAbstract;
import com.jaeksoft.searchlib.index.IndexSingle;
import com.jaeksoft.searchlib.web.controller.CommonController;

public class CacheController extends CommonController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6570698209309006505L;

	private List<LRUCache<?, ?>> cacheList;

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
			IndexAbstract indexAbstract = client.getIndex();
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
			return cacheList;
		}

	}

	public void doFlush(Component comp) {
		synchronized (this) {
			LRUCache<?, ?> cache = (LRUCache<?, ?>) getRecursiveComponentAttribute(
					comp, "cacheItem");
			cache.clear();
			reloadPage();
		}
	}

	public void onSave(Event event) throws SearchLibException {
		synchronized (this) {
			getClient().saveConfig();
			reloadPage();
		}
	}

	@Override
	public void reloadPage() {
		synchronized (this) {
			reset();
			super.reloadPage();
		}
	}

}
