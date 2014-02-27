<%@ page import="com.jaeksoft.searchlib.renderer.Renderer"%>
<%@ page import="com.jaeksoft.searchlib.renderer.RendererSort"%>
<div class="osscmnrdr oss-input-div">
	<form id="osssearchform" method="get" autocomplete="off"
		action="renderer">
		<%
			Renderer renderer = (Renderer) request.getAttribute("renderer");
			String query = (String) request.getAttribute("query");
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
		<input class="osscmnrdr ossinputrdr" size="60" type="text"
			id="osssearchbox" name="query" value="<%=query%>"
			onkeyup="OpenSearchServer.autosuggest(event, '<%=request.getAttribute("autocompUrl")%>&query=', 'osssearchform', 'osssearchbox', 'ossautocomplete')"
			autocomplete="off" /> <input class="osscmnrdr ossbuttonrdr"
			type="submit" value="<%=renderer.getSearchButtonLabel()%>" />
		<%
			if (renderer.getSorts().size() > 0) {
		%>
		<div id="osssort">
			<select name="sort"
				onchange="document.forms['osssearchform'].submit();">
				<%
					String sort = (String) request.getParameter("sort");
						if (sort == null)
							sort = "";
						for (RendererSort rendererSort : renderer.getSorts()) {
							String selected = sort.equals(rendererSort.getSort()) ? "selected=selected"
									: "";
				%>
				<option <%=selected%> value="<%=rendererSort.getSort()%>"><%=rendererSort.getLabel()%></option>
				<%
					}
				%>
			</select>
		</div>
		<%
			}
		%>
	</form>
	<div style="position: relative">
		<div id="ossautocomplete" class="osscmnrd" style="position: absolute;"></div>
	</div>
</div>