/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.remote;

import java.io.IOException;
import java.net.URI;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;

public class UriRead {

	protected GetMethod getMethod = null;

	public UriRead(URI uri) throws HttpException, IOException {
		HttpClient httpClient = new HttpClient();
		getMethod = new GetMethod(uri.toASCIIString());
		getMethod.addRequestHeader("Connection", "close");
		httpClient.executeMethod(getMethod);
	}

	public int getResponseCode() throws IOException {
		return getMethod.getStatusCode();
	}

	public String getResponseMessage() throws IOException {
		return getMethod.getStatusText();
	}

	public void close() {
		if (getMethod != null) {
			getMethod.releaseConnection();
			getMethod = null;
		}
	}

}
