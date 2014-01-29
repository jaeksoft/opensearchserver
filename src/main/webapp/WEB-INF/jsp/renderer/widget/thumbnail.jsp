<%@ page import="com.jaeksoft.searchlib.schema.FieldValueItem"%>
<%@ page import="com.jaeksoft.searchlib.renderer.RendererField"%>
<%@ page import="com.jaeksoft.searchlib.result.ResultDocument"%>
<%
	ResultDocument resultDocument = (ResultDocument) request
			.getAttribute("resultDocument");
	String[] values = (String[]) request.getAttribute("rendererValues");
	String text = values != null && values.length > 0 ? values[0] : null;
	RendererField rendererField = (RendererField) request
			.getAttribute("rendererField");
	Integer fieldPos = (Integer) request.getAttribute("fieldPos");
	String url = rendererField.getUrlField(resultDocument);
	if (url != null) {
%>
<a target="_top" href="<%=url%>"> <%
 	}
 	if (text != null) {
 %> <img class="ossfieldrdr<%=fieldPos%>" src="<%=text%>"> <%
 	}
 	if (url != null) {
 %></a>
<%
	}
%>