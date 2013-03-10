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

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import com.jaeksoft.searchlib.util.Md5Spliter;
import com.jaeksoft.searchlib.util.ReadWriteLock;

public abstract class WriterAbstract implements WriterInterface {

	final private ReadWriteLock rwl = new ReadWriteLock();

	final protected IndexConfig indexConfig;
	final private Md5Spliter md5spliter;
	private String keyField = null;
	protected List<BeforeUpdateInterface> beforeUpdateList = null;

	private boolean isMergingSource = false;
	private boolean isMergingTarget = false;
	protected boolean isOptimizing = false;

	protected WriterAbstract(IndexConfig indexConfig) {
		this.indexConfig = indexConfig;
		this.keyField = indexConfig.getKeyField();
		if (indexConfig.getKeyMd5RegExp() != null)
			md5spliter = new Md5Spliter(indexConfig.getKeyMd5RegExp());
		else
			md5spliter = null;
	}

	protected boolean acceptDocument(IndexDocument document)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {
		rwl.r.lock();
		try {
			if (keyField == null)
				return true;
			if (md5spliter == null)
				return true;
			FieldContent fieldContent = document.getField(keyField);
			if (fieldContent == null)
				return false;
			return md5spliter.acceptAnyKey(fieldContent.getValues());
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public void addBeforeUpdate(BeforeUpdateInterface beforeUpdate) {
		rwl.w.lock();
		try {
			if (beforeUpdate == null)
				return;
			if (beforeUpdateList == null)
				beforeUpdateList = new ArrayList<BeforeUpdateInterface>();
			beforeUpdateList.add(beforeUpdate);
		} finally {
			rwl.w.unlock();
		}
	}

	protected void setOptimizing(boolean optimizing) {
		rwl.w.lock();
		try {
			this.isOptimizing = optimizing;
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public boolean isOptimizing() {
		rwl.r.lock();
		try {
			return isOptimizing;
		} finally {
			rwl.r.unlock();
		}
	}

	protected void setMergingSource(boolean merging) {
		rwl.w.lock();
		try {
			this.isMergingSource = merging;
		} finally {
			rwl.w.unlock();
		}
	}

	public boolean isMergingSource() {
		rwl.r.lock();
		try {
			return isMergingSource;
		} finally {
			rwl.r.unlock();
		}
	}

	protected void setMergingTarget(boolean merging) {
		rwl.w.lock();
		try {
			this.isMergingTarget = merging;
		} finally {
			rwl.w.unlock();
		}
	}

	public boolean isMergingTarget() {
		rwl.r.lock();
		try {
			return isMergingTarget;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public boolean isMerging() {
		rwl.r.lock();
		try {
			return isMergingSource || isMergingTarget;
		} finally {
			rwl.r.unlock();
		}
	}
}
