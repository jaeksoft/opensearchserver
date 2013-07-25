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

package com.jaeksoft.searchlib.webservice;

import com.jaeksoft.searchlib.webservice.analyzer.AnalyzerImpl;
import com.jaeksoft.searchlib.webservice.autocompletion.AutoCompletionImpl;
import com.jaeksoft.searchlib.webservice.command.CommandImpl;
import com.jaeksoft.searchlib.webservice.crawler.database.DatabaseImpl;
import com.jaeksoft.searchlib.webservice.crawler.filecrawler.FileCrawlerImpl;
import com.jaeksoft.searchlib.webservice.crawler.webcrawler.WebCrawlerImpl;
import com.jaeksoft.searchlib.webservice.delete.DeleteImpl;
import com.jaeksoft.searchlib.webservice.index.IndexImpl;
import com.jaeksoft.searchlib.webservice.learner.LearnerImpl;
import com.jaeksoft.searchlib.webservice.monitor.MonitorImpl;
import com.jaeksoft.searchlib.webservice.scheduler.SchedulerImpl;
import com.jaeksoft.searchlib.webservice.schema.SchemaImpl;
import com.jaeksoft.searchlib.webservice.select.SelectImpl;
import com.jaeksoft.searchlib.webservice.update.UpdateImpl;

public enum WebServiceEnum {

	Analyzer(AnalyzerImpl.class, "/analyzer"),

	AutoCompletion(AutoCompletionImpl.class, "/autocompletion"),

	Command(CommandImpl.class, "/command"),

	Database(DatabaseImpl.class, "/crawler/database"),

	Delete(DeleteImpl.class, "/delete"),

	FileCrawler(FileCrawlerImpl.class, "/crawler/file"),

	Index(IndexImpl.class, "/index"),

	Learner(LearnerImpl.class, "/learner"),

	Monitor(MonitorImpl.class, "/monitor"),

	Scheduler(SchedulerImpl.class, "/scheduler"),

	Select(SelectImpl.class, "/select"),

	Shema(SchemaImpl.class, "/schema"),

	Update(UpdateImpl.class, "/update"),

	WebCrawler(WebCrawlerImpl.class, "/crawler/web");

	final private Class<?> serviceClass;

	final private String defaultPath;

	private WebServiceEnum(Class<?> serviceClass, String defaultPath) {
		this.serviceClass = serviceClass;
		this.defaultPath = defaultPath;
	}

	final public Object getNewInstance() throws InstantiationException,
			IllegalAccessException {
		return serviceClass.newInstance();
	}

	final public Class<?> getServiceClass() {
		return serviceClass;
	}

	final public String getDefaultPath() {
		return defaultPath;
	}
}
