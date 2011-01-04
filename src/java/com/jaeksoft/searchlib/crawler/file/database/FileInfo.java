/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.io.FilenameUtils;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.util.StringUtils;

public class FileInfo {

	private Long fileSystemDate;

	private FileTypeEnum type;

	private String uriString;

	private String name;

	public FileInfo() {
		init();
	}

	public FileInfo(ResultDocument doc) throws UnsupportedEncodingException,
			URISyntaxException {
		this();
		setFileSystemDate(doc.getValue(FileItemFieldEnum.fileSystemDate.name(),
				0));
		String s = doc.getValue(FileItemFieldEnum.fileType.name(), 0);
		if (s != null)
			setType(FileTypeEnum.valueOf(s));
		setUri(doc.getValue(FileItemFieldEnum.uri.name(), 0));
	}

	protected void init() {
		fileSystemDate = null;
		type = null;
		uriString = null;
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
			fileSystemDate = StringUtils.hexStringToLong(d);
		} catch (NumberFormatException e) {
			Logging.logger.warn(e.getMessage());
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

	private void setName(String fullPath) {
		this.name = FilenameUtils.getBaseName(fullPath);
	}

	public String getName() {
		return name;
	}

	public void setUri(String uriString) throws URISyntaxException {
		this.uriString = uriString;
		setName(new URI(uriString).getPath());
	}

	public void setUri(URI uri) {
		this.uriString = uri.toASCIIString();
		setName(uri.getPath());
	}

	/**
	 * Test if a new crawl is needed
	 */
	public boolean isNewCrawlNeeded(FileInfo newFileInfo) {
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
