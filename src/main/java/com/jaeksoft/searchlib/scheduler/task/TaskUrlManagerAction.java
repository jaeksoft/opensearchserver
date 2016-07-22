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

package com.jaeksoft.searchlib.scheduler.task;

import java.io.IOException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.common.database.FetchStatus;
import com.jaeksoft.searchlib.crawler.common.database.IndexStatus;
import com.jaeksoft.searchlib.crawler.common.database.ParserStatus;
import com.jaeksoft.searchlib.crawler.web.database.RobotsTxtStatus;
import com.jaeksoft.searchlib.crawler.web.database.UrlManager;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.scheduler.TaskAbstract;
import com.jaeksoft.searchlib.scheduler.TaskLog;
import com.jaeksoft.searchlib.scheduler.TaskProperties;
import com.jaeksoft.searchlib.scheduler.TaskPropertyDef;
import com.jaeksoft.searchlib.scheduler.TaskPropertyType;
import com.jaeksoft.searchlib.util.Variables;

public class TaskUrlManagerAction extends TaskAbstract {

	final private TaskPropertyDef propCommand = new TaskPropertyDef(TaskPropertyType.comboBox, "Command", "Command",
			"Select the command to execute", 30);

	final private TaskPropertyDef propFilterUrl = new TaskPropertyDef(TaskPropertyType.textBox, "URL prefix",
			"URL prefix", "Filter prefix on the URL", 50);

	final private TaskPropertyDef propFilterLang = new TaskPropertyDef(TaskPropertyType.textBox, "Lang", "Lang",
			"Filter on the lang", 5);

	final private TaskPropertyDef propFilterContentBaseType = new TaskPropertyDef(TaskPropertyType.textBox,
			"ContentBaseType", "Content type", "Filter on the content type", 30);

	final private TaskPropertyDef propFilterContentTypeCharset = new TaskPropertyDef(TaskPropertyType.textBox,
			"ContentTypeCharset", "Content charset", "Filter on the content type charset", 10);

	final private TaskPropertyDef propFilterContentEncoding = new TaskPropertyDef(TaskPropertyType.textBox,
			"ContentEncoding", "Content encoding", "Filter on the content encoding", 10);

	final private TaskPropertyDef propFilterMinContentLength = new TaskPropertyDef(TaskPropertyType.textBox,
			"MinContentLength", "Min length", "Filter on the minimum content length", 10);

	final private TaskPropertyDef propFilterMaxContentLength = new TaskPropertyDef(TaskPropertyType.textBox,
			"MaxContentLength", "Max length", "Filter on the maximum content length", 10);

	final private TaskPropertyDef propFilterHost = new TaskPropertyDef(TaskPropertyType.textBox, "Hostname", "Hostname",
			"Filter on the hostname", 30);

	final private TaskPropertyDef propFilterWithSubDomain = new TaskPropertyDef(TaskPropertyType.listBox,
			"WithSubDomain", "With sub domain", "Filter on the sub domain", 10);

	final private TaskPropertyDef propRobotsTxtStatus = new TaskPropertyDef(TaskPropertyType.listBox,
			"Robots.txt status", "Robots.txt status", "Filter on the Robots.txt status", 20);

	final private TaskPropertyDef propFetchStatus = new TaskPropertyDef(TaskPropertyType.listBox, "Fetch status",
			"Fetch status", "Filter on the fetch status", 20);

	final private TaskPropertyDef propParserStatus = new TaskPropertyDef(TaskPropertyType.listBox, "Parser status",
			"Parser status", "Filter on the Parser status", 20);

	final private TaskPropertyDef propIndexStatus = new TaskPropertyDef(TaskPropertyType.listBox, "Index status",
			"Index status", "Filter on the index status", 20);

	final private TaskPropertyDef propBufferSize = new TaskPropertyDef(TaskPropertyType.textBox, "Buffer size",
			"Buffer size", "Buffer size", 10);

	final private TaskPropertyDef[] taskPropertyDefs = { propCommand, propFilterUrl, propFilterLang,
			propFilterContentBaseType, propFilterContentTypeCharset, propFilterContentEncoding,
			propFilterMinContentLength, propFilterMaxContentLength, propFilterHost, propFilterWithSubDomain,
			propRobotsTxtStatus, propFetchStatus, propParserStatus, propIndexStatus, propBufferSize };

	final public static String CommandDoNothing = "Do nothing";
	final public static String CommandSetToUnfetched = "Set to unfetched";
	final public static String CommandSetToFetchFirst = "Set to fetch first";
	final public static String CommandDeleteAll = "Delete all";
	final public static String CommandDeleteSelection = "Delete selection";
	final public static String CommandLoadSitemap = "Load Sitemap(s)";
	final public static String CommandOptimize = "Optimize";
	final public static String CommandSynchronize = "Synchronize";

	final private static String[] CommandList = { CommandDoNothing, CommandSetToUnfetched, CommandSetToFetchFirst,
			CommandDeleteSelection, CommandDeleteAll, CommandLoadSitemap, CommandSynchronize, CommandOptimize };

