/**
 * License Agreement for OpenSearchServer
 * <p/>
 * Copyright (C) 2013-2016 Emmanuel Keller / Jaeksoft
 * <p/>
 * http://www.open-search-server.com
 * <p/>
 * This file is part of OpenSearchServer.
 * <p/>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.crawler.file.process.fileInstances;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.file.database.FilePathItem;
import com.jaeksoft.searchlib.crawler.file.database.FileTypeEnum;
import com.jaeksoft.searchlib.crawler.file.process.FileInstanceAbstract;
import com.jaeksoft.searchlib.crawler.file.process.fileInstances.swift.SwiftProtocol;
import com.jaeksoft.searchlib.crawler.file.process.fileInstances.swift.SwiftProtocol.ObjectMeta;
import com.jaeksoft.searchlib.crawler.file.process.fileInstances.swift.SwiftToken;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;
import com.jaeksoft.searchlib.util.LinkUtils;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class SwiftFileInstance extends FileInstanceAbstract {

	private SwiftToken token;

	private ObjectMeta object;

	public SwiftFileInstance() {
		super();
		token = null;
	}

	protected SwiftFileInstance(FilePathItem filePathItem, SwiftFileInstance parent, SwiftToken token,
			ObjectMeta object) throws URISyntaxException, UnsupportedEncodingException {
		this.token = token;
		this.object = object;
		init(filePathItem, parent, object.pathName);
	}

	private void authentication(HttpDownloader downloader)
			throws ClientProtocolException, URISyntaxException, IOException, JSONException {
		if (token != null)
			return;
		token = new SwiftToken(downloader, filePathItem.getSwiftAuthURL(), filePathItem.getUsername(),
				filePathItem.getPassword(), filePathItem.getSwiftAuthType(), filePathItem.getSwiftTenant());
	}

	@Override
	public URI init() throws URISyntaxException, UnsupportedEncodingException {
		String path = getPath();
		if (token != null)
			return token.getURI(filePathItem.getSwiftContainer(), path, false);
		return new URI(LinkUtils.concatPath(getFilePathItem().getSwiftAuthURL(), path));
	}

	@Override
	public FileTypeEnum getFileType() {
		if (object == null)
			return FileTypeEnum.directory;
		return object.isDirectory ? FileTypeEnum.directory : FileTypeEnum.file;
	}

	private FileInstanceAbstract[] listFiles(boolean withDirectory)
			throws URISyntaxException, ClientProtocolException, JSONException, IOException {
		HttpDownloader downloader = new HttpDownloader(null, false, null, 600);
		try {
			authentication(downloader);
			List<ObjectMeta> objectList = SwiftProtocol
					.listObjects(downloader, token, filePathItem.getSwiftContainer(), getPath(), withDirectory,
							filePathItem.isIgnoreHiddenFiles(), filePathItem.getExclusionMatchers());
			if (objectList == null)
				return null;

			FileInstanceAbstract[] files = new FileInstanceAbstract[objectList.size()];
			int i = 0;
			FilePathItem fpi = this.getFilePathItem();
			for (ObjectMeta object : objectList)
				files[i++] = new SwiftFileInstance(fpi, this, token, object);
			return files;
		} finally {
			downloader.release();
		}
	}

	@Override
	public FileInstanceAbstract[] listFilesAndDirectories()
			throws URISyntaxException, ClientProtocolException, JSONException, IOException {
		return listFiles(true);
	}

	@Override
	public FileInstanceAbstract[] listFilesOnly()
			throws URISyntaxException, ClientProtocolException, JSONException, IOException {
		return listFiles(false);

	}

	@Override
	public Long getLastModified() {
		if (object == null)
			return null;
		return object.lastModified;

	}

	@Override
	public Long getFileSize() {
		if (object == null)
			return null;
		return object.contentLength;
	}

	@Override
	public String getFileName() {
		if (object == null)
			return null;
		return object.name;
	}

	@Override
	public void delete() throws IOException {
		throw new IOException("Delete not implemented");
	}

	@Override
	public InputStream getInputStream() throws IOException {
		try {
			HttpDownloader downloader = new HttpDownloader(null, false, null, 600);
			authentication(downloader);
			return SwiftProtocol.getObject(downloader, getFilePathItem().getSwiftContainer(), token, object);
		} catch (URISyntaxException e) {
			throw new IOException(e);
		} catch (JSONException e) {
			throw new IOException(e);
		} catch (SearchLibException e) {
			throw new IOException(e);
		}
	}
}
