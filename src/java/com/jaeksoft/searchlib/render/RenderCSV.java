/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C)2011-2012 Emmanuel Keller / Jaeksoft
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

import org.apache.commons.lang.StringEscapeUtils;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.ReturnField;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.snippet.SnippetField;
import com.jaeksoft.searchlib.web.ServletTransaction;

/**
 * @author Naveen
 * 
 */
public class RenderCSV implements Render {

	private PrintWriter writer;
	private AbstractResultSearch result;
	private SearchRequest searchRequest;

	public RenderCSV(AbstractResultSearch result) {
		this.result = result;
		this.searchRequest = result.getRequest();

	}

	private void renderDocuments() throws IOException, ParseException,
			SyntaxError, SearchLibException {
		SearchRequest searchRequest = result.getRequest();
		int start = searchRequest.getStart();
		int end = result.getDocumentCount() + searchRequest.getStart();

		for (int i = start; i < end; i++)
			this.renderDocument(i);
	}

	private void renderDocument(int i) throws IOException, ParseException,
			SyntaxError, SearchLibException {
		ResultDocument doc = result.getDocument(i, null);
		for (ReturnField field : searchRequest.getReturnFieldList()) {
			renderField(doc, field);
			if (field.getName() != null && !field.getName().equals(""))
				writer.print(',');
		}
		for (SnippetField field : searchRequest.getSnippetFieldList()) {
			renderSnippetValue(doc, field);
			String fName = field.getName();
			if (fName != null && fName.length() > 0)
				writer.print(',');
		}

		writer.print('\n');
	}

	private void renderSnippetValue(ResultDocument doc, SnippetField field) {
		FieldValueItem[] snippets = doc.getSnippetArray(field);
		if (snippets == null)
			return;
		for (FieldValueItem snippet : snippets)
			writer.print(StringEscapeUtils.escapeCsv(snippet.getValue()));
	}

	private void renderField(ResultDocument doc, ReturnField field) {
		FieldValueItem[] values = doc.getValueArray(field);
		if (values == null)
			return;
		for (FieldValueItem v : values)
			writer.print(StringEscapeUtils.escapeCsv(v.getValue()));
	}

	public void render(PrintWriter writer) throws Exception {
		this.writer = writer;
		renderDocuments();
	}

	@Override
	public void render(ServletTransaction servletTransaction) throws Exception {
		servletTransaction.setResponseContentType("text/csv; charset-UTF-8");
		render(servletTransaction.getWriter("UTF-8"));
	}
}
