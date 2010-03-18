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

package com.jaeksoft.searchlib.web.controller.runtime;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.cache.FieldCache;
import com.jaeksoft.searchlib.cache.FilterCache;
import com.jaeksoft.searchlib.cache.LRUCache;
import com.jaeksoft.searchlib.cache.SearchCache;
import com.jaeksoft.searchlib.index.IndexAbstract;
import com.jaeksoft.searchlib.index.IndexGroup;
import com.jaeksoft.searchlib.index.IndexSingle;
import com.jaeksoft.searchlib.web.controller.CommonController;

public class CacheController extends CommonController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6570698209309006505L;

	private CacheItem<?>[] searchCacheList;
	private CacheItem<?>[] filterCacheList;
	private CacheItem<?>[] fieldCacheList;

	public CacheController() throws SearchLibException {
		super();
		reset();
	}

	@Override
	public void reset() {
		searchCacheList = null;
		filterCacheList = null;
		fieldCacheList = null;
	}

	private Object[] getIndexList() throws SearchLibException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return null;
			IndexAbstract index = client.getIndex();
			if (index == null)
				return null;
			if (index instanceof IndexGroup)
				return ((IndexGroup) index).getIndices().toArray();
			Object[] list = { index };
			return list;
		}
	}

	public class CacheItem<T extends LRUCache<?, ?>> {
		private IndexSingle index;
		private T cache;

		private CacheItem(IndexSingle index, T cache) {
			this.index = index;
			this.cache = cache;
		}

		public T getCache() {
			return cache;
		}

		public IndexSingle getIndex() {
			return index;
		}

	}

	public CacheItem<?>[] getSearchCacheList() throws SearchLibException {
		synchronized (this) {
			if (searchCacheList != null)
				return searchCacheList;
			Object[] indexList = getIndexList();
			if (indexList == null)
				return null;
			searchCacheList = new CacheItem<?>[indexList.length];
			int i = 0;
			for (Object o : indexList) {
				IndexSingle index = (IndexSingle) o;
				searchCacheList[i++] = new CacheItem<SearchCache>(index, index
						.getSearchCache());
			}
			return searchCacheList;
		}
	}

	public CacheItem<?>[] getFilterCacheList() throws SearchLibException {
		synchronized (this) {
			if (filterCacheList != null)
				return filterCacheList;
			Object[] indexList = getIndexList();
			if (indexList == null)
				return null;
			filterCacheList = new CacheItem<?>[indexList.length];
			int i = 0;
			for (Object o : indexList) {
				IndexSingle index = (IndexSingle) o;
				filterCacheList[i++] = new CacheItem<FilterCache>(index, index
						.getFilterCache());
			}
			return filterCacheList;
		}
	}

	public CacheItem<?>[] getFieldCacheList() throws SearchLibException {
		synchronized (this) {
			if (fieldCacheList != null)
				return fieldCacheList;
			Object[] indexList = getIndexList();
			if (indexList == null)
				return null;
			fieldCacheList = new CacheItem<?>[indexList.length];
			int i = 0;
			for (Object o : indexList) {
				IndexSingle index = (IndexSingle) o;
				fieldCacheList[i++] = new CacheItem<FieldCache>(index, index
						.getFieldCache());
			}
			return fieldCacheList;
		}
	}

	@Override
	public void reloadPage() {
		synchronized (this) {
			searchCacheList = null;
			filterCacheList = null;
			fieldCacheList = null;
			super.reloadPage();
		}
	}

}
