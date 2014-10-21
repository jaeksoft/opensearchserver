/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2014 Emmanuel Keller / Jaeksoft
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
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.InvalidPropertiesFormatException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.stopwords.StopWordsManager;
import com.jaeksoft.searchlib.analysis.synonym.SynonymsManager;
import com.jaeksoft.searchlib.api.ApiManager;
import com.jaeksoft.searchlib.authentication.AuthManager;
import com.jaeksoft.searchlib.autocompletion.AutoCompletionManager;
import com.jaeksoft.searchlib.classifier.Classifier;
import com.jaeksoft.searchlib.classifier.ClassifierManager;
import com.jaeksoft.searchlib.crawler.FieldMap;
import com.jaeksoft.searchlib.crawler.cache.CrawlCacheManager;
import com.jaeksoft.searchlib.crawler.database.DatabaseCrawlList;
import com.jaeksoft.searchlib.crawler.database.DatabaseCrawlMaster;
import com.jaeksoft.searchlib.crawler.database.DatabasePropertyManager;
import com.jaeksoft.searchlib.crawler.file.database.FileManager;
import com.jaeksoft.searchlib.crawler.file.database.FilePathManager;
import com.jaeksoft.searchlib.crawler.file.database.FilePropertyManager;
import com.jaeksoft.searchlib.crawler.file.process.CrawlFileMaster;
import com.jaeksoft.searchlib.crawler.mailbox.MailboxCrawlList;
import com.jaeksoft.searchlib.crawler.mailbox.MailboxCrawlMaster;
import com.jaeksoft.searchlib.crawler.rest.RestCrawlList;
import com.jaeksoft.searchlib.crawler.rest.RestCrawlMaster;
import com.jaeksoft.searchlib.crawler.web.database.CookieManager;
import com.jaeksoft.searchlib.crawler.web.database.CredentialManager;
import com.jaeksoft.searchlib.crawler.web.database.HeaderManager;
import com.jaeksoft.searchlib.crawler.web.database.PatternManager;
import com.jaeksoft.searchlib.crawler.web.database.UrlFilterList;
import com.jaeksoft.searchlib.crawler.web.database.UrlManager;
import com.jaeksoft.searchlib.crawler.web.database.WebPropertyManager;
import com.jaeksoft.searchlib.crawler.web.process.WebCrawlMaster;
import com.jaeksoft.searchlib.crawler.web.robotstxt.RobotsTxtCache;
import com.jaeksoft.searchlib.crawler.web.screenshot.ScreenshotManager;
import com.jaeksoft.searchlib.crawler.web.sitemap.SiteMapList;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.IndexAbstract;
import com.jaeksoft.searchlib.index.IndexConfig;
import com.jaeksoft.searchlib.learning.Learner;
import com.jaeksoft.searchlib.learning.LearnerManager;
import com.jaeksoft.searchlib.logreport.LogReportManager;
import com.jaeksoft.searchlib.parser.ParserSelector;
import com.jaeksoft.searchlib.plugin.IndexPluginTemplateList;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.renderer.Renderer;
import com.jaeksoft.searchlib.renderer.RendererManager;
import com.jaeksoft.searchlib.replication.ReplicationList;
import com.jaeksoft.searchlib.replication.ReplicationMaster;
import com.jaeksoft.searchlib.replication.ReplicationThread;
import com.jaeksoft.searchlib.report.ReportsManager;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.request.RequestMap;
import com.jaeksoft.searchlib.request.RequestTypeEnum;
import com.jaeksoft.searchlib.request.SearchPatternRequest;
import com.jaeksoft.searchlib.scheduler.JobList;
import com.jaeksoft.searchlib.scheduler.TaskEnum;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.script.ScriptManager;
import com.jaeksoft.searchlib.statistics.StatisticsList;
import com.jaeksoft.searchlib.util.IOUtils;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.SimpleLock;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.web.ServletTransaction;
import com.jaeksoft.searchlib.web.controller.PushEvent;

public abstract class Config implements ThreadFactory {

	private final IndexAbstract index;

	private final Schema schema;

	private RequestMap requests = null;

	private ExecutorService threadPool = null;

	private ThreadGroup threadGroup = null;

	private StatisticsList statisticsList = null;

	private ParserSelector parserSelector = null;

	private UrlManager urlManager = null;

	private PatternManager inclusionPatternManager = null;

	private PatternManager exclusionPatternManager = null;

	private CredentialManager webCredentialManager = null;

	private CookieManager webCookieManager = null;

	private HeaderManager webHeaderManager = null;

	private UrlFilterList urlFilterList = null;

	private SiteMapList siteMapList = null;

	private FilePathManager filePatternManager = null;

	private FileManager fileManager = null;

	private StopWordsManager stopWordsManager = null;

	private SynonymsManager synonymsManager = null;

	private DatabasePropertyManager databasePropertyManager = null;

	private WebPropertyManager webPropertyManager = null;

	private FilePropertyManager filePropertyManager = null;

	private AutoCompletionManager autoCompletionManager = null;

	private XPathParser xppConfig = null;

	private WebCrawlMaster webCrawlMaster = null;

	private CrawlFileMaster fileCrawlMaster = null;

	private DatabaseCrawlMaster databaseCrawlMaster = null;

	private DatabaseCrawlList databaseCrawlList = null;

	private RestCrawlMaster restCrawlMaster = null;

	private RestCrawlList restCrawlList = null;

	private MailboxCrawlMaster mailboxCrawlMaster = null;

	private MailboxCrawlList mailboxCrawlList = null;

	private ScreenshotManager screenshotManager = null;

	private RendererManager rendererManager = null;

	private ApiManager apiManager = null;

	private FieldMap webCrawlerFieldMap = null;

	private FieldMap fileCrawlerFieldMap = null;

	private IndexPluginTemplateList indexPluginTemplateList = null;

	private RobotsTxtCache robotsTxtCache = null;

	protected final File indexDir;

	private final SimpleLock replicationLock = new SimpleLock();

	private ReplicationList replicationList = null;

	private ReplicationMaster replicationMaster = null;

	private JobList jobList = null;

	protected ConfigFiles configFiles = null;

	private String urlManagerClass = null;

	private LogReportManager logReportManager = null;

	private TaskEnum taskEnum = null;

	private ClassifierManager classifierManager = null;

	private LearnerManager learnerManager = null;

