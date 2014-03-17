/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.lucene.search.BooleanQuery;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.cluster.VersionFile;
import com.jaeksoft.searchlib.crawler.web.browser.BrowserDriverEnum;
import com.jaeksoft.searchlib.util.FileUtils;
import com.jaeksoft.searchlib.util.Sequence;
import com.jaeksoft.searchlib.util.properties.PropertyItem;
import com.jaeksoft.searchlib.util.properties.PropertyItemListener;
import com.jaeksoft.searchlib.util.properties.PropertyManager;
import com.jaeksoft.searchlib.web.StartStopListener;

public class ClientFactory implements PropertyItemListener {

	public static ClientFactory INSTANCE = null;

	public final VersionFile versionFile;

	public final InstanceProperties properties;

	private final Sequence globalSequence;

	private PropertyItem<Integer> booleanQueryMaxClauseCount;

	private PropertyItem<String> defaultWebBrowserDriver;

	private PropertyItem<Boolean> soapActive;

	private PropertyItem<Boolean> externalParser;

	private PropertyItem<Boolean> logFullTrace;

	private PropertyItem<String> smtpHostname;

	private PropertyItem<Integer> smtpPort;

	private PropertyItem<Boolean> smtpUseSsl;

	private PropertyItem<Boolean> smtpUseTls;

	private PropertyItem<String> smtpSenderEmail;

	private PropertyItem<String> smtpSenderName;

	private PropertyItem<String> smtpUsername;

	private PropertyItem<String> smtpPassword;

	private PropertyItem<Integer> schedulerThreadPoolSize;

	private PropertyItem<Integer> clusterInstanceId;

	private PropertyManager advancedProperties;

	public ClientFactory() throws SearchLibException {
		try {
			versionFile = new VersionFile(
					StartStopListener.OPENSEARCHSERVER_DATA_FILE);
			globalSequence = new Sequence(new File(
					StartStopListener.OPENSEARCHSERVER_DATA_FILE,
					"globalSequence.txt"), 36);
			properties = new InstanceProperties(new File(
					StartStopListener.OPENSEARCHSERVER_DATA_FILE,
					"properties.xml"));
			File advPropFile = new File(
					StartStopListener.OPENSEARCHSERVER_DATA_FILE,
					"advanced.xml");
			advancedProperties = new PropertyManager(advPropFile);
			booleanQueryMaxClauseCount = advancedProperties.newIntegerProperty(
					"booleanQueryMaxClauseCount", 1024, null, null);
			hasBeenSet(booleanQueryMaxClauseCount);
			booleanQueryMaxClauseCount.addListener(this);
			defaultWebBrowserDriver = advancedProperties.newStringProperty(
					"defaultWebBrowserDriver",
					BrowserDriverEnum.FIREFOX.getName());
			defaultWebBrowserDriver.addListener(this);
			soapActive = advancedProperties.newBooleanProperty("soapActive",
					false);
			soapActive.addListener(this);
			externalParser = advancedProperties.newBooleanProperty(
					"externalParser", false);
			externalParser.addListener(this);
			logFullTrace = advancedProperties.newBooleanProperty(
					"logFullTrace", false);
			hasBeenSet(logFullTrace);
			logFullTrace.addListener(this);
			smtpHostname = advancedProperties.newStringProperty("smtpHostname",
					"localhost");
			smtpHostname.addListener(this);
			smtpPort = advancedProperties.newIntegerProperty("smtpPort", 25, 1,
					65536);
			smtpPort.addListener(this);
			smtpUseSsl = advancedProperties.newBooleanProperty("smtpUseSsl",
					false);
			smtpUseSsl.addListener(this);
			smtpUseTls = advancedProperties.newBooleanProperty("smtpUseTls",
					false);
			smtpUseTls.addListener(this);
			smtpSenderEmail = advancedProperties.newStringProperty(
					"smtpSenderEmail", "no-reply@open-search-server.com");
			smtpSenderEmail.addListener(this);
			smtpSenderName = advancedProperties.newStringProperty(
					"smtpSenderName", "");
			smtpSenderName.addListener(this);
			smtpUsername = advancedProperties.newStringProperty("smtpUsername",
					"");
			smtpUsername.addListener(this);
			smtpPassword = advancedProperties.newStringProperty("smtpPassword",
					"");
			smtpPassword.addListener(this);
			schedulerThreadPoolSize = advancedProperties.newIntegerProperty(
					"schedulerThreadPoolSize", 20, 1, 200);
			schedulerThreadPoolSize.addListener(this);
			clusterInstanceId = advancedProperties.newIntegerProperty(
					"clusterInstanceId", null, 0, 131072);
			clusterInstanceId.addListener(this);
			if (clusterInstanceId.getValue() == null)
				clusterInstanceId
						.setValue((int) System.currentTimeMillis() % 131072);
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
		}
	}

