/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.util;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;

public class Debug {

	private List<Debug> childrens;

	private String info;

	private long startTime;

	private long elapsedTime;

	private String className;

	private int hc;

	public Debug() {
		childrens = null;
		className = null;
		info = null;
		startTime = System.currentTimeMillis();
	}

	public void setInfo(Object object) {
		if (object != null) {
			this.className = object.getClass().getCanonicalName();
			this.info = object.toString();
			this.hc = object.hashCode();
		}
		this.elapsedTime = System.currentTimeMillis() - startTime;
		System.out.println(this);
	}

	public Debug addChildren() {
		return addChildren(new Debug());
	}

	public Debug addChildren(Debug debug) {
		if (debug == null)
			return null;
		synchronized (this) {
			if (childrens == null)
				childrens = new ArrayList<Debug>();
			childrens.add(debug);
		}
		return debug;
	}

	private DateFormat dtdf = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
			DateFormat.FULL);

	public void xmlInfo(PrintWriter writer) {
		writer.print("<debug start=\"");
		writer.print(dtdf.format(new Date(startTime)));
		writer.print("\" elapsed=\"");
		writer.print(elapsedTime);
		writer.print("\" info=\"");
		writer.print(StringEscapeUtils.escapeJava(StringEscapeUtils
				.escapeXml(info)));
		writer.print("\" hashCode=\"");
		writer.print(hc);
		writer.print("\" class=\"");
		writer.print(className);
		writer.print("\">");
		if (childrens != null)
			for (Debug children : childrens)
				children.xmlInfo(writer);
		writer.print("</debug>");
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(dtdf.format(new Date(startTime)));
		sb.append(" elapsed:");
		sb.append(elapsedTime);
		sb.append(" info:");
		sb.append(info);
		sb.append(" class:");
		sb.append(" className:");
		return sb.toString();
	}
}
