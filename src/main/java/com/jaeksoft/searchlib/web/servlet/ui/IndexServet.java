package com.jaeksoft.searchlib.web.servlet.ui;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.jaeksoft.searchlib.SearchLibException;

import freemarker.template.TemplateException;

public class IndexServet {

	public final static String TEMPLATE = "index.ftl";

	public final static String PATH = "/ui/index";

	protected void service(UITransaction transaction) throws IOException,
			TemplateException, SearchLibException {
		String index = transaction.request.getParameter("index");
		if (StringUtils.isEmpty(index)) {
			transaction.redirectContext(WelcomeServlet.PATH);
			return;
		}
		transaction.variables.put("index", index);
		transaction.template(PATH);
	}
}
