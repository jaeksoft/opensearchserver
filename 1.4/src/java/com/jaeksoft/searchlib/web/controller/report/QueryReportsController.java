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
package com.jaeksoft.searchlib.web.controller.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.zkoss.bind.annotation.Command;
import org.zkoss.zul.Filedownload;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.facet.Facet;
import com.jaeksoft.searchlib.facet.FacetItem;
import com.jaeksoft.searchlib.facet.FacetItemCountComparator;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.report.ReportsManager;
import com.jaeksoft.searchlib.util.TopSet;

public class QueryReportsController extends ReportsController {

	private Date beginDate;
	private Date endDate;
	private String queryType;
	private int numberOfQuery;
	private String topKeywords;

	private TreeSet<FacetItem> reportSet;

	public QueryReportsController() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() throws SearchLibException {
		beginDate = new Date();
		endDate = new Date();
		queryType = "topqueries";
		numberOfQuery = 100;
		reportSet = null;
	}

	@Command
	public void onCreateReport() throws SearchLibException, ParseException {
		ReportsManager reports = getReportsManager();
		Facet facetReportsList = null;
		if ("topqueries".equals(queryType)) {
			facetReportsList = reports.getSearchReport(topKeywords, beginDate,
					endDate, true, numberOfQuery);
		} else if ("topqueriesnoresult".equals(queryType)) {
			facetReportsList = reports.getSearchReport(topKeywords, beginDate,
					endDate, false, numberOfQuery);
		}
		TopSet<FacetItem> topSet = new TopSet<FacetItem>(
				facetReportsList.getArray(), new FacetItemCountComparator(),
				numberOfQuery);
		reportSet = topSet.getTreeMap();
		reload();
	}

	public TreeSet<FacetItem> getReportSet() {
		return reportSet;
	}

	public boolean isReportSetExists() {
		return reportSet != null;
	}

	@Command
	public void onExportReport() throws SearchLibException, IOException {
		PrintWriter pw = null;
		try {
			File tempFile = File.createTempFile("OSS_Query_Reports", "csv");
			pw = new PrintWriter(tempFile);
			for (FacetItem facetItem : reportSet) {
				pw.print('"');
				pw.print(facetItem.getTerm().replaceAll("\"", "\"\""));
				pw.print('"');
				pw.print(',');
				pw.println(facetItem.getCount());
			}
			pw.close();
			pw = null;
			Filedownload.save(new FileInputStream(tempFile),
					"text/csv; charset-UTF-8", "OSS_Query_Reports.csv");
		} finally {
			if (pw != null)
				IOUtils.closeQuietly(pw);
		}
	}

	/**
	 * @return the beginDate
	 */
	public Date getBeginDate() {
		return beginDate;
	}

	/**
	 * @param beginDate
	 *            the beginDate to set
	 */
	public void setBeginDate(Date beginDate) {
		this.beginDate = beginDate;
	}

	/**
	 * @return the endDate
	 */
	public Date getEndDate() {
		return endDate;
	}

	/**
	 * @param endDate
	 *            the endDate to set
	 */
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	/**
	 * @return the queryType
	 */
	public String getQueryType() {
		return queryType;
	}

	/**
	 * @param queryType
	 *            the queryType to set
	 */
	public void setQueryType(String queryType) {
		this.queryType = queryType;
	}

	/**
	 * @return the numberOfQuery
	 */
	public int getNumberOfQuery() {
		return numberOfQuery;
	}

	/**
	 * @param numberOfQuery
	 *            the numberOfQuery to set
	 */
	public void setNumberOfQuery(int numberOfQuery) {
		this.numberOfQuery = numberOfQuery;
	}

	/**
	 * @return the topKeywords
	 */
	public String getTopKeywords() {
		return topKeywords;
	}

	/**
	 * @param topKeywords
	 *            the topKeywords to set
	 */
	public void setTopKeywords(String topKeywords) {
		this.topKeywords = topKeywords;
	}

}
