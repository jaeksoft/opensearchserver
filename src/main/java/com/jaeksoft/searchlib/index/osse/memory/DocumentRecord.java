/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.index.osse.memory;

import java.io.Closeable;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.TokenStream;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.Analyzer;
import com.jaeksoft.searchlib.analysis.CompiledAnalyzer;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.index.FieldContent;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.index.osse.OsseTermBuffer;
import com.jaeksoft.searchlib.index.osse.OsseTokenTermUpdate;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.IOUtils;
import com.opensearchserver.utils.StringUtils;
import com.sun.jna.Memory;

public class DocumentRecord implements Closeable {

	private final MemoryBuffer memoryBuffer;
	private final OsseTermBuffer termBuffer;

	private final List<Long> fieldPtrArray = new ArrayList<Long>(1);
	private final List<Integer> numberOfTermsArray = new ArrayList<Integer>(1);

	private long currentFieldPtr;
	private int currentNumberOfTerms;

	private OsseFastStringArray ofsa = null;
	private OsseUint64Array ola = null;
	private OsseUint32Array oia = null;

	public DocumentRecord(OsseTermBuffer termBuffer, MemoryBuffer memoryBuffer,
			Schema schema, Map<String, Long> fieldPointerMap,
			IndexDocument document) throws SearchLibException, IOException {
		this.termBuffer = termBuffer;
		this.memoryBuffer = memoryBuffer;
		LanguageEnum lang = document.getLang();
		for (FieldContent fieldContent : document) {
			SchemaField schemaField = schema.getFieldList().get(
					fieldContent.getField());
			if (schemaField == null)
				throw new SearchLibException("Unknown field: "
						+ fieldContent.getField());
			Analyzer analyzer = schema.getAnalyzer(schemaField, lang);
			CompiledAnalyzer compiledAnalyzer = analyzer != null ? analyzer
					.getIndexAnalyzer() : null;
			Long fieldPtr = fieldPointerMap.get(schemaField.getName());
			if (fieldPtr == null)
				continue;
			currentFieldPtr = fieldPtr;
			currentNumberOfTerms = 0;
			for (FieldValueItem valueItem : fieldContent.getValues()) {
				String value = valueItem.getValue();
				if (compiledAnalyzer == null)
					addTerm(value);
				else
					addTerms(value, compiledAnalyzer);
			}
			endField();
		}
	}

	final private void endField() {
		if (currentNumberOfTerms == 0)
			return;
		fieldPtrArray.add(currentFieldPtr);
		numberOfTermsArray.add(currentNumberOfTerms);
	}

	@Override
	final public String toString() {
		return StringUtils.fastConcat(this.hashCode(),
				" numberOfTermsArray.size: ", numberOfTermsArray.size()
						+ " termBuffer:", termBuffer.getTermCount(), '/',
				termBuffer.getByteArrayCount());
	}

	final private void addTerm(final String term) throws IOException {
		termBuffer.addTerm(term);
		currentNumberOfTerms++;
	}

	private void addTerms(String value, CompiledAnalyzer compiledAnalyzer)
			throws IOException {
		OsseTokenTermUpdate ottu = null;
		StringReader stringReader = null;
		try {
			stringReader = new StringReader(value);
			TokenStream tokenStream = compiledAnalyzer.tokenStream(null,
					stringReader);
			ottu = new OsseTokenTermUpdate(termBuffer, tokenStream);
			while (ottu.incrementToken())
				;
			currentNumberOfTerms += ottu.getTermCount();
		} finally {
			IOUtils.close(stringReader, ottu);
		}
	}

	final public int getNumberOfField() {
		return numberOfTermsArray.size();
	}

	final public long getFieldPtrArrayPointer() {
		if (ola == null)
			ola = new OsseUint64Array(memoryBuffer, fieldPtrArray);
		return Memory.nativeValue(ola);
	}

	public long getNumberOfTermsArrayPointer() {
		if (oia == null)
			oia = new OsseUint32Array(memoryBuffer, numberOfTermsArray);
		return Memory.nativeValue(oia);
	}

	public long getTermArrayPointer() {
		if (ofsa == null)
			ofsa = new OsseFastStringArray(memoryBuffer, termBuffer);
		return Memory.nativeValue(ofsa);
	}

	@Override
	public void close() {
		IOUtils.close(ofsa, ola, oia);
		ofsa = null;
		ola = null;
		oia = null;
	}

}
