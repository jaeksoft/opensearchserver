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

package com.jaeksoft.searchlib.renderer.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import com.jaeksoft.searchlib.facet.Facet;
import com.jaeksoft.searchlib.facet.FacetItem;
import com.jaeksoft.searchlib.facet.FacetList;
import com.jaeksoft.searchlib.renderer.field.RendererWidget;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.util.StringUtils;

public class RendererFilterFacetMerge implements RendererFilterInterface {

	private boolean caseSensitive = false;
	private String fieldName;
	private final Map<String, String> map = new TreeMap<String, String>();

	private final String defaultProperties = "casesensitive=false"
			+ StringUtils.LF + "1.label=Word" + StringUtils.LF + "1.value1=doc"
			+ StringUtils.LF + "1.value2=docx" + StringUtils.LF
			+ "1.value3=DOC" + StringUtils.LF + "1.value4=DOCX"
			+ StringUtils.LF + "2.label=PDF" + StringUtils.LF + "2.value1=pdf"
			+ StringUtils.LF + "2.value2=PDF";

	@Override
	public String getDefaultProperties() {
		return defaultProperties;
	}

	@Override
	public void init(String fieldName, String properties) throws IOException {
		this.fieldName = fieldName;
		Properties props = RendererWidget.loadProperties(properties);
		caseSensitive = Boolean.parseBoolean(props.getProperty("casesensitive",
				Boolean.toString(true)));
		int i = 1;
		map.clear();
		for (;;) {
			String label = props.getProperty(i + ".label");
			if (label == null)
				break;
			int j = 1;
			for (;;) {
				String value = props.getProperty(i + ".value" + j);
				if (value == null)
					break;
				if (!caseSensitive)
					value = value.toLowerCase();
				map.put(value, label);
				j++;
			}
			i++;
		}
	}

	@Override
	public void populate(AbstractResultSearch facetResult,
			List<RendererFilterItem> filterItem) {
		FacetList facetList = facetResult.getFacetList();
		if (facetList == null)
			return;
		Facet facet = facetList.getByField(fieldName);
		if (facet == null)
			return;
		FacetItem[] facetItems = facet.getArray();
		if (facetItems == null)
			return;
		TreeMap<String, Item> facetMap = new TreeMap<String, Item>();
		for (FacetItem facetItem : facetItems) {
			String testedValue = facetItem.getTerm();
			if (!caseSensitive)
				testedValue = testedValue.toLowerCase();
			String target = map.get(testedValue);
			if (target == null)
				target = facetItem.getTerm();
			Item item = facetMap.get(target);
			if (item == null) {
				item = new Item();
				facetMap.put(target, item);
			}
			item.add(facetItem);
		}
		for (Map.Entry<String, Item> entry : facetMap.entrySet())
			filterItem.add(entry.getValue().getRendererFilterItem(
					entry.getKey()));
	}

	private class Item {

		private final List<String> terms;
		private long count;

		private Item() {
			terms = new ArrayList<String>(1);
			count = 0;
		}

		private void add(FacetItem facetItem) {
			terms.add(facetItem.getTerm());
			count += facetItem.getCount();
		}

		private final RendererFilterItem getRendererFilterItem(String target) {
			StringBuilder sb = new StringBuilder(fieldName);
			sb.append(":(");
			boolean first = true;
			for (String term : terms) {
				if (!first)
					sb.append(" OR ");
				else
					first = false;
				sb.append('"');
				sb.append(term);
				sb.append('"');
			}
			sb.append(')');
			return new RendererFilterItem(sb.toString(),
					StringUtils.fastConcat(target, " (", count, ")"));
		}
	}
}
