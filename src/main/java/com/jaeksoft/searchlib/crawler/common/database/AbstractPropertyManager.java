/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.common.database;

import java.io.File;
import java.io.IOException;

import com.jaeksoft.searchlib.util.properties.PropertyItem;
import com.jaeksoft.searchlib.util.properties.PropertyManager;

public abstract class AbstractPropertyManager extends PropertyManager {

	final protected PropertyItem<Integer> indexDocumentBufferSize;
	final protected PropertyItem<Boolean> crawlEnabled;
	final protected PropertyItem<Integer> maxThreadNumber;
	final private PropertyItem<String> schedulerAfterSession;
	final private PropertyItem<Boolean> propagateDeletion;

	protected AbstractPropertyManager(File file, int defautBufferSize)
			throws IOException {
		super(file);
		indexDocumentBufferSize = newIntegerProperty("indexDocumentBufferSize",
				defautBufferSize, null, null);
		maxThreadNumber = newIntegerProperty("maxThreadNumber", 10, null, null);
		crawlEnabled = newBooleanProperty("crawlEnabled", false);
		schedulerAfterSession = newStringProperty("schedulerAfterSession", "");
		propagateDeletion = newBooleanProperty("propagateDeletion", true);
	}

	public PropertyItem<Boolean> getCrawlEnabled() {
		return crawlEnabled;
	}

	public PropertyItem<Integer> getIndexDocumentBufferSize() {
		return indexDocumentBufferSize;
	}

	public PropertyItem<Integer> getMaxThreadNumber() {
		return maxThreadNumber;
	}

	public PropertyItem<String> getSchedulerAfterSession() {
		return schedulerAfterSession;
	}

	public PropertyItem<Boolean> getPropagateDeletion() {
		return propagateDeletion;
	}

}
