/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014 Emmanuel Keller / Jaeksoft
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

public abstract class AbstractBaseCollector<C extends CollectorInterface>
		implements CollectorInterface {

	protected C lastCollector;

	@SuppressWarnings("unchecked")
	protected AbstractBaseCollector() {
		setLastCollector((C) this);
	}

	final C setLastCollector(final C collectorInterface) {
		C old = this.lastCollector;
		this.lastCollector = collectorInterface;
		return old;
	}

	@SuppressWarnings("unchecked")
	@Override
	final public <T extends CollectorInterface> T getCollector(
			Class<T> collectorType) {
		CollectorInterface current = lastCollector;
		while (current != null) {
			if (collectorType.isInstance(current))
				return (T) current;
			current = current.getParent();
		}
		return null;
	}

	@Override
	final public C getParent() {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	final public CollectorInterface duplicate() {
		AbstractBaseCollector<C> base = (AbstractBaseCollector<C>) duplicate(this);
		if (lastCollector != this)
			lastCollector.duplicate(base);
		return base;
	}

	@Override
	final public void swap(final int pos1, final int pos2) {
		lastCollector.doSwap(pos1, pos2);
	}
}
