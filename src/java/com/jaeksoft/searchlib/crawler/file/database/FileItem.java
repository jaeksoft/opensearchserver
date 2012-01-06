/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2011 Emmanuel Keller / Jaeksoft
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

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.file.process.FileInstanceAbstract;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.schema.FieldValueItem;
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
	private String lang;
	private String langMethod;
	private Long crawlDate;
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
		lang = null;
		langMethod = null;
		crawlDate = null;
	}

	public FileItem(ResultDocument doc) throws UnsupportedEncodingException,
			URISyntaxException {
		super(doc);

		setRepository(doc.getValueContent(
				FileItemFieldEnum.repository.getName(), 0));
		setDirectory(doc.getValueContent(FileItemFieldEnum.directory.getName(),
				0));
		setSubDirectory(FieldValueItem.buildArrayList(doc
				.getValueList(FileItemFieldEnum.subDirectory.getName())));

		setLang(doc.getValueContent(FileItemFieldEnum.lang.getName(), 0));

		setLangMethod(doc.getValueContent(
				FileItemFieldEnum.langMethod.getName(), 0));

		setCrawlDate(doc.getValueContent(FileItemFieldEnum.crawlDate.getName(),
				0));

		setFileSystemDate(doc.getValueContent(
				FileItemFieldEnum.fileSystemDate.getName(), 0));

		setFileExtension(doc.getValueContent(
				FileItemFieldEnum.fileExtension.getName(), 0));

	}

	public FileItem(FileInstanceAbstract fileInstance)
			throws SearchLibException {
		super(fileInstance);
		setRepository(fileInstance.getFilePathItem().toString());
		FileInstanceAbstract parentFileInstance = fileInstance.getParent();
		if (parentFileInstance != null)
			setDirectory(parentFileInstance.getURI());
		subDirectory = new ArrayList<String>();
		FileInstanceAbstract dir = fileInstance;
		while ((dir = dir.getParent()) != null)
			subDirectory.add(dir.getURI().getPath());
		setCrawlDate(System.currentTimeMillis());
		setFileExtension(FilenameUtils.getExtension(getUri()));
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

	@Override
	public void populate(IndexDocument indexDocument) {
		super.populate(indexDocument);
		indexDocument.setString(FileItemFieldEnum.repository.getName(),
				getRepository());

		indexDocument.setString(FileItemFieldEnum.uri.getName(), getUri());

		if (directory != null)
			indexDocument.setString(FileItemFieldEnum.directory.getName(),
					directory);

		indexDocument.setStringList(FileItemFieldEnum.subDirectory.getName(),
				getSubDirectory());

		if (crawlDate != null)
			indexDocument.setString(FileItemFieldEnum.crawlDate.getName(),
					StringUtils.longToHexString(crawlDate));

		if (lang != null)
			indexDocument.setString(FileItemFieldEnum.lang.getName(), lang);
		if (langMethod != null)
			indexDocument.setString(FileItemFieldEnum.langMethod.getName(),
					langMethod);

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

	public void setSubDirectory(List<String> subDirectoryList) {
		this.subDirectory = subDirectoryList;
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
			Logging.warn(e.getMessage());
		}
	}

}