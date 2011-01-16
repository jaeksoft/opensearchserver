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

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.file.process.FileInstanceAbstract;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.util.StringUtils;

public class FileItem extends FileInfo implements Serializable {

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

	final static DecimalFormat getContentLengthFormat() {
		return new DecimalFormat("00000000000000");
	}

	final static SimpleDateFormat getDateFormat() {
		return new SimpleDateFormat("yyyyMMddHHmmss");
	}

	public final static SimpleDateFormat getNiceDateFormat() {
		return new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
	}

	private String repository;
	private String directory;
	private List<String> subDirectory;
	private Integer contentLength;
	private String lang;
	private String langMethod;
	private Long crawlDate;
	private Long size;
	private String extension;
	private Status status;

	protected FileItem() {
		super();
	}

	@Override
	protected void init() {
		super.init();
		status = Status.UNDEFINED;
		repository = null;
		directory = null;
		subDirectory = null;
		contentLength = null;
		lang = null;
		langMethod = null;
		crawlDate = null;
		size = null;
		extension = "";
	}

	public FileItem(ResultDocument doc) throws UnsupportedEncodingException,
			URISyntaxException {
		super(doc);

		setRepository(doc.getValue(FileItemFieldEnum.repository.name(), 0));
		setDirectory(doc.getValue(FileItemFieldEnum.directory.name(), 0));
		setSubDirectory(doc.getValueList(FileItemFieldEnum.directory.name()));

		setContentLength(doc
				.getValue(FileItemFieldEnum.contentLength.name(), 0));

		setLang(doc.getValue(FileItemFieldEnum.lang.name(), 0));

		setLangMethod(doc.getValue(FileItemFieldEnum.langMethod.name(), 0));

		setCrawlDate(doc.getValue(FileItemFieldEnum.crawlDate.name(), 0));

		setSize(doc.getValue(FileItemFieldEnum.fileSize.name(), 0));

		setExtension(doc.getValue(FileItemFieldEnum.fileExtension.name(), 0));

	}

	public FileItem(FileInstanceAbstract fileInstance)
			throws SearchLibException {
		this();
		setRepository(fileInstance.getFilePathItem().toString());
		setUri(fileInstance.getURI());
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
		setExtension(FilenameUtils.getExtension(getUri()));
		setType(fileInstance.getFileType());
	}

	public Integer getContentLength() {
		return contentLength;
	}

	public Long getCrawlDate() {
		return crawlDate;
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

	public String getLang() {
		return lang;
	}

	public String getLangMethod() {
		return langMethod;
	}

	public String getDirectory() {
		return directory;
	}

	public List<String> getSubDirectory() {
		return subDirectory;
	}

	public Status getStatus() {
		return status;
	}

	public void populate(IndexDocument indexDocument)
			throws UnsupportedEncodingException {

		indexDocument.set(FileItemFieldEnum.repository.name(), getRepository());

		indexDocument.set(FileItemFieldEnum.uri.name(), getUri());

		if (directory != null)
			indexDocument.set(FileItemFieldEnum.directory.name(), directory);

		indexDocument.set(FileItemFieldEnum.subDirectory.name(),
				getSubDirectory());

		if (crawlDate != null)
			indexDocument.set(FileItemFieldEnum.crawlDate.name(),
					StringUtils.longToHexString(crawlDate));

		Long fsd = getFileSystemDate();
		if (fsd != null)
			indexDocument.set(FileItemFieldEnum.fileSystemDate.name(),
					StringUtils.longToHexString(fsd));

		if (contentLength != null)
			indexDocument.set(FileItemFieldEnum.contentLength.name(),
					getContentLengthFormat().format(contentLength));
		if (lang != null)
			indexDocument.set(FileItemFieldEnum.lang.name(), lang);
		if (langMethod != null)
			indexDocument.set(FileItemFieldEnum.langMethod.name(), langMethod);

		indexDocument.set(FileItemFieldEnum.fetchStatus.name(),
				getFetchStatus().value);
		indexDocument.set(FileItemFieldEnum.parserStatus.name(),
				getParserStatus().value);
		indexDocument.set(FileItemFieldEnum.indexStatus.name(),
				getIndexStatus().value);
		if (size != null)
			indexDocument.set(FileItemFieldEnum.fileSize.name(), size);
		if (extension != null)
			indexDocument
					.set(FileItemFieldEnum.fileExtension.name(), extension);
		FileTypeEnum t = getType();
		if (t != null)
			indexDocument.set(FileItemFieldEnum.fileType.name(), t.name());
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
			Logging.logger.error(e.getMessage());
		}
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public void setLangMethod(String langMethod) {
		this.langMethod = langMethod;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public void setDirectory(URI directoryUri) {
		this.directory = directoryUri.toASCIIString();
	}

	public void setSubDirectory(List<String> subDirectory) {
		this.subDirectory = subDirectory;
	}

	public String getRepository() {
		return repository;
	}

	public void setRepository(String r) {
		this.repository = r;
	}

	public void setStatus(Status v) {
		status = v;
	}

	public void setCrawlDate(long d) {
		crawlDate = d;
	}

	public void setCrawlDate(String d) {
		if (d == null)
			return;
		try {
			crawlDate = StringUtils.hexStringToLong(d);
		} catch (NumberFormatException e) {
			Logging.logger.warn(e.getMessage());
		}
	}

	public Long getSize() {
		return size;
	}

	public String getHumanSize() {
		return StringUtils.humanBytes(size);
	}

	public void setSize(Long size) {
		this.size = size;
	}

	public void setSize(String size) {
		try {
			this.size = new Long(size);
		} catch (NumberFormatException e) {
			this.size = null;
		}
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

}