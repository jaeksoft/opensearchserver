<%@ page import="com.jaeksoft.searchlib.result.AbstractResultSearch"%>
<%@ page import="com.jaeksoft.searchlib.request.SearchRequest"%>
<%@ page import="com.jaeksoft.searchlib.result.ResultDocument"%>
<%@ page import="com.jaeksoft.searchlib.renderer.RendererField"%>
<%@ page import="com.jaeksoft.searchlib.renderer.RendererWidgets"%>
<%@ page import="com.jaeksoft.searchlib.renderer.Renderer"%>
<%@ page import="com.jaeksoft.searchlib.schema.FieldValueItem"%>
<%
	AbstractResultSearch result = (AbstractResultSearch) request
			.getAttribute("result");
	if (result != null) {
		Renderer renderer = (Renderer) request.getAttribute("renderer");
		if (result.getDocumentCount() > 0) {
			SearchRequest searchRequest = result.getRequest();
			int start = searchRequest.getStart();
			int end = searchRequest.getStart()
					+ result.getDocumentCount();
%>
<div class="osscmnrdr oss-result">
	<%
		for (int i = start; i < end; i++) {
					ResultDocument resultDocument = result.getDocument(i);
					request.setAttribute("resultDocument", resultDocument);
					Integer fieldPos = 0;
					for (RendererField rendererField : renderer.getFields()) {
						fieldPos++;
						request.setAttribute("fieldPos", fieldPos);
						RendererWidgets widget = rendererField
								.getWidgetName();
						FieldValueItem[] fieldValueItems = rendererField
								.getFieldValue(resultDocument);
						if (fieldValueItems != null) {
							for (FieldValueItem fieldValueItem : fieldValueItems) {
								request.setAttribute("fieldValueItem",
										fieldValueItem);
								request.setAttribute("rendererField",
										rendererField);
	%>
	<div class="osscmnrdr ossfieldrdr<%=fieldPos%>">
		<jsp:include page="<%=widget.getJspPath()%>" flush="true" />
	</div>
	<%
		}
						}
					}
	%>
	<br />
	<%
		}
	%>
	<jsp:include page="paging.jsp" />
</div>
<%
	}
	}
%>