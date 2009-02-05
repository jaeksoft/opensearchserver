/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.highlight;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Set;
import java.util.TreeMap;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermPositionVector;
import org.apache.lucene.index.TermVectorOffsetInfo;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.w3c.dom.Node;

import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.request.Request;
import com.jaeksoft.searchlib.result.DocumentCacheItem;
import com.jaeksoft.searchlib.schema.Field;
import com.jaeksoft.searchlib.schema.FieldList;
import com.jaeksoft.searchlib.schema.FieldValue;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.XPathParser;

public class HighlightField extends FieldValue {

	private FragmenterAbstract fragmenter;
	private String tag;
	private int maxDocChar;
	private String separator;
	private int maxSnippetSize;
	private int maxSnippetNumber;

	private HighlightField(Field field, String tag, int maxDocChar,
			String separator, int maxSnippetNumber, int maxSnippetSize,
			FragmenterAbstract fragmenter) {
		super(field.getName());
		this.tag = tag;
		this.maxDocChar = maxDocChar;
		this.separator = separator;
		this.maxSnippetNumber = maxSnippetNumber;
		this.maxSnippetSize = maxSnippetSize;
		this.fragmenter = fragmenter;
	}

	public HighlightField(Field field) {
		this(field, "em", Integer.MAX_VALUE, "...", 5, 200, null);
	}

	public HighlightField(HighlightField field) {
		super(field.name);
		fragmenter = field.fragmenter;
		tag = field.tag;
		maxDocChar = field.maxDocChar;
		separator = field.separator;
		maxSnippetNumber = field.maxSnippetNumber;
		maxSnippetSize = field.maxSnippetSize;
	}

