/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2014 Emmanuel Keller / Jaeksoft
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.ItemField;
import com.jaeksoft.searchlib.crawler.TargetStatus;
import com.jaeksoft.searchlib.crawler.common.database.AbstractManager;
import com.jaeksoft.searchlib.crawler.common.database.FetchStatus;
import com.jaeksoft.searchlib.crawler.common.database.IndexStatus;
import com.jaeksoft.searchlib.crawler.common.database.ParserStatus;
import com.jaeksoft.searchlib.crawler.file.process.FileInstanceAbstract;
import com.jaeksoft.searchlib.crawler.file.spider.CrawlFile;
import com.jaeksoft.searchlib.crawler.file.spider.CrawlFile.FileIndexDocumentIterator;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.query.QueryUtils;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.request.SearchPatternRequest;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.scheduler.TaskLog;
import com.jaeksoft.searchlib.util.map.SourceField;
import com.jaeksoft.searchlib.util.map.TargetField;

public class FileManager extends AbstractManager {

	public enum SearchTemplate {
		fileSearch, fileInfo, fileExport;
	}

	public FileManager(Client client, File dataDir)
			throws SearchLibException, URISyntaxException, FileNotFoundException {
		dataDir = new File(dataDir, "file_crawler_url");
		if (!dataDir.exists())
			dataDir.mkdir();

		Client dbClient = new Client(dataDir, "/com/jaeksoft/searchlib/file_config.xml", true);
		init(client, dbClient);

	}

	public AbstractSearchRequest fileQuery(SearchTemplate searchTemplate, String repository, String fileName,
			String lang, String langMethod, Integer minSize, Integer maxSize, String fileExtension,
			FetchStatus fetchStatus, ParserStatus parserStatus, IndexStatus indexStatus, Date startcrawlDate,
			Date endCrawlDate, Date startModifiedDate, Date endModifiedDate, FileTypeEnum fileType, String subDirectory,
			Integer minTime, Integer maxTime, String parser) throws SearchLibException {
		try {

			AbstractSearchRequest searchRequest = (AbstractSearchRequest) dbClient.getNewRequest(searchTemplate.name());

			StringBuilder query = new StringBuilder();

			if (fileName != null)
				if ((fileName = fileName.trim()).length() > 0)
					FileItemFieldEnum.INSTANCE.fileName.addQuery(query, fileName, true);

			if (repository != null)
				if ((repository = repository.trim()).length() > 0)
					FileItemFieldEnum.INSTANCE.repository.addFilterQuery(searchRequest, repository, true, false);

			if (fileExtension != null)
				if ((fileExtension = fileExtension.trim()).length() > 0)
					FileItemFieldEnum.INSTANCE.fileExtension.addFilterQuery(searchRequest,
							QueryUtils.escapeQuery(fileExtension), false, false);

			if (lang != null)
				if ((lang = lang.trim()).length() > 0)
					FileItemFieldEnum.INSTANCE.lang.addFilterQuery(searchRequest, QueryUtils.escapeQuery(lang), false,
							false);

			if (langMethod != null) {
				langMethod = langMethod.trim();
				if (langMethod.length() > 0)
					FileItemFieldEnum.INSTANCE.langMethod.addFilterQuery(searchRequest,
							QueryUtils.escapeQuery(langMethod), false, false);
			}

			if (fetchStatus != null && fetchStatus != FetchStatus.ALL)
				FileItemFieldEnum.INSTANCE.fetchStatus.addFilterQuery(searchRequest, fetchStatus.value, false, false);
			if (parserStatus != null && parserStatus != ParserStatus.ALL)
				FileItemFieldEnum.INSTANCE.parserStatus.addFilterQuery(searchRequest, parserStatus.value, false, false);
			if (indexStatus != null && indexStatus != IndexStatus.ALL)
				FileItemFieldEnum.INSTANCE.indexStatus.addFilterQuery(searchRequest, indexStatus.value, false, false);

			if (fileType != null && fileType != FileTypeEnum.ALL)
				FileItemFieldEnum.INSTANCE.fileType.addFilterQuery(searchRequest, fileType.name(), true, false);

			if (minSize != null || maxSize != null) {
				String from, to;
				if (minSize == null)
					from = FileItem.contentLengthFormat.format(0);
				else
					from = FileItem.contentLengthFormat.format(minSize);
				if (maxSize == null)
					to = FileItem.contentLengthFormat.format(Integer.MAX_VALUE);
				else
					to = FileItem.contentLengthFormat.format(maxSize);
				FileItemFieldEnum.INSTANCE.fileSize.addFilterRange(searchRequest, from, to, false, false);
			}

			if (minTime != null || maxTime != null) {
				String from, to;
				if (minTime == null)
					from = FileItem.contentLengthFormat.format(0);
				else
					from = FileItem.contentLengthFormat.format(minTime);
				if (maxTime == null)
					to = FileItem.contentLengthFormat.format(Integer.MAX_VALUE);
				else
					to = FileItem.contentLengthFormat.format(maxTime);
				FileItemFieldEnum.INSTANCE.time.addFilterRange(searchRequest, from, to, false, false);
			}

			if (startcrawlDate != null || endCrawlDate != null) {
				String from, to;
				if (startcrawlDate == null)
					from = "00000000000000";
				else
					from = FileItem.dateFormat.format(startcrawlDate);
				if (endCrawlDate == null)
					to = "99999999999999";
				else
					to = FileItem.dateFormat.format(endCrawlDate);
				FileItemFieldEnum.INSTANCE.crawlDate.addFilterRange(searchRequest, from, to, false, false);
			}

			if (startModifiedDate != null || endModifiedDate != null) {
				String from, to;
				if (startModifiedDate == null)
					from = "00000000000000";
				else
					from = FileItem.dateFormat.format(startModifiedDate);
				if (endModifiedDate == null)
					to = "99999999999999";
				else
					to = FileItem.dateFormat.format(endModifiedDate);
				FileItemFieldEnum.INSTANCE.fileSystemDate.addFilterRange(searchRequest, from, to, false, false);
			}

			if (subDirectory != null)
				FileItemFieldEnum.INSTANCE.subDirectory.addFilterQuery(searchRequest, subDirectory, true, false);

			if (parser != null)
				if ((parser = parser.trim()).length() > 0)
					FileItemFieldEnum.INSTANCE.parser.addFilterQuery(searchRequest, parser, true, false);

			searchRequest.setEmptyReturnsAll(true);
			searchRequest.setQueryString(query.toString().trim());
			return searchRequest;
		} catch (ParseException e) {
			throw new SearchLibException(e);
		}
	}

