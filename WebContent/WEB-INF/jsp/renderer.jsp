<%@page contentType="text/html; charset=UTF-8"%>
<html>
<head>
<meta http-equiv="content-type" content="text/html; charset=UTF-8">
</head>
<%@ page import="com.jaeksoft.searchlib.renderer.Renderer"%>
<%@ page import="com.jaeksoft.searchlib.result.Result"%>
<%@ page import="com.jaeksoft.searchlib.request.SearchRequest"%>
<%@ page import="com.jaeksoft.searchlib.result.ResultDocument"%>
<%@ page import="com.jaeksoft.searchlib.schema.FieldValueItem"%>
<%
	String[] hiddenParameterList = { "use", "name", "login", "key" };
	String query = request.getParameter("query");
	if (query == null)
		query = "";
	Renderer renderer = (Renderer) request.getAttribute("renderer");
	Result result = (Result) request.getAttribute("result");
%>
<body>
	<form method="get">
		<%
			for (String p : hiddenParameterList) {
				String v = request.getParameter(p);
				if (v != null) {
		%>
		<input type="hidden" name="<%=p%>" value="<%=v%>" />
		<%
			}
			}
		%>
		<input type="text" name="query" value="<%=query%>" /> <input
			type="submit" value="Search" />
	</form>
	<p><%=renderer%></p>

	<%
		if (result != null) {
			SearchRequest searchRequest = result.getSearchRequest();
			int start = searchRequest.getStart();
			int end = searchRequest.getStart() + result.getDocumentCount();
	%>
	<ol style="margin: 0pt; padding: 0pt; list-style: none outside none;">
		<%
			for (int i = start; i < end; i++) {
					ResultDocument document = result.getDocument(i);
					String url = document.getValueContent("url", 0);
					FieldValueItem fvi = document.getSnippet("title", 0);
					String title = fvi == null ? null : fvi.getValue();
					fvi = document.getSnippet("content", 0);
					String content = fvi == null ? null : fvi.getValue();
		%>
		<li><h2>
				<a target="_top" href="<%=url%>"><%=title%></a>
			</h2>
			<div><%=content%><br /> <span><%=url%></span>
			</div></li>
		<%
			}
		%>
	</ol>
	<%
		}
	%>
</body>
</html>