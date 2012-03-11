/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpException;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.ItemField;
import com.jaeksoft.searchlib.crawler.common.database.FetchStatus;
import com.jaeksoft.searchlib.crawler.common.database.IndexStatus;
import com.jaeksoft.searchlib.crawler.common.database.ParserStatus;
import com.jaeksoft.searchlib.crawler.file.process.FileInstanceAbstract;
import com.jaeksoft.searchlib.crawler.file.spider.CrawlFile;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.Result;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.util.ExtensibleEnum;
import com.jaeksoft.searchlib.util.map.Target;

public class FileManager {

	private static final String FILE_SEARCH = "fileSearch";

	private static final String FILE_INFO = "fileInfo";

	private final Client fileDbClient;
	private final Client targetClient;

	private ExtensibleEnum<FileInstanceType> fileInstanceTypeEnum = null;

	public FileManager(Client client, File dataDir) throws SearchLibException,
			URISyntaxException, FileNotFoundException {
		dataDir = new File(dataDir, "file_crawler_url");
		if (!dataDir.exists())
			dataDir.mkdir();

		this.fileDbClient = new Client(dataDir, "/file_config.xml", true);
		targetClient = client;
	}

	public Client getFileDbClient() {
		return fileDbClient;
	}

	public void reload(boolean optimize) throws IOException,
			URISyntaxException, SearchLibException, InstantiationException,
			IllegalAccessException, ClassNotFoundException, HttpException {

		if (optimize) {
			fileDbClient.reload();
			fileDbClient.getIndex().optimize();
		}
		fileDbClient.reload();
		targetClient.reload();
	}

	public SearchRequest fileQuery(String repository, String like, String lang,
			String langMethod, Integer minContentLength,
			Integer maxContentLength, FetchStatus fetchStatus,
			ParserStatus parserStatus, IndexStatus indexStatus, Date startDate,
			Date endDate, Date startModifiedDate, Date endModifiedDate,
			FileTypeEnum fileType, String subDirectory)
			throws SearchLibException {
		try {

			SearchRequest searchRequest = fileDbClient
					.getNewSearchRequest(FILE_SEARCH);

			if (repository != null)
				FileItemFieldEnum.repository.addFilterQuery(searchRequest,
						repository, true);

			StringBuffer query = new StringBuffer();
			if (like != null) {
				like = like.trim();
				if (like.length() > 0) {
					FileItemFieldEnum.uri.addQuery(query,
							SearchRequest.escapeQuery(like), false);
					query.append("*");
				}
			}
			if (lang != null) {
				lang = lang.trim();
				if (lang.length() > 0)
					FileItemFieldEnum.lang.addFilterQuery(searchRequest,
							SearchRequest.escapeQuery(lang), false);
			}
			if (langMethod != null) {
				langMethod = langMethod.trim();
				if (langMethod.length() > 0)
					FileItemFieldEnum.langMethod.addFilterQuery(searchRequest,
							SearchRequest.escapeQuery(langMethod), false);
			}

			if (fetchStatus != null && fetchStatus != FetchStatus.ALL)
				FileItemFieldEnum.fetchStatus.addFilterQuery(searchRequest,
						fetchStatus.value, true);
			if (parserStatus != null && parserStatus != ParserStatus.ALL)
				FileItemFieldEnum.parserStatus.addFilterQuery(searchRequest,
						parserStatus.value, false);
			if (indexStatus != null && indexStatus != IndexStatus.ALL)
				FileItemFieldEnum.indexStatus.addFilterQuery(searchRequest,
						indexStatus.value, false);

			if (fileType != null && fileType != FileTypeEnum.ALL)
				FileItemFieldEnum.fileType.addFilterQuery(searchRequest,
						fileType.name(), false);

			if (minContentLength != null || maxContentLength != null) {
				String from, to;
				DecimalFormat df = FileItem.getContentLengthFormat();
				if (minContentLength == null)
					from = df.format(0);
				else
					from = df.format(minContentLength);
				if (maxContentLength == null)
					to = df.format(Integer.MAX_VALUE);
				else
					to = df.format(maxContentLength);
				FileItemFieldEnum.contentLength.addQueryRange(query, from, to,
						false);
			}

			if (startDate != null || endDate != null) {
				String from, to;
				SimpleDateFormat df = FileItem.getDateFormat();
				if (startDate == null)
					from = "00000000000000";
				else
					from = df.format(startDate);
				if (endDate == null)
					to = "99999999999999";
				else
					to = df.format(endDate);
				FileItemFieldEnum.crawlDate.addFilterRange(searchRequest, from,
						to, false);
			}

			if (startModifiedDate != null || endModifiedDate != null) {
				String from, to;
				SimpleDateFormat df = FileItem.getDateFormat();
				if (startModifiedDate == null)
					from = "00000000000000";
				else
					from = df.format(startModifiedDate);
				if (endModifiedDate == null)
					to = "99999999999999";
				else
					to = df.format(endModifiedDate);
				FileItemFieldEnum.fileSystemDate.addFilterRange(searchRequest,
						from, to, false);
			}

			if (subDirectory != null)
				FileItemFieldEnum.subDirectory.addFilterQuery(searchRequest,
						subDirectory, true);

			if (query.length() == 0)
				query.append("*:*");
			searchRequest.setQueryString(query.toString().trim());
			return searchRequest;
		} catch (ParseException e) {
			throw new SearchLibException(e);
		}
	}