	private ScriptManager scriptManager = null;

	private ReportsManager reportsManager = null;

	private AuthManager authManager = null;

	private CrawlCacheManager crawlCacheManager = null;

	private boolean isClosed = false;

	protected Config(File indexDirectory, String configXmlResourceName,
			boolean createIndexIfNotExists, boolean disableCrawler)
			throws SearchLibException {

		try {
			indexDir = indexDirectory;
			if (!indexDir.exists())
				throw new SearchLibException("Index \"" + indexDir.getName()
						+ "\" not found. The index directory does not exist");
			if (!indexDir.isDirectory())
				throw new SearchLibException(
						"Indx not found. The index path is not a directory.");

			File configFile = new File(indexDirectory, "config.xml");

			if (configXmlResourceName != null) {
				InputStream is = getClass().getResourceAsStream(
						configXmlResourceName);
				try {
					FileUtils.copyInputStreamToFile(is, configFile);
				} finally {
					IOUtils.close(is);
				}
			}

			xppConfig = new XPathParser(configFile);

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
			IndexAbstract indexAbstract = getIndexAbstract();
			indexAbstract.addUpdateInterface(getClassifierManager());
			indexAbstract.addUpdateInterface(getLearnerManager());
			indexAbstract.addUpdateInterface(getAuthManager());

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
		} catch (ClassNotFoundException e) {
			throw new SearchLibException(e);
		} catch (JSONException e) {
			throw new SearchLibException(e);
		}
	}

	public File getDirectory() {
		return indexDir;
	}

	private void saveConfigWithoutLock() throws IOException,
			TransformerConfigurationException, SAXException,
			SearchLibException, XPathExpressionException {
		ConfigFileRotation cfr = configFiles.get(indexDir, "config.xml");
		try {
			XmlWriter xmlWriter = new XmlWriter(
					cfr.getTempPrintWriter("UTF-8"), "UTF-8");
			xmlWriter.startElement("configuration");
			getIndexAbstract().writeXmlConfig(xmlWriter);
			getSchema().writeXmlConfig(xmlWriter);
			if (urlManagerClass != null) {
				xmlWriter.startElement("urlManager", "class", urlManagerClass);
				xmlWriter.endElement();
			}
			getStatisticsList().writeXmlConfig(xmlWriter);
			IndexPluginTemplateList iptl = getIndexPluginTemplateList();
			if (iptl != null)
				iptl.writeXmlConfig(xmlWriter);
			xmlWriter.endElement();
			xmlWriter.endDocument();
			cfr.rotate();
		} finally {
			cfr.abort();
		}
	}

	private final ReadWriteLock parsersLock = new ReadWriteLock();

