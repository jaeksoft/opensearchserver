/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013-2014 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.collapse;

import java.text.ParseException;

import com.jaeksoft.searchlib.geo.GeoDistance;
import com.jaeksoft.searchlib.index.FieldCacheIndex;

public class CollapseFunction {

	static abstract class FunctionExecutor {

		abstract String execute(final FieldCacheIndex stringIndex,
				final int doc, final int[] collapsedDocs);

		abstract String execute(final GeoDistance geoDistance, final int doc,
				final int[] collapsedDocs) throws ParseException;

	}

	static class FunctionMinimum extends FunctionExecutor {

		@Override
		final String execute(final FieldCacheIndex stringIndex, final int doc,
				final int[] collapsedDocs) {
			int min = stringIndex.order[doc];
			for (int id : collapsedDocs) {
				int pos = stringIndex.order[id];
				if (pos < min)
					min = pos;
			}
			return stringIndex.lookup[min];
		}

		@Override
		final String execute(final GeoDistance geoDistance, final int doc,
				final int[] collapsedDocs) throws ParseException {
			double min = geoDistance.getDistance(doc);
			for (int id : collapsedDocs) {
				double val = geoDistance.getDistance(id);
				if (val < min)
					min = val;
			}
			return Double.toString(min);
		}
	}

	static class FunctionMaximum extends FunctionExecutor {

		@Override
		final String execute(final FieldCacheIndex stringIndex, final int doc,
				final int[] collapsedDocs) {
			int max = stringIndex.order[doc];
			for (int id : collapsedDocs) {
				int pos = stringIndex.order[id];
				if (pos > max)
					max = pos;
			}
			return stringIndex.lookup[max];
		}

		@Override
		final String execute(final GeoDistance geoDistance, final int doc,
				final int[] collapsedDocs) throws ParseException {
			double max = geoDistance.getDistance(doc);
			for (int id : collapsedDocs) {
				double val = geoDistance.getDistance(id);
				if (val > max)
					max = val;
			}
			return Double.toString(max);
		}
	}

	static class FunctionConcat extends FunctionExecutor {

		@Override
		String execute(final FieldCacheIndex stringIndex, final int doc,
				final int[] collapsedDocs) {
			StringBuilder sb = new StringBuilder();
			sb.append(stringIndex.lookup[stringIndex.order[doc]]);
			for (int id : collapsedDocs) {
				sb.append('|');
				sb.append(stringIndex.lookup[stringIndex.order[id]]);
			}
			return sb.toString();
		}

		@Override
		final String execute(final GeoDistance geoDistance, final int doc,
				final int[] collapsedDocs) throws ParseException {
			StringBuilder sb = new StringBuilder();
			sb.append(geoDistance.getDistance(doc));
			for (int id : collapsedDocs) {
				sb.append('|');
				sb.append(geoDistance.getDistance(id));
			}
			return sb.toString();
		}
	}

	static class FunctionCount extends FunctionExecutor {

		@Override
		String execute(final FieldCacheIndex stringIndex, final int doc,
				final int[] collapsedDocs) {
			return Integer.toString(collapsedDocs.length + 1);
		}

		@Override
		String execute(final GeoDistance geoDistance, final int doc,
				final int[] collapsedDocs) throws ParseException {
			return Integer.toString(collapsedDocs.length + 1);
		}
	}

}
