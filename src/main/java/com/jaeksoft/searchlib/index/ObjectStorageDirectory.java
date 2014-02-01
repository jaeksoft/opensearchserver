/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013-2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.index;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.file.process.fileInstances.swift.SwiftProtocol;
import com.jaeksoft.searchlib.crawler.file.process.fileInstances.swift.SwiftProtocol.ObjectMeta;
import com.jaeksoft.searchlib.crawler.file.process.fileInstances.swift.SwiftToken;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;
import com.jaeksoft.searchlib.util.IOUtils;

public class ObjectStorageDirectory extends Directory {

	private final HttpDownloader httpDownloader;
	private final SwiftToken swiftToken;
	private final String container;

	public ObjectStorageDirectory(Config config, SwiftToken token,
			String container) throws SearchLibException {
		this.httpDownloader = config.getWebCrawlMaster().getNewHttpDownloader(
				true);
		this.swiftToken = token;
		this.container = container;
	}

	@Override
	public String[] listAll() throws IOException {
		try {
			List<String> files = SwiftProtocol.listObjects(httpDownloader,
					swiftToken, container);
			return files.toArray(new String[files.size()]);
		} catch (URISyntaxException e) {
			throw new IOException(e);
		} catch (SearchLibException e) {
			throw new IOException(e);
		}
	}

	@Override
	public boolean fileExists(String name) throws IOException {
		try {
			return SwiftProtocol.headObject(httpDownloader, swiftToken,
					container, name) != null;
		} catch (IllegalStateException e) {
			throw new IOException(e);
		} catch (URISyntaxException e) {
			throw new IOException(e);
		} catch (SearchLibException e) {
			throw new IOException(e);
		}
	}

	private ObjectMeta getObjectMeta(final String name) throws IOException {
		try {
			ObjectMeta meta = SwiftProtocol.headObject(httpDownloader,
					swiftToken, container, name);
			if (meta == null)
				throw new IOException("No meta information");
			return meta;
		} catch (IllegalStateException e) {
			throw new IOException(e);
		} catch (URISyntaxException e) {
			throw new IOException(e);
		} catch (SearchLibException e) {
			throw new IOException(e);
		}

	}

	@Override
	final public long fileModified(final String name) throws IOException {
		ObjectMeta meta = getObjectMeta(name);
		if (meta.contentLength == null)
			throw new IOException("No content-length information");
		return meta.contentLength;
	}

	@Override
	public void touchFile(String name) throws IOException {
		try {
			SwiftProtocol.touchObject(httpDownloader, swiftToken, container,
					name);
		} catch (IllegalStateException e) {
			throw new IOException(e);
		} catch (URISyntaxException e) {
			throw new IOException(e);
		} catch (SearchLibException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void deleteFile(String name) throws IOException {
		try {
			SwiftProtocol.deleteObject(httpDownloader, swiftToken, container,
					name);
		} catch (IllegalStateException e) {
			throw new IOException(e);
		} catch (URISyntaxException e) {
			throw new IOException(e);
		} catch (SearchLibException e) {
			throw new IOException(e);
		}
	}

	@Override
	public long fileLength(String name) throws IOException {
		ObjectMeta meta = getObjectMeta(name);
		if (meta.lastModified == null)
			throw new IOException("No last-modified information");
		return meta.lastModified;
	}

	@Override
	public IndexOutput createOutput(String name) throws IOException {
		return new Output(getObjectMeta(name));
	}

	@Override
	public IndexInput openInput(String name) throws IOException {
		return new Input(getObjectMeta(name));
	}

	@Override
	public void close() throws IOException {
		httpDownloader.release();
	}

	public class Input extends IndexInput {

		private final ObjectMeta meta;
		private long pos;

		private Input(final ObjectMeta meta) {
			super("ObjectStorage");
			this.meta = meta;
			this.pos = 0;
		}

		@Override
		public void close() throws IOException {
		}

		@Override
		public long getFilePointer() {
			return pos;
		}

		@Override
		public void seek(long pos) throws IOException {
			this.pos = pos;

		}

		@Override
		public long length() {
			return meta.contentLength;
		}

		@Override
		public byte readByte() throws IOException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void readBytes(byte[] b, int offset, int len) throws IOException {
			InputStream inputStream = null;
			try {
				inputStream = SwiftProtocol.readObject(httpDownloader,
						swiftToken, container, meta.pathName, offset, offset
								+ len - 1);
				// TODO copy to byte array
			} catch (IllegalStateException e) {
				throw new IOException(e);
			} catch (URISyntaxException e) {
				throw new IOException(e);
			} catch (SearchLibException e) {
				throw new IOException(e);
			} finally {
				IOUtils.close(inputStream);
			}
		}
	}

	public class Output extends IndexOutput {

		private final ObjectMeta meta;
		private long pos;

		private Output(final ObjectMeta meta) {
			this.meta = meta;
			this.pos = 0;
		}

		@Override
		public void flush() throws IOException {
			// TODO Auto-generated method stub

		}

		@Override
		public void close() throws IOException {
		}

		@Override
		public long getFilePointer() {
			return pos;
		}

		@Override
		public void seek(long pos) throws IOException {
			this.pos = pos;
		}

		@Override
		public long length() throws IOException {
			return meta.contentLength;
		}

		@Override
		public void writeByte(byte b) throws IOException {
			// TODO Auto-generated method stub

		}

		@Override
		public void writeBytes(byte[] b, int offset, int length)
				throws IOException {
			// TODO Auto-generated method stub

		}

	}

}