	@Override
	public String getName() {
		return "Web crawler - URL database";
	}

	@Override
	public TaskPropertyDef[] getPropertyList() {
		return taskPropertyDefs;
	}

	@Override
	public String[] getPropertyValues(Config config, TaskPropertyDef propertyDef, TaskProperties taskProperties) {
		if (propertyDef == propCommand)
			return CommandList;
		else if (propertyDef == propRobotsTxtStatus)
			return RobotsTxtStatus.getNames();
		else if (propertyDef == propFetchStatus)
			return FetchStatus.getNames();
		else if (propertyDef == propParserStatus)
			return ParserStatus.getNames();
		else if (propertyDef == propIndexStatus)
			return IndexStatus.getNames();
		else if (propertyDef == propFilterWithSubDomain)
			return ClassPropertyEnum.BOOLEAN_LIST;
		return null;
	}

	@Override
	public String getDefaultValue(Config config, TaskPropertyDef propertyDef) {
		if (propertyDef == propCommand)
			return CommandList[0];
		else if (propertyDef == propRobotsTxtStatus)
			return RobotsTxtStatus.ALL.name;
		else if (propertyDef == propFetchStatus)
			return FetchStatus.ALL.name;
		else if (propertyDef == propParserStatus)
			return ParserStatus.ALL.name;
		else if (propertyDef == propIndexStatus)
			return IndexStatus.ALL.name;
		else if (propertyDef == propBufferSize)
			return "10000";
		else if (propertyDef == propFilterWithSubDomain)
			return Boolean.FALSE.toString();
		return null;
	}

	private AbstractSearchRequest selectionRequest = null;

	private String manualCommand = null;

	private Integer manualBufferSize = null;

	public void setManual(AbstractSearchRequest selectionRequest, String manualCommand, int bufferSize) {
		this.selectionRequest = selectionRequest;
		this.manualCommand = manualCommand;
		this.manualBufferSize = bufferSize;
	}

	@Override
	public void execute(Client client, TaskProperties properties, Variables variables, TaskLog taskLog)
			throws SearchLibException, IOException {
		UrlManager urlManager = client.getUrlManager();
		taskLog.setInfo("URL manager Action started");

		final String command;
		final int bufferSize;

		if (manualCommand != null) {
			command = manualCommand;
			bufferSize = manualBufferSize;
		} else {
			command = properties.getValue(propCommand);
			bufferSize = Integer.parseInt(properties.getValue(propBufferSize));
			String urlLike = properties.getValue(propFilterUrl);
			String lang = properties.getValue(propFilterLang);
			String contentBaseType = properties.getValue(propFilterContentBaseType);
			String contentTypeCharset = properties.getValue(propFilterContentTypeCharset);
			String contentEncoding = properties.getValue(propFilterContentEncoding);
			Integer minContentLength = properties.getValueInteger(propFilterMinContentLength);
			Integer maxContentLength = properties.getValueInteger(propFilterMaxContentLength);
			String host = properties.getValue(propFilterHost);
			boolean withSubDomain = properties.getValueBoolean(propFilterWithSubDomain, false);
			RobotsTxtStatus robotsTxtStatus = RobotsTxtStatus.findByName(properties.getValue(propRobotsTxtStatus));
			FetchStatus fetchStatus = FetchStatus.findByName(properties.getValue(propFetchStatus));
			ParserStatus parserStatus = ParserStatus.findByName(properties.getValue(propParserStatus));
			IndexStatus indexStatus = IndexStatus.findByName(properties.getValue(propIndexStatus));
			selectionRequest = urlManager.getSearchRequest(UrlManager.SearchTemplate.urlSearch, urlLike, host,
					withSubDomain, lang, null, contentBaseType, contentTypeCharset, contentEncoding, minContentLength,
					maxContentLength, robotsTxtStatus, fetchStatus, null, parserStatus, indexStatus, null, null, null,
					null);
		}

		if (CommandLoadSitemap.equals(command)) {
			taskLog.setInfo("URL manager: Handle SiteMaps");
			urlManager.updateSiteMap(taskLog);
		} else if (CommandSetToFetchFirst.equals(command)) {
			taskLog.setInfo("URL manager: Update status to ");
			urlManager.updateFetchStatus(selectionRequest, FetchStatus.FETCH_FIRST, bufferSize, taskLog);
		} else if (CommandSetToUnfetched.equals(command)) {
			taskLog.setInfo("URL manager: Update status to ");
			urlManager.updateFetchStatus(selectionRequest, FetchStatus.UN_FETCHED, bufferSize, taskLog);
		} else if (CommandDeleteAll.equals(command)) {
			taskLog.setInfo("URL manager: Delete All");
			urlManager.deleteAll(taskLog);
		} else if (CommandDeleteSelection.equals(command)) {
			taskLog.setInfo("URL manager: Delete selection");
			urlManager.deleteUrls(selectionRequest, bufferSize, taskLog);
		} else if (CommandSynchronize.equals(command)) {
			taskLog.setInfo("URL manager: synchronize");
			urlManager.synchronizeIndex(selectionRequest, bufferSize, taskLog);
		}
	}
}
