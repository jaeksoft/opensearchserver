/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.snippet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.lucene.search.Query;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.CompiledAnalyzer;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.ReaderInterface;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.schema.AbstractField;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.schema.FieldValueOriginEnum;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.schema.SchemaFieldList;
import com.jaeksoft.searchlib.snippet.SnippetVectors.SnippetVector;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.Timer;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class SnippetField extends AbstractField<SnippetField> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1989504404725110730L;

	private FragmenterAbstract fragmenterTemplate;
	private String tag;
	private String[] tags;
	private String separator;
	private String unescapedSeparator;
	private int maxSnippetSize;
	private int maxSnippetNumber;
	private int timeLimit;
	private transient SnippetQueries snippetQueries;
	private transient Query query;
	private transient CompiledAnalyzer queryAnalyzer;
	private transient CompiledAnalyzer indexAnalyzer;

	private SnippetField(String fieldName, String tag, String separator,
			int maxSnippetSize, int maxSnippetNumber,
			FragmenterAbstract fragmenterTemplate, int timeLimit) {
		super(fieldName);
		this.snippetQueries = null;
		setTag(tag);
		setSeparator(separator);
		this.maxSnippetSize = maxSnippetSize;
		this.maxSnippetNumber = maxSnippetNumber;
		this.fragmenterTemplate = fragmenterTemplate;
		this.timeLimit = timeLimit;
	}

	public SnippetField(String fieldName) {
		this(fieldName, "em", "...", 200, 1, FragmenterAbstract.NOFRAGMENTER, 0);
	}

	@Override
	public SnippetField duplicate() {
		return new SnippetField(name, tag, separator, maxSnippetSize,
				maxSnippetNumber, fragmenterTemplate, timeLimit);
	}

	public String getFragmenter() {
		return fragmenterTemplate.getClass().getSimpleName();
	}

	public void setFragmenter(String fragmenterName) throws SearchLibException {
		try {
			fragmenterTemplate = FragmenterAbstract.newInstance(fragmenterName);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		}
	}

	/**
	 * @return the tag
	 */
	public String getTag() {
		return tag;
	}

	/**
	 * @param tag
	 *            the tag to set
	 */
	public void setTag(String tag) {
		this.tag = tag;
		if (tag != null && tag.length() > 0) {
			tags = new String[2];
			tags[0] = '<' + tag + '>';
			tags[1] = "</" + tag + '>';
		} else
			tags = null;
	}

	/**
	 * @return the separator
	 */
	public String getSeparator() {
		return separator;
	}

	/**
	 * @param separator
	 *            the separator to set
	 */
	public void setSeparator(String separator) {
		this.separator = separator;
		unescapedSeparator = separator == null ? null : StringEscapeUtils
				.unescapeHtml(separator);
	}

	/**
	 * @return the maxSnippetSize
	 */
	public int getMaxSnippetSize() {
		return maxSnippetSize;
	}

	/**
	 * @param maxSnippetSize
	 *            the maxSnippetSize to set
	 */
	public void setMaxSnippetSize(int maxSnippetSize) {
		this.maxSnippetSize = maxSnippetSize;
	}

	/**
	 * @return the maxSnippetNumber
	 */
	public int getMaxSnippetNumber() {
		return maxSnippetNumber;
	}

	/**
	 * @param maxSnippetNumber
	 *            the maxSnippetNumber to set
	 */
	public void setMaxSnippetNumber(int maxSnippetNumber) {
		this.maxSnippetNumber = maxSnippetNumber;
	}

	/**
	 * Retourne la liste des champs "snippet".
	 * 
	 * @param xPath
	 * @param node
	 * @param target
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static void copySnippetFields(Node node, SchemaFieldList source,
			SnippetFieldList target) throws InstantiationException,
			IllegalAccessException {
		String fieldName = XPathParser.getAttributeString(node, "name");
		String tag = XPathParser.getAttributeString(node, "tag");
		if (tag == null)
			tag = "em";
		int maxSnippetNumber = XPathParser.getAttributeValue(node,
				"maxSnippetNumber");
		if (maxSnippetNumber == 0)
			maxSnippetNumber = 1;
		int maxSnippetSize = XPathParser.getAttributeValue(node,
				"maxSnippetSize");
		if (maxSnippetSize == 0)
			maxSnippetSize = 200;
		int timeLimit = DomUtils.getAttributeInteger(node, "timeLimit", 0);

		FragmenterAbstract fragmenter = FragmenterAbstract
				.newInstance(XPathParser.getAttributeString(node,
						"fragmenterClass"));
		fragmenter.setAttributes(node.getAttributes());
		String separator = XPathParser.getAttributeString(node, "separator");
		if (separator == null)
			separator = "...";
		SchemaField schemaField = source.get(fieldName);
		if (schemaField == null)
			return;
		SnippetField field = new SnippetField(schemaField.getName(), tag,
				separator, maxSnippetSize, maxSnippetNumber, fragmenter,
				timeLimit);
		target.put(field);
	}

	public final void reset() {
		snippetQueries = null;
		query = null;
		queryAnalyzer = null;
		indexAnalyzer = null;
	}

	public void initSearchTerms(AbstractSearchRequest searchRequest)
			throws ParseException, SyntaxError, IOException, SearchLibException {
		synchronized (this) {
			if (snippetQueries != null)
				return;
			this.query = searchRequest.getSnippetQuery();
			this.queryAnalyzer = searchRequest.getAnalyzer()
					.getCompiledAnalyzer(name);
			this.indexAnalyzer = searchRequest.getConfig().getSchema()
					.getIndexPerFieldAnalyzer(searchRequest.getLang())
					.getCompiledAnalyzer(name);
			snippetQueries = new SnippetQueries(this.query, name);
		}
	}

	private final void appendSubString(String text, int start, int end,
			StringBuilder sb) {
		if (text == null)
			return;
		int l = text.length();
		if (end > l)
			end = l;
		if (end < start)
			return;
		sb.append(text.substring(start, end));
	}

	private final SnippetVector checkValue(SnippetVector currentVector,
			Iterator<SnippetVector> vectorIterator, int startOffset,
			Fragment fragment) {
		if (currentVector == null)
			return null;
		StringBuilder result = new StringBuilder();
		String originalText = fragment.getOriginalText();
		int originalTextLength = originalText.length();
		int endOffset = startOffset + originalTextLength;
		int pos = 0;
		while (currentVector != null) {
			int end = currentVector.end - fragment.vectorOffset;
			if (end > endOffset)
				break;
			int start = currentVector.start - fragment.vectorOffset;
			if (start >= startOffset) {
				appendSubString(originalText, pos, start - startOffset, result);
				if (tags != null)
					result.append(tags[0]);
				appendSubString(originalText, start - startOffset, end
						- startOffset, result);
				if (tags != null)
					result.append(tags[1]);
				pos = end - startOffset;
			}
			currentVector = vectorIterator.hasNext() ? vectorIterator.next()
					: null;
		}
		if (result.length() == 0)
			return currentVector;
		if (pos < originalTextLength)
			appendSubString(originalText, pos, originalTextLength, result);
		fragment.setHighlightedText(result.toString());
		return currentVector;
	}

	public boolean getSnippets(final int docId, final ReaderInterface reader,
			final List<FieldValueItem> values,
			final List<FieldValueItem> snippets, final Timer parentTimer)
			throws IOException, ParseException, SyntaxError, SearchLibException {

		if (values == null)
			return false;

		final Timer timer = new Timer(parentTimer, "SnippetField " + this.name);
		final long halfTimeExpiration = this.timeLimit == 0 ? 0 : timer
				.getStartOffset(this.timeLimit / 2);
		final long expiration = this.timeLimit == 0 ? 0 : timer
				.getStartOffset(this.timeLimit);

		FragmenterAbstract fragmenter = fragmenterTemplate.newInstance();
		SnippetVector currentVector = null;

		Timer t = new Timer(timer, "extractTermVectorIterator");

		Iterator<SnippetVector> vectorIterator = SnippetVectors
				.extractTermVectorIterator(docId, reader, snippetQueries, name,
						values, indexAnalyzer, t, halfTimeExpiration);
		if (vectorIterator != null)
			currentVector = vectorIterator.hasNext() ? vectorIterator.next()
					: null;

		t.end(null);

		t = new Timer(timer, "getFraments");

		int startOffset = 0;
		FragmentList fragments = new FragmentList();
		int vectorOffset = 0;
		for (FieldValueItem valueItem : values) {
			String value = valueItem.getValue();
			if (value != null) {
				// VectorOffset++ depends of EndOffset bug #patch Lucene 579 and
				// 1458
				fragmenter.getFragments(value, fragments, vectorOffset++);
			}
		}

		t.end(null);

		if (fragments.size() == 0) {
			timer.end(null);
			return false;
		}

		t = new Timer(timer, "checkValue");

		Fragment fragment = fragments.first();
		while (fragment != null) {
			currentVector = checkValue(currentVector, vectorIterator,
					startOffset, fragment);
			startOffset += fragment.getOriginalText().length();
			fragment = fragment.next();
		}

		t.end(null);

		Timer sbTimer = new Timer(timer, "snippetBuilder");

		boolean result = false;
		int snippetCounter = maxSnippetNumber;
		int scoredFragment = 0;
		while (snippetCounter-- != 0) {
			Fragment bestScoreFragment = null;
			fragment = Fragment.findNextHighlightedFragment(fragments.first());
			List<Fragment> scoreFragments = new ArrayList<Fragment>(0);
			double maxSearchScore = 0;

			t = new Timer(sbTimer, "fragmentScore");
			boolean expired = false;

			while (fragment != null) {
				double sc = fragment.searchScore(name, queryAnalyzer, query);
				if (sc > maxSearchScore)
					maxSearchScore = sc;
				scoreFragments.add(fragment);
				fragment = Fragment
						.findNextHighlightedFragment(fragment.next());
				scoredFragment++;
				if (expiration != 0) {
					if (System.currentTimeMillis() > expiration) {
						expired = true;
						break;
					}
				}
			}

			t.end("fragmentScore " + scoredFragment + " " + expired);

			for (Fragment frag : scoreFragments)
				bestScoreFragment = Fragment.bestScore(bestScoreFragment, frag,
						maxSearchScore, maxSnippetSize);

			if (bestScoreFragment != null) {
				SnippetBuilder snippetBuilder = new SnippetBuilder(
						maxSnippetSize, unescapedSeparator, tags,
						bestScoreFragment);
				if (snippetBuilder.length() > 0)
					snippets.add(new FieldValueItem(
							FieldValueOriginEnum.SNIPPET, snippetBuilder
									.toString()));
				fragments.remove(snippetBuilder.getFragments());
				result = true;
				continue;
			}

			if (fragments.first() == null)
				break;
			SnippetBuilder snippetBuilder = new SnippetBuilder(maxSnippetSize,
					unescapedSeparator, tags, fragments.first());
			if (snippetBuilder.length() > 0) {
				snippets.add(new FieldValueItem(FieldValueOriginEnum.SNIPPET,
						snippetBuilder.toString()));
				fragments.remove(snippetBuilder.getFragments());
			}
		}

		sbTimer.end(null);

		timer.end(null);

		return result;
	}

	@Override
	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement("field", "name", name, "tag", tag, "separator",
				separator, "maxSnippetSize", Integer.toString(maxSnippetSize),
				"maxSnippetNumber", Integer.toString(maxSnippetNumber),
				"fragmenterClass",
				fragmenterTemplate != null ? fragmenterTemplate.getClass()
						.getSimpleName() : null, "timeLimit", Long
						.toString(timeLimit));
		xmlWriter.endElement();
	}

	@Override
	public int compareTo(SnippetField f) {
		int c = super.compareTo(f);
		if (c != 0)
			return c;
		if ((c = fragmenterTemplate.getClass().getName()
				.compareTo(f.fragmenterTemplate.getClass().getName())) != 0)
			return c;
		if ((c = tag.compareTo(f.tag)) != 0)
			return c;
		if ((c = separator.compareTo(f.separator)) != 0)
			return c;
		if ((c = maxSnippetSize - f.maxSnippetSize) != 0)
			return c;
		if ((c = maxSnippetNumber - f.maxSnippetNumber) != 0)
			return c;
		return 0;
	}

	/**
	 * @return the timeLimit
	 */
	public int getTimeLimit() {
		return timeLimit;
	}

	/**
	 * @param timeLimit
	 *            the timeLimit to set
	 */
	public void setTimeLimit(int timeLimit) {
		this.timeLimit = timeLimit;
	}
}
