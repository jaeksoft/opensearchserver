/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Button;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;

import com.jaeksoft.searchlib.facet.FacetField;

public class FacetFieldRenderer implements RowRenderer<FacetField> {

	public class MultivaluedListener implements EventListener<Event> {

		protected FacetField facetField;

		public MultivaluedListener(FacetField facetField) {
			this.facetField = facetField;
		}

		@Override
		public void onEvent(Event event) {
			Listbox listbox = (Listbox) event.getTarget();
			Listitem listitem = listbox.getSelectedItem();
			if (listitem != null)
				facetField.setMultivalued(listitem.getValue().toString());
		}

	}

	public class PostCollapsingListener extends MultivaluedListener {

		public PostCollapsingListener(FacetField facetField) {
			super(facetField);
		}

		@Override
		public void onEvent(Event event) {
			Listbox listbox = (Listbox) event.getTarget();
			Listitem listitem = listbox.getSelectedItem();
			if (listitem != null)
				facetField.setPostCollapsing(listitem.getValue().toString());
		}

	}

	public class MinCountListener extends MultivaluedListener {

		public MinCountListener(FacetField facetField) {
			super(facetField);
		}

		@Override
		public void onEvent(Event event) {
			Intbox intbox = (Intbox) event.getTarget();
			if (intbox != null)
				facetField.setMinCount(intbox.getValue());
		}
	}

	@Override
	public void render(Row row, FacetField facetField, int index) {
		new Label(facetField.getName()).setParent(row);
		Listbox listbox = new Listbox();
		listbox.setMold("select");
		listbox.appendItem("no", "no");
		listbox.appendItem("yes", "yes");
		listbox.setSelectedIndex(facetField.isMultivalued() ? 1 : 0);
		listbox.addEventListener("onSelect",
				new MultivaluedListener(facetField));
		listbox.setParent(row);

		listbox = new Listbox();
		listbox.setMold("select");
		listbox.appendItem("no", "no");
		listbox.appendItem("yes", "yes");
		listbox.setSelectedIndex(facetField.isPostCollapsing() ? 1 : 0);
		listbox.addEventListener("onSelect", new PostCollapsingListener(
				facetField));
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
