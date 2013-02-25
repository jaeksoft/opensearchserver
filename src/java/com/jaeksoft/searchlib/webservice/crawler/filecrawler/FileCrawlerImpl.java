/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2013 Emmanuel Keller / Jaeksoft
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

import javax.jws.WebParam;
import javax.xml.ws.WebServiceException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.file.database.FileInstanceType;
import com.jaeksoft.searchlib.crawler.file.database.FilePathItem;
import com.jaeksoft.searchlib.crawler.file.database.FilePathManager;
import com.jaeksoft.searchlib.crawler.file.process.CrawlFileMaster;
import com.jaeksoft.searchlib.crawler.file.process.fileInstances.swift.SwiftToken.AuthType;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.CommonServices;
import com.jaeksoft.searchlib.webservice.crawler.CrawlerUtils;

public class FileCrawlerImpl extends CommonServices implements SoapFileCrawler,
		RestFileCrawler {

	private CrawlFileMaster getCrawlMaster(String use, String login, String key) {
		try {
			Client client = getLoggedClient(use, login, key,
					Role.FILE_CRAWLER_START_STOP);
			ClientFactory.INSTANCE.properties.checkApi();
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

	private CommonResult injectRepository(String use, String login, String key,
			FileInstanceType type, String path, Boolean ignoreHiddenFile,
			Boolean withSubDirectory, Boolean enabled, int delay,
			String username, String password, String domain, String host,
			String swiftContainer, String swiftTenant, String swiftAuthURL,
			AuthType swiftAuthType) {
		try {
			Client client = getLoggedClient(use, login, key,
					Role.FILE_CRAWLER_EDIT_PARAMETERS);
			ClientFactory.INSTANCE.properties.checkApi();
			FilePathItem filePathItem = new FilePathItem(client);
			filePathItem.setType(type);
			filePathItem.setPath(path);
			filePathItem.setIgnoreHiddenFiles(ignoreHiddenFile);
			filePathItem.setWithSubDir(withSubDirectory);
			filePathItem.setEnabled(enabled);
			filePathItem.setDelay(delay);
			if (username != null)
				filePathItem.setUsername(username);
			if (password != null)
				filePathItem.setPassword(password);
			if (domain != null)
				filePathItem.setDomain(domain);
			if (host != null)
				filePathItem.setHost(host);
			if (swiftContainer != null)
				filePathItem.setSwiftContainer(swiftContainer);
			if (swiftTenant != null)
				filePathItem.setSwiftTenant(swiftTenant);
			if (swiftAuthURL != null)
				filePathItem.setSwiftAuthURL(swiftAuthURL);
			if (swiftAuthType != null)
				filePathItem.setSwiftAuthType(swiftAuthType);
			FilePathManager filePathManager = client.getFilePathManager();
			FilePathItem checkFilePathItem = filePathManager.get(filePathItem);
			if (checkFilePathItem != null)
				filePathManager.remove(checkFilePathItem);
			filePathManager.add(filePathItem);
			return new CommonResult(true,
					checkFilePathItem == null ? "Inserted" : "Updated");
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		} catch (InterruptedException e) {
			throw new WebServiceException(e);
		} catch (IOException e) {
			throw new WebServiceException(e);
		}
	}

	@Override
	public CommonResult injectLocalFileRepository(String use, String login,
			String key, String path, Boolean ignoreHiddenFile,
			Boolean withSubDirectory, Boolean enabled, int delay) {
		return injectRepository(use, login, key, FileInstanceType.Local, path,
				ignoreHiddenFile, withSubDirectory, enabled, delay, null, null,
				null, null, null, null, null, null);
	}

	private CommonResult removeFileRepository(String use, String login,
			String key, FileInstanceType type, String path, String username,
			String domain, String host, String swiftContainer) {
		try {
			Client client = getLoggedClient(use, login, key,
					Role.FILE_CRAWLER_EDIT_PARAMETERS);
			ClientFactory.INSTANCE.properties.checkApi();
			FilePathItem filePathItem = new FilePathItem(client);
			filePathItem.setType(type);
			filePathItem.setPath(path);
			if (username != null)
				filePathItem.setUsername(username);
			if (domain != null)
				filePathItem.setDomain(domain);
			if (host != null)
				filePathItem.setHost(host);
			if (swiftContainer != null)
				filePathItem.setSwiftContainer(swiftContainer);
			FilePathManager filePathManager = client.getFilePathManager();
			FilePathItem checkFilePathItem = filePathManager.get(filePathItem);
			if (checkFilePathItem == null)
				throw new WebServiceException("Nothing to delete");
			filePathManager.remove(checkFilePathItem);
			return new CommonResult(true, "");
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		} catch (InterruptedException e) {
			throw new WebServiceException(e);
		} catch (IOException e) {
			throw new WebServiceException(e);
		}
	}

	@Override
	public CommonResult removeLocalFileRepository(String use, String login,
			String key, String path) {
		return removeFileRepository(use, login, key, FileInstanceType.Local,
				path, null, null, null, null);
	}

	@Override
	public CommonResult injectSmbRepository(@WebParam(name = "use") String use,
			String login, String key, String path, Boolean ignoreHiddenFile,
			Boolean withSubDirectory, Boolean enabled, int delay,
			String username, String password, String domain, String host) {
		return injectRepository(use, login, key, FileInstanceType.Smb, path,
				ignoreHiddenFile, withSubDirectory, enabled, delay, username,
				password, domain, host, null, null, null, null);
	}

	@Override
	public CommonResult removeSmbRepository(@WebParam(name = "use") String use,
			String login, String key, String path, String username,
			String domain, String host) {
		return removeFileRepository(use, login, key, FileInstanceType.Smb,
				path, username, domain, host, null);
	}

	@Override
	public CommonResult injectFtpRepository(String use, String login,
			String key, String path, Boolean ignoreHiddenFile,
			Boolean withSubDirectory, Boolean enabled, int delay,
			String username, String password, String domain, String host,
			boolean ssl) {
		return injectRepository(use, login, key, ssl ? FileInstanceType.Ftps
				: FileInstanceType.Ftp, path, ignoreHiddenFile,
				withSubDirectory, enabled, delay, username, password, domain,
				host, null, null, null, null);
	}

	@Override
	public CommonResult removeFtpRepository(@WebParam(name = "use") String use,
			String login, String key, String path, String username,
			String host, boolean ssl) {
		return removeFileRepository(use, login, key,
				ssl ? FileInstanceType.Ftps : FileInstanceType.Ftp, path,
				username, null, host, null);
	}

	@Override
	public CommonResult runOnceXML(String use, String login, String key) {
		return runOnce(use, login, key);
	}

	@Override
	public CommonResult runOnceJSON(String use, String login, String key) {
		return runOnce(use, login, key);
	}

	@Override
	public CommonResult runForeverXML(String use, String login, String key) {
		return runForever(use, login, key);
	}

	@Override
	public CommonResult runForeverJSON(String use, String login, String key) {
		return runForever(use, login, key);
	}

	@Override
	public CommonResult stopXML(String use, String login, String key) {
		return stop(use, login, key);
	}

	@Override
	public CommonResult stopJSON(String use, String login, String key) {
		return stop(use, login, key);
	}

	@Override
	public CommonResult statusXML(String use, String login, String key) {
		return status(use, login, key);
	}

	@Override
	public CommonResult statusJSON(String use, String login, String key) {
		return status(use, login, key);
	}

	@Override
	public CommonResult injectLocalFileRepositoryXML(String use, String login,
			String key, String path, Boolean ignoreHiddenFile,
			Boolean withSubDirectory, Boolean enabled, int delay) {
		return injectLocalFileRepository(use, login, key, path,
				ignoreHiddenFile, withSubDirectory, enabled, delay);
	}

	@Override
	public CommonResult injectLocalFileRepositoryJSON(String use, String login,
			String key, String path, Boolean ignoreHiddenFile,
			Boolean withSubDirectory, Boolean enabled, int delay) {
		return injectLocalFileRepository(use, login, key, path,
				ignoreHiddenFile, withSubDirectory, enabled, delay);
	}

	@Override
	public CommonResult removeLocalFileRepositoryXML(String use, String login,
			String key, String path) {
		return removeLocalFileRepository(use, login, key, path);
	}

	@Override
	public CommonResult removeLocalFileRepositoryJSON(String use, String login,
			String key, String path) {
		return removeLocalFileRepository(use, login, key, path);
	}

	@Override
	public CommonResult injectSmbRepositoryXML(String use, String login,
			String key, String path, Boolean ignoreHiddenFile,
			Boolean withSubDirectory, Boolean enabled, int delay,
			String username, String password, String domain, String host) {
		return injectSmbRepository(use, login, key, path, ignoreHiddenFile,
				withSubDirectory, enabled, delay, username, password, domain,
				host);
	}

	@Override
	public CommonResult injectSmbRepositoryJSON(String use, String login,
			String key, String path, Boolean ignoreHiddenFile,
			Boolean withSubDirectory, Boolean enabled, int delay,
			String username, String password, String domain, String host) {
		return injectSmbRepository(use, login, key, path, ignoreHiddenFile,
				withSubDirectory, enabled, delay, username, password, domain,
				host);
	}

	@Override
	public CommonResult removeSmbRepositoryXML(String use, String login,
			String key, String path, String username, String domain, String host) {
		return removeSmbRepository(use, login, key, path, username, domain,
				host);
	}

	@Override
	public CommonResult removeSmbRepositoryJSON(String use, String login,
			String key, String path, String username, String domain, String host) {
		return removeSmbRepository(use, login, key, path, username, domain,
				host);
	}

	@Override
	public CommonResult injectFtpRepositoryXML(String use, String login,
			String key, String path, Boolean ignoreHiddenFile,
			Boolean withSubDirectory, Boolean enabled, int delay,
			String username, String password, String domain, String host,
			boolean ssl) {
		return injectFtpRepository(use, login, key, path, ignoreHiddenFile,
				withSubDirectory, enabled, delay, username, password, domain,
				host, ssl);
	}

	@Override
	public CommonResult injectFtpRepositoryJSON(String use, String login,
			String key, String path, Boolean ignoreHiddenFile,
			Boolean withSubDirectory, Boolean enabled, int delay,
			String username, String password, String domain, String host,
			boolean ssl) {
		return injectFtpRepository(use, login, key, path, ignoreHiddenFile,
				withSubDirectory, enabled, delay, username, password, domain,
				host, ssl);
	}

	@Override
	public CommonResult removeFtpRepositoryXML(String use, String login,
			String key, String path, String username, String host, boolean ssl) {
		return removeFtpRepository(use, login, key, path, username, host, ssl);
	}

	@Override
	public CommonResult removeFtpRepositoryJSON(String use, String login,
			String key, String path, String username, String host, boolean ssl) {
		return removeFtpRepository(use, login, key, path, username, host, ssl);
	}

	@Override
	public CommonResult injectSwiftRepository(String use, String login,
			String key, String path, Boolean ignoreHiddenFile,
			Boolean withSubDirectory, Boolean enabled, int delay,
			String username, String password, String tenant, String container,
			String authURL, AuthType authType) {
		return injectRepository(use, login, key, FileInstanceType.Swift, path,
				ignoreHiddenFile, withSubDirectory, enabled, delay, username,
				password, null, null, container, tenant, authURL, authType);
	}

	@Override
	public CommonResult injectSwiftRepositoryXML(String use, String login,
			String key, String path, Boolean ignoreHiddenFile,
			Boolean withSubDirectory, Boolean enabled, int delay,
			String username, String password, String tenant, String container,
			String authURL, AuthType authType) {
		return injectSwiftRepository(use, login, key, path, ignoreHiddenFile,
				withSubDirectory, enabled, delay, username, password, tenant,
				container, authURL, authType);
	}

	@Override
	public CommonResult injectSwiftRepositoryJSON(String use, String login,
			String key, String path, Boolean ignoreHiddenFile,
			Boolean withSubDirectory, Boolean enabled, int delay,
			String username, String password, String tenant, String container,
			String authURL, AuthType authType) {
		return injectSwiftRepository(use, login, key, path, ignoreHiddenFile,
				withSubDirectory, enabled, delay, username, password, tenant,
				container, authURL, authType);
	}

	@Override
	public CommonResult removeSwiftRepository(String use, String login,
			String key, String path, String username, String container) {
		return removeFileRepository(use, login, key, FileInstanceType.Swift,
				path, username, null, null, container);
	}

	@Override
	public CommonResult removeSwiftRepositoryJSON(String use, String login,
			String key, String path, String username, String container) {
		return removeSwiftRepository(use, login, key, path, username, container);
	}

	@Override
	public CommonResult removeSwiftRepositoryXML(String use, String login,
			String key, String path, String username, String container) {
		return removeSwiftRepository(use, login, key, path, username, container);
	}
}
