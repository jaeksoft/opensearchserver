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

package com.jaeksoft.searchlib.web;

import java.io.PrintWriter;
import java.util.HashSet;

import org.apache.commons.lang.StringEscapeUtils;

import com.jaeksoft.searchlib.util.XmlInfo;

public class ServletException extends Exception implements XmlInfo {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2340702838065290696L;

	public ServletException(String msg) {
		super(msg);
	}

	public ServletException(Exception e) {
		super(e.toString());
		e.printStackTrace();
	}

	public void xmlInfo(PrintWriter writer, HashSet<String> classDetail) {
		writer.println("<error>" + StringEscapeUtils.escapeXml(getMessage())
				+ "</error>");
	}

}
