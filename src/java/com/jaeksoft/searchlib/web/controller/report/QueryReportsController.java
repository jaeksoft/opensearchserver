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
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Textbox;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.facet.Facet;
import com.jaeksoft.searchlib.facet.FacetItem;
import com.jaeksoft.searchlib.facet.FacetItemCountComparator;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.report.ReportsManager;
import com.jaeksoft.searchlib.util.TopSet;

public class QueryReportsController extends ReportsController {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1445551912133033172L;

	private Datebox beginDate, endDate;
	private Listbox reportsView, queryType, topQueriesDisplay;
	private Listitem topQueries, topHundred;
	private Facet facetReportsList;
	private Textbox topKeywords;

	public QueryReportsController() throws SearchLibException {
		super();
	}

	@Override
	public void doAfterCompose(Component component) throws Exception {
		super.doAfterCompose(component);
		queryType.setSelectedItem(topQueries);
		topQueriesDisplay.setSelectedItem(topHundred);
		reset();
	}

	@Override
	protected void reset() throws SearchLibException {
		facetReportsList = null;
		reportsView.getItems().clear();
	}

	public void onClick$createReport(Event event) throws SearchLibException,
			ParseException {
		ReportsManager reports = getReportsManager();
		int noOfResults = Integer.parseInt(topQueriesDisplay.getSelectedItem()
				.getLabel());
		String topKeywordsFilters = topKeywords.getText();
		String getFromDate = beginDate.getText();
		String getToDate = endDate.getText();
		if (queryType.getSelectedItem().getLabel()
				.equalsIgnoreCase("Top Queries")) {
			reportsView.getItems().clear();
			facetReportsList = reports.getSearchReport(topKeywordsFilters,
					getFromDate, getToDate, true, noOfResults);
		} else if (queryType.getSelectedItem().getLabel()
				.equalsIgnoreCase("Top Queries with no Results")) {
			reportsView.getItems().clear();
			facetReportsList = reports.getSearchReport(topKeywordsFilters,
					getFromDate, getToDate, false, noOfResults);
		}
		TopSet<FacetItem> topSet = new TopSet<FacetItem>(
				facetReportsList.getArray(), new FacetItemCountComparator(),
				500);
		TreeSet<FacetItem> topFacetItem = topSet.getTreeMap();
		for (FacetItem topItems : topFacetItem) {
			Listitem li = new Listitem();
			new Listcell(topItems.getTerm()).setParent(li);
			new Listcell(Integer.toString(topItems.getCount())).setParent(li);
			li.setParent(reportsView);
		}
		reportsView.setAutopaging(true);
		reportsView.setVisible(true);
	}

	public void onClick$exportReport(Event event) throws SearchLibException,
			IOException {

		if (facetReportsList == null || facetReportsList.getArray().length > 0) {
			alert("Please Create Some Report to Export");
			return;
		}
		PrintWriter pw = null;
		try {
			File tempFile = File.createTempFile("OSS_Query_Reports", "csv");
			pw = new PrintWriter(tempFile);
			int k = facetReportsList.getArray().length;
			for (int l = 0; l < k; l++) {
				pw.print('"');
				pw.print(facetReportsList.getTerm(l).replaceAll("\"", "\"\""));
				pw.print('"');
				pw.print(',');
				pw.println(facetReportsList.getCount(l));
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

}
