/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.classifier;

public enum ClassificationMethodEnum {

	BESTSCORE("Best score"),

	MULTIVALUED("Multivalued");

	private String label;

	private ClassificationMethodEnum(String label) {
		this.label = label;
	}

	final public String getLabel() {
		return label;
	}

	final public static ClassificationMethodEnum find(String method) {
		if (method == null || method.length() == 0)
			return BESTSCORE;
		return valueOf(method);
	}
}
