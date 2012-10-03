<%@ page import="com.jaeksoft.searchlib.result.AbstractResultSearch"%>
<%@ page import="com.jaeksoft.searchlib.request.SearchRequest"%>
<%@ page import="com.jaeksoft.searchlib.result.ResultDocument"%>
<%@ page import="com.jaeksoft.searchlib.renderer.RendererField"%>
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
					int j = 0;
					for (RendererField rendererField : renderer.getFields()) {
						j++;
						String url = rendererField
								.getUrlField(resultDocument);
						if (url != null)
							if (url.length() == 0)
								url = null;
						FieldValueItem[] fieldValueItems = rendererField
								.getFieldValue(resultDocument);
						if (fieldValueItems != null) {
							for (FieldValueItem fieldValueItem : fieldValueItems) {
	%>
	<div class="osscmnrdr ossfieldrdr<%=j%>">
		<%
			if (url != null) {
		%>
		<a target="_top" href="<%=url%>"><%=fieldValueItem.getValue()%></a>
		<%
			} else {
		%><%=fieldValueItem.getValue()%>
		<%
			}
		%>
	</div>
	<%
		} }
	%>
	<%
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