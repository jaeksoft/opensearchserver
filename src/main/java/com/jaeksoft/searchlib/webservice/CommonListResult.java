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
package com.jaeksoft.searchlib.webservice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.jaeksoft.searchlib.autocompletion.AutoCompletionItem;

@XmlRootElement(name = "result")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class CommonListResult extends CommonResult {

	public final Collection<String> items;

	public CommonListResult() {
		items = null;
	}

	public CommonListResult(Set<String> items) {
		super(true, items.size() + " item(s) found");
		this.items = items;
	}

	public CommonListResult(String[] items) {
		super(true, items == null ? "No items" : items.length
				+ " item(s) found");
		this.items = items == null ? null : Arrays.asList(items);
	}

	public CommonListResult(List<String> items) {
		super(true, items.size() + " item(s) found");
		this.items = items;
	}

	public CommonListResult(Collection<AutoCompletionItem> items) {
		super(true, items.size() + " item(s) found");
		this.items = new ArrayList<String>(items.size());
		for (AutoCompletionItem item : items)
			this.items.add(item.getName());
	}

	public void computeInfos() {
		setInfo(items == null ? "No items" : items.size() + " item(s) found");
	}
}
