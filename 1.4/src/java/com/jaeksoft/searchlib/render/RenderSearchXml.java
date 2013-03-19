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

import org.apache.commons.lang.StringEscapeUtils;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.facet.Facet;
import com.jaeksoft.searchlib.facet.FacetField;
import com.jaeksoft.searchlib.facet.FacetItem;
import com.jaeksoft.searchlib.facet.FacetList;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.join.JoinResult;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.result.collector.DocIdInterface;
import com.jaeksoft.searchlib.result.collector.JoinDocInterface;

public class RenderSearchXml extends
		AbstractRenderDocumentsXml<SearchRequest, AbstractResultSearch> {

	public RenderSearchXml(AbstractResultSearch result) {
		super(result);
	}

	private void renderJoinResult(JoinResult joinResult, JoinDocInterface docs,
			int pos) throws IOException, SearchLibException {
		if (joinResult == null)
			return;
		if (!joinResult.isReturnFields())
			return;
		writer.print("\t\t<join paramPosition=\"");
		writer.print(joinResult.getParamPosition());
		writer.println("\">");
		renderDocument(joinResult.getForeignResult().getRequest(),
				joinResult.getDocument(docs, pos, renderingTimer));
		writer.println("\t\t</join>");

	}

	private void renderJoinResults(JoinResult[] joinResults,
			DocIdInterface docs, int pos) throws IOException,
			SearchLibException {
		if (joinResults == null)
			return;
		if (!(docs instanceof JoinDocInterface))
			return;
		for (JoinResult joinResult : joinResults)
			renderJoinResult(joinResult, (JoinDocInterface) docs, pos);
	}

	@Override
	protected void renderDocument(int pos, ResultDocument doc)
			throws IOException, ParseException, SyntaxError, SearchLibException {
		renderDocumentPrefix(pos, doc);
		renderDocumentContent(pos, doc);
		renderJoinResults(result.getJoinResult(), result.getDocs(), pos);
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
