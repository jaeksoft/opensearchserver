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

import java.io.IOException;
import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

public class UrlReadObject {

	// private HttpURLConnection huc;
	private StreamReadObject sro;
	private GetMethod getMethod;

	public UrlReadObject(URL url) throws IOException {
		HttpClient httpClient = new HttpClient();
		getMethod = new GetMethod(url.toExternalForm());
		getMethod.addRequestHeader("Connection", "close");
		httpClient.executeMethod(getMethod);
		sro = new StreamReadObject(getMethod.getResponseBodyAsStream());
		// getMethod.huc = (HttpURLConnection) url.openConnection();
		// sro = new StreamReadObject(huc.getInputStream());
	}

	public Object read() throws IOException, ClassNotFoundException {
		return sro.read();
	}

	public int getResponseCode() throws IOException {
		return getMethod.getStatusCode();
	}

	public String getResponseMessage() throws IOException {
		return getMethod.getStatusText();
	}

	public void close() {
		if (sro != null) {
			sro.close();
			sro = null;
		}
		/*
		 * if (huc != null) { huc.disconnect(); huc = null; }
		 */
		if (getMethod != null) {
			getMethod.releaseConnection();
			getMethod = null;
		}
	}
}