	protected Client newClient(File initFileOrDir,
			boolean createIndexIfNotExists, boolean disableCrawler)
			throws SearchLibException {
		return new Client(initFileOrDir, createIndexIfNotExists, disableCrawler);
	}

	final public Client getNewClient(File initFileOrDir,
			boolean createIndexIfNotExists, boolean disableCrawler)
			throws SearchLibException {
		try {
			if (!FileUtils
					.isSubDirectory(
							StartStopListener.OPENSEARCHSERVER_DATA_FILE,
							initFileOrDir))
				throw new SearchLibException("Security alert: " + initFileOrDir
						+ " is outside OPENSEARCHSERVER_DATA ("
						+ StartStopListener.OPENSEARCHSERVER_DATA_FILE + ")");
		} catch (IOException e) {
			throw new SearchLibException(e);
		}
		return newClient(initFileOrDir, createIndexIfNotExists, disableCrawler);
	}

	public static void setInstance(ClientFactory cf) {
		INSTANCE = cf;
	}

	public PropertyItem<Integer> getBooleanQueryMaxClauseCount() {
		return booleanQueryMaxClauseCount;
	}

	public PropertyItem<String> getDefaultWebBrowserDriver() {
		return defaultWebBrowserDriver;
	}

	public PropertyItem<Boolean> getSoapActive() {
		return soapActive;
	}

	public PropertyItem<Boolean> getExternalParser() {
		return externalParser;
	}

	public PropertyItem<Boolean> getLogFullTrace() {
		return logFullTrace;
	}

	public PropertyItem<String> getSmtpHostname() {
		return smtpHostname;
	}

	public PropertyItem<Integer> getSmtpPort() {
		return smtpPort;
	}

	public PropertyItem<Boolean> getSmtpUseSsl() {
		return smtpUseSsl;
	}

	public PropertyItem<Boolean> getSmtpUseTls() {
		return smtpUseTls;
	}

	public PropertyItem<String> getSmtpSenderEmail() {
		return smtpSenderEmail;
	}

	public PropertyItem<String> getSmtpSenderName() {
		return smtpSenderName;
	}

	public PropertyItem<String> getSmtpUsername() {
		return smtpUsername;
	}

	public PropertyItem<String> getSmtpPassword() {
		return smtpPassword;
	}

	public PropertyItem<Integer> getSchedulerThreadPoolSize() {
		return schedulerThreadPoolSize;
	}

	public PropertyItem<Integer> getClusterInstanceId() {
		return clusterInstanceId;
	}

	@Override
	public void hasBeenSet(PropertyItem<?> prop) throws SearchLibException {
		if (prop == booleanQueryMaxClauseCount)
			BooleanQuery.setMaxClauseCount(booleanQueryMaxClauseCount
					.getValue());
		else if (prop == logFullTrace)
			Logging.setShowStackTrace(logFullTrace.isValue());
		try {
			advancedProperties.save();
		} catch (IOException e) {
			throw new SearchLibException(e);
		}
	}

	public Sequence getGlobalSequence() {
		return globalSequence;
	}

}
