<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=utf-8"
	language="java"%>
<%@ page import="com.jaeksoft.searchlib.renderer.Renderer"%>
<%@ page import="com.jaeksoft.searchlib.renderer.RendererField"%>
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
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<style type="text/css">
.osscmnrdr{
<%=renderer.getCommonStyle()==null?"":renderer.getCommonStyle()%>
}
.ossinputrdr{
<%=renderer.getInputStyle()==null?"":renderer.getInputStyle()%>
}
.ossbuttonrdr{
<%=renderer.getButtonStyle()==null?"":renderer.getButtonStyle()%>
}
<%
	int j = 0;
	for (RendererField rendererField : renderer.getFields()) {
		j++;
%>
.ossfieldrdr<%=j%>{
<%=rendererField.getStyle()==null?"":rendererField.getStyle()%>
}
<%
	}
%>
a:link{
<%=renderer.getAlink()==null?"":renderer.getAlink()%>
}
a:hover{
<%=renderer.getAhover()==null?"":renderer.getAhover()%>
}
a:visited{
<%=renderer.getAvisited()==null?"":renderer.getAvisited()%>
}
a:active{
<%=renderer.getAactive()==null?"":renderer.getAactive()%>
}
#ossautocomplete{
<%=renderer.getAutocompleteStyle()==null?"":renderer.getAutocompleteStyle()%>
}
#ossautocompletelist {
<%=renderer.getAutocompleteListStyle()==null?"":renderer.getAutocompleteListStyle()%>
}
.ossautocomplete_link {
<%=renderer.getAutocompleteLinkStyle()==null?"":renderer.getAutocompleteLinkStyle()%>
}
.ossautocomplete_link_over {
<%=renderer.getAutocompleteLinkHoverStyle()==null?"":renderer.getAutocompleteLinkHoverStyle()%>
}
.ossnumfound {
<%=renderer.getDocumentFoundStyle()==null?"":renderer.getDocumentFoundStyle()%>
}

</style>
</head>
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
		
		 
		<input class="osscmnrdr ossinputrdr" size="60" type="text" id="query" name="query" value="<%=query%>" onkeyup="autosuggest(event)" autocomplete="off"/>
		<input class="osscmnrdr ossbuttonrdr"
			type="submit" value="<%=renderer.getSearchButtonLabel()%>" />
				<div style="position:absolute;">
					<div id="ossautocomplete"></div>
				</div>
	</form>
 
	<br/>
	<%
		if (result != null && result.getDocumentCount() > 0) {
			SearchRequest searchRequest = result.getSearchRequest();
			int start = searchRequest.getStart();
			int end = searchRequest.getStart() + result.getDocumentCount();
			float time=(float)(searchRequest.getFinalTime());
	%>
	<div class="ossnumfound"><%=result.getDocumentCount()%> documents found (<%=time/1000 %> seconds)</div>
	<ul style="margin: 0px; padding: 0px; list-style-type: none">
		<%
			for (int i = start; i < end; i++) {
					ResultDocument resultDocument = result.getDocument(i);
					
		%>
		<li>
		<%
					j = 0;
					for (RendererField rendererField : renderer.getFields()) {
						j++;
						String url = rendererField.getUrlField(resultDocument);
						if (url != null)
							if (url.length() == 0)
								url = null;
						FieldValueItem[] fieldValueItems = rendererField.getFieldValue(resultDocument);
						for (FieldValueItem fieldValueItem : fieldValueItems) {
		%>
			<div class="osscmnrdr ossfieldrdr<%=j%>">
			<% if (url != null) { %>
				<a target="_top" href="<%=url%>">
			<% } %>
			<%=fieldValueItem.getValue()%>
			<% if (url != null) { %>
				</a>
			<% } %>
			</div>
		<%
						} // end loop fieldValueItem	
				} // (end loop renderer fields)
		%>
		<br/>
		</li>
		<%
			} // (end loop documents)
		%>
	</ul>
	<table>
		<tr>
			<%
				for (int i = paging.getLeftPage(); i <= paging.getRightPage(); i++) {
			%>
			<td><a href="<%=getUrl.toString()%>&page=<%=i%>" class="osscmnrdr"><%=i%></a></td>
			<%
				} // (end loop paging)
			%>
		</tr>
	</table>
	<%
		} // (if result != null)
	%>
	<hr />
	<div align="right">
		<a href="http://www.open-search-server.com/" target="_blank">
	 				<label class="osscmnrdr">Enterprise Search Made Yours.</label></a>
	 			<img alt="OPENSEARCHSERVER" src=" images/oss_logo_32.png" style="vertical-align:bottom" />
	 </div>
<script type="text/javascript" src="js/opensearchserver.js"> </script>
</body>
</html>