/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.web.spider;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import com.jaeksoft.searchlib.SearchLibException;

public class HadoopDownloader extends DownloaderAbstract {

	private URI redirectLocation;
	private Long contentLength;
	private String contentDispositionFilename;
	private String contentBaseType;
	private String contentTypeCharset;
	private String contentEncoding;
	private Integer statusCode;

	public HadoopDownloader() {
	}

	public boolean get(URI uri) {
		return false;
	}

	@Override
	public URI getRedirectLocation() throws SearchLibException {
		return redirectLocation;
	}

	@Override
	public Long getContentLength() {
		return contentLength;
	}

	@Override
	public String getContentDispositionFilename() {
		return contentDispositionFilename;
	}

	@Override
	public String getContentBaseType() {
		return contentBaseType;
	}

	@Override
	public String getContentTypeCharset() {
		return contentTypeCharset;
	}

	@Override
	public String getContentEncoding() {
		return contentEncoding;
	}

	@Override
	public Integer getStatusCode() {
		return statusCode;
	}

	@Override
	public InputStream getContent() throws IllegalStateException, IOException {
		// TODO Auto-generated method stub
		return null;
	}
}
