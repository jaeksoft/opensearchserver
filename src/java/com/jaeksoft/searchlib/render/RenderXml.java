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

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;

import com.jaeksoft.searchlib.collapse.Collapse;
import com.jaeksoft.searchlib.facet.Facet;
import com.jaeksoft.searchlib.facet.FacetField;
import com.jaeksoft.searchlib.facet.FacetItem;
import com.jaeksoft.searchlib.facet.FacetList;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.highlight.HighlightField;
import com.jaeksoft.searchlib.request.Request;
import com.jaeksoft.searchlib.result.Result;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.schema.Field;
import com.jaeksoft.searchlib.web.ServletTransaction;

public class RenderXml implements Render {

	private PrintWriter writer;
	private Result result;
	private Request request;

	public RenderXml(Result result) {
		this.result = result;
		this.request = result.getRequest();
	}

	private void renderPrefix() throws ParseException, SyntaxError {
		writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		writer.println("<response>");
		writer.println("<header>");
		writer.println("\t<status>0</status>");
		writer.println("\t<query>" + request.getQueryParsed() + "</query>");
		writer.println("</header>");
	}

	private void renderSuffix() {
		writer.println("</response>");
	}

	private void renderDocuments() throws CorruptIndexException, IOException,
			ParseException, SyntaxError {
		Request request = result.getRequest();
		int end = request.getEnd();
		int length = result.getDocs().length;
		if (end > length)
			end = length;
		int cdc = 0;
		Collapse collapse = result.getCollapse();
		if (collapse.isActive())
			cdc = collapse.getDocCount();
		writer.println("<result name=\"response\" numFound=\""
				+ result.getNumFound() + "\" collapsedDocCount=\"" + cdc
				+ "\" start=\"" + request.getStart() + "\" maxScore=\""
				+ result.getMaxScore() + "\" time=\""
				+ result.getTimer().duration() + "\">");
		if (!request.isDelete())
			for (int i = request.getStart(); i < end; i++)
				this.renderDocument(i);
		writer.println("</result>");
	}

	private void renderDocument(int pos) throws CorruptIndexException,
			IOException, ParseException, SyntaxError {
		writer.println("\t<doc score=\"" + result.getDocs()[pos].score
				+ "\" pos=\"" + pos + "\">");
		ResultDocument doc = result.getDocument(pos);
		for (Field field : request.getReturnFieldList())
			renderField(doc, field);
		for (HighlightField field : request.getHighlightFieldList())
			renderHighlightValue(doc, field);

		Collapse collapse = result.getCollapse();
		if (collapse.isActive()) {
			int cc = collapse.getCount(pos);
			if (cc > 0) {
				writer.println("\t\t<collapseCount>" + cc + "</collapseCount>");
			}
		}
		writer.println("\t</doc>");
	}

	private void renderField(ResultDocument doc, Field field)
			throws CorruptIndexException, IOException {
		String fieldName = field.getName();
		String[] values = doc.getValues(field);
		if (values == null)
			return;
		writer.println();
		for (String v : values)
			writer.println("\t\t<field name=\""
					+ fieldName
					+ "\">"
					+ StringEscapeUtils.escapeJava(StringEscapeUtils
							.escapeXml(v)) + "</field>");
	}

	private void renderHighlightValue(ResultDocument doc, HighlightField field)
			throws IOException {
		String fieldName = field.getName();
		String[] snippets = doc.getSnippets(field);
		if (snippets == null)
			return;
		writer.println();
		for (String snippet : snippets)
			writer.println("\t\t<highlight name=\""
					+ fieldName
					+ "\">"
					+ StringEscapeUtils.escapeJava(StringEscapeUtils
							.escapeXml(snippet)) + "</highlight>");
	}

	private void renderFacet(Facet facet) throws Exception {
		FacetField facetField = facet.getFacetField();
		writer.println("\t\t<field name=\"" + facetField.getName() + "\">");
		for (FacetItem facetItem : facet) {
			writer.println("\t\t\t<facet name=\""
					+ StringEscapeUtils.escapeJava(StringEscapeUtils
							.escapeXml(facetItem.getTerm())) + "\">"
					+ facetItem.getCount() + "</facet>");
		}
		writer.println("\t\t</field>");
	}

	private void renderFacets() throws Exception {
		FacetList facetList = result.getFacetList();
		if (facetList == null)
			return;
		if (facetList.size() == 0)
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
