/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 *
 * OpenSearchServer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSearchServer.  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.web.controller.crawler.web;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.database.UrlManager;
import com.jaeksoft.searchlib.facet.Facet;
import com.jaeksoft.searchlib.facet.FacetItem;
import com.jaeksoft.searchlib.web.controller.CommonController;

@AfterCompose(superclass = true)
public class HostsController extends CommonController {

	private transient FacetItem[] hostFacetList;

	private transient int minHostFacetCount;

	public HostsController() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() {
		hostFacetList = null;
		minHostFacetCount = 0;
	}

	@Command
	public void onSearch() throws SearchLibException {
		synchronized (this) {
			hostFacetList = null;
			reload();
		}
	}

	private UrlManager getUrlManager() throws SearchLibException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return null;
			return client.getUrlManager();
		}
	}

	public FacetItem[] getHostFacetList() throws SearchLibException {
		synchronized (this) {
			if (hostFacetList != null)
				return hostFacetList;
			UrlManager urlManager = getUrlManager();
			if (urlManager == null)
				return null;
			Facet facet = urlManager.getHostFacetList(minHostFacetCount);
			if (facet == null)
				return null;
			hostFacetList = facet.getArray();
			return hostFacetList;
		}
	}

	/**
	 * @return the minHostFacetCount
	 */
	public int getMinHostFacetCount() {
		return minHostFacetCount;
	}

	/**
	 * @param minHostFacetCount
	 *            the minHostFacetCount to set
	 * @throws SearchLibException
	 */
	public void setMinHostFacetCount(int minHostFacetCount)
			throws SearchLibException {
		if (minHostFacetCount < 0)
			minHostFacetCount = 0;
		this.minHostFacetCount = minHostFacetCount;
		onSearch();
	}

}
