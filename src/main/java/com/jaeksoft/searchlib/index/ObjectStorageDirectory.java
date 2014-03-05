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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.NoLockFactory;
import org.apache.lucene.store.NoSuchDirectoryException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.cache.LRUCache;
import com.jaeksoft.searchlib.crawler.file.process.fileInstances.swift.SwiftProtocol;
import com.jaeksoft.searchlib.crawler.file.process.fileInstances.swift.SwiftProtocol.ObjectMeta;
import com.jaeksoft.searchlib.crawler.file.process.fileInstances.swift.SwiftToken;
import com.jaeksoft.searchlib.crawler.web.spider.DownloadItem;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;
import com.jaeksoft.searchlib.util.IOUtils;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.array.BytesOutputStream;

public class ObjectStorageDirectory extends Directory {

	private final HttpDownloader httpDownloader;
	private final SwiftToken swiftToken;
	private final String container;
	private final ByteCache inputCache;
	private String[] listAllCache;

	public ObjectStorageDirectory(HttpDownloader httpDownloader,
			SwiftToken token, String container) throws SearchLibException {
		this.httpDownloader = httpDownloader;
		this.swiftToken = token;
		this.container = container;
		this.lockFactory = NoLockFactory.getNoLockFactory();
		this.inputCache = new ByteCache(100);
		this.listAllCache = null;
	}

