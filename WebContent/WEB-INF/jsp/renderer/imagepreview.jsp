
<%
	String thumbnail = (String) request.getAttribute("thumbnail");
	String thumbnailUrl = (String) request
			.getAttribute("thumbnail_url");
	if (thumbnailUrl != null) {
%>
<a target="_top" href="<%=thumbnailUrl%>"> <%
 	}
 	if (thumbnail != null) {
 %> <img class="ossfieldrdr<%=request.getAttribute("thumbnail_css")%>"
	src="<%=thumbnail%>"> <%
 	}
 	if (thumbnailUrl != null) {
 %></a>
<%
	}
%>