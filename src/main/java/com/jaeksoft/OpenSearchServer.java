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

package com.jaeksoft;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;
import javax.ws.rs.ApplicationPath;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.opensearchserver.cluster.ClusterServer;
import com.opensearchserver.cluster.ClusterServiceImpl;
import com.opensearchserver.crawler.web.WebCrawlerServer;
import com.opensearchserver.crawler.web.session.CrawlSessionServiceImpl;
import com.opensearchserver.extractor.ExtractorServer;
import com.opensearchserver.extractor.ExtractorServiceImpl;
import com.opensearchserver.job.JobServer;
import com.opensearchserver.job.scheduler.SchedulerServiceImpl;
import com.opensearchserver.job.script.ScriptServiceImpl;
import com.opensearchserver.renderer.RendererServer;
import com.opensearchserver.renderer.RendererServer.RendererApplication;
import com.opensearchserver.utils.server.AbstractServer;
import com.opensearchserver.utils.server.RestApplication;
import com.opensearchserver.utils.server.ServletApplication;

public class OpenSearchServer extends AbstractServer {

	private final static int DEFAULT_PORT = 9090;
	private final static String DEFAULT_HOSTNAME = "0.0.0.0";
	private final static String MAIN_JAR = "opensearchserver.jar";
	public final static String DEFAULT_DATADIR_NAME = "opensearchserver";

	private OpenSearchServer() {
		super(DEFAULT_HOSTNAME, DEFAULT_PORT, MAIN_JAR, DEFAULT_DATADIR_NAME);
	}

	@ApplicationPath("/")
	public static class OpenSearchServerApplication extends RestApplication {

		@Override
		public Set<Class<?>> getClasses() {
			Set<Class<?>> classes = super.getClasses();
			classes.add(ClusterServiceImpl.class);
			classes.add(ExtractorServiceImpl.class);
			classes.add(ScriptServiceImpl.class);
			classes.add(SchedulerServiceImpl.class);
			classes.add(CrawlSessionServiceImpl.class);
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

	private int maxThreads = 1000;

	@Override
	public void defineOptions(Options options) {
		super.defineOptions(options);
		options.addOption(JobServer.THREADS_OPTION);
	}

	@Override
	public void commandLine(CommandLine cmd) throws IOException {
		maxThreads = Integer
				.parseInt(JobServer.THREADS_OPTION.getValue("1000"));
	}

	@Override
	public void load() throws IOException {
		File data_directory = getCurrentDataDir();
		ClusterServer.load(this, subDir(data_directory, "cluster"), null);
		ExtractorServer.load(this, subDir(data_directory, "extractor"), null);
		RendererServer.load("/", null, 1, subDir(data_directory, "renderer"));
		JobServer.load(this, maxThreads);
		WebCrawlerServer.load(this);
	}

	@Override
	public ServletApplication getServletApplication() {
		return new RendererApplication("/");
	}

	@Override
	public RestApplication getRestApplication() {
		return new OpenSearchServerApplication();
	}

	public static void main(String[] args) throws IOException,
			ServletException, ParseException {
		new OpenSearchServer().start(args);
	}
}
