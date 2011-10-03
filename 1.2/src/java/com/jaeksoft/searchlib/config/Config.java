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

package com.jaeksoft.searchlib.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.queryParser.ParseException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.analysis.stopwords.StopWordsManager;
import com.jaeksoft.searchlib.analysis.synonym.SynonymsManager;
import com.jaeksoft.searchlib.collapse.CollapseMode;
import com.jaeksoft.searchlib.crawler.FieldMap;
import com.jaeksoft.searchlib.crawler.database.DatabaseCrawlList;
import com.jaeksoft.searchlib.crawler.database.DatabaseCrawlMaster;
import com.jaeksoft.searchlib.crawler.file.database.FileManager;
import com.jaeksoft.searchlib.crawler.file.database.FilePathManager;
import com.jaeksoft.searchlib.crawler.file.database.FilePropertyManager;
import com.jaeksoft.searchlib.crawler.file.process.CrawlFileMaster;
import com.jaeksoft.searchlib.crawler.web.database.CredentialManager;
import com.jaeksoft.searchlib.crawler.web.database.PatternManager;
import com.jaeksoft.searchlib.crawler.web.database.SiteMapList;
import com.jaeksoft.searchlib.crawler.web.database.UrlFilterList;
import com.jaeksoft.searchlib.crawler.web.database.UrlManagerAbstract;
import com.jaeksoft.searchlib.crawler.web.database.WebPropertyManager;
import com.jaeksoft.searchlib.crawler.web.process.WebCrawlMaster;
import com.jaeksoft.searchlib.crawler.web.robotstxt.RobotsTxtCache;
import com.jaeksoft.searchlib.crawler.web.screenshot.ScreenshotManager;
import com.jaeksoft.searchlib.facet.FacetField;
import com.jaeksoft.searchlib.filter.Filter;
import com.jaeksoft.searchlib.filter.FilterList;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.IndexAbstract;
import com.jaeksoft.searchlib.index.IndexConfig;
import com.jaeksoft.searchlib.index.IndexSingle;
import com.jaeksoft.searchlib.logreport.LogReportManager;
import com.jaeksoft.searchlib.parser.ParserSelector;
import com.jaeksoft.searchlib.plugin.IndexPluginTemplateList;
import com.jaeksoft.searchlib.render.Render;
import com.jaeksoft.searchlib.render.RenderJsp;
import com.jaeksoft.searchlib.render.RenderXml;
import com.jaeksoft.searchlib.renderer.Renderer;
import com.jaeksoft.searchlib.renderer.RendererManager;
import com.jaeksoft.searchlib.replication.ReplicationList;
import com.jaeksoft.searchlib.replication.ReplicationMaster;
import com.jaeksoft.searchlib.replication.ReplicationThread;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.request.SearchRequestMap;
import com.jaeksoft.searchlib.result.Result;
import com.jaeksoft.searchlib.scheduler.JobList;
import com.jaeksoft.searchlib.scheduler.TaskEnum;
import com.jaeksoft.searchlib.schema.Field;
import com.jaeksoft.searchlib.schema.FieldList;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.snippet.SnippetField;
import com.jaeksoft.searchlib.sort.SortList;
import com.jaeksoft.searchlib.statistics.StatisticsList;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.web.ServletTransaction;

public abstract class Config {

	private IndexAbstract index = null;

	private Schema schema = null;

	private SearchRequestMap searchRequests = null;

	private ExecutorService threadPool = null;

	private StatisticsList statisticsList = null;

	private ParserSelector parserSelector = null;

	private UrlManagerAbstract urlManager = null;

	private PatternManager inclusionPatternManager = null;

	private PatternManager exclusionPatternManager = null;

	private CredentialManager webCredentialManager = null;

	private UrlFilterList urlFilterList = null;

	private SiteMapList siteMapList = null;

	private FilePathManager filePatternManager = null;

	private FileManager fileManager = null;

	private StopWordsManager stopWordsManager = null;

	private SynonymsManager synonymsManager = null;

	private WebPropertyManager webPropertyManager = null;

	private FilePropertyManager filePropertyManager = null;

	private XPathParser xppConfig = null;

	private WebCrawlMaster webCrawlMaster = null;

	private CrawlFileMaster fileCrawlMaster = null;

	private DatabaseCrawlMaster databaseCrawlMaster = null;

	private DatabaseCrawlList databaseCrawlList = null;

	private ScreenshotManager screenshotManager = null;

	private RendererManager rendererManager = null;

	private FieldMap webCrawlerFieldMap = null;

	private FieldMap fileCrawlerFieldMap = null;

	private IndexPluginTemplateList indexPluginTemplateList = null;

	private RobotsTxtCache robotsTxtCache = null;

	private File indexDir;

