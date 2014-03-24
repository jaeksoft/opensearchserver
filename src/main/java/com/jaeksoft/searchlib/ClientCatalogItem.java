/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib;

import java.io.File;
import java.io.IOException;

import com.jaeksoft.searchlib.index.IndexStatistics;
import com.jaeksoft.searchlib.util.LastModifiedAndSize;

public class ClientCatalogItem implements Comparable<ClientCatalogItem> {

	private String indexName;

	private LastModifiedAndSize lastModifiedAndSize;

	public ClientCatalogItem(String indexName) {
		this.indexName = indexName;
		this.lastModifiedAndSize = null;
	}

	public String getIndexName() {
		return indexName;
	}

	public Client getClient() {
		try {
			return ClientCatalog.getLoadedClient(indexName);
		} catch (SearchLibException e) {
			Logging.error(e);
			return null;
		}
	}

	public long getSize() {
		if (lastModifiedAndSize == null)
			return -1;
		return lastModifiedAndSize.getSize();
	}

	public Integer getNumDocs() throws IOException, SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		IndexStatistics stats = client.getStatistics();
		if (stats == null)
			return null;
		return (int) stats.getNumDocs();
	}

	public long getLastModified() {
		if (lastModifiedAndSize == null)
			return -1;
		return lastModifiedAndSize.getLastModified();
	}

	public File getLastModifiedFile() {
		if (lastModifiedAndSize == null)
			return null;
		return lastModifiedAndSize.getLastModifiedFile();
	}

	public String getOptimizationStatus() throws IOException,
			SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getOptimizationStatus();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ClientCatalogItem))
			return false;
		ClientCatalogItem item = (ClientCatalogItem) o;
		return indexName.equals(item.indexName);
	}

	@Override
	public int compareTo(ClientCatalogItem o) {
		return indexName.compareTo(o.indexName);
	}

	public void computeInfos() throws SearchLibException {
		lastModifiedAndSize = ClientCatalog.getLastModifiedAndSize(indexName);
	}
}
