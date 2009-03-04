/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.controller.query;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.RowRenderer;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.filter.Filter;
import com.jaeksoft.searchlib.filter.Filter.Source;

public class FiltersController extends QueryController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 989287631079056922L;

	public FiltersController() throws SearchLibException {
		super();
	}

	public RowRenderer getFilterRenderer() {
		return new FilterRenderer();
	}

	public void onFilterAdd() throws SearchLibException {
		getRequest().getFilterList().add("", Source.REQUEST);
		reloadPage();
	}

	public void onFilterRemove(Event event) throws SearchLibException {
		Filter filter = (Filter) event.getData();
		getRequest().getFilterList().remove(filter);
		reloadPage();
	}

}
