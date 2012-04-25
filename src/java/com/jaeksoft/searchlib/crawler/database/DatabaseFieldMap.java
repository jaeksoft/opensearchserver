/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.database;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.crawler.FieldMapGeneric;
import com.jaeksoft.searchlib.crawler.web.database.HostUrlList.ListType;
import com.jaeksoft.searchlib.crawler.web.process.WebCrawlMaster;
import com.jaeksoft.searchlib.crawler.web.process.WebCrawlThread;
import com.jaeksoft.searchlib.crawler.web.spider.Crawl;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.parser.Parser;
import com.jaeksoft.searchlib.parser.ParserSelector;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.util.map.GenericLink;

public class DatabaseFieldMap extends FieldMapGeneric<DatabaseFieldTarget> {

	@Override
	protected DatabaseFieldTarget loadTarget(String targetName, Node node) {
		return new DatabaseFieldTarget(targetName, node);
	}

	@Override
	protected void writeTarget(XmlWriter xmlWriter, DatabaseFieldTarget target)
			throws SAXException {
		target.writeXml(xmlWriter);
	}

	public boolean isUrl() {
		for (GenericLink<String, DatabaseFieldTarget> link : getList()) {
			DatabaseFieldTarget dfTarget = link.getTarget();
			if (dfTarget.isCrawlUrl())
				return true;
		}
		return false;
	}

	final public void mapResultSet(WebCrawlMaster webCrawlMaster,
			ParserSelector parserSelector, LanguageEnum lang,
			ResultSet resultSet, Set<String> columns, IndexDocument target)
			throws SQLException, InstantiationException,
			IllegalAccessException, ClassNotFoundException, SearchLibException,
			ParseException, IOException, SyntaxError, URISyntaxException,
			InterruptedException {
		for (GenericLink<String, DatabaseFieldTarget> link : getList()) {
			String columnName = link.getSource();
			if (!columns.contains(columnName))
				continue;
			String content = resultSet.getString(columnName);
			if (content == null)
				continue;
			DatabaseFieldTarget dfTarget = link.getTarget();
			if (dfTarget == null)
				continue;
			if (dfTarget.isFilePath()) {
				File file = new File(dfTarget.getFilePath(content));
				if (file.exists()) {
					Parser parser = parserSelector.getParser(file.getName(),
							null);
					if (parser != null) {
						try {
							parser.parseContent(file, lang);
						} catch (IOException e) {
							Logging.warn(e.getMessage(), e);
						}
						parser.populate(target);
					}
				}
			}
			if (dfTarget.isCrawlUrl()) {
				WebCrawlThread crawlThread = webCrawlMaster.manualCrawl(
						new URL(content), ListType.DBCRAWL);
				crawlThread.waitForStart(60);
				crawlThread.waitForEnd(60);
				Crawl crawl = crawlThread.getCurrentCrawl();
				if (crawl != null) {
					IndexDocument targetIndexDocument = crawl
							.getTargetIndexDocument();
					if (targetIndexDocument != null)
						target.add(targetIndexDocument);
				}
			}
			if (dfTarget.isConvertHtmlEntities())
				content = StringEscapeUtils.unescapeHtml(content);
			if (dfTarget.isRemoveTag())
				content = StringUtils.removeTag(content);
			if (dfTarget.hasRegexpPattern())
				content = dfTarget.applyRegexPattern(content);
			target.add(dfTarget.getName(), new FieldValueItem(content));
		}
	}
}
