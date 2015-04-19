<%@ page import="com.jaeksoft.searchlib.schema.FieldValueItem"%>
<%@ page import="com.jaeksoft.searchlib.renderer.field.RendererField"%>
<%@ page import="com.jaeksoft.searchlib.result.ResultDocument"%>
<%
	ResultDocument resultDocument = (ResultDocument) request
			.getAttribute("resultDocument");
	String value = (String) request.getAttribute("rendererValue");
	RendererField rendererField = (RendererField) request
			.getAttribute("rendererField");
	Integer fieldPos = (Integer) request.getAttribute("fieldPos");
	String url = rendererField.getUrlField(resultDocument);
	if (url != null) {
%>
<a target="_top" href="<%=url%>"> <%
 	}
 	if (value != null) {
 %> <img class="ossfieldrdr<%=fieldPos%>" src="<%=value%>"> <%
 	}
 	if (url != null) {
 %></a>
<%
	}
%>