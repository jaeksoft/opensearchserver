/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.scheduler;

import com.jaeksoft.searchlib.util.ExtensibleEnum;
import com.jaeksoft.searchlib.util.ExtensibleEnumItem;

public class TaskEnumItem extends ExtensibleEnumItem<TaskEnumItem> {

	private TaskAbstract task;

	protected TaskEnumItem(ExtensibleEnum<TaskEnumItem> en,
			Class<? extends TaskAbstract> taskClass) {
		super(en, null);
		try {
			this.task = taskClass.newInstance();
			setName(task.getName());
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 
	 * @return the task abstract
	 */
	public TaskAbstract getTask() {
		return task;
	}

	@Override
	public String toString() {
		return getName();
	}
}
