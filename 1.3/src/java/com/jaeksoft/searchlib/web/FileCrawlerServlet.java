/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.file.database.FileInstanceType;
import com.jaeksoft.searchlib.crawler.file.database.FilePathItem;
import com.jaeksoft.searchlib.crawler.file.database.FilePathManager;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.user.User;
import com.jaeksoft.searchlib.util.ExtensibleEnum;

public class FileCrawlerServlet extends WebCrawlerServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3367169960498597933L;

	private void doCreateLocation(Client client, ServletTransaction transaction)
			throws SearchLibException {
		ExtensibleEnum<FileInstanceType> fileTypeEnum = client.getFileManager()
				.getFileTypeEnum();
		String fileType = transaction.getParameterString("type");
		if (fileType != null) {
			FilePathManager filePathManager = client.getFilePathManager();
			FilePathItem filePathItem = new FilePathItem(client);
			Boolean setDefault = setDefaultValues(transaction, filePathItem);
			if (setDefault) {
				Boolean isValidTypeInstance = getFileCrawlInstance(client,
						fileTypeEnum, filePathItem, transaction, fileType);
				FilePathItem checkFilePath = filePathManager.get(filePathItem);
				if (isValidTypeInstance && checkFilePath == null) {
					filePathManager.add(filePathItem);
				} else {
					transaction
							.addXmlResponse("Info",
									"The location already exists or it is not a valied instance type");
				}
			} else {
				transaction.addXmlResponse("Info", "Missing default values.");
			}
		} else
			transaction.addXmlResponse("Info",
					"FileCrawler type is needed to create an instance.");
	}

	private Boolean getFileCrawlInstance(Client client,
			ExtensibleEnum<FileInstanceType> fileTypeEnum,
			FilePathItem filePathItem, ServletTransaction transaction,
			String fileType) throws SearchLibException {
		FileInstanceType fileInstanceType = client.getFileManager()
				.findTypeByScheme(fileType);
		if (fileInstanceType != null) {
			filePathItem.setType(fileInstanceType);
			createFileCrawlInstance(fileInstanceType, client, filePathItem,
					transaction);
			transaction.addXmlResponse("Info",
					"A new file crawler instance is created.");
			return true;
		} else
			return false;
	}

	private void createFileCrawlInstance(FileInstanceType fileInstanceType,
			Client client, FilePathItem filePathItem,
			ServletTransaction transaction) {
		if (fileInstanceType.getScheme().equalsIgnoreCase("smb")) {
			String domain = transaction.getParameterString("domain");
			filePathItem.setDomain(domain);
		}
		String username = transaction.getParameterString("username");
		String password = transaction.getParameterString("password");
		String host = transaction.getParameterString("host");
		filePathItem.setPassword(password);
		filePathItem.setHost(host);
		filePathItem.setUsername(username);
	}

	private Boolean setDefaultValues(ServletTransaction transaction,
			FilePathItem filePathItem) {
		Boolean enabled = transaction.getParameterBoolean("enabled", false);
		Boolean ignoreHidden = transaction.getParameterBoolean("ignorehidden");
		Boolean withSubDirectory = transaction
				.getParameterBoolean("withsubdirectory");
		String path = transaction.getParameterString("path");
		Integer delayBetweenAccess = transaction.getParameterInteger("delay");
		if (enabled != null && ignoreHidden != null && withSubDirectory != null
				&& delayBetweenAccess != null && path != null) {
			filePathItem.setWithSubDir(withSubDirectory);
			filePathItem.setDelay(delayBetweenAccess);
			filePathItem.setEnabled(enabled);
			filePathItem.setPath(path);
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected void doRequest(ServletTransaction transaction)
			throws ServletException {
		try {
			User user = transaction.getLoggedUser();
			if (user != null
					&& !user.hasRole(transaction.getIndexName(),
							Role.FILE_CRAWLER_START_STOP))
				throw new SearchLibException("Not permitted");

			Client client = transaction.getClient();
			String cmd = transaction.getParameterString("cmd");
			if (cmd.equalsIgnoreCase("create")) {
				doCreateLocation(client, transaction);
			} else {
				doCrawlMaster(client.getFileCrawlMaster(), transaction);
			}
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

}
