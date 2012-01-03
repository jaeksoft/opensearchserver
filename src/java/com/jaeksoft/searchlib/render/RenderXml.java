/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2011 Emmanuel Keller / Jaeksoft
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.facet.Facet;
import com.jaeksoft.searchlib.facet.FacetField;
import com.jaeksoft.searchlib.facet.FacetItem;
import com.jaeksoft.searchlib.facet.FacetList;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.Result;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.schema.Field;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.snippet.SnippetField;
import com.jaeksoft.searchlib.spellcheck.SpellCheck;
import com.jaeksoft.searchlib.spellcheck.SpellCheckItem;
import com.jaeksoft.searchlib.spellcheck.SpellCheckList;
import com.jaeksoft.searchlib.web.ServletTransaction;

public class RenderXml implements Render {

	private Result result;
	private SearchRequest searchRequest;
	private Matcher controlMatcher;
	private StringBuffer stringBuffer;

	public RenderXml(Result result) {
		this.result = result;
		this.searchRequest = result.getSearchRequest();
		Pattern p = Pattern.compile("\\p{Cntrl}");
		controlMatcher = p.matcher("");
		this.stringBuffer = new StringBuffer();

	}

	private String xmlTextRender(String text) {
		controlMatcher.reset(text);
		return StringEscapeUtils.escapeXml(controlMatcher.replaceAll(""));
	}

	private void renderPrefix() throws ParseException, SyntaxError,
			SearchLibException, IOException {
		stringBuffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		stringBuffer.append("<response>");
		stringBuffer.append("<header>");
		stringBuffer.append("\t<status>0</status>");
		stringBuffer.append("\t<query>");
		stringBuffer.append(StringEscapeUtils.escapeXml(searchRequest
				.getQueryParsed()));
		stringBuffer.append("</query>");
		stringBuffer.append("</header>");
	}

	private void renderSuffix() {
		stringBuffer.append("</response>");
	}

	private void renderDocuments() throws CorruptIndexException, IOException,
			ParseException, SyntaxError {
		SearchRequest searchRequest = result.getSearchRequest();
		int start = searchRequest.getStart();
		int end = result.getDocumentCount() + searchRequest.getStart();
		stringBuffer.append("<result name=\"response\" numFound=\"");
		stringBuffer.append(result.getNumFound());
		stringBuffer.append("\" collapsedDocCount=\"");
		stringBuffer.append(result.getCollapseDocCount());
		stringBuffer.append("\" start=\"");
		stringBuffer.append(searchRequest.getStart());
		stringBuffer.append("\" rows=\"");
		stringBuffer.append(searchRequest.getRows());
		stringBuffer.append("\" maxScore=\"");
		stringBuffer.append(result.getMaxScore());
		stringBuffer.append("\" time=\"");
		stringBuffer.append(searchRequest.getFinalTime());
		stringBuffer.append("\">");
		for (int i = start; i < end; i++)
			this.renderDocument(i);
		stringBuffer.append("</result>");
	}

	private void renderDocument(int pos) throws CorruptIndexException,
			IOException, ParseException, SyntaxError {
		stringBuffer.append("\t<doc score=\"");
		stringBuffer.append(result.getScore(pos));
		stringBuffer.append("\" pos=\"");
		stringBuffer.append(pos);
		stringBuffer.append("\">");
		ResultDocument doc = result.getDocument(pos);
		for (Field field : searchRequest.getReturnFieldList())
			renderField(doc, field);
		for (SnippetField field : searchRequest.getSnippetFieldList())
			renderSnippetValue(doc, field);

		int cc = result.getCollapseCount(pos);
		if (cc > 0) {
			stringBuffer.append("\t\t<collapseCount>");
			stringBuffer.append(cc);
			stringBuffer.append("</collapseCount>");
		}
		stringBuffer.append("\t</doc>");
	}

	private void renderField(ResultDocument doc, Field field)
			throws CorruptIndexException, IOException {
		String fieldName = field.getName();
		List<FieldValueItem> values = doc.getValueList(field);
		if (values == null)
			return;
		for (FieldValueItem v : values) {
			stringBuffer.append("\t\t<field name=\"");
			stringBuffer.append(fieldName);
			stringBuffer.append('"');
			Float b = v.getBoost();
			if (b != null) {
				stringBuffer.append(" boost=\"");
				stringBuffer.append(b);
				stringBuffer.append('"');
			}
			stringBuffer.append('>');
			stringBuffer.append(xmlTextRender(v.getValue()));
			stringBuffer.append("</field>");
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
			stringBuffer.append("\t\t<snippet name=\"");
			stringBuffer.append(fieldName);
			stringBuffer.append('"');
			if (highlighted)
				stringBuffer.append(" highlighted=\"yes\"");
			stringBuffer.append('>');
			stringBuffer.append(xmlTextRender(snippet.getValue()));
			stringBuffer.append("\t\t</snippet>");
		}
	}

	private void renderFacet(Facet facet) throws Exception {
		FacetField facetField = facet.getFacetField();
		stringBuffer.append("\t\t<field name=\"");
		stringBuffer.append(facetField.getName());
		stringBuffer.append("\">");
		for (FacetItem facetItem : facet) {
			stringBuffer.append("\t\t\t<facet name=\"");
			stringBuffer
					.append(StringEscapeUtils.escapeXml(facetItem.getTerm()));
			stringBuffer.append("\">");
			stringBuffer.append(facetItem.getCount());
			stringBuffer.append("</facet>");
		}
		stringBuffer.append("\t\t</field>");
	}

	private void renderFacets() throws Exception {
		FacetList facetList = result.getFacetList();
		if (facetList == null)
			return;
		stringBuffer.append("<faceting>");
		for (Facet facet : facetList)
			renderFacet(facet);
		stringBuffer.append("</faceting>");
	}

	private void renderSpellCheck(SpellCheck spellCheck) throws Exception {
		String fieldName = spellCheck.getFieldName();
		stringBuffer.append("\t\t<field name=\"");
		stringBuffer.append(fieldName);
		stringBuffer.append("\">");
		for (SpellCheckItem spellCheckItem : spellCheck) {
			stringBuffer.append("\t\t\t<word name=\"");
			stringBuffer.append(StringEscapeUtils.escapeXml(spellCheckItem
					.getWord()));
			stringBuffer.append("\">");
			for (String suggest : spellCheckItem.getSuggestions()) {
				stringBuffer.append("\t\t\t\t<suggest>");
				stringBuffer.append(StringEscapeUtils.escapeXml(suggest));
				stringBuffer.append("</suggest>");
			}
			stringBuffer.append("\t\t\t</word>");
		}
		stringBuffer.append("\t\t</field>");
	}

	private void renderSpellChecks() throws Exception {
		SpellCheckList spellChecklist = result.getSpellCheckList();
		if (spellChecklist == null)
			return;
		stringBuffer.append("<spellcheck>");
		for (SpellCheck spellCheck : spellChecklist)
			renderSpellCheck(spellCheck);
		stringBuffer.append("</spellcheck>");
	}

	public void render(PrintWriter writer) throws Exception {
		renderPrefix();
		renderDocuments();
		renderFacets();
		renderSpellChecks();
		renderSuffix();
		writer.println(stringBuffer.toString());
	}

	public StringBuffer getXmlResults() {
		return stringBuffer;
	}

	@Override
	public void render(ServletTransaction servletTransaction) throws Exception {
		servletTransaction.setResponseContentType("text/xml");
		render(servletTransaction.getWriter("UTF-8"));
	}
}
