/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.parser;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.streamlimiter.LimitException;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiter;

public abstract class Parser extends ParserFactory {

	private IndexDocument sourceDocument;

	private StreamLimiter streamLimiter;

	private List<ParserResultItem> resultItems;

	private Set<String> detectedLinks;

	private Exception error;

	protected Parser(ParserFieldEnum[] fieldList) {
		super(fieldList);
		sourceDocument = null;
		streamLimiter = null;
		resultItems = new ArrayList<ParserResultItem>(0);
		detectedLinks = new TreeSet<String>();
		error = null;
	}

	public IndexDocument getSourceDocument() {
		return sourceDocument;
	}

	private void setSourceDocument(IndexDocument sourceDocument) {
		this.sourceDocument = sourceDocument;
	}

	protected ParserResultItem getNewParserResultItem() {
		ParserResultItem result = new ParserResultItem(this);
		resultItems.add(result);
		return result;
	}

	public List<ParserResultItem> getParserResults() {
		return resultItems;
	}

	public Set<String> getDetectedLinks() {
		return detectedLinks;
	}

	protected final void addDetectedLink(String link) {
		detectedLinks.add(link);
	}

	public boolean popupateResult(int resultPos, IndexDocument indexDocument)
			throws IOException {
		if (resultItems == null)
			return false;
		if (resultPos >= resultItems.size())
			return false;
		resultItems.get(resultPos).populate(indexDocument);
		return true;
	}

	protected abstract void parseContent(StreamLimiter streamLimiter,
			LanguageEnum lang) throws IOException, SearchLibException;

	final public void doParserContent(IndexDocument sourceDocument,
			StreamLimiter streamLimiter, LanguageEnum lang) {
		if (sourceDocument != null)
			setSourceDocument(sourceDocument);
		try {
			this.streamLimiter = streamLimiter;
			parseContent(streamLimiter, lang);
		} catch (IllegalArgumentException e) {
			this.error = e;
			Logging.warn(e);
		} catch (NullPointerException e) {
			this.error = e;
			Logging.warn(e);
		} catch (Exception e) {
			this.error = e;
			Logging.warn(e);
		}
	}

	final public Exception getError() {
		return error;
	}

	final public StreamLimiter getStreamLimiter() {
		return streamLimiter;
	}

	public String getMd5size() throws NoSuchAlgorithmException, LimitException,
			IOException {
		String hash = null;
		if (streamLimiter != null)
			hash = streamLimiter.getMD5Hash() + '_' + streamLimiter.getSize();
		return hash;
	}

	public boolean equals(Parser one) {
		return this.getClass().getName().equals(one.getClass().getName());
	}

	public String getFirstLang() {
		if (resultItems == null)
			return null;
		for (ParserResultItem result : resultItems) {
			String value = result.getFieldValue(ParserFieldEnum.lang, 0);
			if (value != null)
				return value;
		}
		return null;
	}
}
