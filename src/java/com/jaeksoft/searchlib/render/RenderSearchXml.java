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
import com.jaeksoft.searchlib.facet.Facet;
import com.jaeksoft.searchlib.facet.FacetField;
import com.jaeksoft.searchlib.facet.FacetItem;
import com.jaeksoft.searchlib.facet.FacetList;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.schema.Field;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.snippet.SnippetField;

public class RenderSearchXml extends
		AbstractRenderXml<SearchRequest, AbstractResultSearch> {

	private PrintWriter writer;

	public RenderSearchXml(AbstractResultSearch result) {
		super(result);
	}

	private void renderPrefix() throws ParseException, SyntaxError,
			SearchLibException, IOException {
		writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		writer.println("<response>");
		writer.println("<header>");
		writer.println("\t<status>0</status>");
		writer.print("\t<query>");
		writer.print(StringEscapeUtils.escapeXml(request.getQueryParsed()));
		writer.println("</query>");
		writer.println("</header>");
	}

	private void renderSuffix() {
		writer.println("</response>");
	}

	private void renderDocuments() throws IOException, ParseException,
			SyntaxError {
		SearchRequest searchRequest = result.getRequest();
		int start = searchRequest.getStart();
		int end = result.getDocumentCount() + searchRequest.getStart();
		writer.print("<result name=\"response\" numFound=\"");
		writer.print(result.getNumFound());
		writer.print("\" collapsedDocCount=\"");
		writer.print(result.getCollapseDocCount());
		writer.print("\" start=\"");
		writer.print(searchRequest.getStart());
		writer.print("\" rows=\"");
		writer.print(searchRequest.getRows());
		writer.print("\" maxScore=\"");
		writer.print(result.getMaxScore());
		writer.print("\" time=\"");
		writer.print(searchRequest.getFinalTime());
		writer.println("\">");
		for (int i = start; i < end; i++)
			this.renderDocument(i);
		writer.println("</result>");
	}

	private void renderDocument(int pos) throws IOException, ParseException,
			SyntaxError {
		writer.print("\t<doc score=\"");
		writer.print(result.getScore(pos));
		writer.print("\" pos=\"");
		writer.print(pos);
		writer.println("\">");
		ResultDocument doc = result.getDocument(pos);
		for (Field field : request.getReturnFieldList())
			renderField(doc, field);
		for (SnippetField field : request.getSnippetFieldList())
			renderSnippetValue(doc, field);

		int cc = result.getCollapseCount(pos);
		if (cc > 0) {
			writer.print("\t\t<collapseCount>");
			writer.print(cc);
			writer.println("</collapseCount>");
		}
		writer.println("\t</doc>");
	}

	private void renderField(ResultDocument doc, Field field)
			throws IOException {
		String fieldName = field.getName();
		List<FieldValueItem> values = doc.getValueList(field);
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

	private void renderFacet(Facet facet) throws Exception {
		FacetField facetField = facet.getFacetField();
		writer.print("\t\t<field name=\"");
		writer.print(facetField.getName());
		writer.println("\">");
		for (FacetItem facetItem : facet) {
			writer.print("\t\t\t<facet name=\"");
			writer.print(StringEscapeUtils.escapeXml(facetItem.getTerm()));
			writer.print("\">");
			writer.print(facetItem.getCount());
			writer.print("</facet>");
		}
		writer.println("\t\t</field>");
	}

	private void renderFacets() throws Exception {
		FacetList facetList = result.getFacetList();
		if (facetList == null)
			return;
		writer.println("<faceting>");
		for (Facet facet : facetList)
			renderFacet(facet);
		writer.println("</faceting>");
	}

	@Override
	public void render(PrintWriter writer) throws Exception {
		this.writer = writer;
		renderPrefix();
		renderDocuments();
		renderFacets();
		renderSuffix();
	}

}
