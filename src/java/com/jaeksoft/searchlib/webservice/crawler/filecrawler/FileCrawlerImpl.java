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
package com.jaeksoft.searchlib.webservice.crawler.filecrawler;

import java.io.IOException;

import javax.xml.ws.WebServiceException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.file.database.FilePathItem;
import com.jaeksoft.searchlib.crawler.file.database.FilePathManager;
import com.jaeksoft.searchlib.crawler.file.process.CrawlFileMaster;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.CommonServices;
import com.jaeksoft.searchlib.webservice.crawler.CrawlerUtils;

public class FileCrawlerImpl extends CommonServices implements SoapFileCrawler {

	private CrawlFileMaster getCrawlMaster(String use, String login, String key) {
		try {
			ClientFactory.INSTANCE.properties.checkApi();
			Client client = getLoggedClient(use, login, key,
					Role.FILE_CRAWLER_START_STOP);
			return client.getFileCrawlMaster();
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		} catch (InterruptedException e) {
			throw new WebServiceException(e);
		} catch (IOException e) {
			throw new WebServiceException(e);
		}
	}

	@Override
	public CommonResult runOnce(String use, String login, String key) {
		return CrawlerUtils.runOnce(getCrawlMaster(use, login, key));
	}

	@Override
	public CommonResult runForever(String use, String login, String key) {
		return CrawlerUtils.runForever(getCrawlMaster(use, login, key));
	}

	@Override
	public CommonResult stop(String use, String login, String key) {
		return CrawlerUtils.stop(getCrawlMaster(use, login, key));
	}

	@Override
	public CommonResult status(String use, String login, String key) {
		return CrawlerUtils.status(getCrawlMaster(use, login, key));
	}

	@Override
	public CommonResult injectRepository(String use, String login, String key,
			String filePath, Boolean ignoreHiddenFile,
			Boolean withSubDirectory, Boolean enabled, int delay,
			String username, String password, String domain, String host) {
		String message = null;
		try {
			ClientFactory.INSTANCE.properties.checkApi();
			Client client = getLoggedClient(username, login, key,
					Role.FILE_CRAWLER_EDIT_PARAMETERS);
			FilePathItem filePathItem = new FilePathItem(client);
			FilePathItem currentFilePath = new FilePathItem(client);
			filePathItem.setPath(filePath);
			filePathItem.setWithSubDir(withSubDirectory);
			filePathItem.setEnabled(enabled);
			filePathItem.setDelay(delay);
			if (username != null && username.trim().equals(""))
				filePathItem.setUsername(username);
			if (password != null && password.equals(""))
				filePathItem.setPassword(password);
			if (domain != null && domain.equals(""))
				filePathItem.setDomain(domain);
			if (host != null && host.equals(""))
				filePathItem.setHost(host);
			message = filePattern(client, currentFilePath, filePathItem);
			return new CommonResult(true, message);
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		} catch (InterruptedException e) {
			throw new WebServiceException(e);
		} catch (IOException e) {
			throw new WebServiceException(e);
		}
	}

	private String filePattern(Client client, FilePathItem currentFilePath,
			FilePathItem filePathItem) throws SearchLibException {
		String message = null;

		FilePathManager filePathManager = client.getFilePathManager();
		FilePathItem checkFilePath = filePathManager.get(currentFilePath);
		if (filePathItem == null) {
			if (checkFilePath != null) {
				new WebServiceException("The location already exists");
			}
		} else {
			if (checkFilePath != null)
				if (checkFilePath.hashCode() != filePathItem.hashCode()) {
					new WebServiceException("The location already exists");
				}
			filePathManager.remove(filePathItem);
		}
		filePathManager.add(filePathItem);
		message = "File pattern Added";
		return message;
	}
}
