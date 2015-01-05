/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013-2015 Emmanuel Keller / Jaeksoft
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
import com.jaeksoft.searchlib.webservice.cluster.ClusterImpl;
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
import com.jaeksoft.searchlib.webservice.parser.ParserImpl;
import com.jaeksoft.searchlib.webservice.query.document.DocumentsImpl;
import com.jaeksoft.searchlib.webservice.query.morelikethis.MoreLikeThisImpl;
import com.jaeksoft.searchlib.webservice.query.namedEntity.NamedEntityImpl;
import com.jaeksoft.searchlib.webservice.query.search.SearchImpl;
import com.jaeksoft.searchlib.webservice.query.spellcheck.SpellcheckImpl;
import com.jaeksoft.searchlib.webservice.replication.ReplicationImpl;
import com.jaeksoft.searchlib.webservice.scheduler.SchedulerImpl;
import com.jaeksoft.searchlib.webservice.screenshot.ScreenshotImpl;
import com.jaeksoft.searchlib.webservice.script.ScriptImpl;
import com.jaeksoft.searchlib.webservice.stopwords.StopWordsImpl;
import com.jaeksoft.searchlib.webservice.synonyms.SynonymsImpl;
import com.jaeksoft.searchlib.webservice.user.UserImpl;

public enum WebServiceEnum {

	Analyzer(AnalyzerImpl.class),

	AutoCompletion(AutoCompletionImpl.class),

	Cluster(ClusterImpl.class),

	Command(CommandImpl.class),

	Database(DatabaseImpl.class),

	Document(DocumentImpl.class),

	Documents(DocumentsImpl.class),

	Field(FieldImpl.class),

	FileCrawler(FileCrawlerImpl.class),

	Index(IndexImpl.class),

	Learner(LearnerImpl.class),

	Monitor(MonitorImpl.class),

	MoreLikeThis(MoreLikeThisImpl.class),

	NamedEntity(NamedEntityImpl.class),

	Parser(ParserImpl.class),

	Replication(ReplicationImpl.class),

	RestCrawler(RestCrawlerImpl.class),

	Scheduler(SchedulerImpl.class),

	Screenshot(ScreenshotImpl.class),

	Script(ScriptImpl.class),

	Search(SearchImpl.class),

	Spellcheck(SpellcheckImpl.class),

	Synonyms(SynonymsImpl.class),

	Stopwords(StopWordsImpl.class),

	User(UserImpl.class),

	WebCrawler(WebCrawlerImpl.class);

	final public Class<?> serviceClass;

	private WebServiceEnum(Class<?> serviceClass) {
		this.serviceClass = serviceClass;
	}

	@SuppressWarnings("unchecked")
	final public <T> T getNewInstance() {
		try {
			return (T) serviceClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	final public Class<?> getServiceClass() {
		return serviceClass;
	}

}
