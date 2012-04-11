/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.file.spider;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.FieldMap;
import com.jaeksoft.searchlib.crawler.common.database.FetchStatus;
import com.jaeksoft.searchlib.crawler.common.database.ParserStatus;
import com.jaeksoft.searchlib.crawler.common.process.CrawlStatistics;
import com.jaeksoft.searchlib.crawler.file.database.FileItem;
import com.jaeksoft.searchlib.crawler.file.database.FileItemFieldEnum;
import com.jaeksoft.searchlib.crawler.file.process.FileInstanceAbstract;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.parser.Parser;
import com.jaeksoft.searchlib.parser.ParserFieldEnum;
import com.jaeksoft.searchlib.parser.ParserSelector;
import com.jaeksoft.searchlib.streamlimiter.LimitException;

public class CrawlFile {

	private IndexDocument targetIndexDocument;
	private final FileInstanceAbstract fileInstance;
	private final FileItem fileItem;
	private Parser parser;
	private String error;
	private final FieldMap fileFieldMap;
	private final Config config;
	private final FileItemFieldEnum fileItemFieldEnum;

	public CrawlFile(FileInstanceAbstract fileInstance, FileItem fileItem,
			Config config, CrawlStatistics currentStats,
			FileItemFieldEnum fileItemFieldEnum) throws SearchLibException {
		this.targetIndexDocument = null;
		this.fileFieldMap = config.getFileCrawlerFieldMap();
		this.fileInstance = fileInstance;
		this.fileItem = fileItem;
		this.fileItem.setCrawlDate(System.currentTimeMillis());
		this.parser = null;
		this.error = null;
		this.config = config;
		this.fileItemFieldEnum = fileItemFieldEnum;
	}

	/**
	 * Télécharge le fichier et extrait les informations
	 * 
	 * @param userAgent
	 */
	public void download() {
		synchronized (this) {
			try {
				fileItem.setFetchStatus(FetchStatus.FETCHED);

				ParserSelector parserSelector = config.getParserSelector();
				Parser parser = parserSelector.getParser(
						fileItem.getFileName(), null);

				// Get default parser
				if (parser == null)
					parser = parserSelector.getFileCrawlerDefaultParser();

				// Parser Choice
				if (parser == null) {
					fileItem.setParserStatus(ParserStatus.NOPARSER);
					return;
				}

				fileItem.setParserStatus(ParserStatus.PARSED);

				IndexDocument sourceDocument = fileItem
						.getIndexDocument(fileItemFieldEnum);

				parser.setSourceDocument(sourceDocument);
				parser.parseContent(fileInstance, null);

				fileItem.setLang(parser.getFieldValue(ParserFieldEnum.lang, 0));

				this.parser = parser;

			} catch (MalformedURLException e) {
				fileItem.setFetchStatus(FetchStatus.ERROR);
			} catch (FileNotFoundException e) {
				fileItem.setFetchStatus(FetchStatus.GONE);
			} catch (LimitException e) {
				Logging.warn(e.toString() + " (" + fileItem.getUri() + ")", e);
				fileItem.setFetchStatus(FetchStatus.SIZE_EXCEED);
				setError(e.getMessage());
			} catch (InstantiationException e) {
				Logging.warn(e.getMessage(), e);
				fileItem.setParserStatus(ParserStatus.PARSER_ERROR);
				setError(e.getMessage());
			} catch (IllegalAccessException e) {
				Logging.warn(e.getMessage(), e);
				fileItem.setParserStatus(ParserStatus.PARSER_ERROR);
				setError(e.getMessage());
			} catch (ClassNotFoundException e) {
				Logging.warn(e.getMessage(), e);
				fileItem.setParserStatus(ParserStatus.PARSER_ERROR);
				setError(e.getMessage());
			} catch (IOException e) {
				Logging.warn(e.getMessage(), e);
				fileItem.setFetchStatus(FetchStatus.ERROR);
			} catch (Exception e) {
				Logging.warn(e.getMessage(), e);
				fileItem.setFetchStatus(FetchStatus.ERROR);
				fileItem.setParserStatus(ParserStatus.PARSER_ERROR);
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
			if (targetIndexDocument != null)
				return targetIndexDocument;

			targetIndexDocument = new IndexDocument();
			IndexDocument fileIndexDocument = fileItem
					.getIndexDocument(fileItemFieldEnum);
			fileFieldMap.mapIndexDocument(fileIndexDocument,
					targetIndexDocument);

			if (parser != null)
				parser.populate(targetIndexDocument);

			return targetIndexDocument;
		}
	}

	public void setError(String error) {
		this.error = error;
	}
}