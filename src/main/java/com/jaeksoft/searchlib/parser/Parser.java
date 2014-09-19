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

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.transform.TransformerConfigurationException;

import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.streamlimiter.LimitException;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiter;
import com.jaeksoft.searchlib.util.FileUtils;
import com.jaeksoft.searchlib.util.StringUtils;

public abstract class Parser extends ParserFactory {

	private IndexDocument sourceDocument;

	private StreamLimiter streamLimiter;

	private List<ParserResultItem> resultItems;

	private Set<String> detectedLinks;

	private Throwable error;

	protected Parser(ParserFieldEnum[] fieldList, boolean externalAllowed) {
		super(fieldList, externalAllowed);
		sourceDocument = null;
		streamLimiter = null;
		resultItems = new ArrayList<ParserResultItem>(0);
		detectedLinks = new TreeSet<String>();
		error = null;
	}

	protected Parser(ParserFieldEnum[] fieldList) {
		this(fieldList, true);
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

	public ExternalParser.Results getExternalResults() {
		return new ExternalParser.Results(resultItems, detectedLinks, error);
	}

	public void setExternalResults(ExternalParser.Results results) {
		if (results == null)
			return;
		if (results.results != null)
			for (ExternalParser.Result result : results.results)
				resultItems.add(new ParserResultItem(this, result));
		if (results.links != null)
			detectedLinks.addAll(results.links);
	}

	public Set<String> getDetectedLinks() {
		return detectedLinks;
	}

	protected final void addDetectedLink(final String link) {
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

	private final String getErrorText(Throwable t) {
		return StringUtils.fastConcat("Error while working on URL: ",
				streamLimiter.getOriginURL(), " : ", t.getMessage());
	}

	final public void doParserContentExternal(
			final IndexDocument sourceDocument,
			final StreamLimiter streamLimiter, final LanguageEnum lang) {
		if (!externalAllowed) {
			doParserContent(sourceDocument, streamLimiter, lang);
			return;
		}
		this.streamLimiter = streamLimiter;
		if (sourceDocument != null)
			setSourceDocument(sourceDocument);
		File tempDir = null;
		try {
			tempDir = FileUtils.createTempDirectory("oss-external-parser", "");
			ExternalParser.doParserContent(this, tempDir, sourceDocument,
					streamLimiter, lang);
		} catch (IOException e) {
			this.error = e;
			Logging.warn(getErrorText(e), e);
		} catch (SearchLibException e) {
			this.error = e;
			Logging.warn(getErrorText(e), e);
		} catch (TransformerConfigurationException e) {
			this.error = e;
			Logging.warn(getErrorText(e), e);
		} catch (SAXException e) {
			this.error = e;
			Logging.warn(getErrorText(e), e);
		} finally {
			if (tempDir != null)
				if (tempDir.exists())
					if (tempDir.isDirectory())
						FileUtils.deleteDirectoryQuietly(tempDir);
		}
	}

	final public void doParserContent(final IndexDocument sourceDocument,
			final StreamLimiter streamLimiter, final LanguageEnum lang) {
		if (sourceDocument != null)
			setSourceDocument(sourceDocument);
		try {
			this.streamLimiter = streamLimiter;
			parseContent(streamLimiter, lang);
		} catch (Exception e) {
			this.error = e;
			Logging.warn(getErrorText(e), e);
		}
	}

	final public Throwable getError() {
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

	public void mergeFiles(File fileDir, File destFile)
			throws SearchLibException {
		throw new SearchLibException(
				"This parser does not support file merge feature");
	}

}
