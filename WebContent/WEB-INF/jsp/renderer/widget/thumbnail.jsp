
<%
	String thumbnailUrl = (String) request.getAttribute("url");
	String thumbnail = (String) request.getAttribute("value");
	if (thumbnailUrl != null) {
%>
<a target="_top" href="<%=thumbnailUrl%>"> <%
 	}
 	if (thumbnail != null) {
 %> <img class="ossfieldrdr<%=request.getAttribute("css")%>"
	src="<%=thumbnail%>"> <%
 	}
 	if (thumbnailUrl != null) {
 %></a>
<%
	}
%>