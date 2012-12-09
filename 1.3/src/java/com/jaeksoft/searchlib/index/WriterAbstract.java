/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import com.jaeksoft.searchlib.util.Md5Spliter;

public abstract class WriterAbstract implements WriterInterface {

	final protected IndexConfig indexConfig;
	final private Md5Spliter md5spliter;
	private String keyField = null;
	protected List<BeforeUpdateInterface> beforeUpdateList = null;
	protected boolean optimizing;

	protected WriterAbstract(IndexConfig indexConfig) {
		this.indexConfig = indexConfig;
		optimizing = false;
		this.keyField = indexConfig.getKeyField();
		if (indexConfig.getKeyMd5RegExp() != null)
			md5spliter = new Md5Spliter(indexConfig.getKeyMd5RegExp());
		else
			md5spliter = null;
	}

	protected boolean acceptDocument(IndexDocument document)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {
		if (keyField == null)
			return true;
		if (md5spliter == null)
			return true;
		FieldContent fieldContent = document.getField(keyField);
		if (fieldContent == null)
			return false;
		return md5spliter.acceptAnyKey(fieldContent.getValues());
	}

	@Override
	public void addBeforeUpdate(BeforeUpdateInterface beforeUpdate) {
		if (beforeUpdate == null)
			return;
		if (beforeUpdateList == null)
			beforeUpdateList = new ArrayList<BeforeUpdateInterface>();
		beforeUpdateList.add(beforeUpdate);
	}

	@Override
	public boolean isOptimizing() {
		return optimizing;
	}

}
