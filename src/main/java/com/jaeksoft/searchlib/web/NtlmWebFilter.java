package com.jaeksoft.searchlib.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import jcifs.http.NtlmHttpFilter;

import org.apache.commons.lang3.StringUtils;

import com.jaeksoft.searchlib.Logging;

/**
 * Servlet Filter implementation class NtmlWebFilter
 */

public class NtlmWebFilter implements Filter {

	private final NtlmHttpFilter ntmlHttpFilter;

	/**
	 * Default constructor.
	 */
	public NtlmWebFilter() {
		String jcifsProps = System.getProperty("jcifs.properties");
		ntmlHttpFilter = StringUtils.isEmpty(jcifsProps) ? null
				: new NtlmHttpFilter();
		if (ntmlHttpFilter != null)
			Logging.info("NtmlWebFilter activated with " + jcifsProps);
	}

	/**
	 * @see Filter#destroy()
	 */
	@Override
	public void destroy() {
		if (ntmlHttpFilter != null)
			ntmlHttpFilter.destroy();
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		if (ntmlHttpFilter != null)
			ntmlHttpFilter.doFilter(request, response, chain);
		else
			chain.doFilter(request, response);
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		if (ntmlHttpFilter != null)
			ntmlHttpFilter.init(filterConfig);
	}
}
