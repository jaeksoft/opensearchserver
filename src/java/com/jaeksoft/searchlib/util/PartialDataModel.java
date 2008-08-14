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

package com.jaeksoft.searchlib.util;

import java.util.Iterator;

import javax.faces.model.DataModel;

public abstract class PartialDataModel<T> extends DataModel {

	protected PartialList<T> list;
	protected int currentStart;
	protected int currentIndex;
	protected int windowRows;
	protected Object data;

	public PartialDataModel(int windowRows) {
		this.windowRows = windowRows;
		this.currentStart = 0;
		this.list = new PartialList<T>(windowRows);
		this.currentIndex = -1;
	}

	private boolean needUpdate(int index) {
		return index < currentStart || index >= currentStart + windowRows;
	}

	public Iterator<T> iterator() {
		synchronized (this) {
			return list.iterator();
		}
	}

	protected abstract void update(int start);

	@Override
	public int getRowCount() {
		synchronized (this) {
			return (int) list.getSize();
		}
	}

	@Override
	public Object getRowData() {
		synchronized (this) {
			if (currentIndex == -1)
				return null;
			return list.get(currentIndex - currentStart);
		}
	}

	@Override
	public int getRowIndex() {
		synchronized (this) {
			return currentIndex;
		}
	}

	@Override
	public Object getWrappedData() {
		synchronized (this) {
			return data;
		}
	}

	@Override
	public boolean isRowAvailable() {
		synchronized (this) {
			return !needUpdate(currentIndex);
		}
	}

	@Override
	public void setRowIndex(int index) {
		synchronized (this) {
			currentIndex = index;
			if (index == -1)
				return;
			if (needUpdate(index)) {
				currentStart = index;
				update(index);
			}
		}

	}

	@Override
	public void setWrappedData(Object data) {
		synchronized (this) {
			this.data = data;
		}
	}

	public PartialList<T> getPartialList() {
		synchronized (this) {
			return list;
		}
	}

}
