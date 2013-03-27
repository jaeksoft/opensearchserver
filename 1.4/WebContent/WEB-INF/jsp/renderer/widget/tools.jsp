<%@ page import="com.jaeksoft.searchlib.renderer.RendererResult"%>
<%@ page import="com.jaeksoft.searchlib.schema.FieldValueItem"%>
<%@ page import="com.jaeksoft.searchlib.renderer.RendererField"%>
<%@ page import="com.jaeksoft.searchlib.result.ResultDocument"%>
<%
	ResultDocument resultDocument = (ResultDocument) request
			.getAttribute("resultDocument");
	FieldValueItem fieldValueItem = (FieldValueItem) request
			.getAttribute("fieldValueItem");
	RendererField rendererField = (RendererField) request
			.getAttribute("rendererField");
	Integer fieldPos = (Integer) request.getAttribute("fieldPos");
	String url = rendererField.getUrlField(resultDocument);
	String text = fieldValueItem.getValue();
	RendererResult rendererResult = (RendererResult) request
			.getAttribute("rendererResult");
	String viewerUrl = rendererResult.getViewerUrl(resultDocument, url);
	if (viewerUrl != null) {
%>
<a target="_top" href="<%=viewerUrl%>">Viewer</a>
<%
	}
%>