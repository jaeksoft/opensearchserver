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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.lucene.queryParser.ParseException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.pojojdbc.Transaction;
import com.jaeksoft.pojojdbc.connection.JDBCConnection;
import com.jaeksoft.searchlib.crawler.filter.PatternUrlManager;
import com.jaeksoft.searchlib.crawler.property.PropertyManager;
import com.jaeksoft.searchlib.crawler.robotstxt.RobotsTxtCache;
import com.jaeksoft.searchlib.crawler.spider.ParserSelector;
import com.jaeksoft.searchlib.crawler.urldb.UrlManager;
import com.jaeksoft.searchlib.filter.Filter;
import com.jaeksoft.searchlib.filter.FilterList;
import com.jaeksoft.searchlib.highlight.HighlightField;
import com.jaeksoft.searchlib.index.IndexAbstract;
import com.jaeksoft.searchlib.index.IndexLocal;
import com.jaeksoft.searchlib.render.Render;
import com.jaeksoft.searchlib.render.RenderJsp;
import com.jaeksoft.searchlib.render.RenderXml;
import com.jaeksoft.searchlib.request.Request;
import com.jaeksoft.searchlib.request.RequestList;
import com.jaeksoft.searchlib.result.Result;
import com.jaeksoft.searchlib.schema.FieldList;
import com.jaeksoft.searchlib.schema.FieldValue;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.schema.SortField;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlInfo;

public abstract class Config implements XmlInfo {

	private IndexAbstract index = null;

	private Schema schema = null;

	private RequestList requests = null;

	private PatternUrlManager patternUrlManager = null;

	private RobotsTxtCache robotsTxtCache = null;

	private ParserSelector parserSelector = null;

	private JDBCConnection crawlDatabase = null;

	protected XPathParser xpp = null;

	protected Config(File homeDir, File configFile,
			boolean createIndexIfNotExists)
			throws ParserConfigurationException, SAXException, IOException,
			XPathExpressionException, DOMException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		xpp = new XPathParser(configFile);

		schema = Schema
				.fromXmlConfig(xpp.getNode("/configuration/schema"), xpp);
		requests = RequestList.fromXmlConfig(this, xpp, xpp
				.getNode("/configuration/requests"));

		index = getIndex(homeDir, createIndexIfNotExists);

		// Database info
		Node node = xpp.getNode("/configuration/database");
		if (node != null) {
			String databaseDriver = XPathParser.getAttributeString(node,
					"driver");
			String databaseUrl = XPathParser.getAttributeString(node, "url");
			if (databaseDriver != null && databaseUrl != null) {
				if (homeDir != null)
					databaseUrl = databaseUrl.replace("${root}", homeDir
							.getAbsolutePath());
				crawlDatabase = new JDBCConnection(databaseDriver, databaseUrl);
			}
		}

		if (crawlDatabase != null) {
			patternUrlManager = new PatternUrlManager(this);
			robotsTxtCache = new RobotsTxtCache();
		}

		// Parser info
		node = xpp.getNode("/configuration/parserSelector");
		if (node != null)
			parserSelector = ParserSelector.fromXmlConfig(xpp, node);
	}

	protected IndexAbstract getIndex(File homeDir,
			boolean createIndexIfNotExists) throws XPathExpressionException,
			IOException {
		return new IndexLocal(homeDir, xpp, xpp
				.getNode("/configuration/indices/index"),
				createIndexIfNotExists);

	}

	public Schema getSchema() {
		return this.schema;
	}

	public IndexAbstract getIndex() {
		return this.index;
	}

	public Request getNewRequest(String requestName, HttpServletRequest request)
			throws ParseException {

		Request req = requests.get(requestName).clone();

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
			FieldList<SortField> sortFields = req.getSortFieldList();
			for (String value : values)
				sortFields.add(SortField.newSortField(value));
		}
		return req;
	}

	public Render getRender(HttpServletRequest request, Result<?> result) {

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

	public PatternUrlManager getPatternUrlManager() {
		return patternUrlManager;
	}

	public RobotsTxtCache getRobotsTxtCache() {
		return robotsTxtCache;
	}

	public ParserSelector getParserSelector() {
		return parserSelector;
	}

	private void createCrawlDatabase() throws SQLException {
		Transaction transaction = null;
		try {
			transaction = crawlDatabase.getNewTransaction(false,
					Connection.TRANSACTION_READ_UNCOMMITTED, ";create=true");

			transaction
					.update("CREATE TABLE pattern(pattern VARCHAR(2048), PRIMARY KEY(pattern))");
			transaction
					.update("CREATE TABLE property(name VARCHAR(128), value VARCHAR(2048), PRIMARY KEY(name))");
			transaction
					.update("CREATE TABLE url(url VARCHAR(2048), host VARCHAR(2048), "
							+ "fetchStatus SMALLINT, parserStatus SMALLINT, indexStatus SMALLINT, "
							+ "when TIMESTAMP, retry SMALLINT, indexed TIMESTAMP, "
							+ "PRIMARY KEY (url))");
			transaction.update("CREATE INDEX url_host ON url(host)");
			transaction
					.update("CREATE INDEX url_fetch_status ON url(fetchStatus)");
			transaction
					.update("CREATE INDEX url_parser_status ON url(parserStatus)");
			transaction
					.update("CREATE INDEX url_index_status ON url(indexStatus)");
			transaction.update("CREATE INDEX url_when ON url(when)");
			transaction.update("CREATE INDEX url_retry ON url(retry)");
			transaction.update("CREATE INDEX url_indexed ON url(indexed)");
			transaction.commit();
		} catch (SQLException e) {
			throw e;
		} finally {
			if (transaction != null)
				transaction.close();
		}
	}

	public Transaction getDatabaseTransaction(boolean autoCommit)
			throws SQLException {
		try {
			return crawlDatabase.getNewTransaction(autoCommit,
					Connection.TRANSACTION_READ_UNCOMMITTED);
		} catch (SQLException e) {
			if (e.getMessage().endsWith("not found.")
					&& e.getMessage().startsWith("Database"))
				createCrawlDatabase();
			return crawlDatabase.getNewTransaction(autoCommit,
					Connection.TRANSACTION_READ_UNCOMMITTED);
		}
	}

	private UrlManager urlManager;

	public UrlManager getUrlManager() {
		synchronized (this) {
			if (urlManager != null)
				return urlManager;
			urlManager = new UrlManager(this);
			return urlManager;
		}
	}

	private PropertyManager propertyManager;

	public PropertyManager getPropertyManager() {
		synchronized (this) {
			if (propertyManager != null)
				return propertyManager;
			propertyManager = new PropertyManager(this);
			return propertyManager;
		}

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
