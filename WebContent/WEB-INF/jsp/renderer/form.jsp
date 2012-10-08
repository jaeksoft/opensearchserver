<%@ page import="com.jaeksoft.searchlib.renderer.Renderer"%>
<div class="osscmnrdr oss-input-div">
	<form method="get" autocomplete="off" action="renderer">
		<%
			Renderer renderer = (Renderer) request.getAttribute("renderer");
			String query = request.getParameter("query");
			if (query == null)
				query = "";
			String[] hiddenParameterList = (String[]) request
					.getAttribute("hiddenParameterList");
			for (String p : hiddenParameterList) {
				String v = request.getParameter(p);
				if (v != null) {
		%>
		<input type="hidden" name="<%=p%>" value="<%=v%>" />
		<%
			}
		%>
		<%
			}
		%>
		<input class="osscmnrdr ossinputrdr" size="60" type="text" id="query"
			name="query" value="<%=query%>" onkeyup="autosuggest(event)"
			autocomplete="off" /> <input class="osscmnrdr ossbuttonrdr"
			type="submit" value="<%=renderer.getSearchButtonLabel()%>" />
	</form>
	<div style="position: relative">
		<div id="ossautocomplete" class="osscmnrd" style="position: absolute;"></div>
	</div>
</div>