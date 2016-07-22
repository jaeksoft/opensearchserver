/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.webservice.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalogItem;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.util.ThreadUtils;

@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@XmlRootElement(name = "info")
@JsonInclude(Include.NON_NULL)
public class IndexInfo {

	final public String indexName;
	final public Date lastModified;
	final public Integer numDocs;
	final public Long size;
	final public String humanSize;
	final public List<String> threads;
	final public Boolean loaded;

	public IndexInfo() {
		indexName = null;
		lastModified = null;
		numDocs = null;
		size = null;
		humanSize = null;
		threads = null;
		loaded = null;
	}

	public IndexInfo(ClientCatalogItem clientCatalogItem) throws IOException, SearchLibException {
		indexName = clientCatalogItem.getIndexName();
		lastModified = clientCatalogItem.getLastModifiedDate();
		numDocs = clientCatalogItem.getNumDocs();
		size = clientCatalogItem.getSize();
		humanSize = clientCatalogItem.getSizeString();
		Client client = clientCatalogItem.getClient();
		List<String> threadList = null;
		if (client != null) {
			loaded = true;
			ThreadGroup threadGroup = client.getThreadGroup();
			if (threadGroup != null) {
				Thread[] threadArray = ThreadUtils.getThreadArray(threadGroup);
				if (threadArray != null) {
					threadList = new ArrayList<String>(threadArray.length);
					for (Thread thread : threadArray)
						threadList.add(thread.getName());
				}
			}
		} else {
			loaded = false;
		}
		threads = threadList;
	}

	/**
	 * @return the indexName
	 */
	public String getIndexName() {
		return indexName;
	}

	/**
	 * @return the lastModified
	 */
	public Date getLastModified() {
		return lastModified;
	}

	/**
	 * @return the numDocs
	 */
	public Integer getNumDocs() {
		return numDocs;
	}

	/**
	 * @return the size
	 */
	public Long getSize() {
		return size;
	}

	/**
	 * @return the humanSize
	 */
	public String getHumanSize() {
		return humanSize;
	}

	/**
	 * @return the threads
	 */
	public List<String> getThreads() {
		return threads;
	}

	public Integer getThreadCount() {
		return threads == null ? null : threads.size();
	}

	/**
	 * @return the loaded
	 */
	public Boolean getLoaded() {
		return loaded;
	}
}