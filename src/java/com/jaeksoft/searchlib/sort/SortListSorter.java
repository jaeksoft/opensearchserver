package com.jaeksoft.searchlib.sort;

import com.jaeksoft.searchlib.result.ResultSearch;

public class SortListSorter implements SorterInterface {

	private String[] values1;
	private String[] values2;
	private boolean[] descendant;

	protected SortListSorter(SortList sortList) {
		values1 = sortList.newStringArray();
		values2 = sortList.newStringArray();
		descendant = sortList.newDescArray();
	}

	@Override
	public boolean isBefore(ResultSearch r1, int i1, ResultSearch r2, int i2) {
		r1.loadSortValues(i1, values1);
		r2.loadSortValues(i2, values2);
		for (int i = 0; i < values1.length; i++) {
			String v1 = values1[i];
			String v2 = values2[i];
			boolean desc = descendant[i];
			int c = 0;
			// Take care of null values
			if (v1 == null) {
				if (v2 != null)
					c = -1;
			} else if (v2 == null) {
				c = 1;
			} else
				c = values1[i].compareTo(values2[i]);
			if (desc) {
				if (c > 0)
					return true;
				if (c < 0)
					return false;
			} else {
				if (c > 0)
					return false;
				if (c < 0)
					return true;
			}
		}
		return false;
	}
}
