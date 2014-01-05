/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012-2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.result.collector;

abstract class AbstractCollector implements CollectorInterface {

	protected final CollectorInterface parent;
	protected final int maxDoc;

	protected AbstractCollector(final CollectorInterface parent,
			final int maxDoc) {
		this.maxDoc = maxDoc;
		this.parent = parent;
	}

	@Override
	final public int getMaxDoc() {
		return maxDoc;
	}

	@SuppressWarnings("unchecked")
	@Override
	final public <T extends CollectorInterface> T getCollector(
			Class<T> collectorType) {
		if (collectorType.isInstance(this))
			return (T) this;
		if (parent == null)
			return null;
		return parent.getCollector(collectorType);
	}

}
