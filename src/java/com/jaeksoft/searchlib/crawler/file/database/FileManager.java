/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
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

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.store.LockObtainFailedException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.common.database.FetchStatus;
import com.jaeksoft.searchlib.crawler.common.database.IndexStatus;
import com.jaeksoft.searchlib.crawler.common.database.ParserStatus;
import com.jaeksoft.searchlib.crawler.file.spider.CrawlFile;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.Result;
import com.jaeksoft.searchlib.result.ResultDocument;

public class FileManager {

	private static final int MAX_FILE_RETURN = 10000;
	private static final String FILE_SEARCH = "fileSearch";

	public enum Field {

		FILE("uri"), WHEN("when"), CONTENTBASETYPE("contentBaseType"), CONTENTTYPECHARSET(
				"contentTypeCharset"), CONTENTENCODING("contentEncoding"), CONTENTLENGTH(
				"contentLength"), LANG("lang"), LANGMETHOD("langMethod"), FETCHSTATUS(
				"fetchStatus"), RESPONSECODE("responseCode"), PARSERSTATUS(
				"parserStatus"), INDEXSTATUS("indexStatus");

		private final String name;

		private Field(String name) {
			this.name = name;
		}

		private void addFilterQuery(SearchRequest request, Object value)
				throws ParseException {
			StringBuffer sb = new StringBuffer();
			addQuery(sb, value);
			request.addFilter(sb.toString());
		}

		private void addFilterRange(SearchRequest request, Object from,
				Object to) throws ParseException {
			StringBuffer sb = new StringBuffer();
			addQueryRange(sb, from, to);
			request.addFilter(sb.toString());
		}

		private void addQuery(StringBuffer sb, Object value) {
			sb.append(" ");
			sb.append(name);
			sb.append(":");
			sb.append(value);
		}