	protected final ReadWriteLock rwl = new ReadWriteLock();

	protected final Lock longTermLock = new ReentrantLock();

	private Mailer mailer = null;

	private ReplicationList replicationList = null;

	private ReplicationMaster replicationMaster = null;

	private JobList jobList = null;

	protected ConfigFiles configFiles = null;

	private String urlManagerClass = null;

	private LogReportManager logReportManager = null;

	private TaskEnum taskEnum = null;

	protected Config(File indexDirectory, String configXmlResourceName,
			boolean createIndexIfNotExists, boolean disableCrawler)
			throws SearchLibException {

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

			index = newIndex(indexDir, xppConfig, createIndexIfNotExists);
			schema = Schema.fromXmlConfig(this,
					xppConfig.getNode("/configuration/schema"), xppConfig);

			configFiles = new ConfigFiles();

			urlManagerClass = xppConfig.getAttributeString(
					"/configuration/urlManager", "class");
			if (urlManagerClass == null)
				urlManagerClass = "UrlManager";

			if (disableCrawler) {
				getFilePropertyManager().getCrawlEnabled().setValue(false);
				getWebPropertyManager().getCrawlEnabled().setValue(false);
			}
			getFileCrawlMaster();
			getWebCrawlMaster();
			getJobList();

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

	public File getDirectory() {
		rwl.r.lock();
		try {
			return indexDir;
		} finally {
			rwl.r.unlock();
		}
	}

	private void saveConfigWithoutLock() throws IOException,
			TransformerConfigurationException, SAXException,
			SearchLibException, XPathExpressionException {
		ConfigFileRotation cfr = configFiles.get(indexDir, "config.xml");
		try {
			XmlWriter xmlWriter = new XmlWriter(
					cfr.getTempPrintWriter("UTF-8"), "UTF-8");
			xmlWriter.startElement("configuration");
			getIndex().writeXmlConfig(xmlWriter);
			getSchema().writeXmlConfig(xmlWriter);
			if (urlManagerClass != null) {
				xmlWriter.startElement("urlManager", "class", urlManagerClass);
				xmlWriter.endElement();
			}
			getStatisticsList().writeXmlConfig(xmlWriter);
			IndexPluginTemplateList iptl = getIndexPluginTemplateList();
			if (iptl != null)
				iptl.writeXmlConfig(xmlWriter);
			getMailer();
			if (mailer != null)
				mailer.writeXmlConfig(xmlWriter);
			xmlWriter.endElement();
			xmlWriter.endDocument();
			cfr.rotate();
		} finally {
			cfr.abort();
		}
	}

	public void saveParsers() throws SearchLibException {
		ConfigFileRotation cfr = configFiles.get(indexDir, "parsers.xml");
		if (!longTermLock.tryLock())
			throw new SearchLibException("Replication in process");
		try {
			rwl.w.lock();
			try {
				XmlWriter xmlWriter = new XmlWriter(
						cfr.getTempPrintWriter("UTF-8"), "UTF-8");
				getParserSelector().writeXmlConfig(xmlWriter);
				xmlWriter.endDocument();
				cfr.rotate();
			} catch (TransformerConfigurationException e) {
				throw new SearchLibException(e);
			} catch (SAXException e) {
				throw new SearchLibException(e);
			} catch (IOException e) {
				throw new SearchLibException(e);
			} finally {
				rwl.w.unlock();
			}
		} finally {
			longTermLock.unlock();
			cfr.abort();
		}
	}

	public void saveJobs() throws SearchLibException {
		ConfigFileRotation cfr = configFiles.get(indexDir, "jobs.xml");
		if (!longTermLock.tryLock())
			throw new SearchLibException("Replication in process");
		try {
			JobList jobList = getJobList();
			rwl.w.lock();
			try {
				XmlWriter xmlWriter = new XmlWriter(
						cfr.getTempPrintWriter("UTF-8"), "UTF-8");
				jobList.writeXml(xmlWriter);
				xmlWriter.endDocument();
				cfr.rotate();
			} catch (TransformerConfigurationException e) {
				throw new SearchLibException(e);
			} catch (SAXException e) {
				throw new SearchLibException(e);
			} catch (IOException e) {
				throw new SearchLibException(e);
			} finally {
				rwl.w.unlock();
			}
			jobList.checkExecution();
		} finally {
			longTermLock.unlock();
			cfr.abort();
		}
	}

	public void saveReplicationList() throws IOException,
			TransformerConfigurationException, SAXException, SearchLibException {
		ConfigFileRotation cfr = configFiles.get(indexDir, "replication.xml");
		if (!longTermLock.tryLock())
			throw new SearchLibException("Replication in process");
		try {
			rwl.w.lock();
			try {
				PrintWriter pw = cfr.getTempPrintWriter("UTF-8");
				XmlWriter xmlWriter = new XmlWriter(pw, "UTF-8");
				getReplicationList().writeXml(xmlWriter);
				xmlWriter.endDocument();
				cfr.rotate();
			} finally {
				rwl.w.unlock();
			}
		} finally {
			longTermLock.unlock();
			cfr.abort();
		}
	}

	public void saveRequests() throws SearchLibException {
		ConfigFileRotation cfr = configFiles.get(indexDir, "requests.xml");
		if (!longTermLock.tryLock())
			throw new SearchLibException("Replication in process");
		try {
			rwl.w.lock();
			try {
				PrintWriter pw = cfr.getTempPrintWriter("UTF-8");
				XmlWriter xmlWriter = new XmlWriter(pw, "UTF-8");
				getSearchRequestMap().writeXmlConfig(xmlWriter);
				xmlWriter.endDocument();
				cfr.rotate();
			} finally {
				rwl.w.unlock();
			}
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (TransformerConfigurationException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} finally {
			longTermLock.unlock();
			cfr.abort();
		}
	}

	public void saveConfig() throws SearchLibException {
		if (!longTermLock.tryLock())
			throw new SearchLibException("Replication in process");
		try {
			rwl.w.lock();
			try {
				saveConfigWithoutLock();
			} finally {
				rwl.w.unlock();
			}
		} catch (TransformerConfigurationException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} catch (XPathExpressionException e) {
			throw new SearchLibException(e);
		} finally {
			longTermLock.unlock();
		}
	}

	private IndexAbstract newIndex(File indexDir, XPathParser xpp,
			boolean createIndexIfNotExists) throws XPathExpressionException,
			IOException, URISyntaxException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		NodeList nodeList = xpp.getNodeList("/configuration/indices/index");
		switch (nodeList.getLength()) {
		default:
			return null;
		case 1:
			return new IndexSingle(indexDir, new IndexConfig(xpp,
					xpp.getNode("/configuration/indices/index")),
					createIndexIfNotExists);
		}
	}

	public ExecutorService getThreadPool() {
		rwl.r.lock();
		try {
			if (threadPool != null)
				return threadPool;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (threadPool != null)
				return threadPool;
			threadPool = Executors.newCachedThreadPool();
			return threadPool;
		} finally {
			rwl.w.unlock();
		}
	}

	public Schema getSchema() {
		return schema;
	}

	public DatabaseCrawlList getDatabaseCrawlList() throws SearchLibException {
		rwl.r.lock();
		try {
			if (databaseCrawlList != null)
				return databaseCrawlList;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (databaseCrawlList != null)
				return databaseCrawlList;
			databaseCrawlList = DatabaseCrawlList.fromXml(
					getDatabaseCrawlMaster(), new File(indexDir,
							"databaseCrawlList.xml"));
			return databaseCrawlList;
		} catch (ParserConfigurationException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (XPathExpressionException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.w.unlock();
		}
	}

	public void saveDatabaseCrawlList() throws SearchLibException {
		ConfigFileRotation cfr = configFiles.get(indexDir,
				"databaseCrawlList.xml");
		if (!longTermLock.tryLock())
			throw new SearchLibException("Replication in process");
		try {
			rwl.w.lock();
			try {
				XmlWriter xmlWriter = new XmlWriter(
						cfr.getTempPrintWriter("UTF-8"), "UTF-8");
				getDatabaseCrawlList().writeXml(xmlWriter);
				xmlWriter.endDocument();
				cfr.rotate();
			} finally {
				rwl.w.unlock();
			}
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (TransformerConfigurationException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} finally {
			longTermLock.unlock();
			cfr.abort();
		}
	}

	protected WebCrawlMaster getNewWebCrawlMaster() throws SearchLibException {
		return new WebCrawlMaster(this);
	}

	public WebCrawlMaster getWebCrawlMaster() throws SearchLibException {
		rwl.r.lock();
		try {
			if (webCrawlMaster != null)
				return webCrawlMaster;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (webCrawlMaster != null)
				return webCrawlMaster;
			return webCrawlMaster = getNewWebCrawlMaster();
		} finally {
			rwl.w.unlock();
		}
	}

	public CrawlFileMaster getFileCrawlMaster() throws SearchLibException {
		rwl.r.lock();
		try {
			if (fileCrawlMaster != null)
				return fileCrawlMaster;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (fileCrawlMaster != null)
				return fileCrawlMaster;
			fileCrawlMaster = new CrawlFileMaster(this);
			return fileCrawlMaster;
		} finally {
			rwl.w.unlock();
		}
	}

	public DatabaseCrawlMaster getDatabaseCrawlMaster()
			throws SearchLibException {
		rwl.r.lock();
		try {
			if (databaseCrawlMaster != null)
				return databaseCrawlMaster;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (databaseCrawlMaster != null)
				return databaseCrawlMaster;
			return databaseCrawlMaster = new DatabaseCrawlMaster(this);
		} finally {
			rwl.w.unlock();
		}
	}

	public ReplicationMaster getReplicationMaster() {
		rwl.r.lock();
		try {
			if (replicationMaster != null)
				return replicationMaster;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (replicationMaster != null)
				return replicationMaster;
			return replicationMaster = new ReplicationMaster(this);
		} finally {
			rwl.w.unlock();
		}
	}

	protected ParserSelector getNewParserSelector(XPathParser xpp, Node node)
			throws XPathExpressionException, DOMException, IOException,
			SearchLibException {
		return new ParserSelector(this, xpp, node);
	}

	final public ParserSelector getParserSelector() throws SearchLibException {
		rwl.r.lock();
		try {
			if (parserSelector != null)
				return parserSelector;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (parserSelector != null)
				return parserSelector;
			File parserFile = new File(indexDir, "parsers.xml");
			if (parserFile.exists()) {
				XPathParser xpp = new XPathParser(parserFile);
				parserSelector = getNewParserSelector(xpp,
						xpp.getNode("/parsers"));
			} else {
				Node node = xppConfig.getNode("/configuration/parsers");
				if (node != null)
					parserSelector = getNewParserSelector(xppConfig, node);
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
			rwl.w.unlock();
		}
	}

	public TaskEnum getJobTaskEnum() {
		rwl.r.lock();
		try {
			if (taskEnum != null)
				return taskEnum;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			taskEnum = new TaskEnum();
			return taskEnum;
		} finally {
			rwl.w.unlock();
		}
	}

	public JobList getJobList() throws SearchLibException {
		rwl.r.lock();
		try {
			if (jobList != null)
				return jobList;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (jobList != null)
				return jobList;
			File file = new File(indexDir, "jobs.xml");
			jobList = JobList.fromXml(this, file);
			jobList.checkExecution();
			return jobList;
		} catch (XPathExpressionException e) {
			throw new SearchLibException(e);
		} catch (ParserConfigurationException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.w.unlock();
		}
	}

	public Mailer getMailer() throws XPathExpressionException {
		rwl.r.lock();
		try {
			if (mailer != null)
				return mailer;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (mailer != null)
				return mailer;
			return mailer = Mailer.fromXmlConfig(xppConfig
					.getNode("/configuration/mailer"));
		} finally {
			rwl.w.unlock();
		}
	}

	public ReplicationList getReplicationList() throws SearchLibException {
		rwl.r.lock();
		try {
			if (replicationList != null)
				return replicationList;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (replicationList != null)
				return replicationList;
			return replicationList = new ReplicationList(
					getReplicationMaster(), new File(indexDir,
							"replication.xml"));
		} catch (XPathExpressionException e) {
			throw new SearchLibException(e);
		} catch (ParserConfigurationException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.w.unlock();
		}
	}

	public ScreenshotManager getScreenshotManager() throws SearchLibException {
		rwl.r.lock();
		try {
			if (screenshotManager != null)
				return screenshotManager;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (screenshotManager != null)
				return screenshotManager;
			return screenshotManager = new ScreenshotManager(this);
		} finally {
			rwl.w.unlock();
		}
	}

	private File getRendererDirectory() {
		File directory = new File(getDirectory(), "renderers");
		if (!directory.exists())
			directory.mkdir();
		return directory;
	}

	public RendererManager getRendererManager() throws SearchLibException {
		rwl.r.lock();
		try {
			if (rendererManager != null)
				return rendererManager;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (rendererManager != null)
				return rendererManager;
			return rendererManager = new RendererManager(this,
					getRendererDirectory());
		} catch (XPathExpressionException e) {
			throw new SearchLibException(e);
		} catch (ParserConfigurationException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.w.unlock();
		}
	}

	public void save(Renderer renderer) throws SearchLibException,
			UnsupportedEncodingException {
		ConfigFileRotation cfr = configFiles.get(getRendererDirectory(),
				URLEncoder.encode(renderer.getName(), "UTF-8") + ".xml");
		if (!longTermLock.tryLock())
			throw new SearchLibException("Replication in process");
		try {
			rwl.w.lock();
			try {
				XmlWriter xmlWriter = new XmlWriter(
						cfr.getTempPrintWriter("UTF-8"), "UTF-8");
				renderer.writeXml(xmlWriter);
				xmlWriter.endDocument();
				cfr.rotate();
			} catch (TransformerConfigurationException e) {
				throw new SearchLibException(e);
			} catch (SAXException e) {
				throw new SearchLibException(e);
			} catch (IOException e) {
				throw new SearchLibException(e);
			} finally {
				rwl.w.unlock();
			}
		} finally {
			longTermLock.unlock();
			cfr.abort();
		}

	}

	public void delete(Renderer renderer) throws SearchLibException {
		if (!longTermLock.tryLock())
			throw new SearchLibException("Replication in process");
		ConfigFileRotation cfr = null;
		try {
			rwl.w.lock();
			try {
				cfr = configFiles
						.get(getRendererDirectory(),
								URLEncoder.encode(renderer.getName(), "UTF-8")
										+ ".xml");
				cfr.delete();
			} catch (IOException e) {
				throw new SearchLibException(e);
			} finally {
				rwl.w.unlock();
			}
		} finally {
			longTermLock.unlock();
			if (cfr != null)
				cfr.abort();
		}
	}

	public String getIndexName() {
		return getDirectory().getName();
	}

	public IndexAbstract getIndex() {
		return this.index;
	}

	public IndexPluginTemplateList getIndexPluginTemplateList()
			throws SearchLibException {
		rwl.r.lock();
		try {
			if (indexPluginTemplateList != null)
				return indexPluginTemplateList;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (indexPluginTemplateList != null)
				return indexPluginTemplateList;
			Node node = xppConfig.getNode("/configuration/indexPlugins");
			if (node == null)
				return null;
			return indexPluginTemplateList = IndexPluginTemplateList
					.fromXmlConfig(xppConfig, node);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (XPathExpressionException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.w.unlock();
		}

	}

	protected File getStatStorage() {
		return new File(getDirectory(), "statstore");
	}

	public StatisticsList getStatisticsList() throws SearchLibException {
		rwl.r.lock();
		try {
			if (statisticsList != null)
				return statisticsList;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (statisticsList != null)
				return statisticsList;
			statisticsList = StatisticsList.fromXmlConfig(xppConfig,
					xppConfig.getNode("/configuration/statistics"),
					getStatStorage());
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
		} finally {
			rwl.w.unlock();
		}
	}

	public LogReportManager getLogReportManager() throws SearchLibException {
		rwl.r.lock();
		try {
			if (logReportManager != null)
				return logReportManager;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (logReportManager != null)
				return logReportManager;
			return logReportManager = new LogReportManager(getIndexName());
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.w.unlock();
		}
	}

	public SearchRequest getNewSearchRequest() {
		return new SearchRequest(this);
	}

	public SearchRequest getNewSearchRequest(String requestName)
			throws SearchLibException {
		if (requestName == null)
			throw new SearchLibException("No request name");
		return new SearchRequest(getSearchRequestMap().get(requestName));
	}

	public SearchRequestMap getSearchRequestMap() throws SearchLibException {
		rwl.r.lock();
		try {
			if (searchRequests != null)
				return searchRequests;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (searchRequests != null)
				return searchRequests;
			File requestFile = new File(indexDir, "requests.xml");
			if (requestFile.exists()) {
				XPathParser xpp = new XPathParser(requestFile);
				searchRequests = SearchRequestMap.fromXmlConfig(this, xpp,
						xpp.getNode("/requests"));
			} else
				searchRequests = SearchRequestMap
						.fromXmlConfig(this, xppConfig,
								xppConfig.getNode("/configuration/requests"));
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
			rwl.w.unlock();
		}
	}

	protected UrlManagerAbstract getNewUrlManagerInstance()
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		return (UrlManagerAbstract) Class.forName(
				"com.jaeksoft.searchlib.crawler.web.database."
						+ urlManagerClass).newInstance();
	}

	final public UrlManagerAbstract getUrlManager() throws SearchLibException {
		rwl.r.lock();
		try {
			if (urlManager != null)
				return urlManager;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (urlManager != null)
				return urlManager;
			UrlManagerAbstract ua = getNewUrlManagerInstance();
			ua.init((Client) this, indexDir);
			return urlManager = ua;
		} catch (FileNotFoundException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.w.unlock();
		}
	}

	public StopWordsManager getStopWordsManager() throws SearchLibException {
		rwl.r.lock();
		try {
			if (stopWordsManager != null)
				return stopWordsManager;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (stopWordsManager != null)
				return stopWordsManager;
			return stopWordsManager = new StopWordsManager(this, new File(
					indexDir, "stopwords"));
		} finally {
			rwl.w.unlock();
		}
	}

	public SynonymsManager getSynonymsManager() throws SearchLibException {
		rwl.r.lock();
		try {
			if (synonymsManager != null)
				return synonymsManager;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (synonymsManager != null)
				return synonymsManager;
			return synonymsManager = new SynonymsManager(this, new File(
					indexDir, "synonyms"));
		} finally {
			rwl.w.unlock();
		}
	}

	public PatternManager getInclusionPatternManager()
			throws SearchLibException {
		rwl.r.lock();
		try {
			if (inclusionPatternManager != null)
				return inclusionPatternManager;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (inclusionPatternManager != null)
				return inclusionPatternManager;
			return inclusionPatternManager = new PatternManager(indexDir,
					"patterns.xml");
		} finally {
			rwl.w.unlock();
		}
	}

	public PatternManager getExclusionPatternManager()
			throws SearchLibException {
		rwl.r.lock();
		try {
			if (exclusionPatternManager != null)
				return exclusionPatternManager;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (exclusionPatternManager != null)
				return exclusionPatternManager;
			return exclusionPatternManager = new PatternManager(indexDir,
					"patterns_exclusion.xml");
		} finally {
			rwl.w.unlock();
		}
	}

	public CredentialManager getWebCredentialManager()
			throws SearchLibException {
		rwl.r.lock();
		try {
			if (webCredentialManager != null)
				return webCredentialManager;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (webCredentialManager != null)
				return webCredentialManager;
			return webCredentialManager = new CredentialManager(indexDir,
					"web_credentials.xml");
		} finally {
			rwl.w.unlock();
		}
	}

	public SiteMapList getSiteMapList() throws SearchLibException {
		rwl.r.lock();
		try {
			if (siteMapList != null)
				return siteMapList;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (siteMapList != null)
				return siteMapList;
			return siteMapList = new SiteMapList(indexDir,
					"webcrawler-sitemap.xml");
		} finally {
			rwl.w.unlock();
		}
	}

	public void saveSiteMapList() throws SearchLibException {
		ConfigFileRotation cfr = configFiles.get(indexDir,
				"webcrawler-sitemap.xml");
		if (!longTermLock.tryLock())
			throw new SearchLibException("Replication in process");
		try {
			rwl.w.lock();
			try {
				XmlWriter xmlWriter = new XmlWriter(
						cfr.getTempPrintWriter("UTF-8"), "UTF-8");
				getSiteMapList().writeXml(xmlWriter);
				xmlWriter.endDocument();
				cfr.rotate();
			} finally {
				rwl.w.unlock();
			}
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (TransformerConfigurationException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} finally {
			longTermLock.unlock();
			cfr.abort();
		}
	}

	public UrlFilterList getUrlFilterList() throws SearchLibException {
		rwl.r.lock();
		try {
			if (urlFilterList != null)
				return urlFilterList;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (urlFilterList != null)
				return urlFilterList;
			return urlFilterList = new UrlFilterList(indexDir,
					"webcrawler-urlfilter.xml");
		} finally {
			rwl.w.unlock();
		}
	}

	public void saveUrlFilterList() throws SearchLibException {
		ConfigFileRotation cfr = configFiles.get(indexDir,
				"webcrawler-urlfilter.xml");
		if (!longTermLock.tryLock())
			throw new SearchLibException("Replication in process");
		try {
			rwl.w.lock();
			try {
				XmlWriter xmlWriter = new XmlWriter(
						cfr.getTempPrintWriter("UTF-8"), "UTF-8");
				getUrlFilterList().writeXml(xmlWriter);
				xmlWriter.endDocument();
				cfr.rotate();
			} finally {
				rwl.w.unlock();
			}
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (TransformerConfigurationException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} finally {
			longTermLock.unlock();
			cfr.abort();
		}
	}

	public void push(ReplicationThread replicationThread)
			throws SearchLibException {
		longTermLock.lock();
		try {
			rwl.r.lock();
			try {
				replicationThread.push();
			} finally {
				rwl.r.unlock();
			}
		} finally {
			longTermLock.unlock();
		}
	}

	public FilePathManager getFilePathManager() throws SearchLibException {
		rwl.r.lock();
		try {
			if (filePatternManager != null)
				return filePatternManager;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (filePatternManager != null)
				return filePatternManager;
			return filePatternManager = new FilePathManager(this, indexDir);
		} finally {
			rwl.w.unlock();
		}
	}

	protected FileManager getNewFileManagerInstance()
			throws FileNotFoundException, SearchLibException,
			URISyntaxException {
		return new FileManager((Client) this, indexDir);
	}

	final public FileManager getFileManager() throws SearchLibException {
		rwl.r.lock();
		try {
			if (fileManager != null)
				return fileManager;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (fileManager != null)
				return fileManager;
			return fileManager = getNewFileManagerInstance();
		} catch (FileNotFoundException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.w.unlock();
		}
	}

	public WebPropertyManager getWebPropertyManager() throws SearchLibException {
		rwl.r.lock();
		try {
			if (webPropertyManager != null)
				return webPropertyManager;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (webPropertyManager != null)
				return webPropertyManager;
			return webPropertyManager = new WebPropertyManager(new File(
					indexDir, "webcrawler-properties.xml"));
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.w.unlock();
		}
	}

	public FilePropertyManager getFilePropertyManager()
			throws SearchLibException {
		rwl.r.lock();
		try {
			if (filePropertyManager != null)
				return filePropertyManager;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (filePropertyManager != null)
				return filePropertyManager;
			return filePropertyManager = new FilePropertyManager(new File(
					indexDir, "filecrawler-properties.xml"));
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.w.unlock();
		}
	}

	public SearchRequest getNewSearchRequest(ServletTransaction transaction)
			throws ParseException, SyntaxError, SearchLibException {

		String requestName = transaction.getParameterString("qt", "search");
		SearchRequest searchRequest = getNewSearchRequest(requestName);
		if (searchRequest == null)
			searchRequest = getNewSearchRequest();

		String p;
		Integer i;
		Boolean b;

		if ((p = transaction.getParameterString("query")) != null)
			searchRequest.setQueryString(p);
		else if ((p = transaction.getParameterString("q")) != null)
			searchRequest.setQueryString(p);

		if ((i = transaction.getParameterInteger("start")) != null)
			searchRequest.setStart(i);

		if ((i = transaction.getParameterInteger("rows")) != null)
			searchRequest.setRows(i);

		if ((p = transaction.getParameterString("lang")) != null)
			searchRequest.setLang(LanguageEnum.findByCode(p));

		if ((p = transaction.getParameterString("collapse.mode")) != null)
			searchRequest.setCollapseMode(CollapseMode.valueOfLabel(p));

		if ((p = transaction.getParameterString("collapse.field")) != null)
			searchRequest.setCollapseField(getSchema().getFieldList().get(p)
					.getName());

		if ((i = transaction.getParameterInteger("collapse.max")) != null)
			searchRequest.setCollapseMax(i);

		if ((p = transaction.getParameterString("withDocs")) != null)
			searchRequest.setWithDocument(true);

		if ((p = transaction.getParameterString("log")) != null)
			searchRequest.setLogReport(true);

		if (searchRequest.isLogReport()) {
			for (int j = 1; j <= 10; j++) {
				p = transaction.getParameterString("log" + j);
				if (p == null)
					break;
				searchRequest.addCustomLog(p);
			}
		}

		String[] values;

		if ((values = transaction.getParameterValues("fq")) != null) {
			FilterList fl = searchRequest.getFilterList();
			for (String value : values)
				if (value != null)
					if (value.trim().length() > 0)
						fl.add(value, false, Filter.Source.REQUEST);
		}

		if ((values = transaction.getParameterValues("fqn")) != null) {
			FilterList fl = searchRequest.getFilterList();
			for (String value : values)
				if (value != null)
					if (value.trim().length() > 0)
						fl.add(value, true, Filter.Source.REQUEST);
		}

		if ((values = transaction.getParameterValues("rf")) != null) {
			FieldList<Field> rf = searchRequest.getReturnFieldList();
			for (String value : values)
				if (value != null)
					if (value.trim().length() > 0)
						rf.add(new Field(getSchema().getFieldList().get(value)));
		}

		if ((values = transaction.getParameterValues("hl")) != null) {
			FieldList<SnippetField> snippetFields = searchRequest
					.getSnippetFieldList();
			for (String value : values)
				snippetFields.add(new SnippetField(getSchema().getFieldList()
						.get(value).getName()));
		}

		if ((values = transaction.getParameterValues("fl")) != null) {
			FieldList<Field> returnFields = searchRequest.getReturnFieldList();
			for (String value : values)
				returnFields.add(getSchema().getFieldList().get(value));
		}

		if ((values = transaction.getParameterValues("sort")) != null) {
			SortList sortList = searchRequest.getSortList();
			for (String value : values)
				sortList.add(value);
		}

		if ((values = transaction.getParameterValues("facet")) != null) {
			FieldList<FacetField> facetList = searchRequest.getFacetFieldList();
			for (String value : values)
				facetList.add(FacetField.buildFacetField(value, false, false));
		}
		if ((values = transaction.getParameterValues("facet.collapse")) != null) {
			FieldList<FacetField> facetList = searchRequest.getFacetFieldList();
			for (String value : values)
				facetList.add(FacetField.buildFacetField(value, false, true));
		}
		if ((values = transaction.getParameterValues("facet.multi")) != null) {
			FieldList<FacetField> facetList = searchRequest.getFacetFieldList();
			for (String value : values)
				facetList.add(FacetField.buildFacetField(value, true, false));
		}
		if ((values = transaction.getParameterValues("facet.multi.collapse")) != null) {
			FieldList<FacetField> facetList = searchRequest.getFacetFieldList();
			for (String value : values)
				facetList.add(FacetField.buildFacetField(value, true, true));
		}

		if ((b = transaction.getParameterBoolean("mlt", "yes")) != null)
			searchRequest.setMoreLikeThis(b);

		if ((p = transaction.getParameterString("mlt.docquery")) != null)
			searchRequest.setMoreLikeThisDocQuery(p);

		if ((i = transaction.getParameterInteger("mlt.minwordlen")) != null)
			searchRequest.setMoreLikeThisMinWordLen(i);

		if ((i = transaction.getParameterInteger("mlt.maxwordlen")) != null)
			searchRequest.setMoreLikeThisMaxWordLen(i);

		if ((i = transaction.getParameterInteger("mlt.mindocfreq")) != null)
			searchRequest.setMoreLikeThisMinDocFreq(i);

		if ((i = transaction.getParameterInteger("mlt.mintermfreq")) != null)
			searchRequest.setMoreLikeThisMinTermFreq(i);

		if ((p = transaction.getParameterString("mlt.stopwords")) != null)
			searchRequest.setMoreLikeThisStopWords(p);

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

	public RobotsTxtCache getRobotsTxtCache() throws SearchLibException {
		rwl.r.lock();
		try {
			if (robotsTxtCache != null)
				return robotsTxtCache;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (robotsTxtCache != null)
				return robotsTxtCache;
			return robotsTxtCache = new RobotsTxtCache();
		} finally {
			rwl.w.unlock();
		}
	}

	public FieldMap getWebCrawlerFieldMap() throws SearchLibException {
		rwl.r.lock();
		try {
			if (webCrawlerFieldMap != null)
				return webCrawlerFieldMap;

		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (webCrawlerFieldMap != null)
				return webCrawlerFieldMap;
			return webCrawlerFieldMap = new FieldMap(new File(indexDir,
					"webcrawler-mapping.xml"));
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (XPathExpressionException e) {
			throw new SearchLibException(e);
		} catch (ParserConfigurationException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.w.unlock();
		}
	}

	public FieldMap getFileCrawlerFieldMap() throws SearchLibException {
		rwl.r.lock();
		try {
			if (fileCrawlerFieldMap != null)
				return fileCrawlerFieldMap;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (fileCrawlerFieldMap != null)
				return fileCrawlerFieldMap;
			return fileCrawlerFieldMap = new FieldMap(new File(indexDir,
					"filecrawler-mapping.xml"));
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (XPathExpressionException e) {
			throw new SearchLibException(e);
		} catch (ParserConfigurationException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.w.unlock();
		}
	}

	private void prepareClose(boolean waitForEnd) throws SearchLibException {
		boolean isFileCrawlMaster;
		boolean isWebCrawlMaster;
		boolean isDatabaseCrawlMaster;
		boolean isUrlManager;
		rwl.r.lock();
		try {
			isFileCrawlMaster = fileCrawlMaster != null;
			isWebCrawlMaster = webCrawlMaster != null;
			isDatabaseCrawlMaster = databaseCrawlMaster != null;
			isUrlManager = urlManager != null;
		} finally {
			rwl.r.unlock();
		}
		if (isFileCrawlMaster)
			fileCrawlMaster.abort();
		if (isWebCrawlMaster)
			webCrawlMaster.abort();
		if (isDatabaseCrawlMaster)
			databaseCrawlMaster.abort();

		if (waitForEnd) {
			if (isFileCrawlMaster)
				fileCrawlMaster.waitForEnd(0);
			if (isWebCrawlMaster)
				webCrawlMaster.waitForEnd(0);
			if (isDatabaseCrawlMaster)
				databaseCrawlMaster.waitForEnd(0);
		}
		if (isUrlManager)
			urlManager.free();
	}

	private void closeQuiet() {
		try {
			getIndex().close();
		} catch (Exception e) {
			Logging.warn(e.getMessage(), e);
		}
		try {
			getLogReportManager().close();
		} catch (Exception e) {
			Logging.warn(e.getMessage(), e);
		}
		try {
			StatisticsList statList = getStatisticsList();
			if (statList != null)
				statList.save(getStatStorage());
		} catch (Exception e) {
			Logging.warn(e.getMessage(), e);
		}
	}

	private void close(File trashDir) {
		try {
			prepareClose(true);
		} catch (SearchLibException e) {
			Logging.warn(e.getMessage(), e);
		}
		longTermLock.lock();
		try {
			rwl.w.lock();
			try {
				closeQuiet();
				if (trashDir != null)
					indexDir.renameTo(trashDir);
			} finally {
				rwl.w.unlock();
			}
		} finally {
			longTermLock.unlock();
		}
	}

	public void close() {
		close(null);
	}

	public void trash(File trashDir) throws SearchLibException, NamingException {
		close(trashDir);
	}

	public void delete() throws IOException {
		FileUtils.deleteDirectory(getDirectory());
	}
}