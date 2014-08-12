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
package com.jaeksoft.searchlib.web;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.ntlmv2.filter.NtlmFilter;

import com.jaeksoft.searchlib.Logging;

/**
 * Servlet Filter implementation class NtmlWebFilter
 */

public class NtlmWebFilter implements Filter {

	private final NtlmFilter ntlmFilter;

	private final Properties properties;

	/**
	 * Default constructor.
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public NtlmWebFilter() throws FileNotFoundException, IOException {
		String propFile = System.getProperty("ntlm.properties");
		if (!StringUtils.isEmpty(propFile)) {
			properties = new Properties();
			properties.load(new FileReader(propFile));
			ntlmFilter = new NtlmFilter();
			Logging.info("NtmlWebFilter activated with " + propFile);
		} else {
			properties = null;
			ntlmFilter = null;
		}
	}

	/**
	 * @see Filter#destroy()
	 */
	@Override
	public void destroy() {
		if (ntlmFilter != null)
			ntlmFilter.destroy();
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		if (ntlmFilter != null)
			ntlmFilter.doFilter(request, response, chain);
		else
			chain.doFilter(request, response);
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		if (ntlmFilter == null)
			return;
		ServletContext context = filterConfig.getServletContext();
		for (String name : properties.stringPropertyNames())
			context.setAttribute(name, properties.getProperty(name));
		ntlmFilter.init(new NtlmFilterConfig(filterConfig));
	}

	private class NtlmFilterConfig implements FilterConfig {

		private final FilterConfig filterConfig;

		private NtlmFilterConfig(FilterConfig filterConfig) {
			this.filterConfig = filterConfig;
		}

		@Override
		public String getFilterName() {
			return filterConfig.getFilterName();
		}

		@Override
		public ServletContext getServletContext() {
			return filterConfig.getServletContext();
		}

		@Override
		public String getInitParameter(String name) {
			return properties.getProperty(name);
		}

		@Override
		public Enumeration<?> getInitParameterNames() {
			return new ParamEnumeration(properties.stringPropertyNames()
					.iterator());
		}

		private class ParamEnumeration implements Enumeration<String> {

			private Iterator<String> iterator;

			public ParamEnumeration(Iterator<String> iterator) {
				this.iterator = iterator;
			}

			@Override
			public boolean hasMoreElements() {
				return iterator.hasNext();
			}

			@Override
			public String nextElement() {
				return iterator.next();
			}

		}

	}

}