		private void addQueryRange(StringBuffer sb, Object from, Object to) {
			sb.append(" ");
			sb.append(name);
			sb.append(":[");
			sb.append(from);
			sb.append(" TO ");
			sb.append(to);
			sb.append("]");
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private final Client fileDbClient;
	private final Client targetClient;

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

	public void deleteByOriginalUri(String value) throws SearchLibException {
		try {
			URI uri = (new File(value)).toURI();

			// Delete in file index
			SearchRequest deleteRequest = fileDbClient.getNewSearchRequest();
			deleteRequest.setQueryString(FileItemFieldEnum.originalUri.name()
					+ ":\"" + SearchRequest.escapeQuery(uri.toASCIIString())
					+ '"');
			deleteRequest.setDelete(true);
			fileDbClient.search(deleteRequest);

			// Delete in final index if a mapping is found
			List<String> mappedField = targetClient.getFileCrawlerFieldMap()
					.getLinks(FileItemFieldEnum.originalUri.name());
			SearchRequest deleteRequestTarget = targetClient
					.getNewSearchRequest();
			deleteRequestTarget.setQueryString(mappedField.get(0) + ":\""
					+ SearchRequest.escapeQuery(uri.toASCIIString()) + '"');
			deleteRequestTarget.setDelete(true);
			targetClient.search(deleteRequestTarget);

			reload(true);
		} catch (CorruptIndexException e) {
			throw new SearchLibException(e);
		} catch (LockObtainFailedException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} catch (ParseException e) {
			throw new SearchLibException(e);
		} catch (SyntaxError e) {
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
			throw new SearchLibException(e);
		} catch (InterruptedException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		}

	}

	public FileItem find(String path) throws SearchLibException,
			CorruptIndexException, ParseException, UnsupportedEncodingException {
		SearchRequest request = getPathSearchRequest();
		request.setQueryString("*:*");
		
		request.addFilter(FileItemFieldEnum.uri.name() + ":\""
				+ SearchRequest.escapeQuery(path) + '"');
		List<FileItem> listFileItem = new ArrayList<FileItem>();
		getFiles(request, null, false, 0, 10, listFileItem);

		if (listFileItem.size() > 0)
			return listFileItem.get(0);

		return null;
	}

	private SearchRequest getPathSearchRequest() throws SearchLibException {
		SearchRequest searchRequest = fileDbClient.getNewSearchRequest();
		searchRequest.setDefaultOperator("OR");
		searchRequest.setRows(0);
		searchRequest.addReturnField("uri");
		searchRequest.addReturnField("originalUri");
		searchRequest.addReturnField("contentLength");
		searchRequest.addReturnField("lang");
		searchRequest.addReturnField("langMethod");
		searchRequest.addReturnField("when");
		searchRequest.addReturnField("parserStatus");
		searchRequest.addReturnField("indexStatus");
		searchRequest.addReturnField("fetchStatus");
		searchRequest.addReturnField("crawlDate");
		searchRequest.addReturnField("fileSystemDate");
		searchRequest.addReturnField("fileSize");
		searchRequest.addReturnField("fileExtension");
		return searchRequest;
	}

	private SearchRequest getDirectoryUriSearchRequest()
			throws SearchLibException {
		SearchRequest searchRequest = fileDbClient.getNewSearchRequest();
		searchRequest.setDefaultOperator("OR");
		searchRequest.setRows(MAX_FILE_RETURN);
		searchRequest.addReturnField("uri");
		return searchRequest;
	}

	public List<FileItem> findAllByDirectoryURI(URI parentUri)
			throws SearchLibException, CorruptIndexException, ParseException,
			UnsupportedEncodingException {

		SearchRequest request = getDirectoryUriSearchRequest();
		request.setQueryString("*:*");
		request.addFilter(FileItemFieldEnum.directoryUri.name() + ":\""
				+ SearchRequest.escapeQuery(parentUri.toASCIIString()) + '"');
		request.addSort(FileItemFieldEnum.uri.name(), false);

		List<FileItem> listFileItem = new ArrayList<FileItem>();
		getFiles(request, null, false, 0, MAX_FILE_RETURN, listFileItem);

		return listFileItem;
	}

	public SearchRequest fileQuery(String like, String lang, String langMethod,
			Integer minContentLength, Integer maxContentLength,
			FetchStatus fetchStatus, ParserStatus parserStatus,
			IndexStatus indexStatus, Date startDate, Date endDate)
			throws SearchLibException {
		try {

			SearchRequest searchRequest = fileDbClient
					.getNewSearchRequest(FILE_SEARCH);

			StringBuffer query = new StringBuffer();
			if (like != null) {
				like = like.trim();
				if (like.length() > 0) {
					Field.FILE.addQuery(query, SearchRequest.escapeQuery(like));
					query.append("*");
				}
			}
			if (lang != null) {
				lang = lang.trim();
				if (lang.length() > 0)
					Field.LANG.addFilterQuery(searchRequest, SearchRequest
							.escapeQuery(lang));
			}
			if (langMethod != null) {
				langMethod = langMethod.trim();
				if (langMethod.length() > 0)
					Field.LANGMETHOD.addFilterQuery(searchRequest,
							SearchRequest.escapeQuery(langMethod));
			}

			if (fetchStatus != null && fetchStatus != FetchStatus.ALL)
				Field.FETCHSTATUS.addFilterQuery(searchRequest,
						fetchStatus.value);
			if (parserStatus != null && parserStatus != ParserStatus.ALL)
				Field.PARSERSTATUS.addFilterQuery(searchRequest,
						parserStatus.value);
			if (indexStatus != null && indexStatus != IndexStatus.ALL)
				Field.INDEXSTATUS.addFilterQuery(searchRequest,
						indexStatus.value);

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
				Field.CONTENTLENGTH.addQueryRange(query, from, to);
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
				Field.WHEN.addFilterRange(searchRequest, from, to);
			}

			if (query.length() == 0)
				query.append("*:*");
			searchRequest.setQueryString(query.toString().trim());
			return searchRequest;
		} catch (ParseException e) {
			throw new SearchLibException(e);
		}
	}