	public FileInfo getFileInfo(String uriString)
			throws SearchLibException, UnsupportedEncodingException, URISyntaxException {
		AbstractSearchRequest searchRequest = (AbstractSearchRequest) dbClient
				.getNewRequest(SearchTemplate.fileInfo.name());
		StringBuilder sb = new StringBuilder();
		FileItemFieldEnum.INSTANCE.uri.addQuery(sb, uriString, true);
		searchRequest.setQueryString(sb.toString());
		searchRequest.setStart(0);
		searchRequest.setRows(1);
		AbstractResultSearch<?> result = (AbstractResultSearch<?>) dbClient.request(searchRequest);
		if (result.getNumFound() == 0)
			return null;
		return new FileInfo(result.getDocument(0));
	}

	public void getFileInfoList(URI parentDirectory, Map<String, FileInfo> indexFileMap)
			throws SearchLibException, UnsupportedEncodingException, URISyntaxException {
		AbstractSearchRequest searchRequest = (AbstractSearchRequest) dbClient
				.getNewRequest(SearchTemplate.fileInfo.name());
		StringBuilder sb = new StringBuilder();
		String parentUriString = parentDirectory.toASCIIString();
		FileItemFieldEnum.INSTANCE.directory.addQuery(sb, parentUriString, true);
		searchRequest.setQueryString(sb.toString());
		searchRequest.setStart(0);
		searchRequest.setRows(Integer.MAX_VALUE);
		AbstractResultSearch<?> result = (AbstractResultSearch<?>) dbClient.request(searchRequest);
		int l = result.getNumFound();
		for (int i = 0; i < l; i++) {
			FileInfo fileInfo = new FileInfo(result.getDocument(i));
			indexFileMap.put(fileInfo.getUri(), fileInfo);
		}
	}

	public long getFileList(AbstractSearchRequest searchRequest, long start, long rows, List<FileItem> list)
			throws SearchLibException {
		searchRequest.reset();
		searchRequest.setStart((int) start);
		searchRequest.setRows((int) rows);
		try {
			AbstractResultSearch<?> result = (AbstractResultSearch<?>) dbClient.request(searchRequest);
			if (list != null)
				for (ResultDocument doc : result)
					list.add(getNewFileItem(doc));
			return result.getNumFound();
		} catch (RuntimeException e) {
			throw new SearchLibException(e);
		} catch (UnsupportedEncodingException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		}
	}

	public FileItem getNewFileItem(ResultDocument doc) throws UnsupportedEncodingException, URISyntaxException {
		return new FileItem(doc);
	}

	public FileItem getNewFileItem(FileInstanceAbstract fileInstance) throws IOException {
		return new FileItem(fileInstance);
	}

