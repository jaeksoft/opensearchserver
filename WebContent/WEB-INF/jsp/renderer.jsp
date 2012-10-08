<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=utf-8" language="java"%>
<%@ page import="com.jaeksoft.searchlib.renderer.Renderer"%>
<%@ page import="com.jaeksoft.searchlib.result.AbstractResultSearch"%>
<%@ page import="com.jaeksoft.searchlib.request.SearchRequest"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="java.net.URLDecoder"%>
<% Renderer renderer = (Renderer) request.getAttribute("renderer"); %>
<html>
<head>
<style type="text/css">
body,html {
	margin: 0;
	padding: 0;
}
#oss-main {
	margin-left: <%=renderer.getFacetWidth()%>;
}
#oss-facet {
	float: left;
	width: <%=renderer.getFacetWidth()%>;
}
#oss-header {
	width: 100%;
}
#oss-footer {
	width: 100%;
	clear: both;
}
#oss-wrap {
	width: 100%;
	margin:0 auto;
	min-width: 700px;
}
<%=renderer.getFullCSS()%>
</style>
<title>OpenSearchServer</title>
<script type="text/javascript" src="js/opensearchserver.js"></script>
</head>
<body>
	<div id="oss-wrap">
		<div id="oss-header">
			<%=renderer.getHeader()%>
		</div>
		<div id="oss-facet">
				<jsp:include page="renderer/facet.jsp" />
				</div>
			<div id="oss-main">
			<jsp:include page="renderer/form.jsp" />
			<jsp:include page="renderer/numfound.jsp" />
			<jsp:include page="renderer/doclist.jsp" />
			</div>
		<div id="oss-footer">
			<%=renderer.getFooter()%>
		</div>
		<div align="right" style="clear:both;">
			<a href="http://www.open-search-server.com/" target="_blank"><img
				alt="OpenSearchServer Logo" src=" images/oss_logo_32.png"
				style="vertical-align: bottom" /></a>
		</div>
	</div>
</body>
</html>