/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2015 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.opensearchserver;

import java.net.URISyntaxException;

import com.opensearchserver.cluster.client.ClusterMultiClient;
import com.opensearchserver.cluster.manager.ClusterManager;
import com.opensearchserver.crawler.web.WebCrawlerServer;
import com.opensearchserver.crawler.web.client.WebCrawlerMultiClient;
import com.opensearchserver.extractor.ExtractorServiceImpl;
import com.opensearchserver.extractor.ExtractorServiceInterface;
import com.opensearchserver.extractor.ParserManager;
import com.opensearchserver.job.JobServer;
import com.opensearchserver.job.script.ScriptMultiClient;
import com.opensearchserver.provider.AbstractProvider;
import com.opensearchserver.provider.ProviderContext;

public class ServicesProvider extends AbstractProvider {

	@Override
	public void load(ProviderContext context) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unload(ProviderContext context) {
		// TODO Auto-generated method stub

	}

	public ClusterMultiClient getCluster() {
		if (ClusterManager.INSTANCE == null)
			return null;
		return ClusterManager.INSTANCE.getClusterClient();
	}

	public WebCrawlerMultiClient getNewWebCrawler() throws URISyntaxException {
		return new WebCrawlerMultiClient(ClusterManager.INSTANCE
				.getClusterClient().getActiveNodes(
						WebCrawlerServer.SERVICE_NAME), 60000);
	}

	public ScriptMultiClient getNewScriptClient() throws URISyntaxException {
		return new ScriptMultiClient(ClusterManager.INSTANCE.getClusterClient()
				.getActiveNodes(JobServer.SERVICE_NAME_SCRIPT), 60000);
	}

	public ExtractorServiceInterface getNewExtractorClient() {
		if (ParserManager.INSTANCE == null)
			throw new RuntimeException("Extractor service not available");
		return new ExtractorServiceImpl();
	}
}
