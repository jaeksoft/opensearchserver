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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import org.apache.poi.util.IOUtils;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.Result;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.schema.SchemaFieldList;
import com.jaeksoft.searchlib.util.ReadWriteLock;

public class AutoCompletionManager {

	private final ReadWriteLock rwl = new ReadWriteLock();

	private Client autoCompClient;

	private AutoCompletionBuildThread buildThread;

	private Properties properties;

	private File propFile;

	private SchemaField termField;

	private final static String autoCompletionProperties = "autocompletion-properties.xml";
	private final static String autoCompletionSubDirectory = "autocompletion";
	private final static String autoCompletionConfigPath = "/autocompletion_config.xml";
	private final static String autoCompletionPropertyField = "field";
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
		properties = new Properties();
		if (propFile.exists()) {
			FileInputStream inputStream = null;
			try {
				inputStream = new FileInputStream(propFile);
				properties.loadFromXML(inputStream);
			} finally {
				if (inputStream != null)
					IOUtils.closeQuietly(inputStream);
			}
		}
		buildThread = new AutoCompletionBuildThread((Client) config,
				autoCompClient);
		SchemaFieldList schemaFieldList = autoCompClient.getSchema()
				.getFieldList();
		termField = schemaFieldList.get(autoCompletionSchemaFieldTerm);
	}

	private void saveProperties() throws IOException {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(propFile);
			properties.storeToXML(fos, "");
		} finally {
			if (fos != null)
				IOUtils.closeQuietly(fos);
		}
	}

	final private String getPropertyField() {
		return properties.getProperty(autoCompletionPropertyField);
	}

	public String getField() {
		rwl.r.lock();
		try {
			return getPropertyField();
		} finally {
			rwl.r.unlock();
		}
	}

	public void setField(String field) throws SearchLibException {
		rwl.w.lock();
		try {
			properties.setProperty(autoCompletionPropertyField, field);
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

	public void startBuild() throws SearchLibException {
		rwl.r.lock();
		try {
			checkIfRunning();
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			checkIfRunning();
			buildThread.init(getPropertyField());
			buildThread.execute();
			buildThread.waitForStart(60);
		} finally {
			rwl.w.unlock();
		}
	}

	public Result search(String query, int rows) throws SearchLibException {
		rwl.r.lock();
		try {
			if (query == null || query.length() == 0)
				return null;
			SearchRequest searchRequest = autoCompClient.getNewSearchRequest();
			searchRequest.setQueryString(query);
			searchRequest.setDefaultOperator("AND");
			searchRequest.setRows(rows);
			searchRequest.getSortList()
					.add(autoCompletionSchemaFieldFreq, true);
			searchRequest.getReturnFieldList().add(termField);
			return autoCompClient.search(searchRequest);
		} finally {
			rwl.r.unlock();
		}
	}
}
