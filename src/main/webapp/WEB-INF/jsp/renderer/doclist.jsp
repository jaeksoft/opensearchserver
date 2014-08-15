<%@ page import="com.jaeksoft.searchlib.result.AbstractResultSearch"%>
<%@ page import="com.jaeksoft.searchlib.request.AbstractSearchRequest"%>
<%@ page import="com.jaeksoft.searchlib.result.ResultDocument"%>
<%@ page import="com.jaeksoft.searchlib.renderer.field.RendererField"%>
<%@ page
	import="com.jaeksoft.searchlib.renderer.field.RendererWidgetType"%>
<%@ page import="com.jaeksoft.searchlib.renderer.Renderer"%>
<%
	AbstractResultSearch result = (AbstractResultSearch) request
			.getAttribute("result");
	if (result != null) {
		Renderer renderer = (Renderer) request.getAttribute("renderer");
		if (result.getDocumentCount() > 0) {
			AbstractSearchRequest searchRequest = result.getRequest();
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
						RendererWidgetType widget = rendererField
								.getWidgetName();
						String[] fieldValues = rendererField
								.getFieldValue(resultDocument);
						if (fieldValues != null) {
							for (String fieldValue : fieldValues) {
								request.setAttribute("rendererValue",
										fieldValue);
								request.setAttribute("rendererField",
										rendererField);
	%>
	<div
		class="osscmnrdr ossfieldrdr<%=fieldPos%><%=rendererField.renderCssClass()%>">
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