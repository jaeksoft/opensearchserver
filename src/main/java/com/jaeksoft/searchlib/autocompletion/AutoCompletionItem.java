/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.autocompletion;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.query.QueryUtils;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.util.InfoCallback;
import com.jaeksoft.searchlib.util.PropertiesUtils;
import com.jaeksoft.searchlib.util.ReadWriteLock;

public class AutoCompletionItem implements Closeable,
		Comparable<AutoCompletionItem> {

	private final Config config;

	private final ReadWriteLock rwl = new ReadWriteLock();

	private Client autoCompClient = null;

	private AutoCompletionBuildThread buildThread = null;

	private final File autoCompClientDir;

	private final File propFile;

	private int propRows;

	private String propField;

	private final static String autoCompletionConfigPath = "/autocompletion_config.xml";
	private final static String autoCompletionPropertyField = "field";
	private final static String autoCompletionPropertyRows = "rows";
	private final static String autoCompletionPropertyRowsDefault = "10";
	public final static String autoCompletionSchemaFieldTerm = "term";
	public final static String autoCompletionSchemaFieldFreq = "freq";

	public final static String getPropertyField(Properties props) {
		return props.getProperty(autoCompletionPropertyField);
	}

	public AutoCompletionItem(Config config, String name)
			throws SearchLibException {
		this.config = config;
		AutoCompletionManager manager = config.getAutoCompletionManager();
		this.propFile = new File(manager.getDirectory(), name + ".xml");
		this.autoCompClientDir = new File(manager.getDirectory(), name);
		this.propRows = 10;
		this.propField = null;
	}

	public AutoCompletionItem(Config config, File autoCompPropFile,
			File autoCompClientDir) throws SearchLibException,
			InvalidPropertiesFormatException, IOException {
		this.config = config;
		this.propFile = autoCompPropFile;
		this.autoCompClientDir = autoCompClientDir;
		Properties properties = PropertiesUtils.loadFromXml(propFile);
		propField = properties.getProperty(autoCompletionPropertyField);
		propRows = Integer.parseInt(properties.getProperty(
				autoCompletionPropertyRows, autoCompletionPropertyRowsDefault));
	}

	private void checkIndexAndThread() throws SearchLibException {
		if (autoCompClient == null)
			autoCompClient = new Client(autoCompClientDir,
					autoCompletionConfigPath, true);
		if (buildThread == null)
			buildThread = new AutoCompletionBuildThread((Client) config,
					autoCompClient);
	}

	public Client getAutoCompletionClient() {
		rwl.r.lock();
		try {
			return autoCompClient;
		} finally {
			rwl.r.unlock();
		}
	}

	public String getField() {
		rwl.r.lock();
		try {
			return propField;
		} finally {
			rwl.r.unlock();
		}
	}

	public void setField(String field) {
		rwl.w.lock();
		try {
			this.propField = field;
		} finally {
			rwl.w.unlock();
		}
	}

	public int getRows() {
		rwl.r.lock();
		try {
			return propRows;
		} finally {
			rwl.r.unlock();
		}
	}

	public void setRows(int rows) {
		rwl.w.lock();
		try {
			propRows = rows;
		} finally {
			rwl.w.unlock();
		}
	}

	public void save() throws SearchLibException {
		rwl.w.lock();
		try {
			saveProperties();
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public void close() {
		rwl.w.lock();
		try {
			if (autoCompClient != null) {
				autoCompClient.close();
				autoCompClient = null;
			}
		} finally {
			rwl.w.unlock();
		}
	}

	public AutoCompletionBuildThread getBuildThread() {
		rwl.r.lock();
		try {
			return buildThread;
		} finally {
			rwl.r.unlock();
		}
	}

	private void checkIfRunning() throws SearchLibException {
		if (buildThread != null)
			if (buildThread.isRunning())
				throw new SearchLibException("The build is already running");
	}

	private int builder(Integer endTimeOut, int bufferSize,
			InfoCallback infoCallBack) throws SearchLibException, IOException {
		checkIfRunning();
		buildThread.init(propField, bufferSize, infoCallBack);
		buildThread.execute();
		buildThread.waitForStart(300);
		if (endTimeOut != null)
			buildThread.waitForEnd(endTimeOut);
		return buildThread.getIndexNumDocs();
	}

	public int build(Integer waitForEndTimeOut, int bufferSize,
			InfoCallback infoCallBack) throws SearchLibException {
		rwl.r.lock();
		try {
			checkIfRunning();
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			checkIndexAndThread();
			return builder(waitForEndTimeOut, bufferSize, infoCallBack);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.w.unlock();
		}
	}

	public AbstractResultSearch search(String query, Integer rows)
			throws SearchLibException {
		rwl.r.lock();
		try {
			if (query == null || query.length() == 0)
				return null;
			if (rows == null)
				rows = propRows;
			SearchRequest searchRequest = (SearchRequest) autoCompClient
					.getNewRequest("search");
			query = QueryUtils.replaceControlChars(query.replace("\"", ""));
			searchRequest.setQueryString(query);
			searchRequest.setRows(rows);
			return (AbstractResultSearch) autoCompClient.request(searchRequest);
		} finally {
			rwl.r.unlock();
		}
	}

	private void saveProperties() throws IOException {
		Properties properties = new Properties();
		if (propField != null)
			properties.setProperty(autoCompletionPropertyField, propField);
		properties.setProperty(autoCompletionPropertyRows,
				Integer.toString(propRows));
		PropertiesUtils.storeToXml(properties, propFile);
	}

	@Override
	public int compareTo(AutoCompletionItem item) {
		return propFile.getName().compareTo(item.propFile.getName());
	}

}
