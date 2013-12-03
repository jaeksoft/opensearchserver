/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2013 Emmanuel Keller / Jaeksoft
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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import com.jaeksoft.searchlib.SearchLibException;

public class IndexLucene extends IndexAbstract {

	private IndexDirectory indexDirectory = null;

	public IndexLucene(File configDir, IndexConfig indexConfig,
			boolean createIfNotExists) throws IOException, URISyntaxException,
			SearchLibException {
		super(configDir, indexConfig, createIfNotExists);

	}

	@Override
	protected void initIndexDirectory(File indexDir, boolean bCreate)
			throws IOException {
		indexDirectory = new IndexDirectory(indexDir);
	}

	@Override
	protected void closeIndexDirectory() {
		if (indexDirectory != null)
			indexDirectory.close();
	}

	@Override
	protected ReaderInterface getNewReader(IndexConfig indexConfig)
			throws IOException, SearchLibException {
		return new ReaderLocal(indexConfig, indexDirectory, true);
	}

	@Override
	protected WriterInterface getNewWriter(IndexConfig indexConfig,
			boolean bCreate) throws IOException, SearchLibException {
		WriterLucene writer = new WriterLucene(indexConfig, this,
				indexDirectory);
		if (bCreate)
			writer.create();
		return writer;
	}

}
