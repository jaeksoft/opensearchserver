/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2011 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.controller.query;

import org.zkoss.zk.ui.Component;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.filter.Filter;
import com.jaeksoft.searchlib.filter.Filter.Source;

public class FiltersController extends AbstractQueryController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 989287631079056922L;

	@Override
	protected void reset() throws SearchLibException {
	}

	public FiltersController() throws SearchLibException {
		super();
	}

	public void onFilterAdd() throws SearchLibException {
		getRequest().getFilterList().add("", false, Source.REQUEST);
		reloadPage();
	}

	public void onFilterRemove(Component comp) throws SearchLibException {
		Filter filter = (Filter) getRecursiveComponentAttribute(comp,
				"filterItem");
		getRequest().getFilterList().remove(filter);
		reloadPage();
	}
}
