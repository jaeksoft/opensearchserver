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

package com.jaeksoft.searchlib.script;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.http.client.ClientProtocolException;

import com.jaeksoft.pojodbc.Transaction;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.web.browser.BrowserDriver;
import com.jaeksoft.searchlib.crawler.web.browser.BrowserDriverEnum;
import com.jaeksoft.searchlib.crawler.web.database.CookieItem;
import com.jaeksoft.searchlib.crawler.web.spider.DownloadItem;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.script.commands.Selectors;
import com.jaeksoft.searchlib.util.IOUtils;
import com.jaeksoft.searchlib.util.InfoCallback;
import com.jaeksoft.searchlib.utils.Variables;

public class ScriptCommandContext implements Closeable {

	public final Config config;

	public final InfoCallback callback;

	private BrowserDriver<?> currentWebDriver;

	private TreeSet<Selectors.Selector> selectors;

	private OnError onError;

	private CommandEnum[] onErrorNextCommands;

	private Transaction transaction;

	private final Set<Variables> variablesSet;

	private final Variables variables;

	private final Variables contextVariables;

	private List<IndexDocument> indexDocuments;

	private IndexDocument currentIndexDocument;

	private int updatedDocumentCount;

	public static enum OnError {
		FAILURE, RESUME, NEXT_COMMAND;
	}

	public ScriptCommandContext(Config config, InfoCallback callback) {
		this.config = config;
		this.callback = callback;
		currentWebDriver = null;
		selectors = null;
		onError = OnError.FAILURE;
		onErrorNextCommands = null;
		transaction = null;
		variablesSet = new LinkedHashSet<Variables>();
		variables = new Variables();
		contextVariables = new Variables();
		indexDocuments = null;
		currentIndexDocument = null;
		updatedDocumentCount = 0;
	}

	public BrowserDriver<?> getBrowserDriver() {
		return currentWebDriver;
	}

	public Config getConfig() {
		return config;
	}

	private void buildVariables() {
		variables.clear();
		variables.merge(contextVariables);
		for (Variables vars : variablesSet)
			variables.merge(vars);
	}

	public void addVariables(final Variables... variablesList) {
		if (variablesList == null)
			return;
		if (variablesList.length == 0)
			return;
		for (Variables vars : variablesList)
			if (vars != null)
				variablesSet.add(vars);
		buildVariables();
	}

	public void addContextVariables(String name, String value) {
		if (value == null)
			contextVariables.clear(name);
		else
			contextVariables.put(name, value);
		buildVariables();
	}

	public void removeVariables(final Variables... variablesList) {
		if (variablesList == null)
			return;
		if (variablesList.length == 0)
			return;
		for (Variables vars : variablesList)
			if (vars != null)
				variablesSet.remove(vars);
		buildVariables();
	}

	public String replaceVariables(String text) {
		return variables.replace(text);
	}

	public void setBrowserDriver(BrowserDriverEnum browserDriverEnum)
			throws ScriptException {
		close();
		try {
			if (browserDriverEnum != null)
				currentWebDriver = browserDriverEnum.getNewInstance();
		} catch (InstantiationException e) {
			throw new ScriptException(e);
		} catch (IllegalAccessException e) {
			throw new ScriptException(e);
		}
	}

	public void resetSelector(Selectors.Type type) {
		if (selectors == null)
			return;
		if (type == null) {
			selectors.clear();
		} else {
			synchronized (selectors) {
				Iterator<Selectors.Selector> it = selectors.iterator();
				while (it.hasNext()) {
					if (it.next().type == type)
						it.remove();
				}
			}
		}
		if (selectors.size() == 0)
			selectors = null;
	}

	public void addSelector(Selectors.Selector selector) {
		if (selectors == null)
			selectors = new TreeSet<Selectors.Selector>();
		selectors.add(selector);
	}

	public Collection<Selectors.Selector> getSelectors() {
		if (selectors == null)
			return null;
		return selectors;
	}

	public InfoCallback getInfoCallback() {
		return callback;
	}

	@Override
	public void close() {
		IOUtils.close(currentWebDriver);
		currentWebDriver = null;
	}

