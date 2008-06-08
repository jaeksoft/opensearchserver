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
import java.security.NoSuchAlgorithmException;

import com.jaeksoft.searchlib.schema.Schema;

public abstract class WriterAbstract extends NameFilter implements
		WriterInterface {

	protected WriterAbstract(String indexName) {
		super(indexName);
	}

	protected boolean acceptDocument(IndexDocument document)
			throws NoSuchAlgorithmException {
		return true;
	}

	public void updateDocument(String indexName, Schema schema,
			IndexDocument document, boolean forceLocal)
			throws NoSuchAlgorithmException, IOException {
		if (!acceptName(indexName))
			return;
		updateDocument(schema, document, forceLocal);
	}

}
