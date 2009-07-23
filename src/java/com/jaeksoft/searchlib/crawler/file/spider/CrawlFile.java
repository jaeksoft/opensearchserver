/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.crawler.file.spider;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.FieldMap;
import com.jaeksoft.searchlib.crawler.common.database.FetchStatus;
import com.jaeksoft.searchlib.crawler.common.database.ParserStatus;
import com.jaeksoft.searchlib.crawler.common.process.CrawlStatistics;
import com.jaeksoft.searchlib.crawler.file.database.FileItem;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.parser.LimitException;
import com.jaeksoft.searchlib.parser.Parser;
import com.jaeksoft.searchlib.parser.ParserFieldEnum;
import com.jaeksoft.searchlib.parser.ParserSelector;
import com.jaeksoft.searchlib.util.FileUtils;

public class CrawlFile {

	final private static Logger logger = Logger.getLogger(CrawlFile.class
			.getCanonicalName());

	private final FileItem fileItem;
	private final String userAgent;
	private Parser parser;
	private String error;
	private final FieldMap fileFieldMap;
	private final Config config;

	public CrawlFile(FileItem fileItem, Config config,
			CrawlStatistics currentStats) throws SearchLibException {
		this.fileFieldMap = config.getFileCrawlerFieldMap();
		this.fileItem = fileItem;
		this.fileItem.setWhenNow();
		this.userAgent = config.getPropertyManager().getUserAgent()
				.toLowerCase();
		this.parser = null;
		this.error = null;
		this.config = config;
	}

	/**
	 * Télécharge le fichier et extrait les informations
	 * 
	 * @param userAgent
	 */
	public void download(FileItem item) {
		synchronized (this) {
			try {
				ParserSelector parserSelector = config.getParserSelector();
				Parser parser = parserSelector.getParserFromExtension(FileUtils
						.getFileNameExtension(item.getPath()));

				if (parser == null) {
					item.setParserStatus(ParserStatus.NOPARSER);
					return;
				}

				IndexDocument sourceDocument = new IndexDocument();
				fileItem.populate(sourceDocument);

				parser.setSourceDocument(sourceDocument);
				parser.parseContent(new FileInputStream(item.getPath()));
				fileItem.setLang(parser.getFieldValue(ParserFieldEnum.lang, 0));
				fileItem.setParserStatus(ParserStatus.PARSED);
				this.parser = parser;

				fileItem.setFetchStatus(FetchStatus.FETCHED);

			} catch (MalformedURLException e) {
				fileItem.setFetchStatus(FetchStatus.ERROR);
			} catch (FileNotFoundException e) {
				fileItem.setFetchStatus(FetchStatus.GONE);
			} catch (LimitException e) {
				logger.warning(e.toString() + " (" + fileItem.getPath() + ")");
				fileItem.setFetchStatus(FetchStatus.SIZE_EXCEED);
				setError(e.getMessage());
			} catch (InstantiationException e) {
				logger.log(Level.WARNING, e.getMessage(), e);
				fileItem.setParserStatus(ParserStatus.PARSER_ERROR);
				setError(e.getMessage());
			} catch (IllegalAccessException e) {
				logger.log(Level.WARNING, e.getMessage(), e);
				fileItem.setParserStatus(ParserStatus.PARSER_ERROR);
				setError(e.getMessage());
			} catch (ClassNotFoundException e) {
				logger.log(Level.WARNING, e.getMessage(), e);
				fileItem.setParserStatus(ParserStatus.PARSER_ERROR);
				setError(e.getMessage());
			} catch (IOException e) {
				logger.log(Level.WARNING, e.getMessage(), e);
				fileItem.setFetchStatus(FetchStatus.ERROR);
				// setError(e.getMessage() + "  " + fileItem.getPath());
			} catch (Exception e) {
				logger.log(Level.WARNING, e.getMessage(), e);
				fileItem.setFetchStatus(FetchStatus.ERROR);
				// setError(e.getMessage());
			}
		}
	}

	public String getError() {
		return error;
	}

	public FileItem getFileItem() {
		return fileItem;
	}

	public Parser getParser() {
		return parser;
	}

	public IndexDocument getTargetIndexDocument() throws SearchLibException,
			UnsupportedEncodingException {
		synchronized (this) {
			// No parser found
			if (parser == null)
				return null;

			IndexDocument indexDocument = new IndexDocument();
			IndexDocument fileIndexDocument = new IndexDocument();
			fileItem.populate(fileIndexDocument);
			fileFieldMap.mapIndexDocument(fileIndexDocument, indexDocument);

			if (parser != null)
				parser.populate(indexDocument);

			return indexDocument;
		}
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setError(String error) {
		this.error = error;
	}
}