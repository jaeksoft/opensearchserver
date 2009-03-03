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

package com.jaeksoft.searchlib.web.controller;

import org.zkoss.zul.Button;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;

import com.jaeksoft.searchlib.facet.FacetField;

public class FacetFieldRenderer implements RowRenderer {

	@Override
	public void render(Row row, Object data) throws Exception {
		FacetField facetField = (FacetField) data;
		new Label(facetField.getName()).setParent(row);
		Listbox listbox = new Listbox();
		listbox.setMold("select");
		listbox.appendItem("no", "no");
		listbox.appendItem("yes", "yes");
		listbox.setSelectedIndex(facetField.isMultivalued() ? 1 : 0);
		listbox.setParent(row);
		Intbox intbox = new Intbox(facetField.getMinCount());
		intbox.setConstraint("no empty, no negative");
		intbox.setParent(row);
		Button button = new Button("Remove");
		button.addForward(null, "query", "onFacetRemove", facetField);
		button.setParent(row);
	}

}
