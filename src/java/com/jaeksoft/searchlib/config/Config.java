/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.lucene.queryParser.ParseException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.basket.BasketCache;
import com.jaeksoft.searchlib.crawler.web.database.PatternManager;
import com.jaeksoft.searchlib.crawler.web.database.PropertyManager;
import com.jaeksoft.searchlib.crawler.web.database.UrlManager;
import com.jaeksoft.searchlib.crawler.web.process.CrawlMaster;
import com.jaeksoft.searchlib.crawler.web.robotstxt.RobotsTxtCache;
import com.jaeksoft.searchlib.facet.FacetField;
import com.jaeksoft.searchlib.filter.Filter;
import com.jaeksoft.searchlib.filter.FilterList;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.IndexAbstract;
import com.jaeksoft.searchlib.index.IndexConfig;
import com.jaeksoft.searchlib.index.IndexGroup;
import com.jaeksoft.searchlib.index.IndexSingle;
import com.jaeksoft.searchlib.parser.ParserSelector;
import com.jaeksoft.searchlib.plugin.IndexPluginTemplateList;
import com.jaeksoft.searchlib.render.Render;
import com.jaeksoft.searchlib.render.RenderJsp;
import com.jaeksoft.searchlib.render.RenderXml;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.request.SearchRequestMap;
import com.jaeksoft.searchlib.result.Result;
import com.jaeksoft.searchlib.schema.Field;
import com.jaeksoft.searchlib.schema.FieldList;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.snippet.SnippetField;
import com.jaeksoft.searchlib.sort.SortList;
import com.jaeksoft.searchlib.statistics.StatisticsList;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlInfo;

public abstract class Config implements XmlInfo {

	private IndexAbstract index = null;

	private Schema schema = null;

	private SearchRequestMap searchRequests = null;

	private ExecutorService threadPool = null;

	private StatisticsList statisticsList = null;

	private BasketCache basketCache = null;

	private ParserSelector parserSelector = null;

	private UrlManager urlManager = null;

	private PatternManager patternManager = null;

	private PropertyManager propertyManager = null;

	private XPathParser xppConfig = null;

	private CrawlMaster webCrawlMaster = null;

	private IndexPluginTemplateList indexPluginTemplateList = null;

	private RobotsTxtCache robotsTxtCache = null;

	private File indexDir;

	private Lock lock;

	protected Config(File initFileOrDir, String configXmlResourceName,
			boolean createIndexIfNotExists) throws SearchLibException {

		lock = new ReentrantLock(true);
		try {

			File configFile;
			if (initFileOrDir.isDirectory()) {
				indexDir = initFileOrDir;
				configFile = new File(initFileOrDir, "config.xml");
			} else {
				indexDir = initFileOrDir.getParentFile();
				configFile = initFileOrDir;
			}
			if (configXmlResourceName == null)
				xppConfig = new XPathParser(configFile);
			else
				xppConfig = new XPathParser(getClass().getResourceAsStream(
						configXmlResourceName));

			index = getIndex(indexDir, xppConfig, createIndexIfNotExists);
			schema = Schema.fromXmlConfig(xppConfig
					.getNode("/configuration/schema"), xppConfig);

		} catch (XPathExpressionException e) {
			throw new SearchLibException(e);
		} catch (DOMException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} catch (ParserConfigurationException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
			throw new SearchLibException(e);
		}
	}

