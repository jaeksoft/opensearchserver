/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.request.RequestInterfaces;
import com.jaeksoft.searchlib.request.ReturnField;
import com.jaeksoft.searchlib.request.ReturnFieldList;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.AbstractResult;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.result.ResultDocumentsInterface;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.snippet.SnippetField;
import com.jaeksoft.searchlib.snippet.SnippetFieldList;

public abstract class AbstractRenderDocumentsXml<T1 extends AbstractRequest, T2 extends AbstractResult<T1>>
		extends AbstractRenderXml<T1, T2> {

	private final ResultDocumentsInterface<?> resultDocs;

	protected AbstractRenderDocumentsXml(T2 result) {
		super(result);
		resultDocs = result instanceof ResultDocumentsInterface ? (ResultDocumentsInterface<?>) result
				: null;
	}

	final protected void renderDocuments() throws IOException, ParseException,
			SyntaxError, SearchLibException {
		int start = resultDocs.getRequestStart();
		int end = resultDocs.getDocumentCount() + start;
		writer.print("<result name=\"response\" numFound=\"");
		writer.print(resultDocs.getNumFound());
		writer.print("\" collapsedDocCount=\"");
		writer.print(resultDocs.getCollapsedDocCount());
		writer.print("\" start=\"");
		writer.print(start);
		writer.print("\" rows=\"");
		writer.print(resultDocs.getRequestRows());
		writer.print("\" maxScore=\"");
		writeScore(writer, resultDocs.getMaxScore());
		writer.print("\" time=\"");
		writer.print(result.getTimer().tempDuration());
		writer.println("\">");
		for (int i = start; i < end; i++) {
			ResultDocument doc = resultDocs.getDocument(i, renderingTimer);
			this.renderDocument(i, doc);
		}
		writer.println("</result>");
	}

	final protected void renderDocument(AbstractRequest abstractRequest,
			ResultDocument doc) throws IOException {
		if (doc == null)
			return;
		ReturnFieldList returnFieldList = null;
		SnippetFieldList snippetFieldList = null;
		if (abstractRequest instanceof SearchRequest) {
			returnFieldList = ((SearchRequest) abstractRequest)
					.getReturnFieldList();
			snippetFieldList = ((SearchRequest) abstractRequest)
					.getSnippetFieldList();
		} else if (abstractRequest instanceof RequestInterfaces.ReturnedFieldInterface) {
			returnFieldList = ((RequestInterfaces.ReturnedFieldInterface) abstractRequest)
					.getReturnFieldList();
		}
		if (returnFieldList != null)
			for (ReturnField field : returnFieldList)
				renderField(doc, field);
		if (snippetFieldList != null)
			for (SnippetField field : snippetFieldList)
				renderSnippetValue(doc, field);
	}

	final protected void renderDocumentPrefix(int pos, ResultDocument doc) {
		writer.print("\t<doc score=\"");
		writeScore(writer, resultDocs.getScore(pos));
		writer.print("\" pos=\"");
		writer.print(pos);
		writer.print("\" docId=\"");
		writer.print(doc.getDocId());
		writer.println("\">");
	}

	final protected void renderDocumentSuffix() {
		writer.println("\t</doc>");
	}

	final protected void renderDocumentContent(int pos, ResultDocument doc)
			throws SearchLibException, IOException {
		renderDocument(request, doc);
		int cc = resultDocs.getCollapseCount(pos);
		if (cc > 0) {
			writer.print("\t\t<collapseCount>");
			writer.print(cc);
			writer.println("</collapseCount>");
		}
	}

	protected void renderDocument(int pos, ResultDocument doc)
			throws IOException, ParseException, SyntaxError, SearchLibException {
		renderDocumentPrefix(pos, doc);
		renderDocumentContent(pos, doc);
		renderDocumentSuffix();
	}

	private void renderField(ResultDocument doc, ReturnField field)
			throws IOException {
		String fieldName = field.getName();
		FieldValueItem[] values = doc.getValueArray(field);
		if (values == null)
			return;
		for (FieldValueItem v : values) {
			writer.print("\t\t<field name=\"");
			writer.print(fieldName);
			writer.print('"');
			Float b = v.getBoost();
			if (b != null) {
				writer.print(" boost=\"");
				writer.print(b);
				writer.print('"');
			}
			writer.print('>');
			writer.print(xmlTextRender(v.getValue()));
			writer.println("</field>");
		}
	}

	private void renderSnippetValue(ResultDocument doc, SnippetField field)
			throws IOException {
		String fieldName = field.getName();
		FieldValueItem[] snippets = doc.getSnippetArray(field);
		if (snippets == null)
			return;
		boolean highlighted = doc.isHighlighted(field.getName());
		for (FieldValueItem snippet : snippets) {
			writer.print("\t\t<snippet name=\"");
			writer.print(fieldName);
			writer.print('"');
			if (highlighted)
				writer.print(" highlighted=\"yes\"");
			writer.print('>');
			writer.print(xmlTextRender(snippet.getValue()));
			writer.println("\t\t</snippet>");
		}
	}
}
