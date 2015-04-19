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
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.facet.Facet;
import com.jaeksoft.searchlib.facet.FacetField;
import com.jaeksoft.searchlib.facet.FacetItem;
import com.jaeksoft.searchlib.facet.FacetList;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.result.ResultDocument;

public class RenderSearchXml extends
		AbstractRenderDocumentsXml<AbstractSearchRequest, AbstractResultSearch> {

	public RenderSearchXml(AbstractResultSearch result) {
		super(result);
	}

	private void renderJoinResult(ResultDocument joinResultDocument)
			throws IOException, SearchLibException {
		if (joinResultDocument == null)
			return;
		writer.print("\t\t<join paramPosition=\"");
		writer.print(joinResultDocument.getJoinParameter());
		writer.print("\" score=\"");
		writer.print(joinResultDocument.getScore());
		writer.println("\">");
		renderDocument(joinResultDocument);
		writer.println("\t\t</join>");

	}

	private void renderJoinResults(int pos) throws IOException,
			SearchLibException {
		List<ResultDocument> joinResultDocuments = result.getJoinDocumentList(
				pos, renderingTimer);
		if (joinResultDocuments == null)
			return;
		for (ResultDocument joinDocument : joinResultDocuments)
			renderJoinResult(joinDocument);
	}

	@Override
	protected void renderDocument(int pos, ResultDocument doc)
			throws IOException, ParseException, SyntaxError, SearchLibException {
		renderDocumentPrefix(pos, doc);
		renderDocumentContent(pos, doc);
		renderJoinResults(pos);
		renderDocumentSuffix();
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
	public void render() throws Exception {
		renderPrefix(0, request.getQueryParsed());
		renderDocuments();
		renderFacets();
		renderTimers();
		renderSuffix();
	}

}
