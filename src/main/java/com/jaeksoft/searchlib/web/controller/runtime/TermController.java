/*
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2010-2017 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.jaeksoft.searchlib.web.controller.runtime;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.web.controller.CommonController;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zul.Filedownload;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@AfterCompose(superclass = true)
public class TermController extends CommonController {

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

	private transient int currentTermPos;

	private transient String[] fieldList;

	private transient String currentField;

	public TermController() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() throws SearchLibException {
		synchronized (this) {
			searchTerm = StringUtils.EMPTY;
			termList = null;
			fieldList = null;
			currentField = null;
		}
	}

	private int setTermList() throws IOException, SearchLibException {
		synchronized (this) {
			if (termList == null)
				termList = new ArrayList<>();
			else
				termList.clear();
			final Client client = getClient();
			if (client == null)
				return 0;
			final String currentField = getCurrentField();
			if (currentField == null)
				return 0;
			client.termEnum(new Term(currentField, getSearchTerm()), termEnum -> {
				if (termEnum == null)
					return;
				int start = currentTermPos;
				while (start-- != 0 && termEnum.term() != null)
					if (!termEnum.next())
						return;
				int i = 20;
				while (i-- != 0 && termEnum.term() != null) {
					if (!termEnum.term().field().equals(currentField))
						break;
					termList.add(new TermFreq(termEnum));
					if (!termEnum.next())
						break;
				}
			});
			return termList.size();
		}
	}

	private void setFieldList() throws IOException, SearchLibException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return;
			fieldList = StringUtils.toStringArray(client.getIndexAbstract().getFieldNames(), true);
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
	 * @param searchTerm the searchTerm to set
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

	@NotifyChange("*")
	public void setCurrentField(String field) throws IOException, SearchLibException {
		synchronized (this) {
			currentField = field;
			onSearch();
		}
	}

	@Command
	@NotifyChange("*")
	public void onSearch() throws IOException, SearchLibException {
		synchronized (this) {
			currentTermPos = 0;
			currentTermPos += setTermList();
		}
	}

	@Command
	@NotifyChange("*")
	public void onReset() throws IOException, SearchLibException {
		synchronized (this) {
			setSearchTerm("");
			onSearch();
		}
	}

	@Command
	@NotifyChange("*")
	public void onNext() throws IOException, SearchLibException {
		synchronized (this) {
			currentTermPos += setTermList();
		}
	}

	@Command
	@NotifyChange("*")
	public void onExport() throws IOException, SearchLibException {
		synchronized (this) {

			final Client client = getClient();
			if (client == null)
				return;
			final String currentField = getCurrentField();
			if (currentField == null)
				return;

			File tempFile = File.createTempFile("OSS_term_freq", ".csv");

			;
			try (final PrintWriter pw = new PrintWriter(tempFile)) {

				client.termEnum(new Term(currentField, getSearchTerm()), termEnum -> {
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
				});

				pw.close();
				Filedownload.save(new FileInputStream(tempFile), "text/csv; charset-UTF-8",
						"OSS_term_freq_" + currentField + ".csv");
			}
		}
	}

	@Override
	public void eventSchemaChange(Client client) throws SearchLibException {
		synchronized (this) {
			fieldList = null;
			reload();
		}
	}
}
