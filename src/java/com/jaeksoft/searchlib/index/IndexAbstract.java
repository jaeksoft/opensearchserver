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

package com.jaeksoft.searchlib.index;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.jaeksoft.searchlib.util.XmlInfo;

public abstract class IndexAbstract extends NameFilter implements
		ReaderInterface, WriterInterface, XmlInfo {

	protected final ReadWriteLock rwl = new ReentrantReadWriteLock();
	protected final Lock r = rwl.readLock();
	protected final Lock w = rwl.writeLock();

	private IndexConfig indexConfig;

	protected IndexAbstract() {
		super(null);
	}

	protected IndexAbstract(IndexConfig indexConfig) {
		super(indexConfig.getName());
		this.indexConfig = indexConfig;
	}

	public abstract IndexAbstract get(String name);

	public IndexConfig getIndexConfig() {
		return indexConfig;
	}

	public abstract boolean isOnline(String indexName);

	public abstract void setOnline(String indexName, boolean v);

	public abstract boolean isReadOnly(String indexName);

	public abstract void setReadOnly(String indexName, boolean v);

	public abstract void receive(String indexName, long version,
			String fileName, InputStream inputStream) throws IOException;

	public abstract void push(String indexName, URI dest)
			throws URISyntaxException, IOException;

	public abstract long getVersion(String indexName);

	public abstract void reload(String indexName) throws IOException,
			URISyntaxException;

	public abstract void swap(String indexName, long version, boolean deleteOld)
			throws IOException, URISyntaxException;

}
