/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2015 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.webservice.crawler.filecrawler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.jaeksoft.searchlib.crawler.file.database.FilePathItem;
import com.jaeksoft.searchlib.util.StringUtils;

@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(Include.NON_NULL)
public class FilePathResult {

	public enum FileRepositoryType {

		LOCAL,

		SMB_CIFS,

		FTP,

		FTPS,

		DROPBOX,

		SWIFT;

	}

	public final FileRepositoryType type;
	public final String scheme;
	public final String host;
	public final String path;
	public final Boolean enabled;

	/**
	 * For CIFS/SMB
	 */
	public final String domain;

	/**
	 * For SWIFT
	 */
	public static enum SwiftAuthType {

		KEYSTONE,

		IAM;
	}

	public final SwiftAuthType swiftAuthType;
	public final String swiftTenant;
	public final String swiftAuthURL;
	public final String swiftContainer;

	public final String username;

	public final Boolean withSubDirectory;
	public final Boolean ignoreHiddenFiles;
	public final List<String> exclusionPatterns;
	public final Integer delayPerFile;

	// For FTP
	public final Boolean ftpUsePassiveMode;

	public FilePathResult() {
		type = null;
		scheme = null;
		host = null;
		path = null;
		enabled = null;
		swiftAuthType = null;
		swiftTenant = null;
		swiftAuthURL = null;
		swiftContainer = null;
		username = null;
		domain = null;
		withSubDirectory = null;
		ignoreHiddenFiles = null;
		exclusionPatterns = null;
		delayPerFile = null;
		ftpUsePassiveMode = null;
	}

	public FilePathResult(FilePathItem filePathItem) {
		scheme = filePathItem.getType().getScheme();
		host = filePathItem.getHost();
		path = filePathItem.getPath();
		enabled = filePathItem.isEnabled();
		if (filePathItem.getType() == null)
			type = null;
		else
			switch (filePathItem.getType()) {
			case Local:
				type = FileRepositoryType.LOCAL;
				break;
			case Smb:
				type = FileRepositoryType.SMB_CIFS;
				break;
			case Ftp:
				type = FileRepositoryType.FTP;
				break;
			case Ftps:
				type = FileRepositoryType.FTPS;
				break;
			case Dropbox:
				type = FileRepositoryType.DROPBOX;
				break;
			case Swift:
				type = FileRepositoryType.SWIFT;
				break;
			default:
				type = null;
				break;
			}
		if (filePathItem.getSwiftAuthType() == null)
			swiftAuthType = null;
		else {
			switch (filePathItem.getSwiftAuthType()) {
			case IAM:
				swiftAuthType = SwiftAuthType.IAM;
				break;
			case KEYSTONE:
				swiftAuthType = SwiftAuthType.KEYSTONE;
				break;
			default:
				swiftAuthType = null;
				break;
			}
		}
		swiftTenant = filePathItem.getSwiftTenant();
		swiftAuthURL = filePathItem.getSwiftAuthURL();
		swiftContainer = filePathItem.getSwiftContainer();
		username = filePathItem.getUsername();
		domain = filePathItem.getDomain();
		withSubDirectory = filePathItem.isWithSubDir();
		ignoreHiddenFiles = filePathItem.isIgnoreHiddenFiles();
		exclusionPatterns = filePathItem.getExclusionPatterns() == null ? null
				: Arrays.asList(StringUtils.splitLines(filePathItem.getExclusionPatterns()));
		delayPerFile = filePathItem.getDelay();
		ftpUsePassiveMode = type != null && (type == FileRepositoryType.FTP || type == FileRepositoryType.FTPS)
				? filePathItem.isFtpUsePassiveMode() : null;
	}

	public static List<FilePathResult> create(List<FilePathItem> filePathItems) {
		List<FilePathResult> results = new ArrayList<FilePathResult>(filePathItems == null ? 0 : filePathItems.size());
		if (filePathItems != null)
			for (FilePathItem filePathItem : filePathItems)
				results.add(new FilePathResult(filePathItem));
		return results;
	}

}
