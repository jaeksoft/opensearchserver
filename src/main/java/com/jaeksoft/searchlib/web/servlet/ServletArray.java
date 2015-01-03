/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2015 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.servlet;

import io.undertow.servlet.api.DeploymentInfo;

import javax.servlet.http.HttpServlet;

import com.jaeksoft.searchlib.Server;

public class ServletArray {

	public final static Class<?>[] SERVLETS = {
			com.jaeksoft.searchlib.web.servlet.restv1.ActionServlet.class,
			com.jaeksoft.searchlib.web.servlet.restv1.AutoCompletionServlet.class,
			com.jaeksoft.searchlib.web.servlet.restv1.DatabaseServlet.class,
			com.jaeksoft.searchlib.web.servlet.restv1.DeleteServlet.class,
			com.jaeksoft.searchlib.web.servlet.restv1.FileCrawlerServlet.class,
			com.jaeksoft.searchlib.web.servlet.restv1.IndexServlet.class,
			com.jaeksoft.searchlib.web.servlet.restv1.MonitorServlet.class,
			com.jaeksoft.searchlib.web.servlet.restv1.OptimizeServlet.class,
			com.jaeksoft.searchlib.web.servlet.restv1.PatternServlet.class,
			com.jaeksoft.searchlib.web.servlet.restv1.PushServlet.class,
			com.jaeksoft.searchlib.web.servlet.restv1.RendererServlet.class,
			com.jaeksoft.searchlib.web.servlet.restv1.ReportServlet.class,
			com.jaeksoft.searchlib.web.servlet.restv1.SchemaServlet.class,
			com.jaeksoft.searchlib.web.servlet.restv1.ScreenshotServlet.class,
			com.jaeksoft.searchlib.web.servlet.restv1.SearchServlet.class,
			com.jaeksoft.searchlib.web.servlet.restv1.SearchTemplateServlet.class,
			com.jaeksoft.searchlib.web.servlet.restv1.SelectServlet.class,
			com.jaeksoft.searchlib.web.servlet.restv1.StatServlet.class,
			com.jaeksoft.searchlib.web.servlet.restv1.URLBrowserServlet.class,
			com.jaeksoft.searchlib.web.servlet.restv1.ViewerServlet.class,
			com.jaeksoft.searchlib.web.servlet.restv1.WebCrawlerServlet.class,
			com.jaeksoft.searchlib.web.servlet.ui.AffinitiesServlet.class,
			com.jaeksoft.searchlib.web.servlet.ui.AnalyzersServlet.class,
			com.jaeksoft.searchlib.web.servlet.ui.ClassifiersServlet.class,
			com.jaeksoft.searchlib.web.servlet.ui.ClusterServlet.class,
			com.jaeksoft.searchlib.web.servlet.ui.IndicesServlet.class,
			com.jaeksoft.searchlib.web.servlet.ui.JobsServlet.class,
			com.jaeksoft.searchlib.web.servlet.ui.LoginServlet.class,
			com.jaeksoft.searchlib.web.servlet.ui.LogoutServlet.class,
			com.jaeksoft.searchlib.web.servlet.ui.ParsersServlet.class,
			com.jaeksoft.searchlib.web.servlet.ui.QueriesServlet.class,
			com.jaeksoft.searchlib.web.servlet.ui.ReplicationsServlet.class,
			com.jaeksoft.searchlib.web.servlet.ui.TermsServlet.class,
			com.jaeksoft.searchlib.web.servlet.ui.UsersServlet.class,
			com.jaeksoft.searchlib.web.servlet.ui.crawler.DatabaseCrawlerServlet.class,
			com.jaeksoft.searchlib.web.servlet.ui.crawler.FileCrawlerServlet.class,
			com.jaeksoft.searchlib.web.servlet.ui.crawler.MailboxesCrawlerServlet.class,
			com.jaeksoft.searchlib.web.servlet.ui.crawler.RestWSCrawlerServlet.class,
			com.jaeksoft.searchlib.web.servlet.ui.crawler.WebCrawlerServlet.class };

	@SuppressWarnings("unchecked")
	public static void register(DeploymentInfo di) {
		for (Class<?> servletClass : SERVLETS)
			Server.registerServlet(di,
					(Class<? extends HttpServlet>) servletClass);
	}
}
