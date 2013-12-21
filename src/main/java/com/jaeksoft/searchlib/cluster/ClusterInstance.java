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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.jaeksoft.searchlib.util.JsonUtils;

public class ClusterInstance {

	private URI uri = null;
	private Integer id = null;
	private String login = null;
	private String apiKey = null;
	private ClusterStatus status = ClusterStatus.UNKNOWN;
	private Long statusTime = null;
	private int allowedConnectionTimeOut = 1000;

	/**
	 * @return the instanceUrl
	 */
	public URI getUri() {
		return uri;
	}

	/**
	 * @param instanceUrl
	 *            the instanceUrl to set
	 * @throws URISyntaxException
	 */
	public void setUri(String instanceUrl) throws URISyntaxException {
		this.uri = new URI(instanceUrl);
	}

	/**
	 * @return the login
	 */
	public String getLogin() {
		return login;
	}

	/**
	 * @param login
	 *            the login to set
	 */
	public void setLogin(String login) {
		this.login = login;
	}

	/**
	 * @return the apiKey
	 */
	public String getApiKey() {
		return apiKey;
	}

	/**
	 * @param apiKey
	 *            the apiKey to set
	 */
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	/**
	 * @return the status
	 */
	public ClusterStatus getStatus() {
		return status;
	}

	/**
	 * @return the statusTime
	 */
	public Long getStatusTime() {
		return statusTime;
	}

	/**
	 * @return the allowedConnectionTimeOut
	 */
	public int getAllowedConnectionTimeOut() {
		return allowedConnectionTimeOut;
	}

	/**
	 * @param allowedConnectionTimeOut
	 *            the allowedConnectionTimeOut to set
	 */
	public void setAllowedConnectionTimeOut(int allowedConnectionTimeOut) {
		this.allowedConnectionTimeOut = allowedConnectionTimeOut;
	}

	/**
	 * @return the instanceId
	 */
	public Integer getId() {
		return id;
	}

	// TODO
	public void checkStatus() {

	}

	private final static TypeReference<List<ClusterInstance>> ClusterInstanceListTypeRef = new TypeReference<List<ClusterInstance>>() {
	};

	/**
	 * Read the a list of instances stored in JSON format
	 * 
	 * @param file
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	final static List<ClusterInstance> readList(File file)
			throws JsonParseException, JsonMappingException, IOException {
		if (!file.exists())
			return null;
		return JsonUtils.getObject(file, ClusterInstanceListTypeRef);
	}

	/**
	 * Write a list of instances in JSON format
	 * 
	 * @param clusterInstances
	 *            A collection of cluster instances
	 * @param file
	 *            The destination file
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	final static void writeList(Collection<ClusterInstance> clusterInstances,
			File file) throws JsonGenerationException, JsonMappingException,
			IOException {
		JsonUtils.jsonToFile(clusterInstances, file);
	}

}
