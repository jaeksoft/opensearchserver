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

package com.jaeksoft.searchlib.snippet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.lucene.index.TermPositionVector;
import org.apache.lucene.index.TermVectorOffsetInfo;

import com.jaeksoft.searchlib.analysis.TokenTerm;

public class SnippetTermPositionVector implements TermPositionVector {

	private class Info {

		private final List<Integer> positionList = new ArrayList<Integer>();
		private final List<TermVectorOffsetInfo> offsetList = new ArrayList<TermVectorOffsetInfo>();
		private int[] positionArray = null;
		private TermVectorOffsetInfo[] offsetArray = null;

		private final void compile() {
			compilePositions();
			compileOffsets();
		}

		private final void compilePositions() {
			if (positionArray != null
					&& positionArray.length == positionList.size())
				return;
			positionArray = new int[positionList.size()];
			int i = 0;
			for (int pos : positionList)
				positionArray[i++] = pos;
		}

		private final void compileOffsets() {
			if (offsetArray != null && offsetArray.length == offsetList.size())
				return;
			offsetArray = offsetList
					.toArray(new TermVectorOffsetInfo[offsetList.size()]);
		}
	}

	private final String[] terms;
	private final String field;
	private final TreeMap<String, Integer> termMap = new TreeMap<String, Integer>();
	private final Info[] infoArray;
	private final int[] frequencyArray;

	SnippetTermPositionVector(final String field, final String[] terms) {
		this.field = field;
		this.terms = terms;
		if (terms == null) {
			infoArray = null;
			frequencyArray = null;
			return;
		}
		infoArray = new Info[terms.length];
		frequencyArray = new int[terms.length];
		int idx = 0;
		for (String term : terms) {
			if (termMap.containsKey(term))
				continue;
			termMap.put(term, idx);
			frequencyArray[idx] = 0;
			infoArray[idx++] = new Info();
		}
	}

	int addCollection(final Collection<TokenTerm> tokenTerms,
			final int characterOffset, int positionOffset) {
		if (CollectionUtils.isEmpty(tokenTerms))
			return positionOffset;
		for (TokenTerm tokenTerm : tokenTerms) {
			Integer idx = termMap.get(tokenTerm.term);
			if (idx != null) {
				frequencyArray[idx]++;
				Info info = infoArray[idx];
				info.offsetList.add(new TermVectorOffsetInfo(tokenTerm.start
						+ characterOffset, tokenTerm.end + characterOffset));
				info.positionList.add(positionOffset);
			}
			positionOffset += tokenTerm.increment;
		}
		compile();
		return positionOffset;
	}

	void compile() {
		if (infoArray == null)
			return;
		for (Info info : infoArray)
			info.compile();
	}

	@Override
	public String getField() {
		return field;
	}

	@Override
	public int size() {
		return termMap.size();
	}

	@Override
	public String[] getTerms() {
		return terms;
	}

	@Override
	public int[] getTermFrequencies() {
		return frequencyArray;
	}

	@Override
	public int indexOf(String term) {
		return termMap.get(term);
	}

	@Override
	public int[] indexesOf(String[] terms, int start, int len) {
		if (terms == null)
			return null;
		int[] indexes = new int[terms.length];
		int i = 0;
		for (String term : terms) {
			Integer idx = termMap.get(term);
			indexes[i++] = idx == null ? -1 : idx;
		}
		return indexes;
	}

	@Override
	public int[] getTermPositions(int index) {
		return infoArray[index].positionArray;
	}

	@Override
	public TermVectorOffsetInfo[] getOffsets(int index) {
		return infoArray[index].offsetArray;
	}

}