	@Override
	public Object clone() {
		return new HighlightField(this);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 4048179036729127707L;

	/**
	 * Retourne la liste des champs "highlighter".
	 * 
	 * @param xPath
	 * @param node
	 * @param target
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static void copyHighlightFields(Node node,
			FieldList<SchemaField> source, FieldList<HighlightField> target)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		String fieldName = XPathParser.getAttributeString(node, "name");
		String tag = XPathParser.getAttributeString(node, "tag");
		if (tag == null)
			tag = "em";
		int maxDocChar = XPathParser.getAttributeValue(node, "maxDocBytes");
		if (maxDocChar == 0)
			XPathParser.getAttributeValue(node, "maxDocChar");
		if (maxDocChar == 0)
			maxDocChar = Integer.MAX_VALUE;
		int maxSnippetNumber = XPathParser.getAttributeValue(node,
				"maxSnippetNumber");
		if (maxSnippetNumber == 0)
			maxSnippetNumber = 1;
		int maxSnippetSize = XPathParser.getAttributeValue(node,
				"maxSnippetSize");
		if (maxSnippetSize == 0)
			maxSnippetSize = 200;
		FragmenterAbstract fragmenter = FragmenterAbstract
				.newInstance(XPathParser.getAttributeString(node,
						"fragmenterClass"));
		fragmenter.setAttributes(node.getAttributes());
		String separator = XPathParser.getAttributeString(node, "separator");
		if (separator == null)
			separator = "...";
		HighlightField field = new HighlightField(source.get(fieldName), tag,
				maxDocChar, separator, maxSnippetNumber, maxSnippetSize,
				fragmenter);

		target.add(field);
	}

	private String[] extractSearchTerms(Request request) throws ParseException,
			SyntaxError, IOException {
		Query query = request.getQuery();
		query.rewrite(null);
		Set<Term> terms = new HashSet<Term>();
		query.extractTerms(terms);
		String[] searchTerms = new String[terms.size()];
		int i = 0;
		for (Term term : terms)
			if (name.equalsIgnoreCase(term.field()))
				searchTerms[i++] = term.text();
		String[] finalTerms = new String[i];
		for (i = 0; i < finalTerms.length; i++)
			finalTerms[i] = searchTerms[i];
		return finalTerms;
	}

	private Iterator<TermVectorOffsetInfo> extractTermVectorIterator(
			Request request, int docId, ReaderLocal reader) throws IOException,
			ParseException, SyntaxError {
		TermPositionVector termVector = (TermPositionVector) reader
				.getTermFreqVector(docId, name);
		if (termVector == null)
			return null;
		String[] searchTerms = extractSearchTerms(request);
		int[] termsIdx = termVector.indexesOf(searchTerms, 0,
				searchTerms.length);
		TreeMap<Integer, TermVectorOffsetInfo> map = new TreeMap<Integer, TermVectorOffsetInfo>();
		for (int termId : termsIdx) {
			if (termId == -1)
				continue;
			TermVectorOffsetInfo[] offsets = termVector.getOffsets(termId);
			for (TermVectorOffsetInfo offset : offsets)
				map.put(offset.getStartOffset(), offset);
		}
		return map.values().iterator();
	}

	private TermVectorOffsetInfo checkValue(TermVectorOffsetInfo currentVector,
			Iterator<TermVectorOffsetInfo> vectorIterator, int startOffset,
			Fragment fragment) {
		if (currentVector == null)
			return null;
		StringBuffer result = new StringBuffer();
		String originalText = fragment.getOriginalText();
		int originalTextLength = originalText.length();
		int endOffset = startOffset + originalTextLength;
		int pos = 0;
		while (currentVector != null) {
			int end = currentVector.getEndOffset() - fragment.vectorOffset;
			if (end > endOffset)
				break;
			int start = currentVector.getStartOffset() - fragment.vectorOffset;
			if (start >= startOffset) {
				result.append(originalText.substring(pos, start - startOffset));
				result.append("<");
				result.append(tag);
				result.append(">");
				result.append(originalText.substring(start - startOffset, end
						- startOffset));
				result.append("</");
				result.append(tag);
				result.append(">");
				pos = end - startOffset;
			}
			currentVector = vectorIterator.hasNext() ? vectorIterator.next()
					: null;
		}
		if (result.length() == 0)
			return currentVector;
		if (pos < originalTextLength)
			result.append(originalText.substring(pos, originalTextLength));
		fragment.setHighlightedText(result.toString());
		return currentVector;
	}

	public void setSnippets(Request request, DocumentCacheItem doc)
			throws IOException, ParseException, SyntaxError {

		TermVectorOffsetInfo currentVector = null;
		Iterator<TermVectorOffsetInfo> vectorIterator = extractTermVectorIterator(
				request, doc.getDocId(), doc.getReader());
		if (vectorIterator != null)
			currentVector = vectorIterator.hasNext() ? vectorIterator.next()
					: null;
		ArrayList<String> values = doc.getValues(this);
		int startOffset = 0;
		FragmentList fragments = new FragmentList();
		int vectorOffset = 0;
		for (String value : values) {
			if (value != null) {
				// VectorOffset++ depends of EndOffset bug #patch Lucene 579 and
				// 1458
				fragmenter.getFragments(value, fragments, vectorOffset);
				if (fragments.getTotalSize() > maxDocChar)
					break;
			}
		}
		if (fragments.size() == 0)
			return;
		ListIterator<Fragment> fragmentIterator = fragments.iterator();
		while (fragmentIterator.hasNext()) {
			Fragment fragment = fragmentIterator.next();
			currentVector = checkValue(currentVector, vectorIterator,
					startOffset, fragment);
			startOffset += fragment.getOriginalText().length();
		}
		fragmentIterator = fragments.iterator();
		int snippetCounter = maxSnippetNumber;
		while (snippetCounter-- != 0) {
			Fragment fragment = fragments
					.findNextHighlightedFragment(fragmentIterator);
			if (fragment == null)
				break;
			StringBuffer snippet = fragments.getSnippet(maxSnippetSize,
					separator, fragmentIterator, fragment);
			if (snippet != null)
				if (snippet.length() > 0)
					addValue(snippet.toString());
		}
		if (getValuesCount() > 0)
			return;
		fragmentIterator = fragments.iterator();
		StringBuffer snippet = fragments.getSnippet(maxSnippetSize, separator,
				fragmentIterator, fragmentIterator.next());
		if (snippet != null)
			if (snippet.length() > 0)
				addValue(snippet.toString());
	}
}
