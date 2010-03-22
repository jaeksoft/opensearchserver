/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2010 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.lucene.queryParser.ParseException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.collapse.CollapseMode;
import com.jaeksoft.searchlib.crawler.FieldMap;
import com.jaeksoft.searchlib.crawler.file.database.FileManager;
import com.jaeksoft.searchlib.crawler.file.database.FilePathManager;
import com.jaeksoft.searchlib.crawler.file.database.FilePropertyManager;
import com.jaeksoft.searchlib.crawler.file.process.CrawlFileMaster;
import com.jaeksoft.searchlib.crawler.web.database.PatternManager;
import com.jaeksoft.searchlib.crawler.web.database.UrlManager;
import com.jaeksoft.searchlib.crawler.web.database.WebPropertyManager;
import com.jaeksoft.searchlib.crawler.web.process.WebCrawlMaster;
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
import com.jaeksoft.searchlib.util.ConfigFileRotation;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public abstract class Config {

	private IndexAbstract index = null;

	private Schema schema = null;

	private SearchRequestMap searchRequests = null;

	private ExecutorService threadPool = null;

	private StatisticsList statisticsList = null;

	private ParserSelector parserSelector = null;

	private UrlManager urlManager = null;

	private PatternManager patternManager = null;

	private FilePathManager filePatternManager = null;

	private FileManager fileManager = null;

	private WebPropertyManager webPropertyManager = null;

	private FilePropertyManager filePropertyManager = null;

	private XPathParser xppConfig = null;

	private WebCrawlMaster webCrawlMaster = null;

	private CrawlFileMaster fileCrawlMaster = null;

	private FieldMap webCrawlerFieldMap = null;

	private FieldMap fileCrawlerFieldMap = null;

	private IndexPluginTemplateList indexPluginTemplateList = null;

	private RobotsTxtCache robotsTxtCache = null;

	private File indexDir;

	private final Lock lock = new ReentrantLock(true);

	private Mailer mailer = null;

	protected Config(File indexDirectory, String configXmlResourceName,
			boolean createIndexIfNotExists) throws SearchLibException {

		try {
			indexDir = indexDirectory;
			if (!indexDir.isDirectory())
				throw new SearchLibException("Expected to get a directory path");

			if (configXmlResourceName == null)
				xppConfig = new XPathParser(new File(indexDirectory,
						"config.xml"));
			else {
				xppConfig = new XPathParser(getClass().getResourceAsStream(
						configXmlResourceName));

			}

			index = getIndex(indexDir, xppConfig, createIndexIfNotExists);
			schema = Schema.fromXmlConfig(xppConfig
					.getNode("/configuration/schema"), xppConfig);

			getFileCrawlMaster();
			getWebCrawlMaster();

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

	public File getIndexDirectory() {
		return indexDir;
	}

	private void saveConfigWithoutLock() throws IOException,
			TransformerConfigurationException, SAXException,
			SearchLibException, XPathExpressionException {
		ConfigFileRotation cfr = new ConfigFileRotation(indexDir, "config.xml");
		if (!cfr.getTempFile().exists())
			cfr.getTempFile().createNewFile();
		PrintWriter pw = new PrintWriter(cfr.getTempFile());
		try {
			XmlWriter xmlWriter = new XmlWriter(pw, "UTF-8");
			xmlWriter.startElement("configuration");
			getIndex().writeXmlConfig(xmlWriter);
			getSchema().writeXmlConfig(xmlWriter);
			IndexPluginTemplateList iptl = getIndexPluginTemplateList();
			if (iptl != null)
				iptl.writeXmlConfig(xmlWriter);
			getMailer();
			if (mailer != null)
				mailer.writeXmlConfig(xmlWriter);
			xmlWriter.endElement();
			xmlWriter.endDocument();
			pw.close();
			pw = null;
			cfr.rotate();
		} finally {
			if (pw != null)
				pw.close();
		}
	}

	public void saveParsers() throws IOException,
			TransformerConfigurationException, SAXException, SearchLibException {
		ConfigFileRotation cfr = new ConfigFileRotation(indexDir, "parsers.xml");
		PrintWriter pw = cfr.getTempPrintWriter();
		try {
			XmlWriter xmlWriter = new XmlWriter(pw, "UTF-8");
			getParserSelector().writeXmlConfig(xmlWriter);
			xmlWriter.endDocument();
			cfr.rotate();
		} finally {
			pw.close();
		}
	}

	public void saveRequests() throws IOException,
			TransformerConfigurationException, SAXException, SearchLibException {
		ConfigFileRotation cfr = new ConfigFileRotation(indexDir,
				"requests.xml");
		PrintWriter pw = cfr.getTempPrintWriter();
		try {
			XmlWriter xmlWriter = new XmlWriter(pw, "UTF-8");
			getSearchRequestMap().writeXmlConfig(xmlWriter);
			xmlWriter.endDocument();
			cfr.rotate();
		} finally {
			pw.close();
		}
	}

	public void saveConfig() throws SearchLibException {
		lock.lock();
		try {
			saveConfigWithoutLock();
		} catch (TransformerConfigurationException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} catch (XPathExpressionException e) {
			throw new SearchLibException(e);
		} finally {
			lock.unlock();
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

	public WebCrawlMaster getWebCrawlMaster() throws SearchLibException {
		lock.lock();
		try {
			if (webCrawlMaster != null)
				return webCrawlMaster;
			webCrawlMaster = new WebCrawlMaster(this);
			return webCrawlMaster;
		} finally {
			lock.unlock();
		}

	}

	public CrawlFileMaster getFileCrawlMaster() throws SearchLibException {
		lock.lock();
		try {
			if (fileCrawlMaster != null)
				return fileCrawlMaster;
			fileCrawlMaster = new CrawlFileMaster(this);
			return fileCrawlMaster;
		} finally {
			lock.unlock();
		}

	}

	public ParserSelector getParserSelector() throws SearchLibException {
		lock.lock();
		try {
			if (parserSelector == null) {
				File parserFile = new File(indexDir, "parsers.xml");
				if (parserFile.exists()) {
					XPathParser xpp = new XPathParser(parserFile);
					parserSelector = ParserSelector.fromXmlConfig(xpp, xpp
							.getNode("/parsers"));
				} else {
					Node node = xppConfig.getNode("/configuration/parsers");
					if (node != null)
						parserSelector = ParserSelector.fromXmlConfig(
								xppConfig, node);
				}
			}
			return parserSelector;
		} catch (XPathExpressionException e) {
			throw new SearchLibException(e);
		} catch (DOMException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (ParserConfigurationException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} finally {
			lock.unlock();
		}
	}

	public Mailer getMailer() throws XPathExpressionException {
		lock.lock();
		try {
			if (mailer != null)
				return mailer;
			mailer = Mailer.fromXmlConfig(xppConfig
					.getNode("/configuration/mailer"));
			return mailer;
		} finally {
			lock.unlock();
		}

	}

	protected String getIndexName() {
		return getIndexDirectory().getName();
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

	protected File getStatStorage() {
		return new File(getIndexDirectory(), "statstore");
	}

	public StatisticsList getStatisticsList() throws SearchLibException {
		try {
			if (statisticsList == null)
				statisticsList = StatisticsList.fromXmlConfig(xppConfig,
						xppConfig.getNode("/configuration/statistics"),
						getStatStorage());
			return statisticsList;
		} catch (XPathExpressionException e) {
			e.printStackTrace();
			throw new SearchLibException(e);
		} catch (DOMException e) {
			e.printStackTrace();
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			e.printStackTrace();
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new SearchLibException(e);
		} catch (IOException e) {
			e.printStackTrace();
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

	public SearchRequestMap getSearchRequestMap() throws SearchLibException {
		lock.lock();
		try {
			if (searchRequests == null) {
				File requestFile = new File(indexDir, "requests.xml");
				if (requestFile.exists()) {
					XPathParser xpp = new XPathParser(requestFile);
					searchRequests = SearchRequestMap.fromXmlConfig(this, xpp,
							xpp.getNode("/requests"));
				} else
					searchRequests = SearchRequestMap.fromXmlConfig(this,
							xppConfig, xppConfig
									.getNode("/configuration/requests"));
			}
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
		} catch (ParserConfigurationException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			lock.unlock();
		}
	}

	public UrlManager getUrlManager() throws SearchLibException {
		lock.lock();
		try {
			if (urlManager == null)
				urlManager = new UrlManager((Client) this, indexDir);
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

	public FilePathManager getFilePathManager() throws SearchLibException {
		lock.lock();
		try {
			if (filePatternManager == null)
				filePatternManager = new FilePathManager(indexDir);
			return filePatternManager;
		} finally {
			lock.unlock();
		}
	}

	public FileManager getFileManager() throws SearchLibException {
		lock.lock();
		try {
			if (fileManager == null)
				fileManager = new FileManager((Client) this, indexDir);
			return fileManager;
		} catch (FileNotFoundException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} finally {
			lock.unlock();
		}
	}

	public WebPropertyManager getWebPropertyManager() throws SearchLibException {
		lock.lock();
		try {
			if (webPropertyManager == null)
				webPropertyManager = new WebPropertyManager(new File(indexDir,
						"webcrawler-properties.xml"));
			return webPropertyManager;
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			lock.unlock();
		}
	}

	public FilePropertyManager getFilePropertyManager()
			throws SearchLibException {
		lock.lock();
		try {
			if (filePropertyManager == null)
				filePropertyManager = new FilePropertyManager(new File(
						indexDir, "filecrawler-properties.xml"));
			return filePropertyManager;
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
			searchRequest.setLang(LanguageEnum.findByCode(p));

		if ((p = httpRequest.getParameter("collapse.mode")) != null)
			searchRequest.setCollapseMode(CollapseMode.valueOfLabel(p));

		if ((p = httpRequest.getParameter("collapse.field")) != null)
			searchRequest.setCollapseField(getSchema().getFieldList().get(p)
					.getName());

		if ((p = httpRequest.getParameter("collapse.max")) != null)
			searchRequest.setCollapseMax(Integer.parseInt(p));

		if ((p = httpRequest.getParameter("delete")) != null)
			searchRequest.setDelete(true);

		if ((p = httpRequest.getParameter("withDocs")) != null)
			searchRequest.setWithDocument(true);

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
				facetList.add(FacetField.buildFacetField(value, false, false));
		}
		if ((values = httpRequest.getParameterValues("facet.collapse")) != null) {
			FieldList<FacetField> facetList = searchRequest.getFacetFieldList();
			for (String value : values)
				facetList.add(FacetField.buildFacetField(value, false, true));
		}
		if ((values = httpRequest.getParameterValues("facet.multi")) != null) {
			FieldList<FacetField> facetList = searchRequest.getFacetFieldList();
			for (String value : values)
				facetList.add(FacetField.buildFacetField(value, true, false));
		}
		if ((values = httpRequest.getParameterValues("facet.multi.collapse")) != null) {
			FieldList<FacetField> facetList = searchRequest.getFacetFieldList();
			for (String value : values)
				facetList.add(FacetField.buildFacetField(value, true, true));
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

	public FieldMap getWebCrawlerFieldMap() throws SearchLibException {
		lock.lock();
		try {
			if (webCrawlerFieldMap == null)
				webCrawlerFieldMap = new FieldMap(new File(indexDir,
						"webcrawler-mapping.xml"));
			return webCrawlerFieldMap;
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (XPathExpressionException e) {
			throw new SearchLibException(e);
		} catch (ParserConfigurationException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} finally {
			lock.unlock();
		}
	}

	public FieldMap getFileCrawlerFieldMap() throws SearchLibException {
		lock.lock();
		try {
			if (fileCrawlerFieldMap == null)
				fileCrawlerFieldMap = new FieldMap(new File(indexDir,
						"filecrawler-mapping.xml"));
			return fileCrawlerFieldMap;
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (XPathExpressionException e) {
			throw new SearchLibException(e);
		} catch (ParserConfigurationException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} finally {
			lock.unlock();
		}
	}

}
