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
package com.jaeksoft.searchlib.webservice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@XmlRootElement(name = "result")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@JsonInclude(Include.NON_NULL)
public class CommonListResult<T> extends CommonResult {

	public final Collection<T> items;

	public CommonListResult() {
		items = null;
	}

	public CommonListResult(int size) {
		items = new ArrayList<T>(size);
	}

	public CommonListResult(Set<T> items) {
		super(true, items.size() + " item(s) found");
		this.items = items;
	}

	public CommonListResult(T[] items) {
		super(true, items == null ? "No items" : items.length
				+ " item(s) found");
		this.items = items == null ? null : Arrays.asList(items);
	}

	public CommonListResult(List<T> items) {
		super(true, items.size() + " item(s) found");
		this.items = items;
	}

	public void computeInfos() {
		setInfo(items == null ? "No items" : items.size() + " item(s) found");
	}
}
