<%@ page import="com.jaeksoft.searchlib.schema.FieldValueItem"%>
<%@ page import="com.jaeksoft.searchlib.renderer.field.RendererField"%>
<%@ page import="com.jaeksoft.searchlib.renderer.field.RendererWidget"%>
<%@ page import="com.jaeksoft.searchlib.result.ResultDocument"%>
<%@ page import="org.apache.commons.lang3.StringEscapeUtils"%>
<%@ page import="com.jaeksoft.searchlib.util.StringUtils"%>

<%
	ResultDocument resultDocument = (ResultDocument) request
			.getAttribute("resultDocument");
	String value = (String) request.getAttribute("rendererValue");
	RendererField rendererField = (RendererField) request
			.getAttribute("rendererField");
	RendererWidget rendererWidget = rendererField.getWidget();
	value = rendererWidget.getValue(value);
	String url = rendererField.getUrlField(resultDocument);
	if (url != null) {
%>
<a target="_top" href="<%=url%>"><%=value%></a>
<%
	} else {
%>
<%=value%>
<%
	}
%>