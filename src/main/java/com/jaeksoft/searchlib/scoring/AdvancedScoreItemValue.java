/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012-2014 Emmanuel Keller / Jaeksoft
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

import com.jaeksoft.searchlib.index.ReaderAbstract;
import com.jaeksoft.searchlib.index.docvalue.DocValueInterface;
import com.jaeksoft.searchlib.index.docvalue.DocValueType;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.result.collector.docsethit.DistanceCollector;
import com.jaeksoft.searchlib.util.array.FloatBufferedArray;

public class AdvancedScoreItemValue {

	private final DocValueInterface docValues;
	private final float weight;
	private float maxValue;
	private final FloatBufferedArray valueArray;
	public float[] finalArray;
	private final boolean reverse;

	public AdvancedScoreItemValue(final AbstractSearchRequest request,
			final ReaderAbstract reader, final AdvancedScoreItem scoreItem,
			final DistanceCollector distanceCollector) throws IOException {
		String fieldName = scoreItem.getFieldName();
		switch (scoreItem.getType()) {
		case FIELD_ORDER:
			docValues = reader.getDocValueInterface(fieldName, scoreItem
					.isAscending() ? DocValueType.ORD : DocValueType.RORD);
			reverse = false;
			break;
		case DISTANCE:
			docValues = distanceCollector.getDocValue();
			reverse = true;
			break;
		default:
			throw new IOException("Unknown score function");
		}
		weight = (float) scoreItem.getWeight();
		valueArray = new FloatBufferedArray(reader.maxDoc());
		maxValue = 0;
		finalArray = null;
	}

	public final void collect(final int doc) {
		if (docValues == null)
			return;
		float value = docValues.getFloat(doc);
		if (value > maxValue)
			maxValue = value;
		valueArray.add(value);
	}

	public final void endCollection() {
		if (weight == 0)
			return;
		finalArray = valueArray.getFinalArray();
		int i = 0;
		if (reverse)
			for (float value : finalArray)
				finalArray[i++] = ((maxValue - value) / maxValue) * weight;
		else
			for (float value : finalArray)
				finalArray[i++] = (value / maxValue) * weight;
	}

}