	public void saveParsers() throws SearchLibException {
		ConfigFileRotation cfr = configFiles.get(indexDir, "parsers.xml");
		if (!replicationLock.rl.tryLock())
			throw new SearchLibException("Replication in process");
		try {
			parsersLock.w.lock();
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
				parsersLock.w.unlock();
			}
		} finally {
			replicationLock.rl.unlock();
			cfr.abort();
		}
	}

	private final ReadWriteLock jobsLock = new ReadWriteLock();

	public void saveJobs() throws SearchLibException {
		ConfigFileRotation cfr = configFiles.get(indexDir, "jobs.xml");
		if (!replicationLock.rl.tryLock())
			throw new SearchLibException("Replication in process");
		try {
			JobList jobList = getJobList();
			jobsLock.w.lock();
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
				jobsLock.w.unlock();
			}
			jobList.checkExecution();
		} finally {
			replicationLock.rl.unlock();
			cfr.abort();
		}
	}

	private final ReadWriteLock replicationsLock = new ReadWriteLock();

	public void saveReplicationList() throws IOException,
			TransformerConfigurationException, SAXException, SearchLibException {
		ConfigFileRotation cfr = configFiles.get(indexDir, "replication.xml");
		if (!replicationLock.rl.tryLock())
			throw new SearchLibException("Replication in process");
		try {
			replicationsLock.w.lock();
			try {
				PrintWriter pw = cfr.getTempPrintWriter("UTF-8");
				XmlWriter xmlWriter = new XmlWriter(pw, "UTF-8");
				getReplicationList().writeXml(xmlWriter);
				xmlWriter.endDocument();
				cfr.rotate();
			} finally {
				replicationsLock.w.unlock();
			}
		} finally {
			replicationLock.rl.unlock();
			cfr.abort();
		}
	}

	public void saveRequests() throws SearchLibException {
		ConfigFileRotation cfr = configFiles.get(indexDir, "requests.xml");
		if (!replicationLock.rl.tryLock())
			throw new SearchLibException("Replication in process");
		try {
			requestsLock.w.lock();
			try {
				PrintWriter pw = cfr.getTempPrintWriter("UTF-8");
				XmlWriter xmlWriter = new XmlWriter(pw, "UTF-8");
				getRequestMap().writeXmlConfig(xmlWriter);
				xmlWriter.endDocument();
				cfr.rotate();
			} finally {
				requestsLock.w.unlock();
			}
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (TransformerConfigurationException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} finally {
			replicationLock.rl.unlock();
			cfr.abort();
		}
	}

	private final ReadWriteLock configLock = new ReadWriteLock();

	public void saveConfig() throws SearchLibException {
		if (!replicationLock.rl.tryLock())
			throw new SearchLibException("Replication in process");
		try {
			configLock.w.lock();
			try {
				saveConfigWithoutLock();
				schema.recompileAnalyzers();
			} finally {
				configLock.w.unlock();
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
			replicationLock.rl.unlock();
		}
		PushEvent.eventSchemaChange.publish((Client) this);
	}

	private IndexAbstract newIndex(File indexDir, XPathParser xpp,
			boolean createIndexIfNotExists) throws XPathExpressionException,
			IOException, URISyntaxException, SearchLibException, JSONException {
		NodeList nodeList = xpp.getNodeList("/configuration/indices/index");
		switch (nodeList.getLength()) {
		default:
			return null;
		case 1:
			return new IndexConfig(nodeList.item(0)).getNewIndex(indexDir,
					createIndexIfNotExists);
		}
	}

	@Override
	public final Thread newThread(Runnable target) {
		return new Thread(threadGroup, target);
	}

	private final ReadWriteLock threadPoolLock = new ReadWriteLock();

	public ExecutorService getThreadPool() {
		threadPoolLock.r.lock();
		try {
			if (threadPool != null)
				return threadPool;
		} finally {
			threadPoolLock.r.unlock();
		}
		threadPoolLock.w.lock();
		try {
			if (threadPool != null)
				return threadPool;
			if (threadGroup == null)
				threadGroup = new ThreadGroup(ClientCatalog.getThreadGroup(),
						getIndexName());
			threadPool = Executors.newCachedThreadPool(this);
			return threadPool;
		} finally {
			threadPoolLock.w.unlock();
		}
	}

	public ThreadGroup getThreadGroup() {
		threadPoolLock.r.lock();
		try {
			return threadGroup;
		} finally {
			threadPoolLock.r.unlock();
		}
	}

	public Schema getSchema() {
		return schema;
	}

	private File getClassifierDirectory() {
		File directory = new File(this.getDirectory(), "classifiers");
		if (!directory.exists())
			directory.mkdir();
		return directory;
	}

	private final ReadWriteLock classifiersLock = new ReadWriteLock();

	public ClassifierManager getClassifierManager() throws SearchLibException {
		classifiersLock.r.lock();
		try {
			if (classifierManager != null)
				return classifierManager;
		} finally {
			classifiersLock.r.unlock();
		}
		classifiersLock.w.lock();
		try {
			if (classifierManager != null)
				return classifierManager;
			classifierManager = new ClassifierManager((Client) this,
					getClassifierDirectory());
			return classifierManager;
		} catch (XPathExpressionException e) {
			throw new SearchLibException(e);
		} catch (SearchLibException e) {
			throw new SearchLibException(e);
		} catch (ParserConfigurationException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			classifiersLock.w.unlock();
		}
	}

	private final ReadWriteLock crawlCacheLock = new ReadWriteLock();

	public final CrawlCacheManager getCrawlCacheManager()
			throws SearchLibException {
		crawlCacheLock.r.lock();
		try {
			if (crawlCacheManager != null)
				return crawlCacheManager;
		} finally {
			crawlCacheLock.r.unlock();
		}
		crawlCacheLock.w.lock();
		try {
			if (crawlCacheManager != null)
				return crawlCacheManager;
			crawlCacheManager = new CrawlCacheManager(getDirectory());
			return crawlCacheManager;
		} catch (InvalidPropertiesFormatException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} finally {
			crawlCacheLock.w.unlock();
		}
	}

	public File getLearnerDirectory() {
		File directory = new File(this.getDirectory(), "learners");
		if (!directory.exists())
			directory.mkdir();
		return directory;
	}

	private final ReadWriteLock learnersLock = new ReadWriteLock();

	public LearnerManager getLearnerManager() throws SearchLibException {
		learnersLock.r.lock();
		try {
			if (learnerManager != null)
				return learnerManager;
		} finally {
			learnersLock.r.unlock();
		}
		learnersLock.w.lock();
		try {
			if (learnerManager != null)
				return learnerManager;
			learnerManager = new LearnerManager((Client) this,
					getLearnerDirectory());
			return learnerManager;
		} catch (XPathExpressionException e) {
			throw new SearchLibException(e);
		} catch (SearchLibException e) {
			throw new SearchLibException(e);
		} catch (ParserConfigurationException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			learnersLock.w.unlock();
		}
	}

	private final ReadWriteLock authLock = new ReadWriteLock();

	public AuthManager getAuthManager() throws IOException {
		authLock.r.lock();
		try {
			if (authManager != null)
				return authManager;
		} finally {
			authLock.r.unlock();
		}
		authLock.w.lock();
		try {
			if (authManager != null)
				return authManager;
			authManager = new AuthManager(this, indexDir);
			return authManager;
		} catch (ParserConfigurationException e) {
			throw new IOException(e);
		} catch (SAXException e) {
			throw new IOException(e);
		} finally {
			authLock.w.unlock();
		}
	}

	private File getReportsDirectory() {
		File directory = new File(this.getDirectory(), "report");
		if (!directory.exists())
			directory.mkdir();
		return directory;
	}

	private final ReadWriteLock reportsLock = new ReadWriteLock();

	public ReportsManager getReportsManager() throws SearchLibException {
		reportsLock.r.lock();
		try {
			if (reportsManager != null)
				return reportsManager;
		} finally {
			reportsLock.r.unlock();
		}
		reportsLock.w.lock();
		try {
			if (reportsManager != null)
				return reportsManager;
			reportsManager = new ReportsManager(this, getReportsDirectory());
			return reportsManager;
		} finally {
			reportsLock.w.unlock();
		}
	}

	public void saveClassifier(Classifier classifier)
			throws SearchLibException, UnsupportedEncodingException {
		ConfigFileRotation cfr = configFiles.get(getClassifierDirectory(),
				URLEncoder.encode(classifier.getName(), "UTF-8") + ".xml");
		if (!replicationLock.rl.tryLock())
			throw new SearchLibException("Replication in process");
		try {
			classifiersLock.w.lock();
			try {
				XmlWriter xmlWriter = new XmlWriter(
						cfr.getTempPrintWriter("UTF-8"), "UTF-8");
				classifier.writeXml(xmlWriter);
				xmlWriter.endDocument();
				cfr.rotate();
			} catch (TransformerConfigurationException e) {
				throw new SearchLibException(e);
			} catch (SAXException e) {
				throw new SearchLibException(e);
			} catch (IOException e) {
				throw new SearchLibException(e);
			} finally {
				classifiersLock.w.unlock();
			}
		} finally {
			replicationLock.rl.unlock();
			cfr.abort();
		}

	}

	public void deleteClassifier(Classifier classifier)
			throws SearchLibException, IOException {
		ConfigFileRotation cfr = configFiles.get(getClassifierDirectory(),
				URLEncoder.encode(classifier.getName(), "UTF-8") + ".xml");
		if (!replicationLock.rl.tryLock())
			throw new SearchLibException("Replication in process");
		try {
			classifiersLock.w.lock();
			try {
				cfr.delete();
			} finally {
				classifiersLock.w.unlock();
			}
		} finally {
			replicationLock.rl.unlock();
			cfr.abort();
		}
	}

	public void saveLearner(Learner learner) throws SearchLibException,
			UnsupportedEncodingException {
		ConfigFileRotation cfr = configFiles.get(getLearnerDirectory(),
				URLEncoder.encode(learner.getName(), "UTF-8") + ".xml");
		if (!replicationLock.rl.tryLock())
			throw new SearchLibException("Replication in process");
		try {
			learnersLock.w.lock();
			try {
				XmlWriter xmlWriter = new XmlWriter(
						cfr.getTempPrintWriter("UTF-8"), "UTF-8");
				learner.writeXml(xmlWriter);
				xmlWriter.endDocument();
				cfr.rotate();
			} catch (TransformerConfigurationException e) {
				throw new SearchLibException(e);
			} catch (SAXException e) {
				throw new SearchLibException(e);
			} catch (IOException e) {
				throw new SearchLibException(e);
			} finally {
				learnersLock.w.unlock();
			}
		} finally {
			replicationLock.rl.unlock();
			cfr.abort();
		}

	}

	public void deleteLearner(Learner learner) throws SearchLibException,
			IOException {
		ConfigFileRotation cfr = configFiles.get(getLearnerDirectory(),
				URLEncoder.encode(learner.getName(), "UTF-8") + ".xml");
		if (!replicationLock.rl.tryLock())
			throw new SearchLibException("Replication in process");
		try {
			learnersLock.w.lock();
			try {
				cfr.delete();
			} finally {
				learnersLock.w.unlock();
			}
		} finally {
			replicationLock.rl.unlock();
			cfr.abort();
		}
	}

	private final ReadWriteLock databaseLock = new ReadWriteLock();

	public DatabaseCrawlList getDatabaseCrawlList() throws SearchLibException {
		databaseLock.r.lock();
		try {
			if (databaseCrawlList != null)
				return databaseCrawlList;
		} finally {
			databaseLock.r.unlock();
		}
		databaseLock.w.lock();
		try {
			if (databaseCrawlList != null)
				return databaseCrawlList;
			databaseCrawlList = DatabaseCrawlList.fromXml(
					getDatabaseCrawlMaster(), getDatabasePropertyManager(),
					new File(indexDir, "databaseCrawlList.xml"));
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
			databaseLock.w.unlock();
		}
	}

	public void saveDatabaseCrawlList() throws SearchLibException {
		ConfigFileRotation cfr = configFiles.get(indexDir,
				"databaseCrawlList.xml");
		if (!replicationLock.rl.tryLock())
			throw new SearchLibException("Replication in process");
		try {
			databaseLock.w.lock();
			try {
				XmlWriter xmlWriter = new XmlWriter(
						cfr.getTempPrintWriter("UTF-8"), "UTF-8");
				getDatabaseCrawlList().writeXml(xmlWriter);
				xmlWriter.endDocument();
				cfr.rotate();
			} finally {
				databaseLock.w.unlock();
			}
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (TransformerConfigurationException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} finally {
			replicationLock.rl.unlock();
			cfr.abort();
		}
	}

	private final ReadWriteLock restCrawlLock = new ReadWriteLock();

	public RestCrawlList getRestCrawlList() throws SearchLibException {
		restCrawlLock.r.lock();
		try {
			if (restCrawlList != null)
				return restCrawlList;
		} finally {
			restCrawlLock.r.unlock();
		}
		restCrawlLock.w.lock();
		try {
			if (restCrawlList != null)
				return restCrawlList;
			restCrawlList = RestCrawlList.fromXml(getRestCrawlMaster(),
					new File(indexDir, "restCrawlList.xml"));
			return restCrawlList;
		} catch (ParserConfigurationException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (XPathExpressionException e) {
			throw new SearchLibException(e);
		} finally {
			restCrawlLock.w.unlock();
		}
	}

	public void saveRestCrawlList() throws SearchLibException {
		ConfigFileRotation cfr = configFiles.get(indexDir, "restCrawlList.xml");
		if (!replicationLock.rl.tryLock())
			throw new SearchLibException("Replication in process");
		try {
			restCrawlLock.w.lock();
			try {
				XmlWriter xmlWriter = new XmlWriter(
						cfr.getTempPrintWriter("UTF-8"), "UTF-8");
				getRestCrawlList().writeXml(xmlWriter);
				xmlWriter.endDocument();
				cfr.rotate();
			} finally {
				restCrawlLock.w.unlock();
			}
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (TransformerConfigurationException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} finally {
			replicationLock.rl.unlock();
			cfr.abort();
		}
	}

	private final ReadWriteLock mailboxCrawlLock = new ReadWriteLock();

	public MailboxCrawlList getMailboxCrawlList() throws SearchLibException {
		mailboxCrawlLock.r.lock();
		try {
			if (mailboxCrawlList != null)
				return mailboxCrawlList;
		} finally {
			mailboxCrawlLock.r.unlock();
		}
		mailboxCrawlLock.w.lock();
		try {
			if (mailboxCrawlList != null)
				return mailboxCrawlList;
			mailboxCrawlList = MailboxCrawlList.fromXml(
					getMailboxCrawlMaster(), new File(indexDir,
							"mailboxCrawlList.xml"));
			return mailboxCrawlList;
		} catch (ParserConfigurationException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (XPathExpressionException e) {
			throw new SearchLibException(e);
		} finally {
			mailboxCrawlLock.w.unlock();
		}
	}

	public void saveMailboxCrawlList() throws SearchLibException {
		ConfigFileRotation cfr = configFiles.get(indexDir,
				"mailboxCrawlList.xml");
		if (!replicationLock.rl.tryLock())
			throw new SearchLibException("Replication in process");
		try {
			mailboxCrawlLock.w.lock();
			try {
				XmlWriter xmlWriter = new XmlWriter(
						cfr.getTempPrintWriter("UTF-8"), "UTF-8");
				getMailboxCrawlList().writeXml(xmlWriter);
				xmlWriter.endDocument();
				cfr.rotate();
			} finally {
				mailboxCrawlLock.w.unlock();
			}
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (TransformerConfigurationException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} finally {
			replicationLock.rl.unlock();
			cfr.abort();
		}
	}

	protected WebCrawlMaster getNewWebCrawlMaster() throws SearchLibException {
		return new WebCrawlMaster(this);
	}

	private final ReadWriteLock webCrawlLock = new ReadWriteLock();

	public WebCrawlMaster getWebCrawlMaster() throws SearchLibException {
		webCrawlLock.r.lock();
		try {
			if (webCrawlMaster != null)
				return webCrawlMaster;
		} finally {
			webCrawlLock.r.unlock();
		}
		webCrawlLock.w.lock();
		try {
			if (webCrawlMaster != null)
				return webCrawlMaster;
			return webCrawlMaster = getNewWebCrawlMaster();
		} finally {
			webCrawlLock.w.unlock();
		}
	}

	private final ReadWriteLock fileCrawlLock = new ReadWriteLock();

	public CrawlFileMaster getFileCrawlMaster() throws SearchLibException {
		fileCrawlLock.r.lock();
		try {
			if (fileCrawlMaster != null)
				return fileCrawlMaster;
		} finally {
			fileCrawlLock.r.unlock();
		}
		fileCrawlLock.w.lock();
		try {
			if (fileCrawlMaster != null)
				return fileCrawlMaster;
			fileCrawlMaster = new CrawlFileMaster(this);
			return fileCrawlMaster;
		} finally {
			fileCrawlLock.w.unlock();
		}
	}

	public DatabaseCrawlMaster getDatabaseCrawlMaster()
			throws SearchLibException {
		databaseLock.r.lock();
		try {
			if (databaseCrawlMaster != null)
				return databaseCrawlMaster;
		} finally {
			databaseLock.r.unlock();
		}
		databaseLock.w.lock();
		try {
			if (databaseCrawlMaster != null)
				return databaseCrawlMaster;
			return databaseCrawlMaster = new DatabaseCrawlMaster(this);
		} finally {
			databaseLock.w.unlock();
		}
	}

	public RestCrawlMaster getRestCrawlMaster() throws SearchLibException {
		restCrawlLock.r.lock();
		try {
			if (restCrawlMaster != null)
				return restCrawlMaster;
		} finally {
			restCrawlLock.r.unlock();
		}
		restCrawlLock.w.lock();
		try {
			if (restCrawlMaster != null)
				return restCrawlMaster;
			return restCrawlMaster = new RestCrawlMaster(this);
		} finally {
			restCrawlLock.w.unlock();
		}
	}

	public MailboxCrawlMaster getMailboxCrawlMaster() throws SearchLibException {
		mailboxCrawlLock.r.lock();
		try {
			if (mailboxCrawlMaster != null)
				return mailboxCrawlMaster;
		} finally {
			mailboxCrawlLock.r.unlock();
		}
		mailboxCrawlLock.w.lock();
		try {
			if (mailboxCrawlMaster != null)
				return mailboxCrawlMaster;
			return mailboxCrawlMaster = new MailboxCrawlMaster(this);
		} finally {
			mailboxCrawlLock.w.unlock();
		}
	}

	public ReplicationMaster getReplicationMaster() {
		replicationsLock.r.lock();
		try {
			if (replicationMaster != null)
				return replicationMaster;
		} finally {
			replicationsLock.r.unlock();
		}
		replicationsLock.w.lock();
		try {
			if (replicationMaster != null)
				return replicationMaster;
			return replicationMaster = new ReplicationMaster(this);
		} finally {
			replicationsLock.w.unlock();
		}
	}

	protected ParserSelector getNewParserSelector(XPathParser xpp, Node node)
			throws XPathExpressionException, DOMException, IOException,
			SearchLibException {
		return new ParserSelector(this, xpp, node);
	}

	final public ParserSelector getParserSelector() throws SearchLibException {
		parsersLock.r.lock();
		try {
			if (parserSelector != null)
				return parserSelector;
		} finally {
			parsersLock.r.unlock();
		}
		parsersLock.w.lock();
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
			parsersLock.w.unlock();
		}
	}

	public TaskEnum getJobTaskEnum() {
		jobsLock.r.lock();
		try {
			if (taskEnum != null)
				return taskEnum;
		} finally {
			jobsLock.r.unlock();
		}
		jobsLock.w.lock();
		try {
			taskEnum = new TaskEnum();
			return taskEnum;
		} finally {
			jobsLock.w.unlock();
		}
	}

	public JobList getJobList() throws SearchLibException {
		jobsLock.r.lock();
		try {
			if (jobList != null)
				return jobList;
		} finally {
			jobsLock.r.unlock();
		}
		jobsLock.w.lock();
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
			jobsLock.w.unlock();
		}
	}

	public ReplicationList getReplicationList() throws SearchLibException {
		replicationsLock.r.lock();
		try {
			if (replicationList != null)
				return replicationList;
		} finally {
			replicationsLock.r.unlock();
		}
		replicationsLock.w.lock();
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
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} finally {
			replicationsLock.w.unlock();
		}
	}

	private final ReadWriteLock screenshotLock = new ReadWriteLock();

	public ScreenshotManager getScreenshotManager() throws SearchLibException {
		screenshotLock.r.lock();
		try {
			if (screenshotManager != null)
				return screenshotManager;
		} finally {
			screenshotLock.r.unlock();
		}
		screenshotLock.w.lock();
		try {
			if (screenshotManager != null)
				return screenshotManager;
			return screenshotManager = new ScreenshotManager(this);
		} finally {
			screenshotLock.w.unlock();
		}
	}

	private File getRendererDirectory() {
		File directory = new File(getDirectory(), "renderers");
		if (!directory.exists())
			directory.mkdir();
		return directory;
	}

	private final ReadWriteLock apiLock = new ReadWriteLock();

	public ApiManager getApiManager() throws SearchLibException,
			TransformerConfigurationException {
		apiLock.r.lock();
		try {
			if (apiManager != null)
				return apiManager;
		} finally {
			apiLock.r.unlock();
		}
		apiLock.w.lock();
		try {
			if (apiManager != null)
				return apiManager;
			return apiManager = new ApiManager(indexDir, "api.xml");
		} catch (XPathExpressionException e) {
			throw new SearchLibException(e);
		} catch (ParserConfigurationException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			apiLock.w.unlock();
		}
	}

	private final ReadWriteLock rendererLock = new ReadWriteLock();

	public RendererManager getRendererManager() throws SearchLibException {
		rendererLock.r.lock();
		try {
			if (rendererManager != null)
				return rendererManager;
		} finally {
			rendererLock.r.unlock();
		}
		rendererLock.w.lock();
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
			rendererLock.w.unlock();
		}
	}

	public void save(Renderer renderer) throws SearchLibException,
			UnsupportedEncodingException {
		ConfigFileRotation cfr = configFiles.get(getRendererDirectory(),
				URLEncoder.encode(renderer.getName(), "UTF-8") + ".xml");
		if (!replicationLock.rl.tryLock())
			throw new SearchLibException("Replication in process");
		try {
			rendererLock.w.lock();
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
				rendererLock.w.unlock();
			}
		} finally {
			replicationLock.rl.unlock();
			cfr.abort();
		}

	}

	public void delete(Renderer renderer) throws SearchLibException {
		if (!replicationLock.rl.tryLock())
			throw new SearchLibException("Replication in process");
		ConfigFileRotation cfr = null;
		try {
			rendererLock.w.lock();
			try {
				cfr = configFiles
						.get(getRendererDirectory(),
								URLEncoder.encode(renderer.getName(), "UTF-8")
										+ ".xml");
				cfr.delete();
			} catch (IOException e) {
				throw new SearchLibException(e);
			} finally {
				rendererLock.w.unlock();
			}
		} finally {
			replicationLock.rl.unlock();
			if (cfr != null)
				cfr.abort();
		}
	}

	public String getIndexName() {
		return getDirectory().getName();
	}

	public IndexAbstract getIndexAbstract() {
		return index;
	}

	public IndexAbstract getIndex() {
		return index;
	}

	private final ReadWriteLock pluginLock = new ReadWriteLock();

	public IndexPluginTemplateList getIndexPluginTemplateList()
			throws SearchLibException {
		pluginLock.r.lock();
		try {
			if (indexPluginTemplateList != null)
				return indexPluginTemplateList;
		} finally {
			pluginLock.r.unlock();
		}
		pluginLock.w.lock();
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
			pluginLock.w.unlock();
		}

	}

	protected File getStatStorage() {
		return new File(getDirectory(), "statstore");
	}

	private final ReadWriteLock statisticsLock = new ReadWriteLock();

	public StatisticsList getStatisticsList() throws SearchLibException {
		statisticsLock.r.lock();
		try {
			if (statisticsList != null)
				return statisticsList;
		} finally {
			statisticsLock.r.unlock();
		}
		statisticsLock.w.lock();
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
			statisticsLock.w.unlock();
		}
	}

	public LogReportManager getLogReportManager() throws SearchLibException {
		reportsLock.r.lock();
		try {
			if (logReportManager != null)
				return logReportManager;
		} finally {
			reportsLock.r.unlock();
		}
		reportsLock.w.lock();
		try {
			if (logReportManager != null)
				return logReportManager;
			return logReportManager = new LogReportManager(getIndexName());
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			reportsLock.w.unlock();
		}
	}

	private final ReadWriteLock autocompletionLock = new ReadWriteLock();

	public AutoCompletionManager getAutoCompletionManager()
			throws SearchLibException {
		autocompletionLock.r.lock();
		try {
			if (autoCompletionManager != null)
				return autoCompletionManager;
		} finally {
			autocompletionLock.r.unlock();
		}
		autocompletionLock.w.lock();
		try {
			if (autoCompletionManager != null)
				return autoCompletionManager;
			return autoCompletionManager = new AutoCompletionManager(this);
		} catch (InvalidPropertiesFormatException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			autocompletionLock.w.unlock();
		}
	}

	public AbstractRequest getNewRequest(String requestName)
			throws SearchLibException {
		if (requestName == null)
			throw new SearchLibException("No request name given");
		AbstractRequest request = getRequestMap().get(requestName);
		if (request == null)
			throw new SearchLibException("No request found: " + requestName);
		try {
			return RequestTypeEnum.getNewCopy(request);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		}
	}

	public final ReadWriteLock requestsLock = new ReadWriteLock();

	public RequestMap getRequestMap() throws SearchLibException {
		requestsLock.r.lock();
		try {
			if (requests != null)
				return requests;
		} finally {
			requestsLock.r.unlock();
		}
		requestsLock.w.lock();
		try {
			if (requests != null)
				return requests;
			File requestFile = new File(indexDir, "requests.xml");
			if (requestFile.exists()) {
				XPathParser xpp = new XPathParser(requestFile);
				requests = RequestMap.fromXmlConfig(this, xpp,
						xpp.getNode("/requests"));
			} else
				requests = RequestMap.fromXmlConfig(this, xppConfig,
						xppConfig.getNode("/configuration/requests"));
			return requests;
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
			requestsLock.w.unlock();
		}
	}

	protected UrlManager getNewUrlManagerInstance()
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		return new UrlManager();
	}

	public final ReadWriteLock urlLock = new ReadWriteLock();

	final public UrlManager getUrlManager() throws SearchLibException {
		urlLock.r.lock();
		try {
			if (urlManager != null)
				return urlManager;
		} finally {
			urlLock.r.unlock();
		}
		urlLock.w.lock();
		try {
			if (urlManager != null)
				return urlManager;
			UrlManager ua = getNewUrlManagerInstance();
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
			urlLock.w.unlock();
		}
	}

	public final ReadWriteLock scriptLock = new ReadWriteLock();

	public ScriptManager getScriptManager() throws SearchLibException {
		scriptLock.r.lock();
		try {
			if (scriptManager != null)
				return scriptManager;
		} finally {
			scriptLock.r.unlock();
		}
		scriptLock.w.lock();
		try {
			if (scriptManager != null)
				return scriptManager;
			return scriptManager = new ScriptManager(this, new File(indexDir,
					"scripts"));
		} finally {
			scriptLock.w.unlock();
		}
	}

	public final ReadWriteLock stopWordsLock = new ReadWriteLock();

	public StopWordsManager getStopWordsManager() throws SearchLibException {
		stopWordsLock.r.lock();
		try {
			if (stopWordsManager != null)
				return stopWordsManager;
		} finally {
			stopWordsLock.r.unlock();
		}
		stopWordsLock.w.lock();
		try {
			if (stopWordsManager != null)
				return stopWordsManager;
			return stopWordsManager = new StopWordsManager(this, new File(
					indexDir, "stopwords"));
		} finally {
			stopWordsLock.w.unlock();
		}
	}

	public final ReadWriteLock synonymsLock = new ReadWriteLock();

	public SynonymsManager getSynonymsManager() throws SearchLibException {
		synonymsLock.r.lock();
		try {
			if (synonymsManager != null)
				return synonymsManager;
		} finally {
			synonymsLock.r.unlock();
		}
		synonymsLock.w.lock();
		try {
			if (synonymsManager != null)
				return synonymsManager;
			return synonymsManager = new SynonymsManager(this, new File(
					indexDir, "synonyms"));
		} finally {
			synonymsLock.w.unlock();
		}
	}

	public final ReadWriteLock inclusionLock = new ReadWriteLock();

	public PatternManager getInclusionPatternManager()
			throws SearchLibException {
		inclusionLock.r.lock();
		try {
			if (inclusionPatternManager != null)
				return inclusionPatternManager;
		} finally {
			inclusionLock.r.unlock();
		}
		inclusionLock.w.lock();
		try {
			if (inclusionPatternManager != null)
				return inclusionPatternManager;
			return inclusionPatternManager = new PatternManager(indexDir,
					"patterns.xml");
		} finally {
			inclusionLock.w.unlock();
		}
	}

	public final ReadWriteLock exclusionLock = new ReadWriteLock();

	public PatternManager getExclusionPatternManager()
			throws SearchLibException {
		exclusionLock.r.lock();
		try {
			if (exclusionPatternManager != null)
				return exclusionPatternManager;
		} finally {
			exclusionLock.r.unlock();
		}
		exclusionLock.w.lock();
		try {
			if (exclusionPatternManager != null)
				return exclusionPatternManager;
			return exclusionPatternManager = new PatternManager(indexDir,
					"patterns_exclusion.xml");
		} finally {
			exclusionLock.w.unlock();
		}
	}

	public final ReadWriteLock credentialLock = new ReadWriteLock();

	public CredentialManager getWebCredentialManager()
			throws SearchLibException {
		credentialLock.r.lock();
		try {
			if (webCredentialManager != null)
				return webCredentialManager;
		} finally {
			credentialLock.r.unlock();
		}
		credentialLock.w.lock();
		try {
			if (webCredentialManager != null)
				return webCredentialManager;
			return webCredentialManager = new CredentialManager(indexDir,
					"web_credentials.xml");
		} finally {
			credentialLock.w.unlock();
		}
	}

	public final ReadWriteLock cookieLock = new ReadWriteLock();

	public CookieManager getWebCookieManager() throws SearchLibException {
		cookieLock.r.lock();
		try {
			if (webCookieManager != null)
				return webCookieManager;
		} finally {
			cookieLock.r.unlock();
		}
		cookieLock.w.lock();
		try {
			if (webCookieManager != null)
				return webCookieManager;
			return webCookieManager = new CookieManager(indexDir,
					"web_cookies.xml");
		} finally {
			cookieLock.w.unlock();
		}
	}

	public final ReadWriteLock headerLock = new ReadWriteLock();

	public HeaderManager getWebHeaderManager() throws SearchLibException {
		headerLock.r.lock();
		try {
			if (webHeaderManager != null)
				return webHeaderManager;
		} finally {
			headerLock.r.unlock();
		}
		headerLock.w.lock();
		try {
			if (webHeaderManager != null)
				return webHeaderManager;
			return webHeaderManager = new HeaderManager(indexDir,
					"web_headers.xml");
		} finally {
			headerLock.w.unlock();
		}
	}

	public final ReadWriteLock siteMapLock = new ReadWriteLock();

	public SiteMapList getSiteMapList() throws SearchLibException {
		siteMapLock.r.lock();
		try {
			if (siteMapList != null)
				return siteMapList;
		} finally {
			siteMapLock.r.unlock();
		}
		siteMapLock.w.lock();
		try {
			if (siteMapList != null)
				return siteMapList;
			return siteMapList = new SiteMapList(indexDir,
					"webcrawler-sitemap.xml");
		} finally {
			siteMapLock.w.unlock();
		}
	}

	public void saveSiteMapList() throws SearchLibException {
		ConfigFileRotation cfr = configFiles.get(indexDir,
				"webcrawler-sitemap.xml");
		if (!replicationLock.rl.tryLock())
			throw new SearchLibException("Replication in process");
		try {
			siteMapLock.w.lock();
			try {
				XmlWriter xmlWriter = new XmlWriter(
						cfr.getTempPrintWriter("UTF-8"), "UTF-8");
				getSiteMapList().writeXml(xmlWriter);
				xmlWriter.endDocument();
				cfr.rotate();
			} finally {
				siteMapLock.w.unlock();
			}
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (TransformerConfigurationException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} finally {
			replicationLock.rl.unlock();
			cfr.abort();
		}
	}

	public final ReadWriteLock urlFilterLock = new ReadWriteLock();

	public UrlFilterList getUrlFilterList() throws SearchLibException {
		urlFilterLock.r.lock();
		try {
			if (urlFilterList != null)
				return urlFilterList;
		} finally {
			urlFilterLock.r.unlock();
		}
		urlFilterLock.w.lock();
		try {
			if (urlFilterList != null)
				return urlFilterList;
			return urlFilterList = new UrlFilterList(indexDir,
					"webcrawler-urlfilter.xml");
		} finally {
			urlFilterLock.w.unlock();
		}
	}

	public void saveUrlFilterList() throws SearchLibException {
		ConfigFileRotation cfr = configFiles.get(indexDir,
				"webcrawler-urlfilter.xml");
		if (!replicationLock.rl.tryLock())
			throw new SearchLibException("Replication in process");
		try {
			urlFilterLock.w.lock();
			try {
				XmlWriter xmlWriter = new XmlWriter(
						cfr.getTempPrintWriter("UTF-8"), "UTF-8");
				getUrlFilterList().writeXml(xmlWriter);
				xmlWriter.endDocument();
				cfr.rotate();
			} finally {
				urlFilterLock.w.unlock();
			}
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (TransformerConfigurationException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} finally {
			replicationLock.rl.unlock();
			cfr.abort();
		}
	}

	public void push(ReplicationThread replicationThread)
			throws SearchLibException {
		if (!replicationLock.rl.tryLock())
			throw new SearchLibException("Replication in process");
		try {
			replicationThread.push();
		} finally {
			replicationLock.rl.unlock();
		}
	}

	public FilePathManager getFilePathManager() throws SearchLibException {
		fileCrawlLock.r.lock();
		try {
			if (filePatternManager != null)
				return filePatternManager;
		} finally {
			fileCrawlLock.r.unlock();
		}
		fileCrawlLock.w.lock();
		try {
			if (filePatternManager != null)
				return filePatternManager;
			return filePatternManager = new FilePathManager(this, indexDir);
		} finally {
			fileCrawlLock.w.unlock();
		}
	}

	protected FileManager getNewFileManagerInstance()
			throws FileNotFoundException, SearchLibException,
			URISyntaxException {
		return new FileManager((Client) this, indexDir);
	}

	final public FileManager getFileManager() throws SearchLibException {
		fileCrawlLock.r.lock();
		try {
			if (fileManager != null)
				return fileManager;
		} finally {
			fileCrawlLock.r.unlock();
		}
		fileCrawlLock.w.lock();
		try {
			if (fileManager != null)
				return fileManager;
			return fileManager = getNewFileManagerInstance();
		} catch (FileNotFoundException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} finally {
			fileCrawlLock.w.unlock();
		}
	}

	public DatabasePropertyManager getDatabasePropertyManager()
			throws SearchLibException {
		databaseLock.r.lock();
		try {
			if (databasePropertyManager != null)
				return databasePropertyManager;
		} finally {
			databaseLock.r.unlock();
		}
		databaseLock.w.lock();
		try {
			if (databasePropertyManager != null)
				return databasePropertyManager;
			return databasePropertyManager = new DatabasePropertyManager(
					new File(indexDir, "dbcrawler-properties.xml"));
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			databaseLock.w.unlock();
		}
	}

	public WebPropertyManager getWebPropertyManager() throws SearchLibException {
		webCrawlLock.r.lock();
		try {
			if (webPropertyManager != null)
				return webPropertyManager;
		} finally {
			webCrawlLock.r.unlock();
		}
		webCrawlLock.w.lock();
		try {
			if (webPropertyManager != null)
				return webPropertyManager;
			return webPropertyManager = new WebPropertyManager(new File(
					indexDir, "webcrawler-properties.xml"));
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			webCrawlLock.w.unlock();
		}
	}

	public FilePropertyManager getFilePropertyManager()
			throws SearchLibException {
		fileCrawlLock.r.lock();
		try {
			if (filePropertyManager != null)
				return filePropertyManager;
		} finally {
			fileCrawlLock.r.unlock();
		}
		fileCrawlLock.w.lock();
		try {
			if (filePropertyManager != null)
				return filePropertyManager;
			return filePropertyManager = new FilePropertyManager(new File(
					indexDir, "filecrawler-properties.xml"));
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			fileCrawlLock.w.unlock();
		}
	}

	final public AbstractRequest getNewRequest(
			final ServletTransaction transaction) throws ParseException,
			SyntaxError, SearchLibException {

		String requestName = transaction.getParameterString("qt");
		AbstractRequest request = null;
		if (requestName != null)
			request = getNewRequest(requestName);
		if (request == null)
			request = new SearchPatternRequest(this);
		request.setFromServlet(transaction, "");
		return request;
	}

	private ReadWriteLock robotsLock = new ReadWriteLock();

	public RobotsTxtCache getRobotsTxtCache() throws SearchLibException,
			ClassNotFoundException {
		robotsLock.r.lock();
		try {
			if (robotsTxtCache != null)
				return robotsTxtCache;
		} finally {
			robotsLock.r.unlock();
		}
		robotsLock.w.lock();
		try {
			if (robotsTxtCache != null)
				return robotsTxtCache;
			return robotsTxtCache = new RobotsTxtCache();
		} finally {
			robotsLock.w.unlock();
		}
	}

	public FieldMap getWebCrawlerFieldMap() throws SearchLibException {
		webCrawlLock.r.lock();
		try {
			if (webCrawlerFieldMap != null)
				return webCrawlerFieldMap;

		} finally {
			webCrawlLock.r.unlock();
		}
		webCrawlLock.w.lock();
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
			webCrawlLock.w.unlock();
		}
	}

	public FieldMap getFileCrawlerFieldMap() throws SearchLibException {
		fileCrawlLock.r.lock();
		try {
			if (fileCrawlerFieldMap != null)
				return fileCrawlerFieldMap;
		} finally {
			fileCrawlLock.r.unlock();
		}
		fileCrawlLock.w.lock();
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
			fileCrawlLock.w.unlock();
		}
	}

	private void prepareClose(boolean waitForEnd) throws SearchLibException {
		boolean isFileCrawlMaster;
		boolean isWebCrawlMaster;
		boolean isDatabaseCrawlMaster;
		boolean isUrlManager;
		boolean isAutoCompletionManager;
		fileCrawlLock.r.lock();
		try {
			isFileCrawlMaster = fileCrawlMaster != null;
		} finally {
			fileCrawlLock.r.unlock();
		}
		webCrawlLock.r.lock();
		try {
			isWebCrawlMaster = webCrawlMaster != null;
		} finally {
			webCrawlLock.r.unlock();
		}
		databaseLock.r.lock();
		try {
			isDatabaseCrawlMaster = databaseCrawlMaster != null;
		} finally {
			databaseLock.r.unlock();
		}
		urlLock.r.lock();
		try {
			isUrlManager = urlManager != null;
		} finally {
			urlLock.r.unlock();
		}
		autocompletionLock.r.lock();
		try {
			isAutoCompletionManager = autoCompletionManager != null;
		} finally {
			autocompletionLock.r.unlock();
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
		if (isAutoCompletionManager)
			autoCompletionManager.close();
	}

	private void closeQuiet() {
		try {
			getIndexAbstract().close();
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
		getThreadPool().shutdown();
		try {
			prepareClose(true);
		} catch (SearchLibException e) {
			Logging.warn(e.getMessage(), e);
		}
		replicationLock.rl.lock();
		try {
			closeQuiet();
			if (trashDir != null)
				indexDir.renameTo(trashDir);
		} finally {
			replicationLock.rl.unlock();
		}
		synchronized (this) {
			isClosed = true;
		}
	}

	public boolean isClosed() {
		synchronized (this) {
			return isClosed;
		}
	}

	public void close() {
		close(null);
	}

	public void trash(File trashDir) throws SearchLibException, NamingException {
		close(trashDir);
	}

	public void delete() throws IOException {
		if (!replicationLock.rl.tryLock())
			throw new IOException("Replication in process");
		try {
			FileUtils.deleteDirectory(getDirectory());
		} finally {
			replicationLock.rl.unlock();
		}
	}

}