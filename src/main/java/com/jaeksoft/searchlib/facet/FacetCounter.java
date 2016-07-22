/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2015 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.facet;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.jaeksoft.searchlib.webservice.query.search.SearchQueryAbstract.Facet.OrderByEnum;

import it.unimi.dsi.fastutil.Swapper;
import it.unimi.dsi.fastutil.ints.IntComparator;

public class FacetCounter implements Comparable<FacetCounter> {

	public long count;

	FacetCounter(Long value) {
		this.count = value == null ? 0 : value;
	}

	FacetCounter(Integer value) {
		this.count = value == null ? 0 : value;
	}

	FacetCounter(FacetCounter counter) {
		this.count = counter == null ? 0 : counter.count;
	}

	@Override
	public int compareTo(FacetCounter o) {
		return Long.compare(count, o.count);
	}

	public long increment() {
		return (++count);
	}

	public long add(Long value) {
		if (value != null)
			this.count += value;
		return count;
	}

	public long add(FacetCounter counter) {
		if (counter != null)
			this.count += counter.count;
		return count;
	}

	public long getCount() {
		return count;
	}

	public static abstract class FacetSorter implements IntComparator, Swapper {

		protected List<Map.Entry<String, FacetCounter>> facetList;

		public FacetSorter(List<Map.Entry<String, FacetCounter>> facetList) {
			this.facetList = facetList;
		}

		@Override
		public void swap(int a, int b) {
			Map.Entry<String, FacetCounter> entry = facetList.get(a);
			facetList.set(a, facetList.set(b, entry));
		}

		public static FacetSorter getSorter(List<Entry<String, FacetCounter>> list, OrderByEnum orderBy) {
			if (orderBy == null)
				return null;
			switch (orderBy) {
			case no_sort:
				return null;
			case count_asc:
				return new FacetCounterAsc(list);
			case count_desc:
				return new FacetCounterDesc(list);
			case term_asc:
				return new FacetTermAsc(list);
			case term_desc:
				return new FacetTermDesc(list);
			}
			return null;
		}
	}

	public static class FacetCounterDesc extends FacetSorter {

		public FacetCounterDesc(List<Map.Entry<String, FacetCounter>> facetList) {
			super(facetList);
		}

		@Override
		public int compare(Integer o1, Integer o2) {
			return Long.compare(facetList.get(o2).getValue().count, facetList.get(o1).getValue().count);
		}

		@Override
		public int compare(int k1, int k2) {
			return Long.compare(facetList.get(k2).getValue().count, facetList.get(k1).getValue().count);
		}
	}

	public static class FacetCounterAsc extends FacetSorter {

		public FacetCounterAsc(List<Map.Entry<String, FacetCounter>> facetList) {
			super(facetList);
		}

		@Override
		public int compare(Integer o1, Integer o2) {
			return Long.compare(facetList.get(o1).getValue().count, facetList.get(o2).getValue().count);
		}

		@Override
		public int compare(int k1, int k2) {
			return Long.compare(facetList.get(k1).getValue().count, facetList.get(k2).getValue().count);
		}
	}

	public static class FacetTermDesc extends FacetSorter {

		public FacetTermDesc(List<Map.Entry<String, FacetCounter>> facetList) {
			super(facetList);
		}

		@Override
		public int compare(Integer o1, Integer o2) {
			return facetList.get(o2).getKey().compareTo(facetList.get(o1).getKey());
		}

		@Override
		public int compare(int k1, int k2) {
			return facetList.get(k2).getKey().compareTo(facetList.get(k1).getKey());
		}
	}

	public static class FacetTermAsc extends FacetSorter {

		public FacetTermAsc(List<Map.Entry<String, FacetCounter>> facetList) {
			super(facetList);
		}

		@Override
		public int compare(Integer o1, Integer o2) {
			return facetList.get(o1).getKey().compareTo(facetList.get(o2).getKey());
		}

		@Override
		public int compare(int k1, int k2) {
			return facetList.get(k1).getKey().compareTo(facetList.get(k2).getKey());
		}
	}

}
