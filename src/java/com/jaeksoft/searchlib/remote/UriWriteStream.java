/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
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

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.PutMethod;

public class UriWriteStream {

	private PutMethod putMethod;

	public UriWriteStream(URI uri, File file) throws IOException {
		putMethod = new PutMethod(uri.toASCIIString());
		FileRequestEntity fre = new FileRequestEntity(file,
				"application/octet-stream");
		putMethod.setRequestEntity(fre);
		HttpClient httpClient = new HttpClient();
		httpClient.executeMethod(putMethod);
	}

	public int getResponseCode() throws IOException {
		return putMethod.getStatusCode();
	}

	public String getResponseMessage() throws IOException {
		return putMethod.getResponseBodyAsString();
	}

	public void close() {
		if (putMethod != null) {
			putMethod.releaseConnection();
			putMethod = null;
		}
	}
}
