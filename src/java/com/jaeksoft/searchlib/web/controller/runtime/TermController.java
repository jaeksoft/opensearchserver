/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.controller.runtime;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.zkoss.zul.Filedownload;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.web.controller.CommonController;

public class TermController extends CommonController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5220310778380178064L;

	public class TermFreq {

		private Term term;

		private int freq;

		private TermFreq(TermEnum termEnum) {
			this.term = termEnum.term();
			this.freq = termEnum.docFreq();
		}

		public String getTerm() {
			return term.text();
		}

		public int getFreq() {
			return freq;
		}

		public String getField() {
			return term.field();
		}
	}

	private List<TermFreq> termList;

	private String searchTerm;

	private TermEnum currentTermEnum;

	private List<String> fieldList;

	private String currentField;

	public TermController() throws SearchLibException {
		super();
		searchTerm = "";
		termList = null;
		fieldList = null;
		currentField = null;
		currentTermEnum = null;
	}

	private TermEnum getTermEnum() throws IOException, SearchLibException {
		if (currentTermEnum == null)
			setTermEnum();
		return currentTermEnum;
	}

	private TermEnum buildTermEnum() throws IOException, SearchLibException {
		return getClient().getIndex().getTermEnum(getCurrentField(),
				getSearchTerm());
	}

	private void setTermEnum() throws IOException, SearchLibException {
		if (currentTermEnum != null)
			currentTermEnum.close();
		currentTermEnum = buildTermEnum();
	}

	private void setTermList() throws IOException, SearchLibException {
		if (termList == null)
			termList = new ArrayList<TermFreq>();
		else
			termList.clear();
		TermEnum termEnum = getTermEnum();
		if (termEnum == null)
			return;
		int i = 20;
		while (i-- != 0 && termEnum.term() != null) {
			if (!termEnum.term().field().equals(currentField))
				break;
			termList.add(new TermFreq(termEnum));
			if (!termEnum.next())
				break;
		}
	}

	private void setFieldList() throws IOException, SearchLibException {
		if (fieldList == null)
			fieldList = new ArrayList<String>();
		else
			fieldList.clear();
		for (Object f : getClient().getIndex().getFieldNames())
			fieldList.add(f.toString());
	}

	public List<TermFreq> getTermList() throws IOException, SearchLibException {
		if (termList == null)
			setTermList();
		return termList;
	}

	public List<String> getFieldList() throws IOException, SearchLibException {
		if (fieldList == null)
			setFieldList();
		return fieldList;
	}

	/**
	 * @param searchTerm
	 *            the searchTerm to set
	 */
	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}

	/**
	 * @return the searchTerm
	 */
	public String getSearchTerm() {
		return searchTerm;
	}

	public String getCurrentField() throws IOException, SearchLibException {
		if (currentField == null && getFieldList().size() > 0)
			currentField = getFieldList().get(0);
		return currentField;
	}

	public void setCurrentField(String field) {
		currentField = field;
	}

	public void onSearch() throws IOException, SearchLibException {
		setTermEnum();
		setTermList();
		reloadPage();
	}

	public void onReset() throws IOException, SearchLibException {
		setSearchTerm("");
		onSearch();
	}

	public void onNext() throws IOException, SearchLibException {
		setTermList();
		reloadPage();
	}

	public void onExport() throws IOException, SearchLibException {
		PrintWriter pw = null;
		TermEnum termEnum = null;
		try {
			File tempFile = File.createTempFile("OSS_term_freq", "csv");
			pw = new PrintWriter(tempFile);
			termEnum = buildTermEnum();
			while (termEnum.term() != null) {
				if (!termEnum.term().field().equals(currentField))
					break;
				pw.print('"');
				pw.print(termEnum.term().text().replaceAll("\"", "\"\""));
				pw.print('"');
				pw.print(',');
				pw.println(termEnum.docFreq());
				if (!termEnum.next())
					break;
			}
			pw.close();
			pw = null;
			Filedownload.save(new FileInputStream(tempFile),
					"text/csv; charset-UTF-8", "OSS_term_freq_" + currentField
							+ ".csv");
		} finally {
			if (pw != null)
				pw.close();
			if (termEnum != null)
				termEnum.close();
		}

	}

	@Override
	public void reset() {
		termList = null;
	}

}
