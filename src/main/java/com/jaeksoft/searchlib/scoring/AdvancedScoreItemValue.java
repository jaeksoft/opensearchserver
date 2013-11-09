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
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.function.DocValues;
import org.apache.lucene.search.function.OrdFieldSource;
import org.apache.lucene.search.function.ReverseOrdFieldSource;
import org.apache.lucene.search.function.ValueSource;

public class AdvancedScoreItemValue {

	private final DocValues docValues;
	private final float weight;
	private final float maxValue;

	public AdvancedScoreItemValue(AdvancedScoreItem scoreItem,
			IndexReader reader) throws IOException {
		String fieldName = scoreItem.getFieldName();
		ValueSource valueSource = scoreItem.isAscending() ? valueSource = new OrdFieldSource(
				fieldName) : new ReverseOrdFieldSource(fieldName);
		docValues = valueSource.getValues(reader);
		weight = scoreItem.getWeight();
		maxValue = docValues.getMaxValue();
	}

	public final float getValue(int doc) {
		return docValues.floatVal(doc) / maxValue * weight;
	}

	public final Explanation getExplanation(int doc) {
		StringBuilder sb = new StringBuilder();
		sb.append("maxValue=");
		sb.append(maxValue);
		sb.append(", weight=");
		sb.append(weight);
		sb.append(", docValue=");
		sb.append(docValues.floatVal(doc));
		return new Explanation(getValue(doc), sb.toString());
	}
}
