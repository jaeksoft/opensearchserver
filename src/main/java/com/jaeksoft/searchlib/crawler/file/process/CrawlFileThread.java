/**
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2008-2016 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.crawler.file.process;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.common.database.FetchStatus;
import com.jaeksoft.searchlib.crawler.common.database.IndexStatus;
import com.jaeksoft.searchlib.crawler.common.database.ParserStatus;
import com.jaeksoft.searchlib.crawler.common.process.CrawlStatistics;
import com.jaeksoft.searchlib.crawler.common.process.CrawlStatus;
import com.jaeksoft.searchlib.crawler.common.process.CrawlThreadAbstract;
import com.jaeksoft.searchlib.crawler.file.database.*;
import com.jaeksoft.searchlib.crawler.file.spider.CrawlFile;
import com.jaeksoft.searchlib.util.InfoCallback;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class CrawlFileThread extends CrawlThreadAbstract<CrawlFileThread, CrawlFileMaster> {

	private final CrawlFileMaster crawlMaster;
	private final FileCrawlQueue crawlQueue;
	private final FileManager fileManager;
	private final long delayBetweenAccesses;
	private final FilePathItem filePathItem;
	private volatile FileItem currentFileItem;
	private long nextTimeTarget;

	protected CrawlFileThread(Config config, CrawlFileMaster crawlMaster, CrawlStatistics sessionStats,
			FilePathItem filePathItem, InfoCallback infoCallback) throws SearchLibException {
		super(config, crawlMaster, null, infoCallback);
		this.fileManager = config.getFileManager();
		this.crawlMaster = (CrawlFileMaster) getThreadMaster();
		this.crawlQueue = (FileCrawlQueue) crawlMaster.getCrawlQueue();
		currentStats = new CrawlStatistics(sessionStats);
		this.delayBetweenAccesses = filePathItem.getDelay();
		nextTimeTarget = 0;
		this.filePathItem = filePathItem;
		this.currentFileItem = null;
	}

	private void sleepInterval(long max) throws InterruptedException {
		long c = System.currentTimeMillis();
		long ms = nextTimeTarget - c;
		nextTimeTarget = c + delayBetweenAccesses;
		if (ms < 0)
			return;
		if (ms > max)
			ms = max;
		sleepMs(ms);
	}

	void browse(final FileInstanceAbstract fileInstance, final Integer depth)
			throws SearchLibException, URISyntaxException, NoSuchAlgorithmException, IOException,
			InstantiationException, IllegalAccessException, ClassNotFoundException, HttpException,
			InterruptedException {
		if (isAborted() || crawlMaster.isAborted())
			return;
		if (fileInstance == null)
			return;
		FileItem fileItem = fileManager.getNewFileItem(fileInstance);
		setCurrentFileItem(fileItem);
		FileTypeEnum fileType = fileItem.getFileType();
		if (fileType == null)
			return;
		switch (fileType) {
		case directory:
			if (depth != null && depth == 0)
				break;
			FileInstanceAbstract[] files = checkDirectory(fileInstance);
			if (files == null)
				break;
			for (FileInstanceAbstract file : files)
				browse(file, depth == null ? null : depth - 1);
			break;
		case file:
			if (!checkFile(fileItem))
				return;
			break;
		default:
			return;
		}
		CrawlFile crawl = crawl(fileInstance, fileItem);
		if (crawl != null)
			crawlQueue.add(currentStats, crawl);

		setStatus(CrawlStatus.INDEXATION);
		crawlQueue.index(false);
	}

	@Override
	public void runner() throws Exception {

		CrawlFileMaster crawlMaster = (CrawlFileMaster) getThreadMaster();
		FileCrawlQueue crawlQueue = (FileCrawlQueue) crawlMaster.getCrawlQueue();

		FileInstanceAbstract fileInstance = FileInstanceAbstract.create(filePathItem, null, filePathItem.getPath());

		browse(fileInstance, null);

		crawlQueue.index(!crawlMaster.isRunning());
	}

	private CrawlFile crawl(FileInstanceAbstract fileInstance, FileItem fileItem)
			throws SearchLibException, InterruptedException {

		long startTime = System.currentTimeMillis();

		sleepInterval(60000);

		setStatus(CrawlStatus.CRAWL);
		currentStats.incUrlCount();

		CrawlFile crawl = new CrawlFile(fileInstance, fileItem, getConfig(), currentStats);

		// Fetch started
		currentStats.incFetchedCount();

		crawl.download();

		if (fileItem.getFetchStatus() == FetchStatus.FETCHED && fileItem.getParserStatus() == ParserStatus.PARSED
				&& fileItem.getIndexStatus() != IndexStatus.META_NOINDEX) {
			fileItem.setIndexStatus(IndexStatus.TO_INDEX);
			currentStats.incParsedCount();
		} else
			currentStats.incIgnoredCount();

		fileItem.setTime((int) (System.currentTimeMillis() - startTime));
		return crawl;
	}

	final private void smartDelete(FileCrawlQueue crawlQueue, FileInfo fileInfo) throws SearchLibException {
		crawlQueue.delete(currentStats, fileInfo.getUri());
		if (fileInfo.getFileType() != FileTypeEnum.directory)
			return;
		HashMap<String, FileInfo> indexFileMap = new HashMap<String, FileInfo>();
		try {
			fileManager.getFileInfoList(new URI(fileInfo.getUri()), indexFileMap);
			for (FileInfo fi : indexFileMap.values())
				smartDelete(crawlQueue, fi);
		} catch (UnsupportedEncodingException e) {
			Logging.warn(e);
		} catch (URISyntaxException e) {
			Logging.warn(e);
		}
	}

	private FileInstanceAbstract[] checkDirectory(FileInstanceAbstract fileInstance)
			throws SearchLibException, URISyntaxException, IOException {

		// Load directory from Index
		HashMap<String, FileInfo> indexFileMap = new HashMap<String, FileInfo>();
		fileManager.getFileInfoList(fileInstance.getURI(), indexFileMap);

		boolean withSubDir = filePathItem.isWithSubDir();

		// If the filePathItem does not support subdir
		if (!withSubDir)
			for (FileInfo fileInfo : indexFileMap.values())
				if (fileInfo.getFileType() == FileTypeEnum.directory)
					smartDelete(crawlQueue, fileInfo);

		// Remove existing files from the map
		FileInstanceAbstract[] files =
				withSubDir ? fileInstance.listFilesAndDirectories() : fileInstance.listFilesOnly();
		if (files != null)
			for (FileInstanceAbstract file : files)
				indexFileMap.remove(file.getURI().toASCIIString());

		// The file that remain in the map can be removed
		if (indexFileMap.size() > 0)
			for (FileInfo fileInfo : indexFileMap.values())
				smartDelete(crawlQueue, fileInfo);

		return files;
	}

	private boolean checkFile(FileItem fileItem)
			throws UnsupportedEncodingException, SearchLibException, URISyntaxException {
		FileInfo oldFileInfo = fileManager.getFileInfo(fileItem.getUri());
		// The file is a new file
		if (oldFileInfo == null) {
			return true;
		}
		// The file has been modified
		if (oldFileInfo.isNewCrawlNeeded(fileItem))
			return true;
		// The file has not changed, we don't need to crawl it
		currentStats.incIgnoredCount();
		return false;
	}

	public FileItem getCurrentFileItem() {
		return currentFileItem;
	}

	public void setCurrentFileItem(FileItem item) {
		currentFileItem = item;
	}

	@Override
	public String getCurrentInfo() {
		FileItem fileItem = currentFileItem;
		return fileItem == null ? StringUtils.EMPTY : fileItem.getDirectory();
	}

}