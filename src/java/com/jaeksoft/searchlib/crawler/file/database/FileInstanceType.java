/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2010-2011 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.crawler.file.database;

import com.jaeksoft.searchlib.crawler.file.process.FileInstanceAbstract;
import com.jaeksoft.searchlib.util.ExtensibleEnum;
import com.jaeksoft.searchlib.util.ExtensibleEnumItem;

public class FileInstanceType extends ExtensibleEnumItem<FileInstanceType> {

	private String label;

	private String scheme;

	private Class<? extends FileInstanceAbstract> classInstance;

	public FileInstanceType(ExtensibleEnum<FileInstanceType> enumeration,
			String name, String label, String scheme,
			Class<? extends FileInstanceAbstract> classInstance) {
		super(enumeration, name);
		this.label = label;
		this.scheme = scheme;
		this.classInstance = classInstance;
	}

	/**
	 * 
	 * @return the label of the scheme
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * 
	 * @return the scheme name
	 */
	public String getScheme() {
		return scheme;
	}

	/**
	 * 
	 * @return a new instance of the FileInstanceAbstract
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public FileInstanceAbstract getNewInstance() throws InstantiationException,
			IllegalAccessException {
		return classInstance.newInstance();
	}

}
