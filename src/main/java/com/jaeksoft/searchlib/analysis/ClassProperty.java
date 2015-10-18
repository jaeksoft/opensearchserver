/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.analysis;

import com.jaeksoft.searchlib.SearchLibException;

public class ClassProperty {

	private final ClassFactory classFactory;
	private final ClassPropertyEnum classPropertyEnum;
	private String value;
	private final Object[] valueList;
	private final int cols;
	private final int rows;

	public ClassProperty(ClassFactory classFactory, ClassPropertyEnum classPropertyEnum, String value,
			Object[] valueList, int cols, int rows) {
		this.classPropertyEnum = classPropertyEnum;
		this.classFactory = classFactory;
		this.value = value;
		this.valueList = valueList;
		this.cols = cols > 1 ? cols : 1;
		this.rows = rows > 1 ? rows : 1;

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
	 *            the value
	 * @throws SearchLibException
	 *             inherited error
	 */
	public void setValue(String value) throws SearchLibException {
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
	public boolean isList() {
		return getValueList() != null && getValueList().length > 0;
	}

	/**
	 * 
	 * @return true if there is no value list
	 */
	public boolean isTextbox() {
		return !isList() && rows <= 1;
	}

	/**
	 * 
	 * @return true if the control should be a multiline textbox
	 */
	public boolean isMultilinetextbox() {
		return !isList() && rows > 1;
	}

	/**
	 * 
	 * @return the number of rows for a textbox control
	 */
	public int getRows() {
		return rows;
	}

	/**
	 * 
	 * @return the number of columns
	 */
	public int getCols() {
		return cols;
	}
}
