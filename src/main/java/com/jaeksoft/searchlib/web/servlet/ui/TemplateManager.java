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

package com.jaeksoft.searchlib.web.servlet.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

public class TemplateManager implements TemplateLoader {

	private final ServletContext context;
	private final long lastModified;
	private final Configuration cfg;

	protected final static String TEMPLATE_PREFIX = "/WEB-INF/ftl/";

	static public TemplateManager INSTANCE = null;

	private TemplateManager(ServletContext context) {
		this.context = context;
		this.lastModified = System.currentTimeMillis();
		cfg = new Configuration(Configuration.VERSION_2_3_21);
		cfg.setTemplateLoader(this);
		cfg.setOutputEncoding("UTF-8");
		cfg.setDefaultEncoding("UTF-8");
		cfg.setTagSyntax(Configuration.AUTO_DETECT_TAG_SYNTAX);
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
	}

	public static synchronized void init(ServletContext context) {
		INSTANCE = new TemplateManager(context);
	}

	@Override
	final public Object findTemplateSource(String name) throws IOException {
		return context.getResourceAsStream(TEMPLATE_PREFIX + name);
	}

	@Override
	final public long getLastModified(Object templateSource) {
		return lastModified;
	}

	@Override
	final public Reader getReader(Object templateSource, String encoding)
			throws IOException {
		return new InputStreamReader((InputStream) templateSource, encoding);
	}

	@Override
	final public void closeTemplateSource(Object templateSource)
			throws IOException {
		((InputStream) templateSource).close();
	}

	final public void template(String templatePath, Map<?, ?> root,
			HttpServletResponse response) throws IOException, TemplateException {
		Template template = cfg.getTemplate(templatePath);
		template.process(root, response.getWriter());
	}
}
