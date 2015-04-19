/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.cluster;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.web.StartStopListener;

public class ClusterManager {

	private final ReadWriteLock rwl = new ReadWriteLock();

	private final File clusterFile;

	private final LinkedHashSet<ClusterInstance> clusterInstances;

	private ClusterManager(File instanceDataDir) throws JsonParseException,
			JsonMappingException, IOException {
		clusterFile = new File(instanceDataDir, "cluster.xml");
		List<ClusterInstance> clusterInstanceList = ClusterInstance
				.readList(clusterFile);
		clusterInstances = clusterInstanceList == null ? new LinkedHashSet<ClusterInstance>(
				0) : new LinkedHashSet<ClusterInstance>(clusterInstanceList);
	}

	private static ClusterManager INSTANCE = null;
	final private static ReadWriteLock rwlInstance = new ReadWriteLock();

	public static final ClusterManager getInstance() throws SearchLibException {
		rwlInstance.r.lock();
		try {
			if (INSTANCE != null)
				return INSTANCE;
		} finally {
			rwlInstance.r.unlock();
		}
		rwlInstance.w.lock();
		try {
			if (INSTANCE != null)
				return INSTANCE;
			return INSTANCE = new ClusterManager(
					StartStopListener.OPENSEARCHSERVER_DATA_FILE);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			rwlInstance.w.unlock();
		}
	}

	public void set(ClusterInstance oldInstance, ClusterInstance newInstance)
			throws IOException {
		if (oldInstance == null && newInstance == null)
			return;
		rwl.w.lock();
		try {
			if (oldInstance != null)
				clusterInstances.remove(oldInstance);
			if (newInstance != null)
				clusterInstances.add(newInstance);
			ClusterInstance.writeList(clusterInstances, clusterFile);
		} finally {
			rwl.w.unlock();
		}
	}

	public Collection<ClusterInstance> getInstances() {
		rwl.r.lock();
		try {
			return clusterInstances;
		} finally {
			rwl.r.unlock();
		}
	}
}
