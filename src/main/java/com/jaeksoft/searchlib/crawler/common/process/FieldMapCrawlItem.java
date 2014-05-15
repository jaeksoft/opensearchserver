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

package com.jaeksoft.searchlib.crawler.common.process;

import com.jaeksoft.searchlib.crawler.FieldMapGeneric;
import com.jaeksoft.searchlib.crawler.common.database.CommonFieldTarget;
import com.jaeksoft.searchlib.process.ThreadItem;
import com.jaeksoft.searchlib.util.map.SourceField;

public abstract class FieldMapCrawlItem<I extends FieldMapCrawlItem<I, T, M>, T extends CrawlThreadAbstract<T, M>, M extends CrawlMasterAbstract<M, T>>
		extends ThreadItem<I, T> {

	private final FieldMapGeneric<SourceField, CommonFieldTarget> fieldMap;

	protected FieldMapCrawlItem(M crawlMaster,
			FieldMapGeneric<SourceField, CommonFieldTarget> fieldMap) {
		super(crawlMaster);
		this.fieldMap = fieldMap;
	}

	@Override
	public void copyTo(I crawlItem) {
		super.copyTo(crawlItem);
		this.fieldMap.copyTo(crawlItem.getFieldMap());
	}

	/**
	 * @return the fieldMap
	 */
	public FieldMapGeneric<SourceField, CommonFieldTarget> getFieldMap() {
		return fieldMap;
	}

}
