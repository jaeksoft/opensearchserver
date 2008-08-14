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

public abstract class PartialDataModel<T> extends DataModel implements Runnable {

	protected PartialList<T> list;
	protected long currentStart;
	protected long currentIndex;
	protected int windowRows;
	protected Object data;

	private Thread thread;

	public PartialDataModel(int windowRows) {
		this.windowRows = windowRows;
		this.currentStart = -1;
		this.list = new PartialList<T>(windowRows);
		this.currentIndex = -1;
		this.thread = null;
	}

	private boolean needUpdate(long index) {
		synchronized (this) {
			return index < currentStart || index >= currentStart + windowRows;
		}
	}

	public Iterator<T> iterator() {
		synchronized (this) {
			return list.iterator();
		}
	}

	protected abstract void update(long start);

	@Override
	public int getRowCount() {
		synchronized (this) {
			return (int) list.getSize();
		}
	}

	@Override
	public Object getRowData() {
		synchronized (this) {
			if (currentIndex == -1 || currentStart == -1)
				return null;
			return list.get(currentIndex - currentStart);
		}
	}

	@Override
	public int getRowIndex() {
		synchronized (this) {
			return (int) currentIndex;
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
			if (needUpdate(currentIndex))
				return false;
			long realIndex = currentIndex - currentStart;
			if (realIndex < 0)
				return false;
			if (realIndex >= list.size)
				return false;
			return true;
		}
	}

	@Override
	public void setRowIndex(int index) {
		synchronized (this) {
			currentIndex = index;
			if (index == -1)
				return;
			if (needUpdate(index))
				populate(index);
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

	public boolean isRunning() {
		synchronized (this) {
			if (thread == null)
				return false;
			return thread.isAlive();
		}
	}

	public void populate(long index) {
		synchronized (this) {
			if (index == currentStart)
				return;
			list.reset();
			currentStart = index;
			thread = new Thread(this);
			thread.start();
		}
	}

	public void run() {
		update(currentStart);
	}

}
