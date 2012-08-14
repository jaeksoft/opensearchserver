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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.result.AbstractResult;
import com.jaeksoft.searchlib.web.ServletTransaction;

public abstract class AbstractRenderXml<T1 extends AbstractRequest, T2 extends AbstractResult<T1>>
		extends AbstractRender<T1, T2> {

	final private Matcher controlMatcher;
	final private Matcher spaceMatcher;

	protected PrintWriter writer;

	protected AbstractRenderXml(T2 result) {
		super(result);
		Pattern p = Pattern.compile("\\p{Cntrl}+");
		controlMatcher = p.matcher("");
		p = Pattern.compile("\\p{Space}+");
		spaceMatcher = p.matcher("");
	}

	public abstract void render() throws Exception;

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

	protected void renderPrefix(int status, String queryString)
			throws ParseException, SyntaxError, SearchLibException, IOException {
		writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		writer.println("<response>");
		writer.println("<header>");
		writer.print("<status>");
		writer.print(status);
		writer.print("</status>");
		writer.print("<query>");
		writer.print(StringEscapeUtils.escapeXml(queryString));
		writer.println("</query>");
		writer.println("</header>");
	}

	protected void renderSuffix() {
		writer.println("</response>");
	}

	@Override
	final public void render(ServletTransaction servletTransaction)
			throws Exception {
		servletTransaction.setResponseContentType("text/xml");
		writer = servletTransaction.getWriter("UTF-8");
		render();
	}

	protected void renderTimers() {
		result.getTimer().writeXml(writer, request.getTimerMinTime(),
				request.getTimerMaxDepth());
	}

}
