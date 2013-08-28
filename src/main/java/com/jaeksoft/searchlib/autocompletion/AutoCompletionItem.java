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
import java.util.Collection;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.query.QueryUtils;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
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

	private final Set<String> propFields;

	private String searchRequest;

	private final static String autoCompletionConfigPath = "/autocompletion_config.xml";
	private final static String autoCompletionPropertyField = "field";
	private final static String autoCompletionPropertyRows = "rows";
	private final static String autoCompletionPropertyRowsDefault = "10";
	public final static String autoCompletionSchemaFieldTerm = "term";
	public final static String autoCompletionSchemaFieldFreq = "freq";
	public final static String autoCompletionSearchRequest = "searchRequest";

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
		this.propFields = new TreeSet<String>();
		this.searchRequest = null;
	}

	private final static File getAutoCompClientDir(File autoCompPropFile)
			throws InvalidPropertiesFormatException {
		String name = autoCompPropFile.getName();
		if (name.length() <= 4 || !name.endsWith(".xml"))
			throw new InvalidPropertiesFormatException(
					"File is not an XML file: "
							+ autoCompPropFile.getAbsolutePath());
		return new File(autoCompPropFile.getParent(), name.substring(0,
				name.length() - 4));
	}

	public AutoCompletionItem(Config config, File autoCompPropFile)
			throws SearchLibException, InvalidPropertiesFormatException,
			IOException {
		this.config = config;
		this.propFields = new TreeSet<String>();
		this.propFile = autoCompPropFile;
		this.autoCompClientDir = getAutoCompClientDir(autoCompPropFile);
		Properties properties = PropertiesUtils.loadFromXml(propFile);
		int i = 1;
		for (;;) {
			String propField = properties
					.getProperty(autoCompletionPropertyField + i);
			if (propField == null)
				break;
			propFields.add(propField);
			i++;
		}
		propRows = Integer.parseInt(properties.getProperty(
				autoCompletionPropertyRows, autoCompletionPropertyRowsDefault));
		searchRequest = properties.getProperty(autoCompletionSearchRequest);
		checkIndexAndThread();
	}

	private void checkIndexAndThread() throws SearchLibException {
		if (autoCompClient == null) {
			if (!autoCompClientDir.exists())
				autoCompClientDir.mkdir();
			autoCompClient = new Client(autoCompClientDir,
					autoCompletionConfigPath, true);
		}
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

	public Collection<String> getFields() {
		rwl.r.lock();
		try {
			return propFields;
		} finally {
			rwl.r.unlock();
		}
	}

	public void addField(String field) {
		rwl.w.lock();
		try {
			propFields.add(field);
		} finally {
			rwl.w.unlock();
		}
	}

	public void setFields(Collection<String> fields) {
		rwl.w.lock();
		try {
			propFields.clear();
			for (String field : fields)
				propFields.add(field);
		} finally {
			rwl.w.unlock();
		}
	}

	public void setField(String[] fields) {
		rwl.w.lock();
		try {
			propFields.clear();
			for (String field : fields)
				propFields.add(field);
		} finally {
			rwl.w.unlock();
		}
	}

	public void removeField(String field) {
		rwl.w.lock();
		try {
			propFields.remove(field);
		} finally {
			rwl.w.unlock();
		}
	}

	public String getName() {
		rwl.r.lock();
		try {
			return autoCompClientDir.getName();
		} finally {
			rwl.r.unlock();
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
			checkIndexAndThread();
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.w.unlock();
		}
	}

	public void delete() throws IOException {
		rwl.w.lock();
		try {
			if (autoCompClient != null) {
				autoCompClient.close();
				autoCompClient.delete();
			}
			propFile.delete();
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
		if (infoCallBack != null)
			infoCallBack.setInfo("Build starts");
		checkIfRunning();
		buildThread.init(propFields, searchRequest, bufferSize, infoCallBack);
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
			AbstractSearchRequest searchRequest = (AbstractSearchRequest) autoCompClient
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
		int i = 1;
		for (String fieldName : propFields) {
			properties.setProperty(autoCompletionPropertyField + i, fieldName);
			i++;
		}
		properties.setProperty(autoCompletionPropertyRows,
				Integer.toString(propRows));
		if (searchRequest != null && searchRequest.length() > 0)
			properties.setProperty(autoCompletionSearchRequest, searchRequest);
		PropertiesUtils.storeToXml(properties, propFile);
	}

	@Override
	public int compareTo(AutoCompletionItem item) {
		return propFile.getName().compareTo(item.propFile.getName());
	}

	/**
	 * @return the searchRequest
	 */
	public String getSearchRequest() {
		rwl.r.lock();
		try {
			return searchRequest;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param searchRequest
	 *            the searchRequest to set
	 */
	public void setSearchRequest(String searchRequest) {
		rwl.w.lock();
		try {
			this.searchRequest = searchRequest;
		} finally {
			rwl.w.unlock();
		}
	}

}