	public long getFiles(AbstractSearchRequest searchRequest, ItemField orderBy, boolean orderAsc, long start,
			long rows, List<FileItem> list) throws SearchLibException {
		searchRequest.setStart((int) start);
		searchRequest.setRows((int) rows);
		try {
			if (orderBy != null)
				orderBy.addSort(searchRequest, !orderAsc);
			AbstractResultSearch<?> result = (AbstractResultSearch<?>) dbClient.request(searchRequest);
			if (list != null)
				for (ResultDocument doc : result)
					list.add(getNewFileItem(doc));
			return result.getNumFound();

		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (RuntimeException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		}
	}

	public final void deleteByRepository(String repository) throws SearchLibException {
		try {
			deleteByRepositoryFromTargetIndex(repository);
			deleteByRepositoryFromFileDBIndex(repository);
		} catch (SearchLibException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (ParseException e) {
			throw new SearchLibException(e);
		} catch (SyntaxError e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
			throw new SearchLibException(e);
		} catch (InterruptedException e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		}
	}

	public final boolean deleteByUri(List<String> rowToDelete) throws SearchLibException {

		if (rowToDelete == null || (rowToDelete != null && rowToDelete.isEmpty()))
			return false;

		dbClient.deleteDocuments(FileItemFieldEnum.INSTANCE.uri.getName(), rowToDelete);

		String targetField = findIndexedFieldOfTargetIndex(targetClient.getFileCrawlerFieldMap(),
				FileItemFieldEnum.INSTANCE.uri.getName());
		if (targetField != null)
			targetClient.deleteDocuments(targetField, rowToDelete);
		return true;

	}

	/**
	 * delete file item from this repository
	 * 
	 * @param repository
	 * @throws SearchLibException
	 * @throws IOException
	 * @throws ParseException
	 * @throws SyntaxError
	 * @throws URISyntaxException
	 * @throws ClassNotFoundException
	 * @throws InterruptedException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private final void deleteByRepositoryFromFileDBIndex(String repository)
			throws SearchLibException, IOException, ParseException, SyntaxError, URISyntaxException,
			ClassNotFoundException, InterruptedException, InstantiationException, IllegalAccessException {

		AbstractSearchRequest deleteRequest = new SearchPatternRequest(dbClient);
		FileItemFieldEnum.INSTANCE.repository.setQuery(deleteRequest, repository, true);
		dbClient.deleteDocuments(deleteRequest);
	}

	private final boolean deleteByRepositoryFromTargetIndex(String repository)
			throws SearchLibException, IOException, ParseException, SyntaxError, URISyntaxException,
			ClassNotFoundException, InterruptedException, InstantiationException, IllegalAccessException {

		List<TargetField> mappedPath = targetClient.getFileCrawlerFieldMap()
				.getLinks(new SourceField(FileItemFieldEnum.INSTANCE.repository.toString()));

		if (mappedPath == null || mappedPath.isEmpty())
			return false;

		AbstractSearchRequest deleteRequest = new SearchPatternRequest(targetClient);
		deleteRequest.setQueryString(mappedPath.get(0).getName() + ":\"" + repository + "\"");

		targetClient.deleteDocuments(deleteRequest);
		return true;
	}

	public void updateCrawlUriDb(List<CrawlFile> crawls) throws SearchLibException {
		List<IndexDocument> documents = new ArrayList<IndexDocument>(crawls.size());
		try {
			for (CrawlFile crawl : crawls) {
				if (crawl == null)
					continue;
				IndexDocument indexDocument = crawl.getFileItem().getIndexDocument();
				documents.add(indexDocument);
			}
			dbClient.updateDocuments(documents);
		} catch (UnsupportedEncodingException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		}
	}

	public void updateCrawlTarget(final List<CrawlFile> crawls, final int documentBufferSize)
			throws SearchLibException {
		try {
			if (crawls == null)
				return;
			List<String> documentsToDelete = new ArrayList<String>(crawls.size());
			List<IndexDocument> documentsToUpdate = new ArrayList<IndexDocument>(documentBufferSize);
			String uniqueField = targetClient.getSchema().getUniqueField();
			for (CrawlFile crawl : crawls) {
				if (crawl == null)
					continue;
				FileItem currentFileItem = crawl.getFileItem();

				FileIndexDocumentIterator indexDocumentIterator = crawl.getTargetIndexDocumentIterator();

				TargetStatus targetStatus = currentFileItem.getIndexStatus().targetStatus;
				if (targetStatus == TargetStatus.TARGET_UPDATE) {
					if (!indexDocumentIterator.hasNext()) {
						currentFileItem.setIndexStatus(IndexStatus.NOTHING_TO_INDEX);
						continue;
					}
					while (indexDocumentIterator.hasNext()) {
						IndexDocument indexDocument = indexDocumentIterator.next();
						indexDocumentIterator.throwError();
						if (indexDocument == null)
							continue;
						if (uniqueField != null && !indexDocument.hasContent(uniqueField)) {
							currentFileItem.setIndexStatus(IndexStatus.INDEX_ERROR);
						} else {
							documentsToUpdate.add(indexDocument);
						}
						if (documentsToUpdate.size() >= documentBufferSize) {
							targetClient.updateDocuments(documentsToUpdate);
							documentsToUpdate.clear();
						}
					}
				} else if (targetStatus == TargetStatus.TARGET_DELETE)
					documentsToDelete.add(currentFileItem.getUri());
			} // crawl loop

			if (documentsToUpdate.size() > 0)
				targetClient.updateDocuments(documentsToUpdate);
			for (CrawlFile crawl : crawls) {
				FileItem currentFileItem = crawl.getFileItem();
				IndexStatus indexStatus = currentFileItem.getIndexStatus();
				if (indexStatus == IndexStatus.TO_INDEX || indexStatus == IndexStatus.NOT_INDEXED)
					currentFileItem.setIndexStatus(IndexStatus.INDEXED);
			}
			if (documentsToDelete.size() > 0) {
				String targetField = findIndexedFieldOfTargetIndex(targetClient.getFileCrawlerFieldMap(),
						FileItemFieldEnum.INSTANCE.uri.getName());
				if (targetField != null)
					targetClient.deleteDocuments(targetField, documentsToDelete);
			}
		} catch (IOException e) {
			throw new SearchLibException(e);
		}
	}

	public void updateFileItems(List<FileItem> fileItems) throws SearchLibException {
		try {
			if (fileItems == null)
				return;
			List<IndexDocument> documents = new ArrayList<IndexDocument>(fileItems.size());
			for (FileItem fileItem : fileItems) {
				if (fileItem == null)
					continue;
				IndexDocument indexDocument = new IndexDocument();
				fileItem.populate(indexDocument);
				documents.add(indexDocument);
			}
			if (documents.size() > 0)
				dbClient.updateDocuments(documents);
		} catch (IOException e) {
			throw new SearchLibException(e);
		}
	}

	public int delete(AbstractSearchRequest searchRequest, TaskLog taskLog) throws SearchLibException {
		setCurrentTaskLog(taskLog);
		try {
			int total = 0;
			List<FileItem> itemList = new ArrayList<FileItem>();
			for (;;) {
				itemList.clear();
				getFileList(searchRequest, 0, 1000, itemList);
				if (itemList.size() == 0)
					break;
				List<String> uriList = new ArrayList<String>(itemList.size());
				for (FileItem fileItem : itemList)
					uriList.add(fileItem.getUri());
				dbClient.deleteDocuments(FileItemFieldEnum.INSTANCE.uri.getName(), uriList);
				total += itemList.size();
				taskLog.setInfo(total + " URI(s) deleted");
				if (taskLog.isAbortRequested())
					throw new SearchLibException.AbortException();
			}
			return total;
		} finally {
			resetCurrentTaskLog();
		}
	}

	public int updateFetchStatus(AbstractSearchRequest searchRequest, FetchStatus fetchStatus, int bufferSize,
			TaskLog taskLog) throws SearchLibException {
		setCurrentTaskLog(taskLog);
		try {
			int total = 0;
			FileItemFieldEnum.INSTANCE.fetchStatus.addFilterQuery(searchRequest, fetchStatus.value, false, true);
			List<FileItem> fileItemList = new ArrayList<FileItem>();
			for (;;) {
				fileItemList.clear();
				getFileList(searchRequest, 0, bufferSize, fileItemList);
				if (fileItemList.size() == 0)
					break;
				for (FileItem fileItem : fileItemList)
					fileItem.setFetchStatus(fetchStatus);
				updateFileItems(fileItemList);
				total += fileItemList.size();
				taskLog.setInfo(total + " URI(s) updated");
				if (taskLog.isAbortRequested())
					throw new SearchLibException.AbortException();
			}
			return total;
		} catch (ParseException e) {
			throw new SearchLibException(e);
		} finally {
			resetCurrentTaskLog();
		}
	}

	public long synchronizeIndex(AbstractSearchRequest searchRequest, int bufferSize, TaskLog taskLog)
			throws SearchLibException {
		String targetField = findIndexedFieldOfTargetIndex(targetClient.getFileCrawlerFieldMap(),
				FileItemFieldEnum.INSTANCE.uri.getName());
		return synchronizeIndex(searchRequest, targetField, FileItemFieldEnum.INSTANCE.uri.getName(), bufferSize,
				taskLog);
	}

}