/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.remote;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PutMethod;

public class UrlWriteObject {

	private PutMethod putMethod;
	private StreamWriteObject swo;

	public UrlWriteObject(URL url, Object object) throws IOException {
		putMethod = new PutMethod(url.toExternalForm());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		swo = new StreamWriteObject(baos);
		swo.write(object);
		swo.close(true);
		swo = null;
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

	public void close() {
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
