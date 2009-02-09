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

package com.jaeksoft.searchlib.config;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;
import javax.xml.xpath.XPathExpressionException;

import org.apache.lucene.queryParser.ParseException;
import org.w3c.dom.NodeList;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.filter.Filter;
import com.jaeksoft.searchlib.filter.FilterList;
import com.jaeksoft.searchlib.highlight.HighlightField;
import com.jaeksoft.searchlib.index.IndexAbstract;
import com.jaeksoft.searchlib.index.IndexConfig;
import com.jaeksoft.searchlib.index.IndexGroup;
import com.jaeksoft.searchlib.index.IndexSingle;
import com.jaeksoft.searchlib.render.Render;
import com.jaeksoft.searchlib.render.RenderJsp;
import com.jaeksoft.searchlib.render.RenderXml;
import com.jaeksoft.searchlib.request.Request;
import com.jaeksoft.searchlib.request.RequestList;
import com.jaeksoft.searchlib.result.Result;
import com.jaeksoft.searchlib.schema.FieldList;
import com.jaeksoft.searchlib.schema.FieldValue;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.sort.SortList;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlInfo;

public abstract class Config implements XmlInfo {

	private IndexAbstract index = null;

	private Schema schema = null;

	private RequestList requests = null;

	protected XPathParser xpp = null;

	protected Config(File homeDir, File configFile,
			boolean createIndexIfNotExists) throws SearchLibException {

		try {
			xpp = new XPathParser(configFile);

			schema = Schema.fromXmlConfig(xpp.getNode("/configuration/schema"),
					xpp);
			requests = RequestList.fromXmlConfig(this, xpp, xpp
					.getNode("/configuration/requests"));

			index = getIndex(homeDir, createIndexIfNotExists);

		} catch (Exception e) {
			throw new SearchLibException(e);
		}
	}

	protected IndexAbstract getIndex(File homeDir,
			boolean createIndexIfNotExists) throws XPathExpressionException,
			IOException, URISyntaxException {
		NodeList nodeList = xpp.getNodeList("/configuration/indices/index");
		switch (nodeList.getLength()) {
		case 0:
			return null;
		case 1:
			return new IndexSingle(homeDir, new IndexConfig(xpp, xpp
					.getNode("/configuration/indices/index")),
					createIndexIfNotExists);
		default:
			return new IndexGroup(homeDir, xpp, xpp
					.getNode("/configuration/indices"), createIndexIfNotExists);
		}

	}

	public Schema getSchema() {
		return this.schema;
	}

	public IndexAbstract getIndex() {
		return this.index;
	}

	public Request getNewRequest() {
		return new Request(this);
	}

	public Request getNewRequest(String requestName) {
		return requests.get(requestName).clone();
	}

	public Request getNewRequest(HttpServletRequest httpRequest)
			throws ParseException {

		String requestName = httpRequest.getParameter("qt");
		if (requestName == null)
			requestName = "search";
		Request request = getNewRequest(requestName);

		if (request == null)
			return null;

		String p;

		if ((p = httpRequest.getParameter("query")) != null)
			request.setQueryString(p);
		else if ((p = httpRequest.getParameter("q")) != null)
			request.setQueryString(p);

		if ((p = httpRequest.getParameter("start")) != null)
			request.setStart(Integer.parseInt(p));

		if ((p = httpRequest.getParameter("rows")) != null)
			request.setRows(Integer.parseInt(p));

		if ((p = httpRequest.getParameter("lang")) != null)
			request.setLang(p);

		if ((p = httpRequest.getParameter("collapse.field")) != null) {
			request.setCollapseField(getSchema().getFieldList().get(p));
			request.setCollapseActive(true);
		}

		if ((p = httpRequest.getParameter("collapse.max")) != null)
			request.setCollapseMax(Integer.parseInt(p));

		if ((p = httpRequest.getParameter("delete")) != null)
			request.setDelete(true);

		if ((p = httpRequest.getParameter("withDocs")) != null)
			request.setWithDocument(true);

		String[] values;

		if ((values = httpRequest.getParameterValues("fq")) != null) {
			FilterList fl = request.getFilterList();
			for (String value : values)
				if (value != null)
					if (value.trim().length() > 0)
						fl.add(value, Filter.Source.REQUEST);
		}

		if ((values = httpRequest.getParameterValues("hl")) != null) {
			FieldList<HighlightField> highlightFields = request
					.getHighlightFieldList();
			for (String value : values)
				highlightFields.add(new HighlightField(getSchema()
						.getFieldList().get(value)));
		}

		if ((values = httpRequest.getParameterValues("fl")) != null) {
			FieldList<FieldValue> returnFields = request.getReturnFieldList();
			for (String value : values)
				returnFields.add(getSchema().getFieldList().get(value));
		}
		if ((values = httpRequest.getParameterValues("sort")) != null) {
			SortList sortList = request.getSortList();
			for (String value : values)
				sortList.add(value);
		}
		return request;
	}

	public Render getRender(HttpServletRequest request, Result result) {

		Render render = null;

		String p;
		if ((p = request.getParameter("render")) != null) {
			if ("jsp".equals(p))
				render = new RenderJsp(request.getParameter("jsp"), result);
		}

		if (render == null)
			render = new RenderXml(result);

		return render;
	}

	public void xmlInfo(PrintWriter writer, HashSet<String> classDetail) {
		writer.println("<configuration>");
		if (index != null)
			index.xmlInfo(writer, classDetail);
		if (schema != null)
			schema.xmlInfo(writer, classDetail);
		if (requests != null)
			requests.xmlInfo(writer, classDetail);
		writer.println("</configuration>");
	}

}