	protected IndexAbstract getIndex(File indexDir, XPathParser xpp,
			boolean createIndexIfNotExists) throws XPathExpressionException,
			IOException, URISyntaxException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		lock.lock();
		try {
			NodeList nodeList = xpp.getNodeList("/configuration/indices/index");
			switch (nodeList.getLength()) {
			case 0:
				return null;
			case 1:
				return new IndexSingle(indexDir, new IndexConfig(xpp, xpp
						.getNode("/configuration/indices/index")),
						createIndexIfNotExists);
			default:
				return new IndexGroup(indexDir, xpp, xpp
						.getNode("/configuration/indices"),
						createIndexIfNotExists, getThreadPool());
			}
		} finally {
			lock.unlock();
		}
	}

	private ExecutorService getThreadPool() {
		lock.lock();
		try {
			if (threadPool == null)
				threadPool = Executors.newCachedThreadPool();
			return threadPool;
		} finally {
			lock.unlock();
		}
	}

	public Schema getSchema() {
		return schema;
	}

	public BasketCache getBasketCache() {
		lock.lock();
		try {
			if (basketCache == null)
				basketCache = new BasketCache(100);
			return basketCache;
		} finally {
			lock.unlock();
		}
	}

	public CrawlMaster getWebCrawlMaster() throws SearchLibException {
		lock.lock();
		try {
			if (webCrawlMaster != null)
				return webCrawlMaster;
			webCrawlMaster = new CrawlMaster(this);
			return webCrawlMaster;
		} finally {
			lock.unlock();
		}

	}

	public ParserSelector getParserSelector() throws SearchLibException {
		lock.lock();
		try {
			if (parserSelector == null) {
				Node node = xppConfig.getNode("/configuration/parsers");
				if (node != null)
					parserSelector = ParserSelector.fromXmlConfig(xppConfig,
							node);
			}
			return parserSelector;
		} catch (XPathExpressionException e) {
			throw new SearchLibException(e);
		} catch (DOMException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			lock.unlock();
		}
	}

	public IndexAbstract getIndex() {
		return this.index;
	}

	public IndexPluginTemplateList getIndexPluginTemplateList()
			throws SearchLibException {
		lock.lock();
		try {
			if (indexPluginTemplateList != null)
				return indexPluginTemplateList;
			Node node = xppConfig.getNode("/configuration/indexPlugins");
			if (node == null)
				return null;
			indexPluginTemplateList = IndexPluginTemplateList.fromXmlConfig(
					xppConfig, node);
			return indexPluginTemplateList;
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (XPathExpressionException e) {
			throw new SearchLibException(e);
		} finally {
			lock.unlock();
		}

	}

	public StatisticsList getStatisticsList() throws SearchLibException {
		try {
			if (statisticsList == null)
				statisticsList = StatisticsList.fromXmlConfig(xppConfig,
						xppConfig.getNode("/configuration/statistics"));
			return statisticsList;
		} catch (XPathExpressionException e) {
			throw new SearchLibException(e);
		} catch (DOMException e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		}
	}

	public SearchRequest getNewSearchRequest() {
		return new SearchRequest(this);
	}

	public SearchRequest getNewSearchRequest(String requestName)
			throws SearchLibException {
		return new SearchRequest(getSearchRequestMap().get(requestName));
	}

	public Map<String, SearchRequest> getSearchRequestMap()
			throws SearchLibException {
		lock.lock();
		try {
			if (searchRequests == null)
				searchRequests = SearchRequestMap
						.fromXmlConfig(this, xppConfig, xppConfig
								.getNode("/configuration/requests"));
			return searchRequests;
		} catch (XPathExpressionException e) {
			throw new SearchLibException(e);
		} catch (DOMException e) {
			throw new SearchLibException(e);
		} catch (ParseException e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
			throw new SearchLibException(e);
		} finally {
			lock.unlock();
		}
	}

	public UrlManager getUrlManager() throws SearchLibException {
		lock.lock();
		try {
			if (urlManager == null)
				urlManager = new UrlManager((Client) this);
			return urlManager;
		} catch (FileNotFoundException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} finally {
			lock.unlock();
		}
	}

	public PatternManager getPatternManager() throws SearchLibException {
		lock.lock();
		try {
			if (patternManager == null)
				patternManager = new PatternManager(indexDir);
			return patternManager;
		} finally {
			lock.unlock();
		}
	}

	public PropertyManager getPropertyManager() throws SearchLibException {
		lock.lock();
		try {
			if (propertyManager == null)
				propertyManager = new PropertyManager(new File(indexDir,
						"crawler-properties.xml"));
			return propertyManager;
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			lock.unlock();
		}
	}

	public SearchRequest getNewSearchRequest(HttpServletRequest httpRequest)
			throws ParseException, SyntaxError, SearchLibException {

		String requestName = httpRequest.getParameter("qt");
		if (requestName == null)
			requestName = "search";
		SearchRequest searchRequest = getNewSearchRequest(requestName);
		if (searchRequest == null)
			searchRequest = getNewSearchRequest();

		String p;

		if ((p = httpRequest.getParameter("index")) != null)
			searchRequest.setIndexName(p);
		if ((p = httpRequest.getParameter("query")) != null)
			searchRequest.setQueryString(p);
		else if ((p = httpRequest.getParameter("q")) != null)
			searchRequest.setQueryString(p);

		if ((p = httpRequest.getParameter("start")) != null)
			searchRequest.setStart(Integer.parseInt(p));

		if ((p = httpRequest.getParameter("rows")) != null)
			searchRequest.setRows(Integer.parseInt(p));

		if ((p = httpRequest.getParameter("lang")) != null)
			searchRequest.setLang(p);

		if ((p = httpRequest.getParameter("collapse.field")) != null) {
			searchRequest.setCollapseField(getSchema().getFieldList().get(p)
					.getName());
			searchRequest.setCollapseActive(true);
		}

		if ((p = httpRequest.getParameter("collapse.max")) != null)
			searchRequest.setCollapseMax(Integer.parseInt(p));

		if ((p = httpRequest.getParameter("delete")) != null)
			searchRequest.setDelete(true);

		if ((p = httpRequest.getParameter("withDocs")) != null)
			searchRequest.setWithDocument(true);

		if ((p = httpRequest.getParameter("noCache")) != null)
			searchRequest.setNoCache(true);

		if ((p = httpRequest.getParameter("debug")) != null)
			searchRequest.setDebug(true);

		String[] values;

		if ((values = httpRequest.getParameterValues("fq")) != null) {
			FilterList fl = searchRequest.getFilterList();
			for (String value : values)
				if (value != null)
					if (value.trim().length() > 0)
						fl.add(value, Filter.Source.REQUEST);
		}

		if ((values = httpRequest.getParameterValues("rf")) != null) {
			FieldList<Field> rf = searchRequest.getReturnFieldList();
			for (String value : values)
				if (value != null)
					if (value.trim().length() > 0)
						rf
								.add(new Field(getSchema().getFieldList().get(
										value)));
		}

		if ((values = httpRequest.getParameterValues("hl")) != null) {
			FieldList<SnippetField> snippetFields = searchRequest
					.getSnippetFieldList();
			for (String value : values)
				snippetFields.add(new SnippetField(getSchema().getFieldList()
						.get(value).getName()));
		}

		if ((values = httpRequest.getParameterValues("fl")) != null) {
			FieldList<Field> returnFields = searchRequest.getReturnFieldList();
			for (String value : values)
				returnFields.add(getSchema().getFieldList().get(value));
		}

		if ((values = httpRequest.getParameterValues("sort")) != null) {
			SortList sortList = searchRequest.getSortList();
			for (String value : values)
				sortList.add(value);
		}

		if ((values = httpRequest.getParameterValues("facet")) != null) {
			FieldList<FacetField> facetList = searchRequest.getFacetFieldList();
			for (String value : values)
				facetList.add(FacetField.buildFacetField(value, false));
		}
		if ((values = httpRequest.getParameterValues("facet.multi")) != null) {
			FieldList<FacetField> facetList = searchRequest.getFacetFieldList();
			for (String value : values)
				facetList.add(FacetField.buildFacetField(value, true));
		}
		return searchRequest;
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

	public RobotsTxtCache getRobotsTxtCache() {
		lock.lock();
		try {
			if (robotsTxtCache != null)
				return robotsTxtCache;
			robotsTxtCache = new RobotsTxtCache();
			return robotsTxtCache;
		} finally {
			lock.unlock();
		}
	}

	public void xmlInfo(PrintWriter writer) {
		writer.println("<configuration>");
		if (index != null)
			index.xmlInfo(writer);
		if (schema != null)
			schema.xmlInfo(writer);
		if (searchRequests != null)
			searchRequests.xmlInfo(writer);
		writer.println("</configuration>");
	}

}
