/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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

import java.io.File;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.schema.SchemaFieldList;
import com.jaeksoft.searchlib.util.InfoCallback;
import com.jaeksoft.searchlib.util.PropertiesUtils;
import com.jaeksoft.searchlib.util.ReadWriteLock;

public class AutoCompletionManager {

	private final ReadWriteLock rwl = new ReadWriteLock();

	private Client autoCompClient;

	private AutoCompletionBuildThread buildThread;

	private File propFile;

	private SchemaField termField;

	private int propRows;

	private String propField;

	private final static String autoCompletionProperties = "autocompletion-properties.xml";
	private final static String autoCompletionSubDirectory = "autocompletion";
	private final static String autoCompletionConfigPath = "/autocompletion_config.xml";
	private final static String autoCompletionPropertyField = "field";
	private final static String autoCompletionPropertyRows = "rows";
	private final static String autoCompletionPropertyRowsDefault = "10";
	public final static String autoCompletionSchemaFieldTerm = "term";
	public final static String autoCompletionSchemaFieldFreq = "freq";

	public final static String getPropertyField(Properties props) {
		return props.getProperty(autoCompletionPropertyField);
	}

	public AutoCompletionManager(Config config) throws SearchLibException,
			InvalidPropertiesFormatException, IOException {
		File subDir = new File(config.getDirectory(),
				autoCompletionSubDirectory);
		if (!subDir.exists())
			subDir.mkdir();
		this.autoCompClient = new Client(subDir, autoCompletionConfigPath, true);
		propFile = new File(config.getDirectory(), autoCompletionProperties);
		Properties properties = PropertiesUtils.loadFromXml(propFile);
		propField = properties.getProperty(autoCompletionPropertyField);
		propRows = Integer.parseInt(properties.getProperty(
				autoCompletionPropertyRows, autoCompletionPropertyRowsDefault));
		buildThread = new AutoCompletionBuildThread((Client) config,
				autoCompClient);
		SchemaFieldList schemaFieldList = autoCompClient.getSchema()
				.getFieldList();
		termField = schemaFieldList.get(autoCompletionSchemaFieldTerm);
	}

	private void saveProperties() throws IOException {
		Properties properties = new Properties();
		if (propField != null)
			properties.setProperty(autoCompletionPropertyField, propField);
		properties.setProperty(autoCompletionPropertyRows,
				Integer.toString(propRows));
		PropertiesUtils.storeToXml(properties, propFile);
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

	public void close() {
		rwl.w.lock();
		try {
			this.autoCompClient.close();
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
		if (buildThread.isRunning())
			throw new SearchLibException("The build is already running");
	}

	private void builder(Long endTimeOut, InfoCallback infoCallBack)
			throws SearchLibException {
		checkIfRunning();
		buildThread.init(propField, infoCallBack);
		buildThread.execute();
		buildThread.waitForStart(60);
		if (endTimeOut != null)
			buildThread.waitForEnd(600);
	}

	public void build(Long waitForEndTimeOut, InfoCallback infoCallBack)
			throws SearchLibException {
		rwl.r.lock();
		try {
			checkIfRunning();
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			builder(waitForEndTimeOut, infoCallBack);
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
			SearchRequest searchRequest = new SearchRequest(autoCompClient);
			searchRequest.setQueryString(query);
			searchRequest.setDefaultOperator("AND");
			searchRequest.setRows(rows);
			searchRequest.getSortList()
					.add(autoCompletionSchemaFieldFreq, true);
			searchRequest.getReturnFieldList().add(termField);
			return (AbstractResultSearch) autoCompClient.request(searchRequest);
		} finally {
			rwl.r.unlock();
		}
	}
}