	public FileInfo getFileInfo(String uriString) throws SearchLibException,
			UnsupportedEncodingException, URISyntaxException {
		SearchRequest searchRequest = fileDbClient
				.getNewSearchRequest(FILE_INFO);
		StringBuffer sb = new StringBuffer();
		FileItemFieldEnum.uri.addQuery(sb, uriString, true);
		searchRequest.setQueryString(sb.toString());
		searchRequest.setStart(0);
		searchRequest.setRows(1);
		Result result = fileDbClient.search(searchRequest);
		if (result.getNumFound() == 0)
			return null;
		return new FileInfo(result.getDocument(0));
	}

	public void getFileInfoList(URI parentDirectory,
			Map<String, FileInfo> indexFileMap) throws SearchLibException,
			UnsupportedEncodingException, URISyntaxException {
		SearchRequest searchRequest = fileDbClient
				.getNewSearchRequest(FILE_INFO);
		StringBuffer sb = new StringBuffer();
		String parentUriString = parentDirectory.toASCIIString();
		FileItemFieldEnum.directory.addQuery(sb, parentUriString, true);
		searchRequest.setQueryString(sb.toString());
		searchRequest.setStart(0);
		searchRequest.setRows(Integer.MAX_VALUE);
		Result result = fileDbClient.search(searchRequest);
		int l = result.getNumFound();
		for (int i = 0; i < l; i++) {
			FileInfo fileInfo = new FileInfo(result.getDocument(i));
			indexFileMap.put(fileInfo.getUri(), fileInfo);
		}
	}

	public FileItem getNewFileItem(ResultDocument doc)
			throws UnsupportedEncodingException, URISyntaxException,
			java.text.ParseException {
		return new FileItem(doc);
	}

	public FileItem getNewFileItem(FileInstanceAbstract fileInstance)
			throws SearchLibException {
		return new FileItem(fileInstance);
	}

