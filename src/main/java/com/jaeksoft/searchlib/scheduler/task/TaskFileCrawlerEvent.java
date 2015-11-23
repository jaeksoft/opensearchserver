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

package com.jaeksoft.searchlib.scheduler.task;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.file.database.FileInstanceType;
import com.jaeksoft.searchlib.crawler.file.database.FilePathItem;
import com.jaeksoft.searchlib.crawler.file.process.FileInstanceAbstract;
import com.jaeksoft.searchlib.scheduler.TaskAbstract;
import com.jaeksoft.searchlib.scheduler.TaskLog;
import com.jaeksoft.searchlib.scheduler.TaskProperties;
import com.jaeksoft.searchlib.scheduler.TaskPropertyDef;
import com.jaeksoft.searchlib.scheduler.TaskPropertyType;
import com.jaeksoft.searchlib.util.IOUtils;
import com.jaeksoft.searchlib.util.Variables;

import jcifs.smb.SmbException;

public class TaskFileCrawlerEvent extends TaskAbstract {

	final private TaskPropertyDef propFilePathItem = new TaskPropertyDef(TaskPropertyType.comboBox, "File path item",
			"File path item", "File path item", 30);

	final private TaskPropertyDef propDirectoryURL = new TaskPropertyDef(TaskPropertyType.textBox, "Directory URL",
			"Directory URL", "Directory URL", 60);

	final private TaskPropertyDef propLogFilePattern = new TaskPropertyDef(TaskPropertyType.textBox, "Log file pattern",
			"Log file pattern", "Log file pattern", 30);

	final private TaskPropertyDef propDomain = new TaskPropertyDef(TaskPropertyType.textBox, "Domain", "Domain",
			"Domain", 30);

	final private TaskPropertyDef propLogin = new TaskPropertyDef(TaskPropertyType.textBox, "Login", "Login", "Login",
			30);

	final private TaskPropertyDef propPassword = new TaskPropertyDef(TaskPropertyType.password, "Password", "Password",
			"Password", 10);

	final private TaskPropertyDef[] taskPropertyDefs = { propFilePathItem, propDirectoryURL, propLogFilePattern,
			propDomain, propLogin, propPassword };

	@Override
	public String getName() {
		return "File crawler - Event crawler";
	}

	@Override
	public TaskPropertyDef[] getPropertyList() {
		return taskPropertyDefs;
	}

	@Override
	public String[] getPropertyValues(Config config, TaskPropertyDef propertyDef, TaskProperties taskProperties)
			throws SearchLibException {
		if (propertyDef == propFilePathItem) {
			List<String> arrayList = new ArrayList<String>();
			config.getFilePathManager().getAllFilePathsString(arrayList);
			return arrayList.toArray(new String[arrayList.size()]);
		}
		return null;
	}

	@Override
	public String getDefaultValue(Config config, TaskPropertyDef propertyDef) {
		return null;
	}

	@Override
	public void execute(Client client, TaskProperties properties, Variables variables, TaskLog taskLog)
			throws SearchLibException, IOException {
		taskLog.setInfo("File crawler event started");

		String filePath = properties.getValue(propFilePathItem);
		String directoryURL = properties.getValue(propDirectoryURL);
		String logFilePattern = properties.getValue(propLogFilePattern);
		String domain = properties.getValue(propDomain);
		String login = properties.getValue(propLogin);
		String password = properties.getValue(propPassword);

		FilePathItem filePathItem = client.getFilePathManager().find(filePath);
		if (filePathItem == null)
			throw new SearchLibException("File path item not found: " + filePath);

		final FilePathItem fpi;
		URI srcURI;
		try {
			srcURI = new URI(directoryURL);

			String scheme = srcURI.getScheme();
			if (scheme.equals("smb")) {
				fpi = new FilePathItem(client);
				fpi.setType(FileInstanceType.Smb);
				fpi.setHost(srcURI.getHost());
				if (domain != null)
					fpi.setDomain(domain);
				if (login != null)
					fpi.setUsername(login);
				if (password != null)
					fpi.setPassword(password);
			} else if (scheme.equals("file")) {
				fpi = new FilePathItem(client);
				fpi.setType(FileInstanceType.Local);
			} else
				throw new SearchLibException("Unsupported scheme: " + scheme);
			int count = 0;
			FileInstanceAbstract fileInstanceSource = FileInstanceAbstract.create(fpi, null, srcURI.getPath());
			FileInstanceAbstract[] fileInstances = fileInstanceSource.listFilesOnly();
			if (fileInstances == null)
				return;
			for (FileInstanceAbstract fileInstance : fileInstances) {
				if (!StringUtils.isEmpty(logFilePattern))
					if (!FilenameUtils.wildcardMatch(fileInstance.getFileName(), logFilePattern))
						continue;
				count++;
				taskLog.setInfo("Extract #" + count + ": " + fileInstance.getURL());
				InputStream is = fileInstance.getInputStream();
				try {
					List<String> lines = IOUtils.readLines(is);
					if (lines == null)
						continue;
					for (String line : lines) {
						taskLog.setInfo("Crawl " + fileInstance.getURL());
						client.getFileCrawlMaster().crawlDirectory(filePathItem, line);
					}
				} catch (NoSuchAlgorithmException e) {
					throw new SearchLibException(e);
				} catch (InstantiationException e) {
					throw new SearchLibException(e);
				} catch (IllegalAccessException e) {
					throw new SearchLibException(e);
				} catch (ClassNotFoundException e) {
					throw new SearchLibException(e);
				} catch (SmbException e) {
					throw new SearchLibException(e);
				} catch (HttpException e) {
					throw new SearchLibException(e);
				} finally {
					IOUtils.closeQuietly(is);
				}
				fileInstance.delete();
			}
			taskLog.setInfo(count + " file(s)");
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		}
	}
}
