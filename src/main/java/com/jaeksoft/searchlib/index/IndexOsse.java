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

package com.jaeksoft.searchlib.index;

import java.io.File;
import java.io.IOException;

import com.jaeksoft.searchlib.index.osse.OsseErrorHandler;
import com.jaeksoft.searchlib.index.osse.OsseIndex;

public class IndexOsse extends IndexAbstract {

	private OsseIndex osseIndex = null;
	private OsseErrorHandler ossErrorHandler = null;

	protected IndexOsse(File configDir, IndexConfig indexConfig,
			boolean createIfNotExists) throws IOException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		super(configDir, indexConfig, createIfNotExists);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void initIndexDirectory(File indexDir, boolean bCreate)
			throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void closeIndexDirectory() {
		// TODO Auto-generated method stub

	}

	@Override
	protected ReaderInterface getNewReader(IndexConfig indexConfig)
			throws IOException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected WriterInterface getNewWriter(IndexConfig indexConfig,
			boolean bCreate) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