	public long getFiles(SearchRequest searchRequest, Field orderBy,
			boolean orderAsc, long start, long rows, List<FileItem> list)
			throws SearchLibException {
		searchRequest.setStart((int) start);
		searchRequest.setRows((int) rows);
		try {
			if (orderBy != null)
				searchRequest.addSort(orderBy.name, !orderAsc);
			Result result = fileDbClient.search(searchRequest);
			if (list != null)
				for (ResultDocument doc : result)
					list.add(new FileItem(doc));
			return result.getNumFound();

		} catch (ParseException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (RuntimeException e) {
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

	public void reload(boolean optimize) throws IOException,
			URISyntaxException, SearchLibException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {

		if (optimize) {
			fileDbClient.reload(null);
			fileDbClient.getIndex().optimize(null);
			targetClient.reload(null);
			targetClient.getIndex().optimize(null);
		}
		fileDbClient.reload(null);
		targetClient.reload(null);
	}

	public final int deleteByFilename(List<String> rowToDelete)
			throws SearchLibException {
		try {
			int nbDeleted = deleteByFilenameFromTargetIndex(rowToDelete);
			deleteByFilenameFromFileDBIndex(rowToDelete);
			return nbDeleted;

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

	/**
	 * Send delete order to DB index for Filename list given
	 * 
	 */
	public final void deleteByFilenameFromFileDBIndex(List<String> rowToDelete)
			throws SearchLibException, IOException, ParseException,
			SyntaxError, URISyntaxException, ClassNotFoundException,
			InterruptedException, InstantiationException,
			IllegalAccessException {
		if (rowToDelete == null
				|| (rowToDelete != null && rowToDelete.isEmpty()))
			return;

		// Build query
		StringBuffer query = new StringBuffer(":(");
		for (String name : rowToDelete) {
			query.append("").append(SearchRequest.escapeQuery(name)).append(
					"* OR ");
			System.out.println(SearchRequest.escapeQuery(name));
		}
		query.replace(query.length() - 3, query.length(), ")");

		SearchRequest deleteRequest = fileDbClient.getNewSearchRequest();
		deleteRequest.setQueryString(FileItemFieldEnum.uri.name()
				+ query.toString());

		deleteRequest.setDelete(true);
		fileDbClient.search(deleteRequest);
		// fileDbClient.reload(null);
	}

	/**
	 * Send delete order to final index for Filename list given
	 * 
	 */
	public final int deleteByFilenameFromTargetIndex(List<String> rowToDelete)
			throws SearchLibException, IOException, ParseException,
			SyntaxError, URISyntaxException, ClassNotFoundException,
			InterruptedException, InstantiationException,
			IllegalAccessException {

		if (rowToDelete == null
				|| (rowToDelete != null && rowToDelete.isEmpty()))
			return 0;

		List<String> mappedPath = targetClient.getFileCrawlerFieldMap()
				.getLinks(FileItemFieldEnum.uri.name());

		if (mappedPath.isEmpty())
			return 0;

		// Build query
		StringBuffer query = new StringBuffer(":(");
		for (String name : rowToDelete) {
			query.append("").append(SearchRequest.escapeQuery(name)).append(
					"* OR ");
			System.out.println(SearchRequest.escapeQuery(name));
		}
		query.replace(query.length() - 3, query.length(), ")");

		// Delete in final index if a mapping is found
		SearchRequest deleteRequestTarget = targetClient.getNewSearchRequest();
		deleteRequestTarget
				.setQueryString(mappedPath.get(0) + query.toString());
		deleteRequestTarget.setDelete(true);

		return targetClient.search(deleteRequestTarget).getNumFound();
	}

	public void updateCrawls(List<CrawlFile> crawls) throws SearchLibException {
		try {
			// Update target index
			List<IndexDocument> documents = new ArrayList<IndexDocument>(crawls
					.size());
			for (CrawlFile crawl : crawls) {
				IndexDocument indexDocument = crawl.getTargetIndexDocument();
				if (indexDocument != null)
					documents.add(indexDocument);
			}
			targetClient.updateDocuments(documents);

			// Update FilePath DB
			documents.clear();
			for (CrawlFile crawl : crawls) {
				IndexDocument indexDocument = new IndexDocument();
				crawl.getFileItem().populate(indexDocument);
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

}