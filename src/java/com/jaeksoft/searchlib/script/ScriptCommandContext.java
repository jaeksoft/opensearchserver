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
import java.util.Collection;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.web.browser.BrowserDriver;
import com.jaeksoft.searchlib.crawler.web.browser.BrowserDriverEnum;
import com.jaeksoft.searchlib.scheduler.TaskLog;

public class ScriptCommandContext implements Closeable {

	public final Config config;

	public final TaskLog taskLog;

	private BrowserDriver<?> currentWebDriver;

	private TreeSet<String> cssSelectors;

	public ScriptCommandContext(Config config, TaskLog taskLog) {
		this.config = config;
		this.taskLog = taskLog;
		currentWebDriver = null;
		cssSelectors = null;
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

	public void resetCssSelector() {
		if (cssSelectors == null)
			return;
		cssSelectors.clear();
		cssSelectors = null;
	}

	public void addCssSelector(String selector) {
		if (cssSelectors == null)
			cssSelectors = new TreeSet<String>();
		cssSelectors.add(selector);
	}

	public Collection<String> getCssSelectors() {
		if (cssSelectors == null)
			return null;
		return cssSelectors;
	}

	public TaskLog getTaskLog() {
		return taskLog;
	}

	@Override
	public void close() {
		try {
			releaseCurrentWebDriver(true);
		} catch (ScriptException e) {
			Logging.info(e);
		}
	}

}
