/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.FieldMap;
import com.jaeksoft.searchlib.index.IndexDocument;

public abstract class ParserIndexDocumentIterator implements
		Iterator<IndexDocument> {

	private IOException ioException = null;
	private SearchLibException searchLibException = null;

	private final Iterator<ParserResultItem> resultIterator;
	private final FieldMap crawlItemFielMap;

	protected ParserIndexDocumentIterator(Parser parser,
			FieldMap crawlItemFielMap) {
		List<ParserResultItem> results = parser == null ? null : parser
				.getParserResults();
		resultIterator = results == null ? null : results.iterator();
		this.crawlItemFielMap = crawlItemFielMap;
	}

	@Override
	public boolean hasNext() {
		if (resultIterator == null)
			return false;
		return resultIterator.hasNext();
	}

	protected abstract IndexDocument getCrawlItemIndexDocument()
			throws UnsupportedEncodingException;

	protected abstract boolean checkPlugins(
			IndexDocument crawlItemIndexDocument,
			IndexDocument targetIndexDocument) throws SearchLibException,
			IOException;

	@Override
	public IndexDocument next() {
		if (resultIterator == null)
			return null;
		try {
			while (resultIterator.hasNext()) {
				ParserResultItem result = resultIterator.next();
				IndexDocument targetIndexDocument = new IndexDocument();
				IndexDocument crawlItemIndexDocument = getCrawlItemIndexDocument();
				crawlItemFielMap.mapIndexDocument(crawlItemIndexDocument,
						targetIndexDocument);

				result.populate(targetIndexDocument);

				if (!checkPlugins(crawlItemIndexDocument, targetIndexDocument))
					continue;

				return targetIndexDocument;
			}
		} catch (IOException e) {
			ioException = e;
		} catch (SearchLibException e) {
			searchLibException = e;
		}
		return null;
	}

	public void throwError() throws IOException, SearchLibException {
		if (ioException != null)
			throw ioException;
		else if (searchLibException != null)
			throw searchLibException;
	}

	@Override
	public void remove() {
	}

}
