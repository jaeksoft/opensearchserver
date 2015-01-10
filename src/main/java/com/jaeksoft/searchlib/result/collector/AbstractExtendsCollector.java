/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014-2015 Emmanuel Keller / Jaeksoft
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

public abstract class AbstractExtendsCollector<C extends CollectorInterface, B extends AbstractBaseCollector<C>>
		implements CollectorInterface {

	final protected B base;
	final protected C parent;

	private final int classType = getClass().hashCode();

	@SuppressWarnings("unchecked")
	protected AbstractExtendsCollector(B base) {
		this.base = (B) base;
		parent = base.setLastCollector((C) this);
	}

	@Override
	final public int getClassType() {
		return classType;
	}

	@Override
	final public <T extends CollectorInterface> T getCollector(
			Class<T> collectorType) {
		return base.getCollector(collectorType);
	}

	@Override
	final public C getParent() {
		return parent;
	}

	@SuppressWarnings("unchecked")
	@Override
	final public CollectorInterface duplicate() {
		B newBase = (B) base.duplicate();
		return newBase.getCollector(this.getClass());
	}

	@Override
	final public void swap(final int pos1, final int pos2) {
		base.lastCollector.doSwap(pos1, pos2);
	}

}
