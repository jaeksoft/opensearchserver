/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.facet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

public class FacetGroup extends Facet {

	private static final long serialVersionUID = -2026182316339285266L;
	private HashMap<String, Integer> facetCount;
	transient private String[] terms;
	private int[] count;

	public FacetGroup(FacetField facetField) {
		super(facetField);
		this.facetCount = new HashMap<String, Integer>();
		this.terms = null;
		this.count = null;
	}

	private void extractTermsAndCount() {
		synchronized (this) {
			Set<Entry<String, Integer>> set = this.facetCount.entrySet();
			int n = set.size();
			this.terms = new String[n];
			this.count = new int[n];
			Iterator<Entry<String, Integer>> iterator = set.iterator();
			int i = 0;
			while (iterator.hasNext()) {
				Entry<String, Integer> entry = iterator.next();
				this.terms[i] = entry.getKey();
				this.count[i] = entry.getValue();
				i++;
			}
		}
	}

	@Override
	public int[] getCount() {
		synchronized (this) {
			if (this.count != null) {
				return this.count;
			}
			extractTermsAndCount();
			return this.count;
		}
	}

	@Override
	public String[] getTerms() {
		synchronized (this) {
			if (this.terms != null) {
				return this.terms;
			}
			extractTermsAndCount();
			return this.terms;
		}
	}

	private void sum(Facet facet) {
		synchronized (this) {
			String[] facetTerms = facet.getTerms();
			int[] facetCount = facet.getCount();
			for (int i = 0; i < facetCount.length; i++) {
				int c = facetCount[i];
				if (c == 0) {
					continue;
				}
				String term = facetTerms[i];
				Integer co = this.facetCount.get(term);
				if (co == null) {
					this.facetCount.put(term, co = new Integer(c));
				} else {
					this.facetCount.put(term, co + c);
				}
			}
			this.terms = null;
			this.count = null;
		}
	}

	public void run(FacetList facetList) throws IOException {
		synchronized (this) {
			Facet facet = facetList.getByField(this.facetField.getName());
			if (facet != null) {
				this.sum(facet);
			}
		}
	}
}
