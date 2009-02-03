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
			IOException {
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

	public Request getNewRequest(String requestName, HttpServletRequest request)
			throws ParseException {

		Request req = getNewRequest(requestName);

		if (request == null)
			return req;

		String p;

		if ((p = request.getParameter("query")) != null)
			req.setQueryString(p);
		else if ((p = request.getParameter("q")) != null)
			req.setQueryString(p);

		if ((p = request.getParameter("start")) != null)
			req.setStart(Integer.parseInt(p));

		if ((p = request.getParameter("rows")) != null)
			req.setRows(Integer.parseInt(p));

		if ((p = request.getParameter("lang")) != null)
			req.setLang(p);

		if ((p = request.getParameter("collapse.field")) != null) {
			req.setCollapseField(getSchema().getFieldList().get(p));
			req.setCollapseActive(true);
		}

		if ((p = request.getParameter("collapse.max")) != null)
			req.setCollapseMax(Integer.parseInt(p));

		if ((p = request.getParameter("forceLocal")) != null)
			req.setForceLocal(true);

		if ((p = request.getParameter("delete")) != null)
			req.setDelete(true);

		if ((p = request.getParameter("withDocs")) != null)
			req.setWithDocument(true);

		String[] values;

		if ((values = request.getParameterValues("fq")) != null) {
			FilterList fl = req.getFilterList();
			for (String value : values)
				if (value != null)
					if (value.trim().length() > 0)
						fl.add(value, Filter.Source.REQUEST);
		}

		if ((values = request.getParameterValues("hl")) != null) {
			FieldList<HighlightField> highlightFields = req
					.getHighlightFieldList();
			for (String value : values)
				highlightFields.add(new HighlightField(getSchema()
						.getFieldList().get(value)));
		}

		if ((values = request.getParameterValues("fl")) != null) {
			FieldList<FieldValue> returnFields = req.getReturnFieldList();
			for (String value : values)
				returnFields.add(getSchema().getFieldList().get(value));
		}
		if ((values = request.getParameterValues("sort")) != null) {
			SortList sortList = req.getSortList();
			for (String value : values)
				sortList.add(value);
		}
		return req;
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
