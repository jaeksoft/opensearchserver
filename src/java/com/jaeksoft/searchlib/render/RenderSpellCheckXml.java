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

package com.jaeksoft.searchlib.render;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.SpellCheckRequest;
import com.jaeksoft.searchlib.result.ResultSpellCheck;
import com.jaeksoft.searchlib.spellcheck.SpellCheck;
import com.jaeksoft.searchlib.spellcheck.SpellCheckItem;
import com.jaeksoft.searchlib.spellcheck.SuggestionItem;

public class RenderSpellCheckXml extends
		AbstractRenderXml<SpellCheckRequest, ResultSpellCheck> {

	private PrintWriter writer;

	public RenderSpellCheckXml(ResultSpellCheck result) {
		super(result);
	}

	private void renderSpellCheck(SpellCheck spellCheck) throws Exception {
		String fieldName = spellCheck.getFieldName();
		writer.print("\t\t<field name=\"");
		writer.print(fieldName);
		writer.println("\">");
		for (SpellCheckItem spellCheckItem : spellCheck) {
			writer.print("\t\t\t<word name=\"");
			writer.print(StringEscapeUtils.escapeXml(spellCheckItem.getWord()));
			writer.println("\">");
			for (SuggestionItem suggestionItem : spellCheckItem
					.getSuggestions()) {
				writer.print("\t\t\t\t<suggest freq=\"");
				writer.print(suggestionItem.getFreq());
				writer.print("\">");
				writer.print(StringEscapeUtils.escapeXml(suggestionItem
						.getTerm()));
				writer.println("</suggest>");
			}
			writer.println("\t\t\t</word>");
		}
		writer.println("\t\t</field>");
	}

	private void renderSpellChecks() throws Exception {
		List<SpellCheck> spellChecklist = result.getSpellCheckList();
		if (spellChecklist == null)
			return;
		writer.println("<spellcheck>");
		for (SpellCheck spellCheck : spellChecklist)
			renderSpellCheck(spellCheck);
		writer.println("</spellcheck>");
	}

	private void renderPrefix() throws ParseException, SyntaxError,
			SearchLibException, IOException {
		writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		writer.println("<response>");
		writer.println("<header>");
		writer.println("\t<status>0</status>");
		writer.print("\t<query>");
		writer.print(StringEscapeUtils.escapeXml(request.getQueryString()));
		writer.println("</query>");
		writer.println("</header>");
	}

	private void renderSuffix() {
		writer.println("</response>");
	}

	@Override
	public void render(PrintWriter writer) throws Exception {
		this.writer = writer;
		renderPrefix();
		renderSpellChecks();
		renderSuffix();
	}

}
