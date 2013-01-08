	<%
		String thumbnail = (String) request.getAttribute("thumbnail");
		if (thumbnail != null) {
			%>
			<img class="ossfieldrdr<%= request.getAttribute("thumbnail_css") %>" src="<%= thumbnail %>">
			<% 
		}
	%>