<%@ page import="com.jaeksoft.searchlib.renderer.RendererResult"%>
<%@ page import="com.jaeksoft.searchlib.schema.FieldValueItem"%>
<%@ page import="com.jaeksoft.searchlib.renderer.field.RendererField"%>
<%@ page import="com.jaeksoft.searchlib.renderer.field.RendererWidget"%>
<%@ page import="com.jaeksoft.searchlib.result.ResultDocument"%>
<%
	ResultDocument resultDocument = (ResultDocument) request
			.getAttribute("resultDocument");
	RendererField rendererField = (RendererField) request
			.getAttribute("rendererField");
	RendererWidget rendererWidget = rendererField.getWidget();
	String[] fieldValues = rendererField.getFieldValue(resultDocument);
	String originalUrl = rendererField.getOriginalUrl(resultDocument);
	String fieldUrl = rendererField.getUrlField(resultDocument);
	RendererResult rendererResult = (RendererResult) request
			.getAttribute("rendererResult");
	boolean parm = false;
	String viewerUrl = rendererResult.getViewerUrl(resultDocument,
			originalUrl);
	if (viewerUrl != null) {
%>
<a target="_top" href="<%=viewerUrl%>">Viewer</a>
<%
	parm = true;
	}
	String openFolderUrl = rendererResult.getOpenFolderUrl(
			resultDocument, fieldUrl);
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
	String openMailboxUrl = rendererResult.getOpenMailboxUrl(
			rendererWidget, fieldValues, fieldUrl);
	if (openMailboxUrl != null) {
		if (parm) {
%>
&nbsp;&nbsp;
<%
	} //if (parm)
%>
<a target="_top" href="<%=openMailboxUrl%>">Open mailbox</a>
<%
	}
%>