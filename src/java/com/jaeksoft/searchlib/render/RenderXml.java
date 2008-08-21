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
import java.util.ArrayList;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.lucene.index.CorruptIndexException;

import com.jaeksoft.searchlib.collapse.Collapse;
import com.jaeksoft.searchlib.facet.Facet;
import com.jaeksoft.searchlib.facet.FacetField;
import com.jaeksoft.searchlib.facet.FacetList;
import com.jaeksoft.searchlib.highlight.HighlightField;
import com.jaeksoft.searchlib.request.Request;
import com.jaeksoft.searchlib.result.DocumentRequestItem;
import com.jaeksoft.searchlib.result.Result;
import com.jaeksoft.searchlib.schema.Field;
import com.jaeksoft.searchlib.web.ServletTransaction;

public class RenderXml implements Render {

	private PrintWriter writer;
	private Result<?> result;
	private Request request;

	public RenderXml(Result<?> result) {
		this.result = result;
		this.request = result.getRequest();
	}

	private void renderPrefix() {
		writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		writer.println("<response>");
		writer.println("<header>");
		writer.println("\t<status>0</status>");
		writer.println("\t<query>" + request.getQuery() + "</query>");
		writer.println("</header>");
	}

	private void renderSuffix() {
		writer.println("</response>");
	}

	private void renderDocuments() throws CorruptIndexException, IOException {
		Request request = result.getRequest();
		int end = request.getEnd();
		if (end > result.getDocs().length) {
			end = result.getDocs().length;
		}
		int cdc = 0;
		Collapse<?> collapse = result.getCollapse();
		if (collapse != null) {
			cdc = collapse.getDocCount();
		}
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
			IOException {
		writer.println("\t<doc score=\"" + result.getScore(pos) + "\" pos=\""
				+ pos + "\">");
		DocumentRequestItem doc = result.document(pos);
		for (Field field : this.request.getReturnFieldList())
			renderField(doc, field);
		for (HighlightField field : this.request.getHighlightFieldList())
			renderHighlightValue(doc, field);

		Collapse<?> collapse = result.getCollapse();
		if (collapse != null) {
			int cc = collapse.getCount(pos);
			if (cc > 0) {
				writer.println("\t\t<collapseCount>" + cc + "</collapseCount>");
			}
		}
		writer.println("\t</doc>");
	}

	private void renderField(DocumentRequestItem doc, Field field)
			throws CorruptIndexException, IOException {
		String fieldName = field.getName();
		ArrayList<String> values = doc.getValues(field);
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

	private void renderHighlightValue(DocumentRequestItem doc,
			HighlightField field) throws IOException {
		String fieldName = field.getName();
		ArrayList<String> snippets = doc.getSnippets(field);
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
		int[] count = facet.getCount();
		String[] terms = facet.getTerms();
		if (count == null || terms == null) {
			return;
		}
		if (count.length != terms.length) {
			throw new Exception("BAD CONTENT SIZE " + count.length + "/"
					+ terms.length);
		}
		FacetField facetField = facet.getFacetField();
		writer.println("\t\t<field name=\"" + facetField.getName() + "\">");
		int minCount = facetField.getMinCount();
		for (int i = 0; i < count.length; i++) {
			if (count[i] >= minCount && terms[i] != null) {
				writer.println("\t\t\t<facet name=\""
						+ StringEscapeUtils.escapeJava(StringEscapeUtils
								.escapeXml(terms[i])) + "\">" + count[i]
						+ "</facet>");
			}
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
