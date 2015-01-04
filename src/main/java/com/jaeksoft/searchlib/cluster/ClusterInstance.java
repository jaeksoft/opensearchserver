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

import java.net.URI;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_EMPTY)
public class ClusterInstance {

	private URI uri;
	private ClusterStatus status;
	private Long fileTime;

	public ClusterInstance() {
		uri = null;
		status = ClusterStatus.UNKOWN;
		fileTime = null;
	}

	/**
	 * @return the instanceUrl
	 */
	public URI getUri() {
		return uri;
	}

	/**
	 * @param uri
	 *            the uri to set
	 */
	public void setUri(URI uri) {
		this.uri = uri;
	}

	/**
	 * 
	 * @return if the instance is online
	 */
	@JsonIgnore
	public boolean isOnline() {
		return status == ClusterStatus.ONLINE;
	}

	/**
	 * @return the status
	 */
	public ClusterStatus getStatus() {
		return status;
	}

	public void setStatus(ClusterStatus status) {
		this.status = status;
	}

	/**
	 * @return the fileTime
	 */
	Long getFileTime() {
		return fileTime;
	}

	/**
	 * @param fileTime
	 *            the fileTime to set
	 */
	void setFileTime(Long fileTime) {
		this.fileTime = fileTime;
	}

}
