/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
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

import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.net.URI;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PutMethod;

public class UriWriteObject {

	private PutMethod putMethod;
	private StreamWriteObject swo;
	private StreamReadObject sro;

	public UriWriteObject(URI uri, Externalizable object) throws IOException {
		putMethod = new PutMethod(uri.toASCIIString());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		swo = new StreamWriteObject(baos);
		swo.write(object);
		swo.close(true);
		swo = null;
		sro = null;
		putMethod.setRequestEntity(new ByteArrayRequestEntity(baos
				.toByteArray()));
		HttpClient httpClient = new HttpClient();
		httpClient.executeMethod(putMethod);
	}

	public int getResponseCode() throws IOException {
		return putMethod.getStatusCode();
	}

	public String getResponseMessage() throws IOException {
		return putMethod.getResponseBodyAsString();
	}

	public Externalizable getResponseObject() throws IOException,
			ClassNotFoundException {
		sro = new StreamReadObject(putMethod.getResponseBodyAsStream());
		return sro.read();
	}

	public void close() {
		if (sro != null) {
			sro.close();
			sro = null;
		}
		if (swo != null) {
			swo.close(false);
			swo = null;
		}
		if (putMethod != null) {
			putMethod.releaseConnection();
			putMethod = null;
		}
	}
}
