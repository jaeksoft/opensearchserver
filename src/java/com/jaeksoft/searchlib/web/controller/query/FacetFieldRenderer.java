/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
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
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.api.Listitem;

import com.jaeksoft.searchlib.facet.FacetField;

public class FacetFieldRenderer implements RowRenderer {

	public class MultivaluedListener implements EventListener {

		protected FacetField facetField;

		public MultivaluedListener(FacetField facetField) {
			this.facetField = facetField;
		}

		public void onEvent(Event event) throws Exception {
			Listbox listbox = (Listbox) event.getTarget();
			Listitem listitem = listbox.getSelectedItem();
			if (listitem != null)
				facetField.setMultivalued(listitem.getValue().toString());
		}

	}

	public class MinCountListener extends MultivaluedListener {

		public MinCountListener(FacetField facetField) {
			super(facetField);
		}

		@Override
		public void onEvent(Event event) throws Exception {
			Intbox intbox = (Intbox) event.getTarget();
			if (intbox != null)
				facetField.setMinCount(intbox.getValue());
		}
	}

	public void render(Row row, Object data) throws Exception {
		FacetField facetField = (FacetField) data;
		new Label(facetField.getName()).setParent(row);
		Listbox listbox = new Listbox();
		listbox.setMold("select");
		listbox.appendItem("no", "no");
		listbox.appendItem("yes", "yes");
		listbox.setSelectedIndex(facetField.isMultivalued() ? 1 : 0);
		listbox.addEventListener("onSelect",
				new MultivaluedListener(facetField));
		listbox.setParent(row);
		Intbox intbox = new Intbox(facetField.getMinCount());
		intbox.setConstraint("no empty, no negative");
		intbox.setParent(row);
		intbox.addEventListener("onChange", new MinCountListener(facetField));
		Button button = new Button("Remove");
		button.addForward(null, "query", "onFacetRemove", facetField);
		button.setParent(row);
	}

}