	public void setOnError(OnError onError, List<CommandEnum> commandList) {
		this.onError = onError;
		this.onErrorNextCommands = null;
		if (commandList == null)
			return;
		if (commandList.size() == 0)
			return;
		onErrorNextCommands = commandList.toArray(new CommandEnum[commandList
				.size()]);
	}

	public OnError getOnError() {
		return onError;
	}

	public CommandEnum[] getOnErrorNextCommands() {
		return onErrorNextCommands;
	}

	public void executeSqlUpdate(String sql) throws SQLException,
			ScriptException {
		if (transaction == null)
			throw new ScriptException("Not database connection");
		transaction.update(sql);
		transaction.commit();
	}

	public void setSql(Transaction transaction) {
		this.transaction = transaction;
	}

	public void addIndexDocument(IndexDocument indexDocument) {
		if (indexDocuments == null)
			indexDocuments = new ArrayList<IndexDocument>(1);
		indexDocuments.add(indexDocument);
		currentIndexDocument = indexDocument;
	}

	public IndexDocument getIndexDocument() {
		return currentIndexDocument;
	}

	public List<IndexDocument> getIndexDocuments() {
		return indexDocuments;
	}

	public void clearIndexDocuments(int updatedCount) {
		if (indexDocuments == null)
			return;
		indexDocuments.clear();
		this.updatedDocumentCount += updatedCount;
	}

	public int getUpdatedDocumentCount() {
		return updatedDocumentCount;
	}

	public void subscript(String scriptName, Variables variables)
			throws ScriptException {
		ScriptLinesRunner runner = null;
		try {
			List<ScriptLine> scriptLines = config.getScriptManager()
					.getContent(scriptName);
			if (scriptLines == null)
				return;
			addVariables(variables);
			runner = new ScriptLinesRunner(this, scriptLines);
			runner.run();
		} catch (IOException e) {
			throw new ScriptException(e);
		} catch (SearchLibException e) {
			throw new ScriptException(e);
		} finally {
			removeVariables(variables);
			if (runner != null)
				IOUtils.closeQuietly(runner);
		}
	}

	public void download(URI uri, File directory) throws ScriptException {
		if (currentWebDriver == null)
			throw new ScriptException("No browser open");
		if (directory == null)
			throw new ScriptException("No directory path given");
		if (directory.exists()) {
			if (!directory.isDirectory())
				throw new ScriptException("The path is not a directory: "
						+ directory.getAbsolutePath());
		} else {
			directory.mkdirs();
			if (!directory.exists())
				throw new ScriptException("Unable to create the directory: "
						+ directory.getAbsolutePath());
		}
		List<CookieItem> cookies = currentWebDriver.getCookies();
		HttpDownloader downloader = null;
		try {
			downloader = config.getWebCrawlMaster().getNewHttpDownloader(true);
			DownloadItem downloadItem = downloader
					.get(uri, null, null, cookies);
			String fileName = downloadItem.getFileName();
			File file = new File(directory, fileName);
			int i = 0;
			while (file.exists() && i < 1024)
				file = new File(directory, fileName + '.' + i);
			FileOutputStream fos = new FileOutputStream(file);
			IOUtils.copy(downloadItem.getContentInputStream(), fos);
			fos.close();
		} catch (SearchLibException e) {
			throw new ScriptException(e);
		} catch (ClientProtocolException e) {
			throw new ScriptException(e);
		} catch (IllegalStateException e) {
			throw new ScriptException(e);
		} catch (IOException e) {
			throw new ScriptException(e);
		} catch (URISyntaxException e) {
			throw new ScriptException(e);
		} finally {
			if (downloader != null)
				downloader.release();
		}
	}

	public void download(URL url, File directory) throws ScriptException {
		try {
			download(url.toURI(), directory);
		} catch (URISyntaxException e) {
			throw new ScriptException(e);
		}
	}

	public void download(String url, File directory) throws ScriptException {
		try {
			download(new URI(url), directory);
		} catch (URISyntaxException e) {
			throw new ScriptException(e);
		}

	}

}
