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

/**
 * @author naveen
 * 
 */
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

public class RenderOpenSearch implements Render {

	private PrintWriter writer;
	private Result result;
	private SearchRequest searchRequest;
	private Matcher controlMatcher;
	private String serverURL;

	public RenderOpenSearch(Result result, String serverURL) {
		this.result = result;
		this.searchRequest = result.getSearchRequest();
		this.serverURL = serverURL;
		Pattern p = Pattern.compile("\\p{Cntrl}");
		controlMatcher = p.matcher("");
	}

	private String xmlTextRender(String text) {
		controlMatcher.reset(text);
		return StringEscapeUtils.escapeXml(controlMatcher.replaceAll(""));
	}

	private void renderPrefix() throws ParseException, SyntaxError,
			SearchLibException, IOException {
		writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		writer.println("<rss version=\"2.0\" "
				+ " xmlns:OpenSearchServer=\"http://www.open-search-server.com/opensearch/1.0/\" "
				+ " xmlns:opensearch=\"http://a9.com/-/spec/opensearch/1.1/\" "
				+ " xmlns:atom=\"http://www.w3.org/2005/Atom\">");
		writer.println("<channel>");
		writer.println("\t<title>");
		writer.print("OpenSearchServer: ");
		writer.print(StringEscapeUtils.escapeXml(searchRequest.getQueryString()));
		writer.print("\t</title>");
		writer.println("\t<description>");
		writer.print("Search results for ");
		writer.print("\"");
		writer.print(StringEscapeUtils.escapeXml(searchRequest.getQueryString()));
		writer.print("\"");
		writer.print("\t</description>");
		writer.println("\t<link>");
		writer.print(StringEscapeUtils.escapeXml(serverURL));
		writer.print("\t</link>");

	}

	private void renderSuffix() {
		writer.println("</channel>");
		writer.println("</rss>");
	}

	private void renderDocuments() throws CorruptIndexException, IOException,
			ParseException, SyntaxError {
		SearchRequest searchRequest = result.getSearchRequest();
		int start = searchRequest.getStart();
		int end = result.getDocumentCount() + searchRequest.getStart();
		writer.println("\t<opensearch:totalResults>");
		writer.print(result.getNumFound());
		writer.print("\t</opensearch:totalResults>");

		writer.println("\t<opensearch:startIndex>");
		writer.print(searchRequest.getStart());
		writer.print("\t</opensearch:startIndex>");

		writer.println("\t<opensearch:itemsPerPage>");
		writer.print(searchRequest.getRows());
		writer.print("\t</opensearch:itemsPerPage>");

		writer.println("\t<opensearch:Query role=\"request\"");
		writer.print(" searchTerms=\"" + searchRequest.getQueryString());
		writer.print("\"/>");
		for (int i = start; i < end; i++)
			this.renderDocument(i);

	}

	private void renderDocument(int pos) throws CorruptIndexException,
			IOException, ParseException, SyntaxError {

		writer.println("<item>");
		ResultDocument doc = result.getDocument(pos);
		for (SnippetField field : searchRequest.getSnippetFieldList())
			renderSnippetValue(doc, field);

		for (Field field : searchRequest.getReturnFieldList())
			renderField(doc, field);

		int cc = result.getCollapseCount(pos);
		if (cc > 0) {
			writer.print("\t\t<OpenSearchServer:collapseCount>");
			writer.print(cc);
			writer.println("</OpenSearchServer:collapseCount>");
		}
		writer.println("\t</item>");
	}

	private void renderField(ResultDocument doc, Field field)
			throws CorruptIndexException, IOException {
		String fieldName = field.getName();
		List<FieldValueItem> values = doc.getValueList(field);
		for (FieldValueItem v : values) {
			if ("url".equalsIgnoreCase(fieldName)) {

				writer.print("\t<link>");

				writer.print(v.getValue());
				writer.println("</link>");
			} else {
				writer.print("\t\t<OpenSearchServer:");
				writer.print(fieldName);
				writer.print('>');

				writer.print(xmlTextRender(v.getValue()));
				writer.print("</OpenSearchServer:");
				writer.print(fieldName);
				writer.print('>');
			}
		}

	}

