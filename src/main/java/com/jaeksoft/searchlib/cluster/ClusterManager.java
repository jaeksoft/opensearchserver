/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013-2014 Emmanuel Keller / Jaeksoft
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
import java.io.FileFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.filefilter.FileFilterUtils;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.util.JsonUtils;
import com.jaeksoft.searchlib.util.NetworksUtils;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.web.StartStopListener;

public class ClusterManager {

	private final ReadWriteLock rwl = new ReadWriteLock();

	private final ClusterInstance clusterInstance;

	private final File clusterDirectory;

	private final File clusterFile;

	private ClusterManager(File instanceDataDir) throws JsonParseException,
			JsonMappingException, IOException, URISyntaxException {
		clusterDirectory = new File(instanceDataDir, ".oss_cluster_nodes");
		if (!clusterDirectory.exists())
			clusterDirectory.mkdir();
		String hardwareAddress = NetworksUtils.getFirstHardwareAddress();
		clusterFile = new File(clusterDirectory, hardwareAddress);
		if (clusterFile.exists() && clusterFile.length() > 0)
			clusterInstance = JsonUtils.getObject(clusterFile,
					ClusterInstance.class);
		else {
			clusterInstance = new ClusterInstance(hardwareAddress);
			saveMe();
		}
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
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} finally {
			rwlInstance.w.unlock();
		}
	}

	public ClusterInstance getMe() {
		return clusterInstance;
	}

	public Collection<ClusterInstance> getInstances() {
		rwl.r.lock();
		try {
			List<ClusterInstance> clusterInstances = new ArrayList<ClusterInstance>();
			File[] files = clusterDirectory
					.listFiles((FileFilter) FileFilterUtils.fileFileFilter());
			for (File file : files) {
				try {
					clusterInstances.add(JsonUtils.getObject(file,
							ClusterInstance.class));
				} catch (JsonParseException e) {
					Logging.warn(e);
				} catch (JsonMappingException e) {
					Logging.warn(e);
				} catch (IOException e) {
					Logging.warn(e);
				}
			}
			return clusterInstances;
		} finally {
			rwl.r.unlock();
		}
	}

	public void saveMe() throws JsonGenerationException, JsonMappingException,
			IOException {
		rwl.w.lock();
		try {
			JsonUtils.jsonToFile(clusterInstance, clusterFile);
		} finally {
			rwl.w.unlock();
		}
	}
}
