/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2011 Emmanuel Keller / Jaeksoft
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

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.util.StringUtils;
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

	private transient List<TermFreq> termList;

	private transient String searchTerm;

	private transient TermEnum currentTermEnum;

	private transient String[] fieldList;

	private transient String currentField;

	public TermController() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() throws SearchLibException {
		synchronized (this) {
			searchTerm = "";
			termList = null;
			fieldList = null;
			currentField = null;
			try {
				if (currentTermEnum != null)
					currentTermEnum.close();
			} catch (IOException e) {
				Logging.warn(e);
			}
			currentTermEnum = null;
		}
	}

	private TermEnum getTermEnum() throws IOException, SearchLibException {
		synchronized (this) {
			if (currentTermEnum == null)
				setTermEnum();
			return currentTermEnum;
		}
	}

	private TermEnum buildTermEnum() throws IOException, SearchLibException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return null;
			String currentField = getCurrentField();
			if (currentField == null)
				return null;
			return client.getTermEnum(currentField, getSearchTerm());
		}
	}

	private void setTermEnum() throws IOException, SearchLibException {
		synchronized (this) {
			if (currentTermEnum != null) {
				currentTermEnum.close();
				currentTermEnum = null;
			}
			currentTermEnum = buildTermEnum();
		}
	}

	private void setTermList() throws IOException, SearchLibException {
		synchronized (this) {
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
	}

	private void setFieldList() throws IOException, SearchLibException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return;
			fieldList = StringUtils.toStringArray(client.getIndexAbstract()
					.getFieldNames(), true);
		}
	}

	public List<TermFreq> getTermList() throws IOException, SearchLibException {
		synchronized (this) {
			if (termList == null)
				setTermList();
			return termList;
		}
	}

	public String[] getFieldList() throws IOException, SearchLibException {
		synchronized (this) {
			if (fieldList == null)
				setFieldList();
			return fieldList;
		}
	}

	/**
	 * @param searchTerm
	 *            the searchTerm to set
	 */
	public void setSearchTerm(String searchTerm) {
		synchronized (this) {
			this.searchTerm = searchTerm;
		}
	}

	/**
	 * @return the searchTerm
	 */
	public String getSearchTerm() {
		synchronized (this) {
			return searchTerm;
		}
	}

	public String getCurrentField() throws IOException, SearchLibException {
		synchronized (this) {
			String[] fieldList = getFieldList();
			if (fieldList == null)
				return null;
			if (currentField == null && fieldList.length > 0)
				currentField = fieldList[0];
			return currentField;
		}
	}

	public void setCurrentField(String field) {
		synchronized (this) {
			currentField = field;
		}
	}

	public void onSearch() throws IOException, SearchLibException {
		synchronized (this) {
			setTermEnum();
			setTermList();
			reloadPage();
		}
	}

	public void onReset() throws IOException, SearchLibException {
		synchronized (this) {
			setSearchTerm("");
			onSearch();
		}
	}

	public void onNext() throws IOException, SearchLibException {
		synchronized (this) {
			setTermList();
			reloadPage();
		}
	}

	public void onExport() throws IOException, SearchLibException {
		synchronized (this) {
			PrintWriter pw = null;
			TermEnum termEnum = null;
			try {
				File tempFile = File.createTempFile("OSS_term_freq", ".csv");
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
				termEnum.close();
				termEnum = null;
				pw = null;
				Filedownload.save(new FileInputStream(tempFile),
						"text/csv; charset-UTF-8", "OSS_term_freq_"
								+ currentField + ".csv");
			} finally {
				if (pw != null)
					pw.close();
				if (termEnum != null)
					termEnum.close();
			}
		}
	}

	@Override
	public void eventSchemaChange() throws SearchLibException {
		synchronized (this) {
			fieldList = null;
			reloadPage();
		}
	}
}
