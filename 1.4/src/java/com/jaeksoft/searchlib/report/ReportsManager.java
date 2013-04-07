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
package com.jaeksoft.searchlib.report;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.facet.Facet;
import com.jaeksoft.searchlib.facet.FacetField;
import com.jaeksoft.searchlib.facet.FacetList;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.AbstractResultSearch;

public class ReportsManager {

	private Client client;
	private Client reportsClient;

	public ReportsManager(Config config, File directory)
			throws SearchLibException {
		this.client = (Client) config;
		this.reportsClient = new Client(directory, "/report_config.xml", true);
	}

	private class ReportFileFilter implements FilenameFilter {

		private final String reportPrefix = "report." + client.getIndexName();

		@Override
		public boolean accept(File dir, String filename) {
			return filename.startsWith(reportPrefix);
		}
	}

	public File[] getReportsList() throws SearchLibException {
		File logDirectory = client.getLogReportManager().getLogDirectory();
		return logDirectory.listFiles(new ReportFileFilter());
	}

	public void updateReportItem(ReportItem reportItem)
			throws SearchLibException {

		try {
			IndexDocument indexDocument = new IndexDocument();
			reportItem.populate(indexDocument);
			reportsClient.updateDocument(indexDocument);

		} catch (MalformedURLException e) {
			throw new SearchLibException(e);
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

	public String[] generateReportItem(String reportLine) {
		return reportLine.split("\\s+");
	}

	private SearchRequest getNewSearchRequest(String defaultOperator, int rows)
			throws SearchLibException {
		SearchRequest searchRequest = new SearchRequest(reportsClient);
		searchRequest.setDefaultOperator(defaultOperator);
		searchRequest.setRows(rows);
		searchRequest.setPhraseSlop(2);
		searchRequest.setStart(0);
		searchRequest.addReturnField("keywords");
		searchRequest.addReturnField("reportId");
		searchRequest.addReturnField("keywordsExact");
		searchRequest.addReturnField("datetime");
		searchRequest.addReturnField("responseTime");
		searchRequest.getFacetFieldList().put(
				new FacetField("keywords", 1, false, false));
		searchRequest.setLang(LanguageEnum.UNDEFINED);
		return searchRequest;
	}

	private SearchRequest addDateFilter(SearchRequest searchRequest,
			String startDate, String endDate) throws ParseException {
		StringBuffer addDateBuffer = new StringBuffer();
		addDateBuffer.append(" ");
		addDateBuffer.append("datetime:[");
		addDateBuffer.append(startDate);
		addDateBuffer.append(" TO ");
		addDateBuffer.append(endDate);
		addDateBuffer.append("]");
		searchRequest.addFilter(addDateBuffer.toString(), false);
		return searchRequest;
	}

	private SearchRequest generateSearchRequest(String topKeywords,
			String startDate, String endDate, boolean withResult, int rows)
			throws ParseException, SearchLibException {
		SearchRequest searchRequest = getNewSearchRequest("OR", rows);
		if (!withResult) {
			StringBuffer addFilterBuffer = new StringBuffer();
			addFilterBuffer.append(" ");
			addFilterBuffer.append("documentsFound:" + 0);
			searchRequest.addFilter(addFilterBuffer.toString(), false);
		}
		searchRequest = addDateFilter(searchRequest, startDate, endDate);
		if (topKeywords != null && !topKeywords.equalsIgnoreCase("")) {
			searchRequest.setPatternQuery("keywordsExact:($$)");
			searchRequest.setQueryString(topKeywords);
		} else
			searchRequest.setQueryString("*:*");
		return searchRequest;
	}

	private final static SimpleDateFormat changeDateFormat = new SimpleDateFormat(
			"yyyyMMddhhmmss");

	private final String modifyDate(Date date) throws java.text.ParseException {
		synchronized (changeDateFormat) {
			return changeDateFormat.format(date);
		}
	}

	public Facet getSearchReport(String topKeywords, Date startDate,
			Date endDate, boolean withResult, int rows)
			throws SearchLibException, ParseException {

		SearchRequest searchRequest;
		String fromDate;
		String dateTo;

		try {
			fromDate = startDate == null ? "00000000000000"
					: modifyDate(startDate);
			dateTo = endDate == null ? "99999999999999" : modifyDate(endDate);
			searchRequest = generateSearchRequest(topKeywords, fromDate,
					dateTo, withResult, rows);
			AbstractResultSearch result = (AbstractResultSearch) reportsClient
					.request(searchRequest);
			FacetList facet = result.getFacetList();
			return facet.getByField("keywords");
		} catch (java.text.ParseException e) {
			throw new SearchLibException(e);
		}
	}

	public void reload(boolean optimize) throws SearchLibException {
		if (optimize) {
			reportsClient.reload();
			reportsClient.getIndex().optimize();
		}
	}

	public int loadReportFile(String filename)
			throws UnsupportedEncodingException, IOException,
			SearchLibException {
		List<String> fileList = new ArrayList<String>();
		int count = 0;

		FileInputStream fstream = null;
		DataInputStream in = null;
		BufferedReader br = null;
		File reportFile = new File(client.getLogReportManager()
				.getLogDirectory(), filename);
		try {
			fstream = new FileInputStream(reportFile);
			in = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				fileList.add(strLine);
				if (fileList.size() >= 1000)
					loadReportItems(fileList, reportFile);
				count++;
			}
			if (fileList.size() != 0)
				loadReportItems(fileList, reportFile);
			in.close();
			return count;
		} finally {
			if (br != null)
				IOUtils.closeQuietly(br);
			if (in != null)
				IOUtils.closeQuietly(in);
			if (fstream != null)
				IOUtils.closeQuietly(fstream);
		}
	}

	public void loadReportItems(List<String> fileList, File reportFile)
			throws UnsupportedEncodingException, SearchLibException {
		List<ReportItem> reportItems = createIndexvalues(fileList, reportFile);
		updateReportItems(reportItems);
		fileList.clear();
	}

	private List<ReportItem> createIndexvalues(List<String> reportFileString,
			File reportFile) throws UnsupportedEncodingException {
		List<ReportItem> listReportItem = new ArrayList<ReportItem>();
		int id = 1;
		for (String reportList : reportFileString) {
			String[] reportItems = generateReportItem(reportList);
			ReportItem reportItem = new ReportItem();
			reportItem.setReportId(reportFile.getName() + "_" + id);
			reportItem.setDatetime(createDate(reportItems[0]));
			reportItem.setKeywords(URLDecoder.decode(reportItems[1], "UTF-8"));
			reportItem.setKeywordsExact(URLDecoder.decode(reportItems[1],
					"UTF-8"));
			reportItem.setResponseTime(reportItems[2]);
			reportItem.setDocumentsFound(reportItems[3]);
			listReportItem.add(reportItem);
			id++;
		}
		return listReportItem;
	}

	public void updateReportItems(List<ReportItem> listItem)
			throws SearchLibException {
		for (ReportItem reportItemList : listItem)
			updateReportItem(reportItemList);
		reload(true);
	}

	public String createDate(String date) {
		String[] sDate = date.split("T");
		String yyymmdd = sDate[0].replaceAll("\\-", "");
		String hhmmss = sDate[1].substring(0, sDate[1].length() - 5);
		hhmmss = hhmmss.replaceAll(":", "");
		return yyymmdd + hhmmss;
	}

}
