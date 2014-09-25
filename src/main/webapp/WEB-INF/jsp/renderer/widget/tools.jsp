<%@ page import="com.jaeksoft.searchlib.renderer.RendererResult"%>
<%@ page import="com.jaeksoft.searchlib.schema.FieldValueItem"%>
<%@ page import="com.jaeksoft.searchlib.renderer.field.RendererField"%>
<%@ page import="com.jaeksoft.searchlib.result.ResultDocument"%>
<%
	ResultDocument resultDocument = (ResultDocument) request
			.getAttribute("resultDocument");
	RendererField rendererField = (RendererField) request
			.getAttribute("rendererField");
	String url = rendererField.getOriginalUrl(resultDocument);
	RendererResult rendererResult = (RendererResult) request
			.getAttribute("rendererResult");
	boolean parm = false;
	String viewerUrl = rendererResult.getViewerUrl(resultDocument, url);
	if (viewerUrl != null) {
%>
<a target="_top" href="<%=viewerUrl%>">Viewer</a>
<%
	parm = true;
	}
	String openFolderUrl = rendererResult.getOpenFolderUrl(
			resultDocument, url);
	if (openFolderUrl != null) {
		if (parm) {
%>
&nbsp;&nbsp;
<%
	} // if (parm)
%>
<a target="_top" href="<%=openFolderUrl%>">Open folder</a>
<%
	parm = true;
	}
%>