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

package com.jaeksoft.searchlib.crawler.file.database;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.commons.io.FilenameUtils;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.crawler.common.database.FetchStatus;
import com.jaeksoft.searchlib.crawler.common.database.IndexStatus;
import com.jaeksoft.searchlib.crawler.common.database.ParserStatus;
import com.jaeksoft.searchlib.result.ResultDocument;

public class FileInfo {

	private Long fileSystemDate;
	private FileTypeEnum type;
	private String uriString;
	private String fileName;
	private FetchStatus fetchStatus;
	private ParserStatus parserStatus;
	private IndexStatus indexStatus;

	final static SimpleDateFormat getDateFormat() {
		return new SimpleDateFormat("yyyyMMddHHmmss");
	}

	public FileInfo() {
		init();
	}

	public FileInfo(ResultDocument doc, FileItemFieldEnum fieldItemFieldEnum)
			throws UnsupportedEncodingException, URISyntaxException {
		init();
		setFileSystemDate(doc.getValueContent(
				fieldItemFieldEnum.fileSystemDate.getName(), 0));
		String s = doc
				.getValueContent(fieldItemFieldEnum.fileType.getName(), 0);
		if (s != null)
			setType(FileTypeEnum.valueOf(s));
		setUri(doc.getValueContent(fieldItemFieldEnum.uri.getName(), 0));
		setFetchStatusInt(doc.getValueContent(
				fieldItemFieldEnum.fetchStatus.getName(), 0));
		setParserStatusInt(doc.getValueContent(
				fieldItemFieldEnum.parserStatus.getName(), 0));
		setIndexStatusInt(doc.getValueContent(
				fieldItemFieldEnum.indexStatus.getName(), 0));
	}

	protected void init() {
		fileSystemDate = null;
		type = null;
		uriString = null;
		fetchStatus = FetchStatus.UN_FETCHED;
		parserStatus = ParserStatus.NOT_PARSED;
		indexStatus = IndexStatus.NOT_INDEXED;
	}

	public Long getFileSystemDate() {
		return fileSystemDate;
	}

	public void setFileSystemDate(Long d) {
		fileSystemDate = d;
	}

	public void setFileSystemDate(String d) {
		if (d == null) {
			fileSystemDate = null;
			return;
		}
		try {
			fileSystemDate = getDateFormat().parse(d).getTime();
		} catch (NumberFormatException e) {
			Logging.warn(e.getMessage());
			fileSystemDate = null;
		} catch (ParseException e) {
			Logging.warn(e.getMessage());
			fileSystemDate = null;
		}
	}

	public FileTypeEnum getType() {
		return type;
	}

	public void setType(FileTypeEnum type) {
		this.type = type;
	}

	public String getUri() {
		return uriString;
	}

	private void setFileName(String fullPath) {
		this.fileName = FilenameUtils.getName(fullPath);
	}

	public String getFileName() {
		return fileName;
	}

	public void setUri(String uriString) throws URISyntaxException {
		this.uriString = uriString;
		setFileName(new URI(uriString).getPath());
	}

	public void setUri(URI uri) {
		this.uriString = uri.toASCIIString();
		setFileName(uri.getPath());
	}

	public FetchStatus getFetchStatus() {
		if (fetchStatus == null)
			return FetchStatus.UN_FETCHED;
		return fetchStatus;
	}

	public void setFetchStatus(FetchStatus status) {
		this.fetchStatus = status;
	}

	public void setFetchStatusInt(int v) {
		this.fetchStatus = FetchStatus.find(v);
	}

	private void setFetchStatusInt(String v) {
		if (v != null)
			setFetchStatusInt(Integer.parseInt(v));
	}

	public IndexStatus getIndexStatus() {
		if (indexStatus == null)
			return IndexStatus.NOT_INDEXED;
		return indexStatus;
	}

	public void setIndexStatus(IndexStatus status) {
		this.indexStatus = status;
	}

	public void setIndexStatusInt(int v) {
		this.indexStatus = IndexStatus.find(v);
	}

	private void setIndexStatusInt(String v) {
		if (v != null)
			setIndexStatusInt(Integer.parseInt(v));
	}

	public ParserStatus getParserStatus() {
		if (parserStatus == null)
			return ParserStatus.NOT_PARSED;
		return parserStatus;
	}

	public void setParserStatus(ParserStatus status) {
		this.parserStatus = status;
	}

	public void setParserStatusInt(int v) {
		this.parserStatus = ParserStatus.find(v);
	}

	private void setParserStatusInt(String v) {
		if (v != null)
			setParserStatusInt(Integer.parseInt(v));
	}

	public boolean isStatusFull() {
		return fetchStatus == FetchStatus.FETCHED
				&& parserStatus == ParserStatus.PARSED
				&& indexStatus == IndexStatus.INDEXED;
	}

	/**
	 * Test if a new crawl is needed
	 */
	public boolean isNewCrawlNeeded(FileInfo newFileInfo) {
		if (!isStatusFull())
			return true;
		if (fileSystemDate == null)
			return true;
		if (type == null)
			return true;
		if (!fileSystemDate.equals(newFileInfo.fileSystemDate))
			return true;
		if (type != newFileInfo.type)
			return true;
		return false;
	}

}
