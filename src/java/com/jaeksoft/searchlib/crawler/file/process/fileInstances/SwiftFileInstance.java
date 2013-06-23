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
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.file.database.FilePathItem;
import com.jaeksoft.searchlib.crawler.file.database.FileTypeEnum;
import com.jaeksoft.searchlib.crawler.file.process.FileInstanceAbstract;
import com.jaeksoft.searchlib.crawler.file.process.fileInstances.swift.SwiftProtocol;
import com.jaeksoft.searchlib.crawler.file.process.fileInstances.swift.SwiftProtocol.ObjectMeta;
import com.jaeksoft.searchlib.crawler.file.process.fileInstances.swift.SwiftToken;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;
import com.jaeksoft.searchlib.util.LinkUtils;

public class SwiftFileInstance extends FileInstanceAbstract {

	private SwiftToken token;

	private ObjectMeta object;

	public SwiftFileInstance() {
		super();
		token = null;
	}

	protected SwiftFileInstance(FilePathItem filePathItem,
			SwiftFileInstance parent, SwiftToken token, ObjectMeta object)
			throws URISyntaxException, SearchLibException,
			UnsupportedEncodingException {
		this.token = token;
		this.object = object;
		init(filePathItem, parent, object.pathName);
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
	public URI init() throws SearchLibException, URISyntaxException,
			UnsupportedEncodingException {
		String path = getPath();
		if (token != null)
			return token.getURI(filePathItem.getSwiftContainer(), getPath(),
					false);
		return new URI(LinkUtils.concatPath(
				getFilePathItem().getSwiftAuthURL(), path));
	}

	@Override
	public FileTypeEnum getFileType() throws SearchLibException {
		if (object == null)
			return FileTypeEnum.directory;
		return object.isDirectory ? FileTypeEnum.directory : FileTypeEnum.file;
	}

	private FileInstanceAbstract[] listFiles(boolean withDirectory)
			throws URISyntaxException, SearchLibException {
		HttpDownloader downloader = new HttpDownloader(null, false, null);
		try {
			authentication(downloader);
			List<ObjectMeta> objectList = SwiftProtocol.listObjects(downloader,
					token, filePathItem.getSwiftContainer(), getPath(),
					withDirectory, filePathItem.isIgnoreHiddenFiles(),
					filePathItem.getExclusionMatchers());
			if (objectList == null)
				return null;

			FileInstanceAbstract[] files = new FileInstanceAbstract[objectList
					.size()];
			int i = 0;
			FilePathItem fpi = this.getFilePathItem();
			for (ObjectMeta object : objectList)
				files[i++] = new SwiftFileInstance(fpi, this, token, object);
			return files;
		} catch (ClientProtocolException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (JSONException e) {
			throw new SearchLibException(e);
		} catch (ParseException e) {
			throw new SearchLibException(e);
		} finally {
			downloader.release();
		}
	}

	@Override
	public FileInstanceAbstract[] listFilesAndDirectories()
			throws URISyntaxException, SearchLibException {
		return listFiles(true);
	}

	@Override
	public FileInstanceAbstract[] listFilesOnly() throws URISyntaxException,
			SearchLibException {
		return listFiles(false);

	}

	@Override
	public Long getLastModified() throws SearchLibException {
		if (object == null)
			return null;
		return object.lastModified;

	}

	@Override
	public Long getFileSize() throws SearchLibException {
		if (object == null)
			return null;
		return object.contentLength;
	}

	@Override
	public String getFileName() throws SearchLibException {
		if (object == null)
			return null;
		return object.name;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		HttpDownloader downloader = new HttpDownloader(null, false, null);
		try {
			authentication(downloader);
			return SwiftProtocol.getObject(downloader, getFilePathItem()
					.getSwiftContainer(), token, object);
		} catch (URISyntaxException e) {
			throw new IOException(e);
		} catch (JSONException e) {
			throw new IOException(e);
		} catch (SearchLibException e) {
			throw new IOException(e);
		}
	}
}
