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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.schema.Field;
import com.jaeksoft.searchlib.schema.FieldList;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.External;
import com.jaeksoft.searchlib.util.XPathParser;

public class HighlightField extends Field implements Externalizable {

	private FragmenterAbstract fragmenter;
	private String tag;
	private int maxDocChar;
	private String separator;
	private int maxSnippetSize;
	private int maxSnippetNumber;
	private String[] searchTerms;

	public HighlightField() {
	}

	private HighlightField(Field field, String tag, int maxDocChar,
			String separator, int maxSnippetNumber, int maxSnippetSize,
			FragmenterAbstract fragmenter) {
		super(field.getName());
		this.searchTerms = null;
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
		this.searchTerms = field.searchTerms;
		fragmenter = field.fragmenter;
		tag = field.tag;
		maxDocChar = field.maxDocChar;
		separator = field.separator;
		maxSnippetNumber = field.maxSnippetNumber;
		maxSnippetSize = field.maxSnippetSize;
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

	private Iterator<TermVectorOffsetInfo> extractTermVectorIterator(int docId,
			ReaderLocal reader) throws IOException, ParseException, SyntaxError {
		if (searchTerms == null)
			return null;
		if (searchTerms.length == 0)
			return null;
		TermPositionVector termVector = (TermPositionVector) reader
				.getTermFreqVector(docId, name);
		if (termVector == null)
			return null;
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

	public void initSearchTerms(SearchRequest searchRequest)
			throws ParseException, SyntaxError, IOException {
		synchronized (this) {
			Query query = searchRequest.getQuery();
			query.rewrite(null);
			Set<Term> terms = new HashSet<Term>();
			query.extractTerms(terms);
			String[] tempTerms = new String[terms.size()];
			int i = 0;
			// Find term for that field only
			for (Term term : terms)
				if (name.equalsIgnoreCase(term.field()))
					tempTerms[i++] = term.text();
			// Build final array
			String[] finalTerms = new String[i];
			for (i = 0; i < finalTerms.length; i++)
				finalTerms[i] = tempTerms[i];
			searchTerms = finalTerms;
		}
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

	public boolean getSnippets(int docId, ReaderLocal reader, String[] values,
			List<String> snippets) throws IOException, ParseException,
			SyntaxError {

		if (values == null)
			return false;
		TermVectorOffsetInfo currentVector = null;
		Iterator<TermVectorOffsetInfo> vectorIterator = extractTermVectorIterator(
				docId, reader);
		if (vectorIterator != null)
			currentVector = vectorIterator.hasNext() ? vectorIterator.next()
					: null;
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
			return false;
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
					snippets.add(snippet.toString());
		}
		if (snippets.size() > 0)
			return true;

		fragmentIterator = fragments.iterator();
		StringBuffer snippet = fragments.getSnippet(maxSnippetSize, separator,
				fragmentIterator, fragmentIterator.next());
		if (snippet != null)
			if (snippet.length() > 0)
				snippets.add(snippet.toString());
		return false;
	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		fragmenter = External.readObject(in);
		tag = External.readUTF(in);
		maxDocChar = in.readInt();
		separator = External.readUTF(in);
		maxSnippetSize = in.readInt();
		maxSnippetNumber = in.readInt();
		searchTerms = External.readStringArray(in);
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		External.writeObject(fragmenter, out);
		External.writeUTF(tag, out);
		out.writeInt(maxDocChar);
		External.writeUTF(separator, out);
		out.writeInt(maxSnippetSize);
		out.writeInt(maxSnippetNumber);
		External.writeStringArray(searchTerms, out);
	}

}
