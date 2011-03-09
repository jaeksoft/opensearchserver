<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=utf-8"
	language="java"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
</head>
<%@ page import="com.jaeksoft.searchlib.renderer.Renderer"%>
<%@ page import="com.jaeksoft.searchlib.result.Result"%>
<%@ page import="com.jaeksoft.searchlib.request.SearchRequest"%>
<%@ page import="com.jaeksoft.searchlib.result.ResultDocument"%>
<%@ page import="com.jaeksoft.searchlib.schema.FieldValueItem"%>
<%@ page import="com.jaeksoft.searchlib.renderer.Paging"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="java.net.URLDecoder"%>
<%
	String[] hiddenParameterList = { "use", "name", "login", "key" };
	String query = request.getParameter("query");
	if (query == null)
		query = "";
	Renderer renderer = (Renderer) request.getAttribute("renderer");
	Result result = (Result) request.getAttribute("result");
	Paging paging = result == null ? null : new Paging(result, 10);
%>
<body>
	<form method="get">
		<%
			StringBuffer getUrl = new StringBuffer("?query=");
			getUrl.append(URLEncoder.encode(query, "UTF-8"));
			for (String p : hiddenParameterList) {
				String v = request.getParameter(p);
				if (v != null) {
					getUrl.append('&');
					getUrl.append(p);
					getUrl.append('=');
					getUrl.append(URLEncoder.encode(v, "UTF-8"));
		%>
		<input type="hidden" name="<%=p%>" value="<%=v%>" />
		<%
			}
			}
		%>
		<input type="text" name="query" value="<%=query%>" /> <input
			type="submit" value="Search" />
	</form>

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
			</div>
		</li>
		<%
			} // (end loop documents)
		%>
	</ol>
	<table>
		<tr>
			<%
				for (int i = paging.getLeftPage(); i <= paging.getRightPage(); i++) {
			%>
			<td><a href="<%=getUrl.toString()%>&page=<%=i%>"><%=i%></a></td>
			<%
				} // (end loop paging)
			%>
		</tr>
	</table>
	<%
		} // (if result != null)
	%>
</body>
</html>