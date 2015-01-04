/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013-2015 Emmanuel Keller / Jaeksoft
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
import java.util.TreeMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.util.JsonUtils;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.web.StartStopListener;

@XmlAccessorType(XmlAccessType.FIELD)
public class ClusterManager {

	@XmlTransient
	private final ReadWriteLock rwl = new ReadWriteLock();

	@XmlTransient
	private static File clusterFile = null;

	@XmlTransient
	private static ClusterManager INSTANCE = null;

	@XmlTransient
	public final static String OSS_CLUSTER_NODES_FILENAME = "cluster.json";

	public final TreeMap<String, ClusterInstance> instancesMap;

	@XmlTransient
	private ClusterInstance me;

	private ClusterManager() {
		instancesMap = new TreeMap<String, ClusterInstance>();
	}

	final private static ReadWriteLock rwlInstance = new ReadWriteLock();

	public final static String OSS_CLUSTER_ID = "oss.cluster.id";

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
			clusterFile = new File(
					StartStopListener.OPENSEARCHSERVER_DATA_FILE,
					OSS_CLUSTER_NODES_FILENAME);
			INSTANCE = JsonUtils.getObject(clusterFile, ClusterManager.class);
			return INSTANCE;
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			rwlInstance.w.unlock();
		}
	}

	public void populateInstances(Collection<ClusterInstance> instances) {
		rwl.r.lock();
		try {
			if (instancesMap != null)
				instances.addAll(instancesMap.values());
		} finally {
			rwl.r.unlock();
		}
	}

	public ClusterInstance getMe() {
		rwl.r.lock();
		try {
			return me;
		} finally {
			rwl.r.unlock();
		}
	}

	public void save() throws JsonGenerationException, JsonMappingException,
			IOException {
		rwl.r.lock();
		try {
			JsonUtils.jsonToFile(this, clusterFile);
		} finally {
			rwl.r.unlock();
		}
	}

}
