/*
 *	License Agreement for Jaeksoft OpenSearchServer Enterprise
 *	Copyright (C) 2011 Emmanuel Keller / Jaeksoft
 *	
 *	This file is part of Jaeksoft OpenSearchServer Enterprise.
 *	See the License for more details. You should have received a
 *	copy of the License along with Jaeksoft OpenSearchServer
 *	Enterprise. If not, see http://www.open-search-server.com
 */
package com.jaeksoft.searchlib.report;

public class ReportList {

	private String reportName;

	public ReportList(String reportname, String reportdate) {

		this.reportName = reportname;
		this.reportDate = reportdate;
	}

	public final String getReportName() {
		return reportName;
	}

	public final void setReportName(String reportName) {
		this.reportName = reportName;
	}

	public final String getReportDate() {
		return reportDate;
	}

	public final void setReportDate(String reportDate) {
		this.reportDate = reportDate;
	}

	private String reportDate;

}
