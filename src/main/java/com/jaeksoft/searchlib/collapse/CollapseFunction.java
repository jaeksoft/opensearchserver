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
package com.jaeksoft.searchlib.collapse;

import java.text.NumberFormat;
import java.text.ParseException;

import com.jaeksoft.searchlib.analysis.filter.DegreesRadiansFilter;
import com.jaeksoft.searchlib.index.FieldCacheIndex;
import com.jaeksoft.searchlib.util.Geospatial;
import com.jaeksoft.searchlib.util.Geospatial.Location;

public class CollapseFunction {

	static abstract class FunctionExecutor {

		private NumberFormat numberFormat = DegreesRadiansFilter
				.getRadiansFormat();

		abstract String execute(final FieldCacheIndex stringIndex,
				final int doc, final int[] collapsedDocs);

		abstract String execute(final Location location, final double radius,
				final FieldCacheIndex stringIndexLatitude,
				final FieldCacheIndex stringIndexLongitude, final int doc,
				final int[] collapsedDocs) throws ParseException;

		final private double getRadians(final FieldCacheIndex stringIndex,
				final int doc) throws ParseException {
			return numberFormat.parse(
					stringIndex.lookup[stringIndex.order[doc]]).doubleValue();
		}

		final protected double getDistance(final Location loc1,
				final FieldCacheIndex stringIndexLatitude,
				final FieldCacheIndex stringIndexLongitude, final int doc,
				final double radius) throws ParseException {
			Location loc2 = new Location(getRadians(stringIndexLatitude, doc),
					getRadians(stringIndexLongitude, doc));
			return Geospatial.distance(loc1, loc2, radius);
		}
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
		final String execute(final Location location, final double radius,
				final FieldCacheIndex stringIndexLatitude,
				final FieldCacheIndex stringIndexLongitude, final int doc,
				final int[] collapsedDocs) throws ParseException {
			double min = getDistance(location, stringIndexLatitude,
					stringIndexLongitude, doc, radius);
			for (int id : collapsedDocs) {
				double val = getDistance(location, stringIndexLatitude,
						stringIndexLongitude, id, radius);
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
		final String execute(final Location location, final double radius,
				final FieldCacheIndex stringIndexLatitude,
				final FieldCacheIndex stringIndexLongitude, final int doc,
				final int[] collapsedDocs) throws ParseException {
			double max = getDistance(location, stringIndexLatitude,
					stringIndexLongitude, doc, radius);
			for (int id : collapsedDocs) {
				double val = getDistance(location, stringIndexLatitude,
						stringIndexLongitude, id, radius);
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
			StringBuffer sb = new StringBuffer();
			sb.append(stringIndex.lookup[stringIndex.order[doc]]);
			for (int id : collapsedDocs) {
				sb.append('|');
				sb.append(stringIndex.lookup[stringIndex.order[id]]);
			}
			return sb.toString();
		}

		@Override
		final String execute(final Location location, final double radius,
				final FieldCacheIndex stringIndexLatitude,
				final FieldCacheIndex stringIndexLongitude, final int doc,
				final int[] collapsedDocs) throws ParseException {
			StringBuffer sb = new StringBuffer();
			sb.append(getDistance(location, stringIndexLatitude,
					stringIndexLongitude, doc, radius));
			for (int id : collapsedDocs) {
				sb.append('|');
				sb.append(getDistance(location, stringIndexLatitude,
						stringIndexLongitude, id, radius));
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
		String execute(final Location location, final double radius,
				final FieldCacheIndex stringIndexLatitude,
				final FieldCacheIndex stringIndexLongitude, final int doc,
				final int[] collapsedDocs) throws ParseException {
			return Integer.toString(collapsedDocs.length + 1);
		}
	}

}
