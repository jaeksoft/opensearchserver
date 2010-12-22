/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2010 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.file.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;

import com.jaeksoft.searchlib.crawler.common.database.FetchStatus;
import com.jaeksoft.searchlib.crawler.common.database.IndexStatus;
import com.jaeksoft.searchlib.crawler.common.database.ParserStatus;
import com.jaeksoft.searchlib.crawler.file.process.FileInstanceAbstract;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.util.StringUtils;

public class FileItem implements Serializable {

	public static final String UTF_8_ENCODING = "UTF-8";

	public enum Status {
		UNDEFINED("Undefined"), INJECTED("Injected"), ALREADY(
				"Already injected"), ERROR("Unknown Error");

		private String name;

		private Status(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -4043010587042224473L;

	final private static Logger logger = Logger.getLogger(FileItem.class
			.getCanonicalName());

	final static DecimalFormat getContentLengthFormat() {
		return new DecimalFormat("00000000000000");
	}

	final static SimpleDateFormat getDateFormat() {
		return new SimpleDateFormat("yyyyMMddHHmmss");
	}

	final static SimpleDateFormat getTecnhicalDateFormat() {
		return new SimpleDateFormat("yyyyMMddHHmmssSSS");
	}

	public final static SimpleDateFormat getNiceDateFormat() {
		return new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
	}

	public URI uri;
	public URI directory;
	public List<String> subDirectory;
	private Integer contentLength;
	private String lang;
	private String langMethod;
	private Date when;
	private FetchStatus fetchStatus;
	private ParserStatus parserStatus;
	private IndexStatus indexStatus;
	private String fileSystemDate;
	private long crawlDate;
	private long size;
	private String extension;
	private FileTypeEnum type;

	private final int count;

	private Status status;

	protected FileItem() {
		status = Status.UNDEFINED;
		uri = null;
		directory = null;
		subDirectory = null;
		contentLength = null;
		lang = null;
		langMethod = null;
		when = new Date();
		fetchStatus = FetchStatus.UN_FETCHED;
		parserStatus = ParserStatus.NOT_PARSED;
		indexStatus = IndexStatus.NOT_INDEXED;
		count = 0;
		crawlDate = 0;
		fileSystemDate = "0";
		size = 0;
		extension = "";
		type = null;
	}

	public FileItem(ResultDocument doc) throws UnsupportedEncodingException,
			URISyntaxException {
		this();

		setURI(parseValueToURI(doc.getValue(FileItemFieldEnum.uri.name(), 0)));
		setDirectory(parseValueToURI(doc.getValue(
				FileItemFieldEnum.directory.name(), 0)));
		setSubDirectory(doc.getValueList(FileItemFieldEnum.directory.name()));

		setContentLength(doc
				.getValue(FileItemFieldEnum.contentLength.name(), 0));

		setLang(doc.getValue(FileItemFieldEnum.lang.name(), 0));

		setLangMethod(doc.getValue(FileItemFieldEnum.langMethod.name(), 0));

		setWhen(doc.getValue(FileItemFieldEnum.when.name(), 0));

		setFetchStatusInt(doc.getValue(FileItemFieldEnum.fetchStatus.name(), 0));

		setParserStatusInt(doc.getValue(FileItemFieldEnum.parserStatus.name(),
				0));

		setIndexStatusInt(doc.getValue(FileItemFieldEnum.indexStatus.name(), 0));

		setCrawlDate(doc.getValue(FileItemFieldEnum.crawlDate.name(), 0));

		setFileSystemDate(doc.getValue(FileItemFieldEnum.fileSystemDate.name(),
				0));

		setSize(doc.getValue(FileItemFieldEnum.fileSize.name(), 0));

		setExtension(doc.getValue(FileItemFieldEnum.fileExtension.name(), 0));

		String s = doc.getValue(FileItemFieldEnum.fileType.name(), 0);
		if (s != null)
			setType(FileTypeEnum.valueOf(s));
	}

	public FileItem(FileInstanceAbstract fileInstance) {
		this();
		setURI(fileInstance.getURI());
		FileInstanceAbstract parentFileInstance = fileInstance.getParent();
		if (parentFileInstance != null)
			setDirectory(parentFileInstance.getURI());
		subDirectory = new ArrayList<String>();
		FileInstanceAbstract dir = fileInstance;
		while ((dir = dir.getParent()) != null)
			subDirectory.add(dir.getURI().getPath());
		setCrawlDate(System.currentTimeMillis());
		setFileSystemDate(fileInstance.getLastModified());
		setSize(fileInstance.getFileSize());
		setExtension(FilenameUtils.getExtension(uri.toASCIIString()));
		setType(fileInstance.getFileType());
	}

	public Integer getContentLength() {
		return contentLength;
	}

	public String getCount() {
		return Integer.toString(count);
	}

	public FetchStatus getFetchStatus() {
		if (fetchStatus == null)
			return FetchStatus.UN_FETCHED;
		return fetchStatus;
	}

	public String getFullLang() {
		StringBuffer sb = new StringBuffer();
		if (lang != null)
			sb.append(lang);
		if (langMethod != null) {
			sb.append('(');
			sb.append(langMethod);
			sb.append(')');
		}
		return sb.toString();
	}

	public IndexDocument getIndexDocument() throws UnsupportedEncodingException {
		IndexDocument indexDocument = new IndexDocument();
		populate(indexDocument);
		return indexDocument;
	}

	public IndexStatus getIndexStatus() {
		if (indexStatus == null)
			return IndexStatus.NOT_INDEXED;
		return indexStatus;
	}

	public String getLang() {
		return lang;
	}

	public String getLangMethod() {
		return langMethod;
	}

	public URI getDirectory() {
		return directory;
	}

	public List<String> getSubDirectory() {
		return subDirectory;
	}

	public ParserStatus getParserStatus() {
		if (parserStatus == null)
			return ParserStatus.NOT_PARSED;
		return parserStatus;
	}

	public URI getURI() {
		return uri;
	}

	public Status getStatus() {
		return status;
	}

	public Date getWhen() {
		return when;
	}

	public boolean isStatusFull() {
		return fetchStatus == FetchStatus.FETCHED
				&& parserStatus == ParserStatus.PARSED
				&& indexStatus == IndexStatus.INDEXED;
	}

	public void populate(IndexDocument indexDocument)
			throws UnsupportedEncodingException {

		indexDocument.set(FileItemFieldEnum.uri.name(), getURI()
				.toASCIIString());

		indexDocument.set(FileItemFieldEnum.directory.name(), getDirectory()
				.toASCIIString());

		indexDocument.set(FileItemFieldEnum.subDirectory.name(),
				getSubDirectory());

		if (when != null)
			indexDocument.set(FileItemFieldEnum.when.name(), getDateFormat()
					.format(when));

		indexDocument.set(FileItemFieldEnum.crawlDate.name(), crawlDate);
		indexDocument.set(FileItemFieldEnum.fileSystemDate.name(),
				fileSystemDate);

		if (contentLength != null)
			indexDocument.set(FileItemFieldEnum.contentLength.name(),
					getContentLengthFormat().format(contentLength));
		if (lang != null)
			indexDocument.set(FileItemFieldEnum.lang.name(), lang);
		if (langMethod != null)
			indexDocument.set(FileItemFieldEnum.langMethod.name(), langMethod);

		indexDocument.set(FileItemFieldEnum.fetchStatus.name(),
				fetchStatus.value);
		indexDocument.set(FileItemFieldEnum.parserStatus.name(),
				parserStatus.value);
		indexDocument.set(FileItemFieldEnum.indexStatus.name(),
				indexStatus.value);
		indexDocument.set(FileItemFieldEnum.fileSize.name(), getSize());
		if (extension != null)
			indexDocument
					.set(FileItemFieldEnum.fileExtension.name(), extension);
		if (type != null)
			indexDocument.set(FileItemFieldEnum.fileType.name(), type.name());
	}

	public void setContentLength(int v) {
		contentLength = v;
	}

	private void setContentLength(String v) {
		if (v == null)
			return;
		if (v.length() == 0)
			return;
		try {
			contentLength = getContentLengthFormat().parse(v).intValue();
		} catch (ParseException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
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

	public void setLang(String lang) {
		this.lang = lang;
	}

	public void setLangMethod(String langMethod) {
		this.langMethod = langMethod;
	}

	public void setDirectory(URI directory) {
		this.directory = directory;
	}

	public void setSubDirectory(List<String> subDirectory) {
		this.subDirectory = subDirectory;
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

	public void setURI(URI uri) {
		this.uri = uri;
	}

	public void setStatus(Status v) {
		status = v;
	}

	public void setWhen(Date d) {
		if (d == null) {
			setWhenNow();
			return;
		}
		when = d;
	}

	public void setWhen(String d) {
		if (d == null) {
			setWhenNow();
			return;
		}
		try {
			when = getDateFormat().parse(d);
		} catch (ParseException e) {
			logger.log(Level.WARNING, e.getMessage(), e);
			setWhenNow();
		}
	}

	public void setWhenNow() {
		setWhen(new Date(System.currentTimeMillis()));
	}

	public void setCrawlDate(long d) {
		crawlDate = d;
	}

	public void setCrawlDate(String d) {
		if (d == null)
			return;

		try {
			crawlDate = Long.parseLong(d);
		} catch (NumberFormatException e) {
			logger.log(Level.WARNING, e.getMessage(), e);
			setWhenNow();
		}
	}

	public String getFileSystemDate() {
		return fileSystemDate;
	}

	public void setFileSystemDate(String d) {
		fileSystemDate = d;
	}

	public void setFileSystemDate(long d) {
		fileSystemDate = getTecnhicalDateFormat().format(new Date(d));
	}

	public long getSize() {
		return size;
	}

	public String getHumanSize() {
		return StringUtils.humanBytes(size);
	}

	public void setSize(long size) {
		this.size = size;
	}

	public void setSize(String size) {
		try {
			this.size = Long.parseLong(size);
		} catch (NumberFormatException e) {
			this.size = 0;
		}
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public FileTypeEnum getType() {
		return type;
	}

	public void setType(FileTypeEnum type) {
		this.type = type;
	}

	/**
	 * Test if a new crawl is needed
	 * 
	 * @throws ParseException
	 */
	public boolean isNewCrawlNeeded(long dateModified) throws ParseException {
		return getTecnhicalDateFormat().parse(fileSystemDate).getTime() != dateModified;
	}

	public File getFile() {
		if (this.getURI() != null)
			return new File(this.getURI());
		return null;
	}

	public FileInputStream getFileInputStream() throws FileNotFoundException {
		if (this.getURI() != null)
			return new FileInputStream(getFile());
		return null;
	}

	public final static URI parseValueToURI(String value)
			throws URISyntaxException {
		if (value != null)
			return new URI(value);
		return null;
	}

}