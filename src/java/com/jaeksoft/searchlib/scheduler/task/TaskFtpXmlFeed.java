/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPFile;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.file.process.fileInstances.FtpFileInstance;
import com.jaeksoft.searchlib.crawler.web.spider.ProxyHandler;
import com.jaeksoft.searchlib.scheduler.TaskAbstract;
import com.jaeksoft.searchlib.scheduler.TaskLog;
import com.jaeksoft.searchlib.scheduler.TaskProperties;
import com.jaeksoft.searchlib.scheduler.TaskPropertyDef;
import com.jaeksoft.searchlib.scheduler.TaskPropertyType;
import com.jaeksoft.searchlib.util.DomUtils;

public class TaskFtpXmlFeed extends TaskAbstract {

	final private TaskPropertyDef propServer = new TaskPropertyDef(
			TaskPropertyType.textBox, "FTP server (hostname)", 100);

	final private TaskPropertyDef propPath = new TaskPropertyDef(
			TaskPropertyType.textBox, "Path", 100);

	final private TaskPropertyDef propLogin = new TaskPropertyDef(
			TaskPropertyType.textBox, "Login", 50);

	final private TaskPropertyDef propPassword = new TaskPropertyDef(
			TaskPropertyType.password, "Password", 50);

	final private TaskPropertyDef propFileNamePattern = new TaskPropertyDef(
			TaskPropertyType.textBox, "File name pattern", 50);

	final private TaskPropertyDef propXsl = new TaskPropertyDef(
			TaskPropertyType.multilineTextBox, "XSL", 100, 30);

	final private TaskPropertyDef propDeleteAfterLoad = new TaskPropertyDef(
			TaskPropertyType.comboBox, "Delete after load", 10);

	final private TaskPropertyDef propTruncateIndexWhenFilesFound = new TaskPropertyDef(
			TaskPropertyType.comboBox, "Truncate index when files are found",
			10);

	final private TaskPropertyDef propBuffersize = new TaskPropertyDef(
			TaskPropertyType.textBox, "Buffer size", 10);

	final private TaskPropertyDef[] taskPropertyDefs = { propServer, propPath,
			propLogin, propPassword, propFileNamePattern, propXsl,
			propDeleteAfterLoad, propTruncateIndexWhenFilesFound,
			propBuffersize };

	@Override
	public String getName() {
		return "FTP XML feed ";
	}

	@Override
	public TaskPropertyDef[] getPropertyList() {
		return taskPropertyDefs;
	}

	@Override
	public String[] getPropertyValues(Config config, TaskPropertyDef propertyDef)
			throws SearchLibException {
		if (propertyDef == propDeleteAfterLoad)
			return ClassPropertyEnum.BOOLEAN_LIST;
		if (propertyDef == propTruncateIndexWhenFilesFound)
			return ClassPropertyEnum.BOOLEAN_LIST;
		return null;
	}

	@Override
	public String getDefaultValue(Config config, TaskPropertyDef propertyDef) {
		if (propertyDef == propPath)
			return "/";
		if (propertyDef == propBuffersize)
			return "50";
		if (propertyDef == propDeleteAfterLoad)
			return Boolean.FALSE.toString();
		if (propertyDef == propTruncateIndexWhenFilesFound)
			return Boolean.FALSE.toString();
		return null;
	}

	private void checkConnect(FTPClient ftp, String server, String login,
			String password) throws IOException {
		try {
			if (ftp.isConnected())
				if (ftp.sendNoOp())
					return;
		} catch (FTPConnectionClosedException e) {
			Logging.warn(e);
		}
		ftp.connect(server);
		ftp.login(login, password);
	}

	@Override
	public void execute(Client client, TaskProperties properties,
			TaskLog taskLog) throws SearchLibException {
		String server = properties.getValue(propServer);
		String path = properties.getValue(propPath);
		String login = properties.getValue(propLogin);
		String password = properties.getValue(propPassword);
		String fileNamePattern = properties.getValue(propFileNamePattern);
		boolean deleteAfterLoad = Boolean.TRUE.toString().equals(
				properties.getValue(propDeleteAfterLoad));
		boolean truncateWhenFilesFound = Boolean.TRUE.toString().equals(
				properties.getValue(propTruncateIndexWhenFilesFound));
		Pattern pattern = null;
		if (fileNamePattern != null && fileNamePattern.length() > 0)
			pattern = Pattern.compile(fileNamePattern);

		String p = properties.getValue(propBuffersize);
		String xsl = properties.getValue(propXsl);
		File xmlTempResult = null;
		int bufferSize = 50;
		if (p != null && p.length() > 0)
			bufferSize = Integer.parseInt(p);
		ProxyHandler proxyHandler = client.getWebPropertyManager()
				.getProxyHandler();
		FTPClient ftp = null;
		InputStream inputStream = null;
		try {
			// FTP Connection
			ftp = new FTPClient();
			checkConnect(ftp, server, login, password);
			FTPFile[] files = ftp.listFiles(path,
					new FtpFileInstance.FileOnlyDirectoryFilter());
			if (files == null)
				return;
			// Sort by ascendant filename
			String[] fileNames = new String[files.length];
			int i = 0;
			for (FTPFile file : files)
				fileNames[i++] = file.getName();
			Arrays.sort(fileNames);
			int ignored = 0;
			int loaded = 0;
			boolean bAlreadyTruncated = false;
			for (String fileName : fileNames) {
				String filePathName = FilenameUtils.concat(path, fileName);
				if (pattern != null)
					if (!pattern.matcher(fileName).find()) {
						ignored++;
						continue;
					}
				if (truncateWhenFilesFound && !bAlreadyTruncated) {
					client.deleteAll();
					bAlreadyTruncated = true;
				}
				taskLog.setInfo("Working on: " + filePathName);
				inputStream = ftp.retrieveFileStream(filePathName);
				Node xmlDoc = null;
				if (xsl != null && xsl.length() > 0) {
					xmlTempResult = File.createTempFile("ossftpfeed", ".xml");
					DomUtils.xslt(new StreamSource(inputStream), xsl,
							xmlTempResult);
					xmlDoc = DomUtils.readXml(new StreamSource(xmlTempResult),
							false);
				} else
					xmlDoc = DomUtils.readXml(new StreamSource(inputStream),
							false);
				client.updateXmlDocuments(xmlDoc, bufferSize, null,
						proxyHandler, taskLog);
				client.deleteXmlDocuments(xmlDoc, bufferSize, taskLog);
				inputStream.close();
				inputStream = null;
				if (!ftp.completePendingCommand())
					throw new SearchLibException("FTP Error");
				if (xmlTempResult != null) {
					xmlTempResult.delete();
					xmlTempResult = null;
				}
				checkConnect(ftp, server, login, password);
				if (deleteAfterLoad)
					ftp.deleteFile(filePathName);
				loaded++;
			}
			taskLog.setInfo(loaded + " file(s) loaded - " + ignored
					+ " file(s) ignored");
		} catch (XPathExpressionException e) {
			throw new SearchLibException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new SearchLibException(e);
		} catch (ParserConfigurationException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
			throw new SearchLibException(e);
		} catch (TransformerException e) {
			throw new SearchLibException(e);
		} finally {
			if (xmlTempResult != null)
				xmlTempResult.delete();
			if (inputStream != null)
				IOUtils.closeQuietly(inputStream);
			try {
				if (ftp != null)
					ftp.disconnect();
			} catch (IOException e) {
				Logging.warn(e);
			}
		}
	}
}
