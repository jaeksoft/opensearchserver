/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.remote;

import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.net.URI;

import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;

public class UriWriteObject extends UriHttp {

	private StreamWriteObject swo = null;
	private StreamReadObject sro = null;

	public UriWriteObject(URI uri, Externalizable object) throws IOException {
		HttpPut httpPut = new HttpPut(uri.toASCIIString());
		httpPut.setConfig(requestConfig);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		swo = new StreamWriteObject(baos);
		swo.write(object);
		swo.close(true);
		swo = null;
		sro = null;
		httpPut.setEntity(new ByteArrayEntity(baos.toByteArray()));
		execute(httpPut);
	}

	public Externalizable getResponseObject() throws IOException,
			ClassNotFoundException {
		sro = new StreamReadObject(httpResponse.getEntity().getContent());
		return sro.read();
	}

	@Override
	public void close() {
		if (sro != null) {
			sro.close();
			sro = null;
		}
		if (swo != null) {
			swo.close(false);
			swo = null;
		}
		super.close();
	}
}
