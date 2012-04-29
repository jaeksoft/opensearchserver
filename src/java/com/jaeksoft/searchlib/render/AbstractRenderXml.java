/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.render;

import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;

import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.result.AbstractResult;
import com.jaeksoft.searchlib.web.ServletTransaction;

public abstract class AbstractRenderXml<T1 extends AbstractRequest, T2 extends AbstractResult<T1>>
		extends AbstractRender<T1, T2> {

	final private Matcher controlMatcher;
	final private Matcher spaceMatcher;

	protected AbstractRenderXml(T2 result) {
		super(result);
		Pattern p = Pattern.compile("\\p{Cntrl}+");
		controlMatcher = p.matcher("");
		p = Pattern.compile("\\p{Space}+");
		spaceMatcher = p.matcher("");
	}

	public abstract void render(PrintWriter writer) throws Exception;

	protected String xmlTextRender(String text) {
		synchronized (controlMatcher) {
			controlMatcher.reset(text);
			text = controlMatcher.replaceAll(" ");
		}
		synchronized (spaceMatcher) {
			spaceMatcher.reset(text);
			text = spaceMatcher.replaceAll(" ");
		}
		return StringEscapeUtils.escapeXml(text);
	}

	@Override
	public void render(ServletTransaction servletTransaction) throws Exception {
		servletTransaction.setResponseContentType("text/xml");
		render(servletTransaction.getWriter("UTF-8"));
	}
}
