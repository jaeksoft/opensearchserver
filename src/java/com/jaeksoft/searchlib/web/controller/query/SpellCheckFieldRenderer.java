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
import org.zkoss.zul.Doublebox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;

import com.jaeksoft.searchlib.spellcheck.SpellCheckField;

public class SpellCheckFieldRenderer implements RowRenderer {

	public class MinScoreListener implements EventListener {

		protected SpellCheckField spellCheckField;

		public MinScoreListener(SpellCheckField spellCheckField) {
			this.spellCheckField = spellCheckField;
		}

		@Override
		public void onEvent(Event event) throws Exception {
			Doublebox doubleBox = (Doublebox) event.getTarget();
			if (doubleBox != null)
				spellCheckField.setMinScore(doubleBox.getValue().floatValue());
		}

	}

	public class SuggestionNumberListener extends MinScoreListener {

		public SuggestionNumberListener(SpellCheckField spellCheckField) {
			super(spellCheckField);
		}

		@Override
		public void onEvent(Event event) throws Exception {
			Intbox intBox = (Intbox) event.getTarget();
			if (intBox != null)
				spellCheckField.setSuggestionNumber(intBox.getValue()
						.intValue());
		}
	}

	public void render(Row row, Object data) throws Exception {
		SpellCheckField spellCheckField = (SpellCheckField) data;
		new Label(spellCheckField.getName()).setParent(row);
		Doublebox doublebox = new Doublebox(spellCheckField.getMinScore());
		doublebox.setConstraint("no empty, no negative");
		doublebox.setParent(row);
		doublebox.addEventListener("onChange", new MinScoreListener(
				spellCheckField));
		Intbox intbox = new Intbox(spellCheckField.getSuggestionNumber());
		intbox.setConstraint("no empty, no negative");
		intbox.setParent(row);
		intbox.addEventListener("onChange", new SuggestionNumberListener(
				spellCheckField));
		Button button = new Button("Remove");
		button.addForward(null, "query", "onFieldRemove", spellCheckField);
		button.setParent(row);
	}
}
