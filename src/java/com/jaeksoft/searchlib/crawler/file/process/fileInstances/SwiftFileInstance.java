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

package com.jaeksoft.searchlib.crawler.file.process.fileInstances;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.file.database.FilePathItem;
import com.jaeksoft.searchlib.crawler.file.database.FileTypeEnum;
import com.jaeksoft.searchlib.crawler.file.process.FileInstanceAbstract;
import com.jaeksoft.searchlib.crawler.file.process.fileInstances.swift.SwiftListObjects;
import com.jaeksoft.searchlib.crawler.file.process.fileInstances.swift.SwiftToken;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;

public class SwiftFileInstance extends FileInstanceAbstract {

	private SwiftToken token;

	public SwiftFileInstance() {
		super();
		token = null;
	}

	protected SwiftFileInstance(FilePathItem filePathItem,
			SwiftFileInstance parent, SwiftToken token, String path)
			throws URISyntaxException, SearchLibException {
		init(filePathItem, parent, path);
		this.token = token;
	}

	private void authentication(HttpDownloader downloader)
			throws ClientProtocolException, URISyntaxException, IOException,
			JSONException, SearchLibException {
		if (token != null)
			return;
		token = new SwiftToken(downloader, filePathItem.getSwiftAuthURL(),
				filePathItem.getUsername(), filePathItem.getPassword(),
				filePathItem.getSwiftAuthType(), filePathItem.getSwiftTenant());
	}

	@Override
	public URI init() throws SearchLibException, URISyntaxException {
		return new URI("swift", filePathItem.getHost(), getPath(), null);
	}

	@Override
	public FileTypeEnum getFileType() throws SearchLibException {
		// Check if there is another kind of file
		return FileTypeEnum.file;
	}

	@Override
	public FileInstanceAbstract[] listFilesAndDirectories()
			throws URISyntaxException, SearchLibException {
		return listFilesOnly();
	}

	@Override
	public FileInstanceAbstract[] listFilesOnly() throws URISyntaxException,
			SearchLibException {
		HttpDownloader downloader = new HttpDownloader(null, false, null);
		try {
			authentication(downloader);
			SwiftListObjects swiftListObjects = new SwiftListObjects(
					downloader, token);
			List<String> objectList = swiftListObjects.getObjectList();
			if (objectList == null)
				return null;
			FileInstanceAbstract[] files = new FileInstanceAbstract[objectList
					.size()];
			int i = 0;
			FilePathItem fpi = this.getFilePathItem();
			for (String objectName : objectList)
				files[i++] = new SwiftFileInstance(fpi, this, token, objectName);
			return files;
		} catch (ClientProtocolException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (JSONException e) {
			throw new SearchLibException(e);
		} finally {
			downloader.release();
		}
	}

	@Override
	public Long getLastModified() throws SearchLibException {
		// TODO
		throw new SearchLibException("To do");

	}

	@Override
	public Long getFileSize() throws SearchLibException {
		// TODO
		throw new SearchLibException("To do");

	}

	@Override
	public String getFileName() throws SearchLibException {
		// TODO
		throw new SearchLibException("To do");

	}

	@Override
	public InputStream getInputStream() throws IOException {
		// TODO
		throw new IOException("To do");

	}

}
