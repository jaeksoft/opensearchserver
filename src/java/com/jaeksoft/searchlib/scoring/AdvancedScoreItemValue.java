/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.scoring;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.function.DocValues;
import org.apache.lucene.search.function.OrdFieldSource;
import org.apache.lucene.search.function.ReverseOrdFieldSource;
import org.apache.lucene.search.function.ValueSource;

public class AdvancedScoreItemValue {

	private final DocValues docValues;
	private final float factor;

	protected AdvancedScoreItemValue() {
		docValues = null;
		factor = 0;
	}

	private AdvancedScoreItemValue(AdvancedScoreItem scoreItem,
			IndexReader reader, float norm) throws IOException {
		String fieldName = scoreItem.getFieldName();
		ValueSource valueSource = scoreItem.isAscending() ? valueSource = new OrdFieldSource(
				fieldName) : new ReverseOrdFieldSource(fieldName);
		docValues = valueSource.getValues(reader);
		factor = (docValues.getMaxValue() * scoreItem.getWeight()) / norm;
	}

	public float getValue(int doc, float score) {
		return factor / docValues.floatVal(doc);
	}

	public static final AdvancedScoreItemValue getInstance(
			AdvancedScoreItem scoreItem, IndexReader reader, float norm)
			throws IOException {
		if (scoreItem.isScore())
			return new AdvancedScoreItemScore(scoreItem);
		return new AdvancedScoreItemValue(scoreItem, reader, norm);
	}
}
