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

import java.io.IOException;

import com.jaeksoft.searchlib.index.FieldCacheIndex;
import com.jaeksoft.searchlib.index.ReaderAbstract;
import com.jaeksoft.searchlib.result.collector.CollapseDistanceInterface;
import com.jaeksoft.searchlib.result.collector.CollapseDocInterface;

public class CollapseFunction {

	static abstract class FunctionExecutor {

		private CollapseDocInterface collapseDocInterface;
		protected FieldCacheIndex stringIndex = null;
		protected CollapseDistanceInterface collapseDistanceInterface = null;

		final void prepare(CollapseFunctionField collapseFunctionField,
				ReaderAbstract reader, CollapseDocInterface collapseDocInterface)
				throws IOException {
			this.collapseDocInterface = collapseDocInterface;
			if (collapseFunctionField.isDistance()) {
				collapseDistanceInterface = collapseDocInterface
						.getCollector(CollapseDistanceInterface.class);
			} else {
				stringIndex = reader.getStringIndex(collapseFunctionField
						.getField());
			}
		}

		final String executeByPos(final int pos) {
			if (stringIndex != null) {
				final int doc = collapseDocInterface.getIds()[pos];
				final int[] collapsedDocs = collapseDocInterface
						.getCollapsedDocs(pos);
				return executeStringIndex(doc, collapsedDocs);
			}
			if (collapseDistanceInterface != null) {
				final float docDistance = collapseDistanceInterface
						.getDistances()[pos];
				final float[] collapsedDistances = collapseDistanceInterface
						.getCollapsedDistances(pos);
				return executeDistance(docDistance, collapsedDistances);
			}
			return null;
		}

		abstract String executeStringIndex(final int doc,
				final int[] collapsedDocs);

		abstract String executeDistance(final float docDistance,
				final float[] collapsedDistances);

	}

	static class FunctionMinimum extends FunctionExecutor {

		@Override
		final String executeStringIndex(final int doc, final int[] collapsedDocs) {
			int min = stringIndex.order[doc];
			for (int id : collapsedDocs) {
				int pos = stringIndex.order[id];
				if (pos < min)
					min = pos;
			}
			return stringIndex.lookup[min];
		}

		@Override
		final String executeDistance(final float docDistance,
				final float[] collapsedDistances) {
			double min = docDistance;
			for (float distance : collapsedDistances)
				if (distance < min)
					min = distance;
			return Double.toString(min);
		}
	}

	static class FunctionMaximum extends FunctionExecutor {

		@Override
		final String executeStringIndex(final int doc, final int[] collapsedDocs) {
			int max = stringIndex.order[doc];
			for (int id : collapsedDocs) {
				int pos = stringIndex.order[id];
				if (pos > max)
					max = pos;
			}
			return stringIndex.lookup[max];
		}

		@Override
		final String executeDistance(final float docDistance,
				final float[] collapsedDistances) {
			double max = docDistance;
			for (float distance : collapsedDistances)
				if (distance > max)
					max = distance;
			return Double.toString(max);
		}
	}

	static class FunctionConcat extends FunctionExecutor {

		@Override
		String executeStringIndex(final int doc, final int[] collapsedDocs) {
			StringBuilder sb = new StringBuilder();
			sb.append(stringIndex.lookup[stringIndex.order[doc]]);
			for (int id : collapsedDocs) {
				sb.append('|');
				sb.append(stringIndex.lookup[stringIndex.order[id]]);
			}
			return sb.toString();
		}

		@Override
		final String executeDistance(final float docDistance,
				final float[] collapsedDistances) {
			StringBuilder sb = new StringBuilder();
			sb.append(docDistance);
			for (float distance : collapsedDistances) {
				sb.append('|');
				sb.append(distance);
			}
			return sb.toString();
		}
	}

	static class FunctionCount extends FunctionExecutor {

		@Override
		String executeStringIndex(final int doc, final int[] collapsedDocs) {
			return Integer.toString(collapsedDocs.length + 1);
		}

		@Override
		String executeDistance(final float docDistance,
				final float[] collapsedDistances) {
			return Integer.toString(collapsedDistances.length + 1);
		}
	}

}
