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
import com.jaeksoft.searchlib.webservice.crawler.rest.RestCrawlerImpl;
import com.jaeksoft.searchlib.webservice.crawler.webcrawler.WebCrawlerImpl;
import com.jaeksoft.searchlib.webservice.document.DocumentImpl;
import com.jaeksoft.searchlib.webservice.fields.FieldImpl;
import com.jaeksoft.searchlib.webservice.index.IndexImpl;
import com.jaeksoft.searchlib.webservice.learner.LearnerImpl;
import com.jaeksoft.searchlib.webservice.monitor.MonitorImpl;
import com.jaeksoft.searchlib.webservice.query.namedEntity.NamedEntityImpl;
import com.jaeksoft.searchlib.webservice.query.search.SearchImpl;
import com.jaeksoft.searchlib.webservice.query.spellcheck.SpellcheckImpl;
import com.jaeksoft.searchlib.webservice.scheduler.SchedulerImpl;
import com.jaeksoft.searchlib.webservice.screenshot.ScreenshotImpl;
import com.jaeksoft.searchlib.webservice.script.ScriptImpl;
import com.jaeksoft.searchlib.webservice.stopwords.StopWordsImpl;
import com.jaeksoft.searchlib.webservice.synonyms.SynonymsImpl;

public enum WebServiceEnum {

	Analyzer(AnalyzerImpl.class, "/analyzer"),

	AutoCompletion(AutoCompletionImpl.class, "/autocompletion"),

	Command(CommandImpl.class, "/command"),

	Database(DatabaseImpl.class, "/crawler/database"),

	Document(DocumentImpl.class, "/document"),

	Field(FieldImpl.class, "/field"),

	FileCrawler(FileCrawlerImpl.class, "/crawler/file"),

	Index(IndexImpl.class, "/index"),

	Learner(LearnerImpl.class, "/learner"),

	Monitor(MonitorImpl.class, "/monitor"),

	NamedEntity(NamedEntityImpl.class, "/namedentity"),

	RestCrawler(RestCrawlerImpl.class, "/crawler/rest"),

	Scheduler(SchedulerImpl.class, "/scheduler"),

	Screenshot(ScreenshotImpl.class, "/screenshot"),

	Script(ScriptImpl.class, "/script"),

	Search(SearchImpl.class, "/search"),

	Spellcheck(SpellcheckImpl.class, "/spellcheck"),

	Synonyms(SynonymsImpl.class, "/synonyms"),

	Stopwords(StopWordsImpl.class, "/stopwords"),

	WebCrawler(WebCrawlerImpl.class, "/crawler/web");

	final public Class<?> serviceClass;

	final public String defaultPath;

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
