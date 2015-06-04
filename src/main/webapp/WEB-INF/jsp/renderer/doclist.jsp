<%@ page import="java.util.List"%>
<%@ page import="com.jaeksoft.searchlib.result.AbstractResultSearch"%>
<%@ page import="com.jaeksoft.searchlib.request.AbstractSearchRequest"%>
<%@ page import="com.jaeksoft.searchlib.result.ResultDocument"%>
<%@ page import="com.jaeksoft.searchlib.renderer.field.RendererField"%>
<%@ page
	import="com.jaeksoft.searchlib.renderer.field.RendererWidgetType"%>
<%@ page import="com.jaeksoft.searchlib.renderer.Renderer"%>
<%
	AbstractResultSearch<?> result = (AbstractResultSearch<?>) request
			.getAttribute("result");
	if (result != null) {
		Renderer renderer = (Renderer) request.getAttribute("renderer");
		if (result.getDocumentCount() > 0) {
			boolean isJoin = renderer.isFieldWithJoin();
			AbstractSearchRequest searchRequest = result.getRequest();
			int start = searchRequest.getStart();
			int end = searchRequest.getStart()
					+ result.getDocumentCount();
%>
<div class="osscmnrdr oss-result">
	<%
		for (int i = start; i < end; i++) {
					ResultDocument mainResultDocument = result
							.getDocument(i);
					List<ResultDocument> joinResultDocuments = null;
					if (isJoin)
						joinResultDocuments = result.getJoinDocumentList(i,
								null);
					Integer fieldPos = 0;
					boolean lastWasReplace = false;
					String[] lastFieldValues = null;
					for (RendererField rendererField : renderer.getFields()) {
						if (rendererField.isReplacePrevious()) {
							if (!lastWasReplace)
								fieldPos--;
							lastWasReplace = true;
							if (lastFieldValues != null)
								continue;
						}
						ResultDocument resultDocument = rendererField
								.getResultDocument(mainResultDocument,
										joinResultDocuments);
						request.setAttribute("resultDocument",
								resultDocument);

						fieldPos++;
						request.setAttribute("fieldPos", fieldPos);
						RendererWidgetType widget = rendererField
								.getWidgetName();
						String[] fieldValues = rendererField
								.getFieldValue(resultDocument);
						lastFieldValues = fieldValues;
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