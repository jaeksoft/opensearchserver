
<%
	String textUrl = (String) request.getAttribute("url");
	String text = (String) request.getAttribute("value");
	if (textUrl != null) {
%>
<a target="_top" href="<%=textUrl%>"><%=text%></a>
<%
	} else {
%><%=text%>
<%
	}
%>