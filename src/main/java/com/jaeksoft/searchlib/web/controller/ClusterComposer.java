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

package com.jaeksoft.searchlib.web.controller;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.cluster.ClusterInstance;
import com.jaeksoft.searchlib.cluster.ClusterManager;
import com.jaeksoft.searchlib.cluster.ClusterStatus;
import com.jaeksoft.searchlib.util.properties.PropertyItem;

@AfterCompose(superclass = true)
public class ClusterComposer extends CommonController {

	public ClusterInstance current = null;

	public ClusterComposer() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() throws SearchLibException {
		current = null;
	}

	public PropertyItem<Integer> getClusterInstanceId() {
		return ClientFactory.INSTANCE.getClusterInstanceId();
	}

	public ClusterManager getClusterManager() throws SearchLibException {
		return ClientCatalog.getClusterManager();
	}

	public String getUrl() throws SearchLibException {
		URI uri = getClusterManager().getMe().getUri();
		if (uri == null)
			return null;
		return uri.toString();
	}

	@NotifyChange("clusterManager")
	public void setUrl(String uriString) throws URISyntaxException,
			SearchLibException, JsonGenerationException, JsonMappingException,
			IOException {
		URI uri = new URI(uriString);
		ClusterManager manager = getClusterManager();
		manager.getMe().setUri(uri);
		manager.saveMe();
	}

	@Command
	@NotifyChange("clusterManager")
	public void onSetOffline() throws SearchLibException,
			JsonGenerationException, JsonMappingException, IOException {
		ClusterManager manager = getClusterManager();
		manager.getMe().setStatus(ClusterStatus.OFFLINE);
		manager.saveMe();
	}

	@Command
	@NotifyChange("clusterManager")
	public void onSetOnline() throws SearchLibException,
			JsonGenerationException, JsonMappingException, IOException {
		ClusterManager manager = getClusterManager();
		manager.getMe().setStatus(ClusterStatus.ONLINE);
		manager.saveMe();
	}
}
