/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.analysis;

import com.jaeksoft.searchlib.SearchLibException;

public class ClassProperty {

	private ClassFactory classFactory;
	private ClassPropertyEnum classPropertyEnum;
	private String value;
	private Object[] valueList;

	public ClassProperty(ClassFactory classFactory,
			ClassPropertyEnum classPropertyEnum, String value,
			Object[] valueList) {
		this.classPropertyEnum = classPropertyEnum;
		this.classFactory = classFactory;
		this.value = value;
		this.valueList = valueList;
	}

	/**
	 * @return the classPropertyEnum
	 */
	public ClassPropertyEnum getClassPropertyEnum() {
		return classPropertyEnum;
	}

	/**
	 * 
	 * @return the value of the property
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Set the value for this property
	 * 
	 * @param value
	 * @throws SearchLibException
	 */
	public void setValue(String value) throws SearchLibException {
		System.out.println("VALUE_STRING " + value.getClass().getName() + " = "
				+ value.toString());
		classFactory.checkValue(classPropertyEnum, value);
		this.value = value;
	}

	/**
	 * 
	 * @return the possible value list (if any)
	 */
	public Object[] getValueList() {
		return valueList;
	}

	/**
	 * 
	 * @return true if a non empty value list is available
	 */
	public boolean isValueList() {
		return getValueList() != null && getValueList().length > 0;
	}

	/**
	 * 
	 * @return true if there is no value list
	 */
	public boolean isTextbox() {
		return !isValueList();
	}
}