	public long getFiles(SearchRequest searchRequest, ItemField orderBy,
			boolean orderAsc, long start, long rows, List<FileItem> list)
			throws SearchLibException {
		searchRequest.setStart((int) start);
		searchRequest.setRows((int) rows);
		try {
			if (orderBy != null)
				orderBy.addSort(searchRequest, !orderAsc);
			Result result = fileDbClient.search(searchRequest);
			if (list != null)
				for (ResultDocument doc : result.getDocuments())
					list.add(getNewFileItem(doc));
			return result.getNumFound();

		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (RuntimeException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} catch (java.text.ParseException e) {
			throw new SearchLibException(e);
		}
	}

	public final void deleteByRepository(String repository)
			throws SearchLibException {
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

	public final boolean deleteByUri(List<String> rowToDelete)
			throws SearchLibException {
		try {
			if (rowToDelete == null
					|| (rowToDelete != null && rowToDelete.isEmpty()))
				return false;

			deleteByUriFromFileDBIndex(rowToDelete);
			deleteByUriFromTargetIndex(rowToDelete);
			return true;

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

	public final boolean deleteByParentUri(List<String> rowToDelete)
			throws SearchLibException {
		if (rowToDelete == null
				|| (rowToDelete != null && rowToDelete.isEmpty()))
			return false;
		try {
			List<Target> mappedPath = targetClient.getFileCrawlerFieldMap()
					.getLinks(FileItemFieldEnum.subDirectory.getName());

			for (String uriString : rowToDelete) {
				URI uri = new URI(uriString);
				String path = uri.getPath();

				SearchRequest searchRequest = fileDbClient
						.getNewSearchRequest();
				FileItemFieldEnum.subDirectory.setQuery(searchRequest, path,
						true);
				fileDbClient.deleteDocuments(searchRequest);

				if (mappedPath != null && !mappedPath.isEmpty()) {
					SearchRequest deleteRequestTarget = targetClient
							.getNewSearchRequest();
					StringBuffer query = new StringBuffer();
					ItemField.addQuery(query, mappedPath.get(0).getName(),
							path, true);
					deleteRequestTarget.setQueryString(query.toString());
					targetClient.deleteDocuments(deleteRequestTarget);

				}
			}
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} catch (ParseException e) {
			throw new SearchLibException(e);
		} catch (CorruptIndexException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
			throw new SearchLibException(e);
		} catch (SyntaxError e) {
			throw new SearchLibException(e);
		} catch (InterruptedException e) {
			throw new SearchLibException(e);
		}
		return true;
	}

	/**
	 * Send delete order to DB index for Filename list given
	 * 
	 * @param rowToDelete
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
	private final void deleteByUriFromFileDBIndex(List<String> rowToDelete)
			throws SearchLibException, IOException, ParseException,
			SyntaxError, URISyntaxException, ClassNotFoundException,
			InterruptedException, InstantiationException,
			IllegalAccessException {

		SearchRequest deleteRequest = fileDbClient.getNewSearchRequest();

		deleteRequest.setQueryString(buildQueryString(
				FileItemFieldEnum.uri.getName(), rowToDelete, true));
		fileDbClient.deleteDocuments(deleteRequest);
	}

	/**
	 * Send delete order to final index for Filename list given
	 * 
	 * @param rowToDelete
	 * @return
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
	private final boolean deleteByUriFromTargetIndex(List<String> rowToDelete)
			throws SearchLibException, IOException, ParseException,
			SyntaxError, URISyntaxException, ClassNotFoundException,
			InterruptedException, InstantiationException,
			IllegalAccessException {

		List<Target> mappedPath = targetClient.getFileCrawlerFieldMap()
				.getLinks(FileItemFieldEnum.uri.getName());

		if (mappedPath == null || mappedPath.isEmpty())
			return false;

		SearchRequest deleteRequestTarget = targetClient.getNewSearchRequest();
		deleteRequestTarget.setQueryString(buildQueryString(mappedPath.get(0)
				.getName(), rowToDelete, true));

		targetClient.deleteDocuments(deleteRequestTarget);
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
			throws SearchLibException, IOException, ParseException,
			SyntaxError, URISyntaxException, ClassNotFoundException,
			InterruptedException, InstantiationException,
			IllegalAccessException {

		SearchRequest deleteRequest = fileDbClient.getNewSearchRequest();
		FileItemFieldEnum.repository.setQuery(deleteRequest, repository, true);
		fileDbClient.deleteDocuments(deleteRequest);
	}

	private final boolean deleteByRepositoryFromTargetIndex(String repository)
			throws SearchLibException, IOException, ParseException,
			SyntaxError, URISyntaxException, ClassNotFoundException,
			InterruptedException, InstantiationException,
			IllegalAccessException {

		List<Target> mappedPath = targetClient.getFileCrawlerFieldMap()
				.getLinks(FileItemFieldEnum.repository.toString());

		if (mappedPath == null || mappedPath.isEmpty())
			return false;

		SearchRequest deleteRequest = targetClient.getNewSearchRequest();
		deleteRequest.setQueryString(mappedPath.get(0).getName() + ":\""
				+ repository + "\"");

		targetClient.deleteDocuments(deleteRequest);
		return true;
	}

	public void updateCrawls(List<CrawlFile> crawls) throws SearchLibException {
		try {
			// Update target index
			List<IndexDocument> documents = new ArrayList<IndexDocument>(
					crawls.size());
			for (CrawlFile crawl : crawls) {
				IndexDocument indexDocument = crawl.getTargetIndexDocument();
				if (indexDocument != null)
					documents.add(indexDocument);
			}
			targetClient.updateDocuments(documents);

			// Update FilePath DB
			documents.clear();
			for (CrawlFile crawl : crawls) {
				IndexDocument indexDocument = crawl.getFileItem()
						.getIndexDocument();
				documents.add(indexDocument);
			}
			fileDbClient.updateDocuments(documents);
		} catch (NoSuchAlgorithmException e) {
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
		}
	}

	/**
	 * Build query from a String list
	 * 
	 * @param rows
	 * @return
	 */
	private String buildQueryString(String parameter, List<String> rows,
			boolean fuzzy) {
		StringBuffer query = new StringBuffer(parameter);
		query.append(":(");
		for (String name : rows) {
			query.append(SearchRequest.escapeQuery(name));
			if (fuzzy)
				query.append("*");
			query.append(" OR ");
		}
		query.replace(query.length() - 4, query.length(), ")");
		return query.toString();
	}

	protected ExtensibleEnum<FileInstanceType> getNewFileInstanceTypeEnum() {
		return new FileInstanceTypeEnum();
	}

	public ExtensibleEnum<FileInstanceType> getFileTypeEnum() {
		synchronized (this) {
			if (fileInstanceTypeEnum != null)
				return fileInstanceTypeEnum;
			fileInstanceTypeEnum = getNewFileInstanceTypeEnum();
			return fileInstanceTypeEnum;
		}
	}

}