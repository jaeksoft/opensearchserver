/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.web.controller.query;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Button;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Textbox;

import com.jaeksoft.searchlib.filter.Filter;

public class FilterRenderer implements RowRenderer {

	public class FilterListener implements EventListener {

		protected Filter filter;

		public FilterListener(Filter filter) {
			this.filter = filter;
		}

		public void onEvent(Event event) throws Exception {
			Textbox textbox = (Textbox) event.getTarget();
			if (textbox != null)
				filter.setQueryString(textbox.getValue());
		}
	}

	public void render(Row row, Object data) throws Exception {
		Filter filter = (Filter) data;
		Textbox textbox = new Textbox(filter.getQueryString());
		textbox.addEventListener("onChange", new FilterListener(filter));
		textbox.setParent(row);
		Button button = new Button("Remove");
		button.addForward(null, "query", "onFilterRemove", filter);
		button.setParent(row);
	}

}
