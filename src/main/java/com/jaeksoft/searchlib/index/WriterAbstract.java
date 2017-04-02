/**
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2008-2017 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.index;

import com.jaeksoft.searchlib.util.Md5Spliter;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class WriterAbstract implements WriterInterface {

	final protected IndexConfig indexConfig;
	final private Md5Spliter md5spliter;
	final private String keyField;

	private AtomicBoolean isMergingSource = new AtomicBoolean(false);
	private AtomicBoolean isMergingTarget = new AtomicBoolean(false);

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
		if (keyField == null)
			return true;
		if (md5spliter == null)
			return true;
		FieldContent fieldContent = document.getField(keyField);
		if (fieldContent == null)
			return false;
		return md5spliter.acceptAnyKey(fieldContent.getValues());
	}

	protected void setMergingSource(boolean merging) {
		isMergingSource.set(merging);
	}

	public boolean isMergingSource() {
		return isMergingSource.get();
	}

	protected void setMergingTarget(boolean merging) {
		isMergingTarget.set(merging);
	}

	public boolean isMergingTarget() {
		return isMergingTarget.get();
	}

	@Override
	public boolean isMerging() {
		return isMergingSource.get() || isMergingTarget.get();
	}

}