	@Override
	public String[] listAll() throws IOException {
		try {
			if (listAllCache != null)
				return listAllCache;
			List<String> files = SwiftProtocol.listObjects(httpDownloader,
					swiftToken, container);
			if (CollectionUtils.isEmpty(files))
				throw new NoSuchDirectoryException("Empty");
			listAllCache = files.toArray(new String[files.size()]);
			return listAllCache;
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	@Override
	public boolean fileExists(String name) throws IOException {
		try {
			if (inputCache.get(name) != null)
				return true;
			ObjectMeta meta = SwiftProtocol.headObject(httpDownloader,
					swiftToken, container, name);
			if (meta == null)
				return false;
			inputCache.add(name, new ByteCacheItem().set(meta.contentLength,
					meta.lastModified));
			return true;
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	private ObjectMeta getObjectMeta(final String name) throws IOException {
		try {
			ObjectMeta meta = SwiftProtocol.headObject(httpDownloader,
					swiftToken, container, name);
			if (meta == null)
				throw new FileNotFoundException("File not found: " + name);
			return meta;
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	@Override
	final public long fileModified(final String name) throws IOException {

		ByteCacheItem byteCacheItem = inputCache.get(name);
		if (byteCacheItem != null && byteCacheItem.lastModified != null)
			return byteCacheItem.lastModified;

		ObjectMeta meta = getObjectMeta(name);
		if (meta.lastModified == null)
			throw new IOException("No lastModified information");

		if (byteCacheItem == null)
			byteCacheItem = inputCache.add(name, new ByteCacheItem());
		byteCacheItem.set(meta.contentLength, meta.lastModified);
		return meta.lastModified;
	}

	@Override
	public void touchFile(String name) throws IOException {
		try {
			SwiftProtocol.touchObject(httpDownloader, swiftToken, container,
					name);
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	@Override
	public void deleteFile(String name) throws IOException {
		try {
			SwiftProtocol.deleteObject(httpDownloader, swiftToken, container,
					name);
			inputCache.remove(name);
			listAllCache = null;
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	@Override
	public long fileLength(String name) throws IOException {

		ByteCacheItem byteCacheItem = inputCache.get(name);
		if (byteCacheItem != null && byteCacheItem.length != null)
			return byteCacheItem.length;

		ObjectMeta meta = getObjectMeta(name);
		if (meta.contentLength == null)
			throw new IOException("No contentLength information");

		if (byteCacheItem == null)
			byteCacheItem = inputCache.add(name, new ByteCacheItem());
		byteCacheItem.set(meta.contentLength, meta.lastModified);

		return meta.contentLength;
	}

	@Override
	public IndexOutput createOutput(String name) throws IOException {
		return new Output(name);
	}

	@Override
	public IndexInput openInput(String name) throws IOException {
		InputStream inputStream = null;
		try {
			ByteCacheItem byteCacheItem = inputCache.get(name);
			if (byteCacheItem != null && byteCacheItem.bytes != null)
				return new Input(name, byteCacheItem.bytes);

			DownloadItem downloadItem = SwiftProtocol.readObject(
					httpDownloader, swiftToken, container, name);
			Long length = downloadItem.getContentLength();
			if (length == null)
				throw new IOException("No content length");
			if (byteCacheItem == null)
				byteCacheItem = new ByteCacheItem();
			byteCacheItem.set(downloadItem.getContentLength(),
					downloadItem.getLastModified());
			if (byteCacheItem.length > 0) {
				inputStream = downloadItem.getContentInputStream();
				byteCacheItem.bytes = IOUtils.toByteArray(inputStream);
				inputStream.read();
			} else
				byteCacheItem.bytes = ArrayUtils.EMPTY_BYTE_ARRAY;
			inputCache.add(name, byteCacheItem);
			return new Input(name, byteCacheItem.bytes);
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException(e);
		} finally {
			IOUtils.close(inputStream);
		}
	}

	@Override
	public void close() throws IOException {
		httpDownloader.release();
		inputCache.clear();
	}

	public class Input extends IndexInput {

		private byte[] bytes;
		private long pos;

		public Input(String path, byte[] bytes) {
			super(StringUtils.fastConcat("ObjectStorage ", path));
			this.bytes = bytes;
			pos = 0;
		}

		@Override
		public void close() throws IOException {
			bytes = null;
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
			return bytes == null ? 0 : bytes.length;
		}

		@Override
		public byte readByte() throws IOException {
			return bytes[(int) pos++];
		}

		@Override
		public void readBytes(byte[] b, int offset, int len) throws IOException {
			System.arraycopy(bytes, (int) pos, b, offset, len);
			pos += len;
		}
	}

	private class Output extends IndexOutput {

		private BytesOutputStream bytes;
		private final String pathName;
		private long pos;
		private long length;

		private Output(final String pathName) {
			this.pathName = pathName;
			this.pos = 0;
			this.bytes = new BytesOutputStream();
			this.length = 0;
		}

		@Override
		public void flush() throws IOException {
		}

		@Override
		public void close() throws IOException {
			try {
				if (bytes == null)
					return;
				DownloadItem downloadItem = SwiftProtocol.writeObject(
						httpDownloader, swiftToken, container, pathName, bytes);
				ByteCacheItem byteCacheItem = new ByteCacheItem();
				byteCacheItem.set(downloadItem.getContentLength(),
						downloadItem.getLastModified());
				if (length > 0)
					byteCacheItem.bytes = bytes.toByteArray();
				else
					byteCacheItem.bytes = ArrayUtils.EMPTY_BYTE_ARRAY;
				inputCache.add(pathName, byteCacheItem);
				bytes = null;
				length = 0;
			} catch (IOException e) {
				throw e;
			} catch (Exception e) {
				throw new IOException(e);
			}
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
			return length;
		}

		@Override
		public void setLength(long len) throws IOException {
			if (len != length)
				throw new IOException("SET LENGTH: " + len + " " + length);
		}

		@Override
		public void writeByte(byte b) throws IOException {
			if (pos == length)
				bytes.write(b);
			else
				bytes.write((int) pos, b);
			pos++;
			length++;
		}

		@Override
		public void writeBytes(byte[] b, int offset, int len)
				throws IOException {
			if (pos == length)
				bytes.write(b, offset, len);
			else
				bytes.write((int) pos, b, offset, len);
			pos += len;
			length += len;
		}

	}

	private class ByteCacheItem {

		private Long length = null;
		private Long lastModified = null;
		private byte[] bytes = null;

		private ByteCacheItem set(Long length, Long lastModified) {
			if (length != null)
				this.length = length;
			if (lastModified != null)
				this.lastModified = lastModified;
			return this;
		}

		@Override
		final public String toString() {
			return StringUtils.fastConcat(length, ' ', lastModified, ' ',
					bytes != null ? bytes.length : 0);
		}
	}

	private class ByteCache extends LRUCache<String, ByteCacheItem> {

		private ByteCache(int maxSize) {
			super("ObjectStorageCache", maxSize);
		}

		private ByteCacheItem get(String name) {
			ByteCacheItem byteCacheItem = super.getAndPromote(name);
			return byteCacheItem;
		}

		private ByteCacheItem add(String name, ByteCacheItem byteCacheItem) {
			put(name, byteCacheItem);
			return byteCacheItem;
		}

	}
}
