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
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;

import com.jaeksoft.pojodbc.Transaction;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.web.browser.BrowserDriver;
import com.jaeksoft.searchlib.crawler.web.browser.BrowserDriverEnum;
import com.jaeksoft.searchlib.script.commands.Selectors;
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

	private Variables variables;

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
		variables = null;
	}

	private void releaseCurrentWebDriver(boolean quietly)
			throws ScriptException {
		if (currentWebDriver == null)
			return;
		if (quietly)
			IOUtils.closeQuietly(currentWebDriver);
		else
			try {
				currentWebDriver.close();
			} catch (IOException e) {
				throw new ScriptException(e);
			}
		currentWebDriver = null;
	}

	public BrowserDriver<?> getBrowserDriver() {
		return currentWebDriver;
	}

	public Config getConfig() {
		return config;
	}

	public void setVariables(Variables variables) {
		this.variables = variables;
	}

	public Variables getVariables() {
		return variables;
	}

	public void setBrowserDriver(BrowserDriverEnum browserDriverEnum)
			throws ScriptException {
		releaseCurrentWebDriver(false);
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
		try {
			releaseCurrentWebDriver(true);
		} catch (ScriptException e) {
			Logging.info(e);
		}
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

}
