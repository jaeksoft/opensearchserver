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
import java.io.InputStream;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class ServerConfiguration {

	public static enum ServiceEnum {

		webcrawler,

		extractor,

		script,

		scheduler,

		renderer,

		search,

		graph;

		/**
		 * @param serverConfiguration
		 * @return true if the service is present
		 */
		public boolean isActive(ServerConfiguration serverConfiguration) {
			if (serverConfiguration == null)
				return true;
			if (serverConfiguration.services == null)
				return true;
			return serverConfiguration.services.contains(this);
		}
	}

	public final Set<ServiceEnum> services = null;

	public final Integer scheduler_max_threads = null;

	/**
	 * @return the number of allowed threads. The default value is 1000.
	 */
	int getSchedulerMaxThreads() {
		return scheduler_max_threads == null ? 1000 : scheduler_max_threads;
	}

	/**
	 * Load the configuration file.
	 * 
	 * @param file
	 * @return an instance of ServerConfiguration
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	static ServerConfiguration getNewInstance(File file)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		return mapper.readValue(file, ServerConfiguration.class);
	}

	/**
	 * Read the configuration file from the resources
	 * 
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	static ServerConfiguration getDefaultConfiguration()
			throws JsonParseException, JsonMappingException, IOException {
		InputStream stream = OpenSearchServer.class
				.getResourceAsStream("server.yaml");
		if (stream == null)
			throw new IOException(
					"Unable to load the default configuration resource: server.yaml");
		try {
			ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
			return mapper.readValue(stream, ServerConfiguration.class);
		} finally {
			if (stream != null)
				IOUtils.closeQuietly(stream);
		}

	}
}
