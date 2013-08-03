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

import java.net.MalformedURLException;

import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.result.ResultDocument;

public class ReportItem {
	private String keywords, keywordsExact, datetime, responseTime,
			documentsFound, reportId;
	private String custom1, custom2, custom3, custom4, custom5, custom6,
			custom7, custom8, custom9;

	public ReportItem(ResultDocument doc) {
		this();
		setDatetime(doc.getValueContent(ReportItemFieldEnum.datetime.name(), 0));
		setKeywords(doc.getValueContent(ReportItemFieldEnum.keywords.name(), 0));
		setKeywordsExact(doc.getValueContent(
				ReportItemFieldEnum.keywordsExact.name(), 0));
		setResponseTime(doc.getValueContent(
				ReportItemFieldEnum.responseTime.name(), 0));
		setDocumentsFound(doc.getValueContent(
				ReportItemFieldEnum.documentsFound.name(), 0));
		setReportId(doc.getValueContent(ReportItemFieldEnum.reportId.name(), 0));
		setCustom1(doc.getValueContent(ReportItemFieldEnum.custom1.name(), 0));
		setCustom2(doc.getValueContent(ReportItemFieldEnum.custom2.name(), 0));
		setCustom3(doc.getValueContent(ReportItemFieldEnum.custom3.name(), 0));
		setCustom4(doc.getValueContent(ReportItemFieldEnum.custom4.name(), 0));
		setCustom5(doc.getValueContent(ReportItemFieldEnum.custom5.name(), 0));
		setCustom6(doc.getValueContent(ReportItemFieldEnum.custom6.name(), 0));
		setCustom7(doc.getValueContent(ReportItemFieldEnum.custom7.name(), 0));
		setCustom8(doc.getValueContent(ReportItemFieldEnum.custom8.name(), 0));
		setCustom9(doc.getValueContent(ReportItemFieldEnum.custom9.name(), 0));
	}

	public final String getReportId() {
		return reportId;
	}

	public final void setReportId(String reportId) {
		this.reportId = reportId;
	}

	public ReportItem() {
		keywords = null;
		datetime = null;
		responseTime = null;
		documentsFound = null;
		reportId = null;
	}

	public final String getDatetime() {
		return datetime;
	}

	public final void setDatetime(String datetime) {
		this.datetime = datetime;
	}

	public final String getResponseTime() {
		return responseTime;
	}

	public final void setResponseTime(String responseTime) {
		this.responseTime = responseTime;
	}

	public final String getDocumentsFound() {
		return documentsFound;
	}

	public final void setDocumentsFound(String documentsFound) {
		this.documentsFound = documentsFound;
	}

	public final String getKeywords() {

		return keywords;
	}

	public final void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public final String getCustom1() {
		return custom1;
	}

	public final void setCustom1(String custom1) {
		this.custom1 = custom1;
	}

	public final String getCustom2() {
		return custom2;
	}

	public final void setCustom2(String custom2) {
		this.custom2 = custom2;
	}

	public final String getCustom3() {
		return custom3;
	}

	public final void setCustom3(String custom3) {
		this.custom3 = custom3;
	}

	public final String getCustom4() {
		return custom4;
	}

	public final void setCustom4(String custom4) {
		this.custom4 = custom4;
	}

	public final String getCustom5() {
		return custom5;
	}

	public final void setCustom5(String custom5) {
		this.custom5 = custom5;
	}

	public final String getCustom6() {
		return custom6;
	}

	public final void setCustom6(String custom6) {
		this.custom6 = custom6;
	}

	public final String getCustom7() {
		return custom7;
	}

	public final void setCustom7(String custom7) {
		this.custom7 = custom7;
	}

	public final String getCustom8() {
		return custom8;
	}

	public final void setCustom8(String custom8) {
		this.custom8 = custom8;
	}

	public final String getCustom9() {
		return custom9;
	}

	public final void setCustom9(String custom9) {
		this.custom9 = custom9;
	}

	public final String getKeywordsExact() {
		return keywordsExact;
	}

	public final void setKeywordsExact(String keywordsExact) {
		this.keywordsExact = keywordsExact;
	}

	public void populate(IndexDocument indexDocument)
			throws MalformedURLException {
		if (datetime != null)
			indexDocument.setString(ReportItemFieldEnum.datetime.name(),
					getDatetime());
		if (keywordsExact != null)
			indexDocument.setString(ReportItemFieldEnum.keywordsExact.name(),
					getKeywordsExact());
		if (keywords != null)
			indexDocument.setString(ReportItemFieldEnum.keywords.name(),
					getKeywords());
		if (responseTime != null)
			indexDocument.setString(ReportItemFieldEnum.responseTime.name(),
					getResponseTime());
		if (documentsFound != null)
			indexDocument.setString(ReportItemFieldEnum.documentsFound.name(),
					getDocumentsFound());
		if (reportId != null)
			indexDocument.setString(ReportItemFieldEnum.reportId.name(),
					getReportId());
		if (custom1 != null)
			indexDocument.setString(ReportItemFieldEnum.custom1.name(),
					getCustom1());
		if (custom2 != null)
			indexDocument.setString(ReportItemFieldEnum.custom2.name(),
					getCustom1());
		if (custom3 != null)
			indexDocument.setString(ReportItemFieldEnum.custom3.name(),
					getCustom1());
		if (custom4 != null)
			indexDocument.setString(ReportItemFieldEnum.custom4.name(),
					getCustom1());
		if (custom5 != null)
			indexDocument.setString(ReportItemFieldEnum.custom5.name(),
					getCustom1());
		if (custom6 != null)
			indexDocument.setString(ReportItemFieldEnum.custom6.name(),
					getCustom1());
		if (custom7 != null)
			indexDocument.setString(ReportItemFieldEnum.custom7.name(),
					getCustom1());
		if (custom8 != null)
			indexDocument.setString(ReportItemFieldEnum.custom8.name(),
					getCustom1());
		if (custom9 != null)
			indexDocument.setString(ReportItemFieldEnum.custom9.name(),
					getCustom1());

	}
}
