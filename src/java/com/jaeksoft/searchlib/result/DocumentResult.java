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

package com.jaeksoft.searchlib.result;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DocumentResult implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4857214643135988896L;

	private ArrayList<DocumentRequestItem> documents;
	private HashMap<String, Integer> docKeys;

	public DocumentResult() {
		documents = new ArrayList<DocumentRequestItem>();
		docKeys = new HashMap<String, Integer>();
	}

	public void add(DocumentRequestItem item) {
		docKeys.put(item.getKey(), documents.size());
		documents.add(item);
	}

	public DocumentRequestItem get(int pos) {
		return documents.get(pos);
	}

	public DocumentRequestItem get(String key) {
		Integer pos = docKeys.get(key);
		if (pos == null)
			return null;
		return documents.get(pos);
	}

	public List<DocumentRequestItem> getList() {
		return documents;
	}
}