	private void renderSnippetValue(ResultDocument doc, SnippetField field)
			throws IOException {
		String fieldName = field.getName();
		FieldValueItem[] snippets = doc.getSnippetArray(field);
		if (snippets == null)
			return;

		for (FieldValueItem snippet : snippets) {
			if ("title".equalsIgnoreCase(fieldName)) {

				writer.print("\t<title>");
				writer.print(StringEscapeUtils.escapeHtml(snippet.getValue()));
				writer.println("</title>");
			} else if ("content".equalsIgnoreCase(fieldName)) {

				writer.print("\t<description>");
				writer.print(StringEscapeUtils.escapeHtml(snippet.getValue()));
				writer.println("</description>");
			} else {
				writer.print("\t\t<OpenSearchServer:");
				writer.print(fieldName);
				writer.print('>');
				writer.print(xmlTextRender(snippet.getValue()));
				writer.print("</OpenSearchServer:");
				writer.print(fieldName);
				writer.print('>');
			}
		}
	}

	private void renderFacet(Facet facet) throws Exception {
		FacetField facetField = facet.getFacetField();
		writer.print("\t\t<OpenSearchServer:");
		writer.print(facetField.getName());
		writer.println(">");
		for (FacetItem facetItem : facet) {
			writer.print("\t\t<OpenSearchServer:");
			writer.print(StringEscapeUtils.escapeXml(facetItem.getTerm()));
			writer.print(">");
			writer.print(facetItem.getCount());
			writer.print("</OpenSearchServer:");
			writer.print(StringEscapeUtils.escapeXml(facetItem.getTerm()));
			writer.print(">");
		}
		writer.print("</OpenSearchServer:");
		writer.print(facetField.getName());
		writer.println(">");
	}

	private void renderFacets() throws Exception {
		FacetList facetList = result.getFacetList();
		if (facetList == null)
			return;
		writer.println("\t\t\t<OpenSearchServer:faceting>");
		for (Facet facet : facetList)
			renderFacet(facet);
		writer.println("</OpenSearchServer:faceting>");
	}

	private void renderSpellCheck(SpellCheck spellCheck) throws Exception {
		String fieldName = spellCheck.getFieldName();
		writer.print("\t\t<OpenSearchServer:");
		writer.print(fieldName);
		writer.println(">");
		for (SpellCheckItem spellCheckItem : spellCheck) {
			writer.print("\t\t\t<OpenSearchServer:");
			writer.print(StringEscapeUtils.escapeXml(spellCheckItem.getWord()));
			writer.println(">");
			for (String suggest : spellCheckItem.getSuggestions()) {
				writer.print("\t\t\t\t<OpenSearchServer:suggest>");
				writer.print(StringEscapeUtils.escapeXml(suggest));
				writer.println("</OpenSearchServer:suggest>");
			}
			writer.print("</OpenSearchServer:");
			writer.print(StringEscapeUtils.escapeXml(spellCheckItem.getWord()));
			writer.println(">");
		}
		writer.print("\t\t</OpenSearchServer:");
		writer.print(fieldName);
		writer.println(">");
	}

	private void renderSpellChecks() throws Exception {
		SpellCheckList spellChecklist = result.getSpellCheckList();
		if (spellChecklist == null)
			return;
		writer.println("\t\t\t<OpenSearchServer:spellcheck>");
		for (SpellCheck spellCheck : spellChecklist)
			renderSpellCheck(spellCheck);
		writer.println("</OpenSearchServer:spellcheck>");
	}

	public void render(PrintWriter writer) throws Exception {
		this.writer = writer;
		renderPrefix();
		renderDocuments();
		renderFacets();
		renderSpellChecks();
		renderSuffix();

	}

	@Override
	public void render(ServletTransaction servletTransaction) throws Exception {
		servletTransaction.setResponseContentType("text/xml");
		render(servletTransaction.getWriter("UTF-8"));
	}
}
