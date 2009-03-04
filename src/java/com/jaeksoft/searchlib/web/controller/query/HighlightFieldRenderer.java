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
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Button;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.api.Listitem;

import com.jaeksoft.searchlib.highlight.HighlightField;

public class HighlightFieldRenderer implements RowRenderer {

	public class FragmenterListener implements EventListener {

		protected HighlightField highlightField;

		protected FragmenterListener(HighlightField highlightField) {
			this.highlightField = highlightField;
		}

		@Override
		public void onEvent(Event event) throws Exception {
			Listbox listbox = (Listbox) event.getTarget();
			Listitem listitem = listbox.getSelectedItem();
			if (listitem != null)
				highlightField.setFragmenter(listitem.getValue().toString());
		}
	}

	public class TagListener extends FragmenterListener {

		protected TagListener(HighlightField highlightField) {
			super(highlightField);
		}

		@Override
		public void onEvent(Event event) throws Exception {
			Textbox textbox = (Textbox) event.getTarget();
			if (textbox != null)
				highlightField.setTag(textbox.getValue());
		}
	}

	public class SeparatorListener extends FragmenterListener {

		protected SeparatorListener(HighlightField highlightField) {
			super(highlightField);
		}

		@Override
		public void onEvent(Event event) throws Exception {
			Textbox textbox = (Textbox) event.getTarget();
			if (textbox != null)
				highlightField.setSeparator(textbox.getValue());
		}
	}

	public class SnippetSizeListener extends FragmenterListener {

		protected SnippetSizeListener(HighlightField highlightField) {
			super(highlightField);
		}

		@Override
		public void onEvent(Event event) throws Exception {
			Intbox intbox = (Intbox) event.getTarget();
			if (intbox != null)
				highlightField.setMaxSnippetSize(intbox.getValue());
		}
	}

	public class SnippetNumberListener extends FragmenterListener {

		protected SnippetNumberListener(HighlightField highlightField) {
			super(highlightField);
		}

		@Override
		public void onEvent(Event event) throws Exception {
			Intbox intbox = (Intbox) event.getTarget();
			if (intbox != null)
				highlightField.setMaxSnippetNumber(intbox.getValue());
		}
	}

	public class DocCharListener extends FragmenterListener {

		protected DocCharListener(HighlightField highlightField) {
			super(highlightField);
		}

		@Override
		public void onEvent(Event event) throws Exception {
			Intbox intbox = (Intbox) event.getTarget();
			if (intbox != null)
				highlightField.setMaxDocChar(intbox.getValue());
		}
	}

	@Override
	public void render(Row row, Object data) throws Exception {
		HighlightField field = (HighlightField) data;
		new Label(field.getName()).setParent(row);

		String fieldFragmenter = field.getFragmenter();
		Listbox listbox = new Listbox();
		listbox.setMold("select");
		int selectedIndex = -1;
		String[] fragmenters = { "NoFragmenter", "SentenceFragmenter",
				"SizeFragmenter" };
		int i = 0;
		for (String fragmenter : fragmenters) {
			listbox.appendItem(fragmenter, fragmenter);
			if (fieldFragmenter.equals(fragmenter))
				selectedIndex = i;
			i++;
		}
		if (selectedIndex >= 0)
			listbox.setSelectedIndex(selectedIndex);
		listbox.addEventListener("onSelect", new FragmenterListener(field));
		listbox.setParent(row);

		Textbox textbox = new Textbox(field.getTag());
		textbox.setCols(5);
		textbox.addEventListener("onChange", new TagListener(field));
		textbox.setParent(row);

		textbox = new Textbox(field.getSeparator());
		textbox.setCols(5);
		textbox.addEventListener("onChange", new SeparatorListener(field));
		textbox.setParent(row);

		Intbox intbox = new Intbox(field.getMaxSnippetSize());
		intbox.setConstraint("no empty, no negative");
		intbox.setCols(5);
		intbox.addEventListener("onChange", new SnippetSizeListener(field));
		intbox.setParent(row);

		intbox = new Intbox(field.getMaxSnippetNumber());
		intbox.setCols(3);
		intbox.setConstraint("no empty, no negative");
		intbox.addEventListener("onChange", new SnippetNumberListener(field));
		intbox.setParent(row);

		intbox = new Intbox(field.getMaxDocChar());
		intbox.setConstraint("no empty, no negative");
		intbox.setCols(10);
		intbox.addEventListener("onChange", new DocCharListener(field));
		intbox.setParent(row);

		Button button = new Button("Remove");
		button.addForward(null, "query", "onHighlightRemove", field);
		button.setParent(row);
	}
}
