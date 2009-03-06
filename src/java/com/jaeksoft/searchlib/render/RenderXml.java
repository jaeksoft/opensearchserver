/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.render;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;

import com.jaeksoft.searchlib.facet.Facet;
import com.jaeksoft.searchlib.facet.FacetField;
import com.jaeksoft.searchlib.facet.FacetItem;
import com.jaeksoft.searchlib.facet.FacetList;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.highlight.HighlightField;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.Result;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.schema.Field;
import com.jaeksoft.searchlib.util.Debug;
import com.jaeksoft.searchlib.web.ServletTransaction;

public class RenderXml implements Render {

	private PrintWriter writer;
	private Result result;
	private SearchRequest searchRequest;

	public RenderXml(Result result) {
		this.result = result;
		this.searchRequest = result.getSearchRequest();
	}

	private void renderPrefix() throws ParseException, SyntaxError {
		writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		writer.println("<response>");
		writer.println("<header>");
		writer.println("\t<status>0</status>");
		writer.print("\t<query>");
		writer.print(searchRequest.getQueryParsed());
		writer.println("</query>");
		Debug debug = result.getDebug();
		if (debug != null)
			debug.xmlInfo(writer);
		writer.println("</header>");
	}

	private void renderSuffix() {
		writer.println("</response>");
	}

	private void renderDocuments() throws CorruptIndexException, IOException,
			ParseException, SyntaxError {
		SearchRequest searchRequest = result.getSearchRequest();
		int start = searchRequest.getStart();
		int end = result.getDocumentCount() + searchRequest.getStart();
		writer.print("<result name=\"response\" numFound=\"");
		writer.print(result.getNumFound());
		writer.print("\" collapsedDocCount=\"");
		writer.print(result.getCollapseDocCount());
		writer.print("\" start=\"");
		writer.print(searchRequest.getStart());
		writer.print("\" maxScore=\"");
		writer.print(result.getMaxScore());
		writer.print("\" time=\"");
		writer.print(searchRequest.getFinalTime());
		writer.println("\">");
		if (!searchRequest.isDelete())
			for (int i = start; i < end; i++)
				this.renderDocument(i);
		writer.println("</result>");
	}

	private void renderDocument(int pos) throws CorruptIndexException,
			IOException, ParseException, SyntaxError {
		writer.print("\t<doc score=\"");
		writer.print(result.getScore(pos));
		writer.print("\" pos=\"");
		writer.print(pos);
		writer.println("\">");
		ResultDocument doc = result.getDocument(pos);
		for (Field field : searchRequest.getReturnFieldList())
			renderField(doc, field);
		for (HighlightField field : searchRequest.getHighlightFieldList())
			renderHighlightValue(doc, field);

		int cc = result.getCollapseCount(pos);
		if (cc > 0) {
			writer.print("\t\t<collapseCount>");
			writer.print(cc);
			writer.println("</collapseCount>");
		}
		writer.println("\t</doc>");
	}

	private void renderField(ResultDocument doc, Field field)
			throws CorruptIndexException, IOException {
		String fieldName = field.getName();
		List<String> values = doc.getValueList(field);
		if (values == null)
			return;
		writer.println();
		for (String v : values) {
			writer.print("\t\t<field name=\"");
			writer.print(fieldName);
			writer.print("\">");
			writer.print(StringEscapeUtils.escapeJava(StringEscapeUtils
					.escapeXml(v)));
			writer.println("</field>");
		}
	}

	private void renderHighlightValue(ResultDocument doc, HighlightField field)
			throws IOException {
		String fieldName = field.getName();
		String[] snippets = doc.getSnippetArray(field);
		if (snippets == null)
			return;
		boolean highlighted = doc.isHighlighted(field.getName());
		writer.println();
		for (String snippet : snippets) {
			writer.print("\t\t<highlight name=\"");
			writer.print(fieldName);
			writer.print('"');
			if (highlighted)
				writer.print(" highlighted=\"yes\"");
			writer.print('>');
			writer.print(StringEscapeUtils.escapeJava(StringEscapeUtils
					.escapeXml(snippet)));
			writer.println("\t\t</highlight>");
		}
	}

	private void renderFacet(Facet facet) throws Exception {
		FacetField facetField = facet.getFacetField();
		writer.print("\t\t<field name=\"");
		writer.print(facetField.getName());
		writer.println("\">");
		for (FacetItem facetItem : facet) {
			writer.print("\t\t\t<facet name=\"");
			writer.print(StringEscapeUtils.escapeJava(StringEscapeUtils
					.escapeXml(facetItem.getTerm())));
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

	public void render(PrintWriter writer) throws Exception {
		this.writer = writer;
		renderPrefix();
		renderDocuments();
		renderFacets();
		renderSuffix();

	}

	public void render(ServletTransaction servletTransaction) throws Exception {
		render(servletTransaction.getWriter("UTF-8"));
	}
}
