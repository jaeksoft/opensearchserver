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

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.ws.rs.ApplicationPath;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.jaeksoft.opensearchserver.ServerConfiguration.ServiceEnum;
import com.opensearchserver.cluster.ClusterServer;
import com.opensearchserver.cluster.manager.ClusterManager;
import com.opensearchserver.cluster.service.ClusterServiceImpl;
import com.opensearchserver.crawler.web.WebCrawlerServer;
import com.opensearchserver.crawler.web.service.WebCrawlerServiceImpl;
import com.opensearchserver.extractor.ExtractorServer;
import com.opensearchserver.extractor.ExtractorServiceImpl;
import com.opensearchserver.graph.GraphServer;
import com.opensearchserver.graph.GraphServiceImpl;
import com.opensearchserver.job.JobServer;
import com.opensearchserver.job.scheduler.SchedulerServiceImpl;
import com.opensearchserver.job.script.ScriptServiceImpl;
import com.opensearchserver.renderer.RendererServer;
import com.opensearchserver.renderer.RendererServer.RendererApplication;
import com.opensearchserver.search.SearchServer;
import com.opensearchserver.search.index.IndexServiceImpl;
import com.opensearchserver.utils.server.AbstractServer;
import com.opensearchserver.utils.server.RestApplication;
import com.opensearchserver.utils.server.ServletApplication;

public class OpenSearchServer extends AbstractServer {

	private final static int DEFAULT_PORT = 9090;
	private final static String DEFAULT_HOSTNAME = "0.0.0.0";
	private final static String MAIN_JAR = "opensearchserver.jar";
	public final static String DEFAULT_DATADIR_NAME = "opensearchserver";

	private final static String RENDERER_CONTEXT_PATH = "/";

	private final static String SERVER_YAML_NAME = "server.yaml";
	private static ServerConfiguration serverConfiguration = null;

	private final HashSet<String> services = new HashSet<String>();

	private OpenSearchServer() {
		super(DEFAULT_HOSTNAME, DEFAULT_PORT, MAIN_JAR, DEFAULT_DATADIR_NAME);
	}

	@ApplicationPath("/")
	public static class OpenSearchServerApplication extends RestApplication {

		@Override
		public Set<Class<?>> getClasses() {
			Set<Class<?>> classes = super.getClasses();
			classes.add(ClusterServiceImpl.class);
			if (ServiceEnum.extractor.isActive(serverConfiguration))
				classes.add(ExtractorServiceImpl.class);
			if (ServiceEnum.script.isActive(serverConfiguration))
				classes.add(ScriptServiceImpl.class);
			if (ServiceEnum.scheduler.isActive(serverConfiguration))
				classes.add(SchedulerServiceImpl.class);
			if (ServiceEnum.webcrawler.isActive(serverConfiguration))
				classes.add(WebCrawlerServiceImpl.class);
			if (ServiceEnum.search.isActive(serverConfiguration))
				classes.add(IndexServiceImpl.class);
			if (ServiceEnum.graph.isActive(serverConfiguration))
				classes.add(GraphServiceImpl.class);
			return classes;
		}
	}

	private File subDir(File dataDir, String name) throws IOException {
		File dir = new File(dataDir, name);
		if (!dir.exists())
			dir.mkdir();
		if (!dir.isDirectory())
			throw new IOException(
					"The configuration directory does not exist or cannot be created: "
							+ dir.getName());
		return dir;
	}

	@Override
	public void defineOptions(Options options) {
		super.defineOptions(options);
		options.addOption(JobServer.THREADS_OPTION);
	}

	@Override
	public void commandLine(CommandLine cmd) throws IOException {
		// Load the configuration file
		File serverConfigurationFile = new File(getCurrentDataDir(),
				SERVER_YAML_NAME);
		if (serverConfigurationFile.exists()
				&& serverConfigurationFile.isFile())
			serverConfiguration = ServerConfiguration
					.getNewInstance(serverConfigurationFile);

		serverConfiguration = ServerConfiguration.getDefaultConfiguration();
	}

	@Override
	public void load() throws IOException {

		File data_directory = getCurrentDataDir();

		ClusterServer.load(this, subDir(data_directory, "cluster"), null, null);

		if (ServiceEnum.extractor.isActive(serverConfiguration)) {
			ExtractorServer.load(this, subDir(data_directory, "extractor"),
					null);
			services.add(ServiceEnum.extractor.name());
		}

		if (ServiceEnum.renderer.isActive(serverConfiguration)) {
			RendererServer.load(RENDERER_CONTEXT_PATH, null, 1,
					subDir(data_directory, "renderer"));
			services.add(ServiceEnum.extractor.name());
		}

		if (ServiceEnum.script.isActive(serverConfiguration)) {
			JobServer.loadScript(this);
			services.add(ServiceEnum.script.name());
		}

		if (ServiceEnum.scheduler.isActive(serverConfiguration)) {
			JobServer.loadScheduler(this,
					serverConfiguration.getSchedulerMaxThreads());
			services.add(ServiceEnum.scheduler.name());
		}

		if (ServiceEnum.webcrawler.isActive(serverConfiguration)) {
			WebCrawlerServer.load(this);
			services.add(ServiceEnum.webcrawler.name());
		}

		if (ServiceEnum.search.isActive(serverConfiguration)) {
			SearchServer.loadIndexManager(this);
			services.add(ServiceEnum.search.name());
		}

		if (ServiceEnum.graph.isActive(serverConfiguration)) {
			GraphServer.load(subDir(data_directory, "graph"));
			services.add(ServiceEnum.graph.name());
		}

	}

	@Override
	public ServletApplication getServletApplication() {
		if (ServiceEnum.renderer.isActive(serverConfiguration))
			return new RendererApplication(RENDERER_CONTEXT_PATH);
		return null;
	}

	@Override
	public RestApplication getRestApplication() {
		return new OpenSearchServerApplication();
	}

	public static void main(String[] args) throws IOException,
			ServletException, ParseException {
		// Start the server
		OpenSearchServer server = new OpenSearchServer();
		server.start(args);
		// Register the services
		ClusterManager.INSTANCE.registerMe(server.services);
	}
}
