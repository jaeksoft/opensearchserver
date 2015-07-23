<%@ page import="com.jaeksoft.searchlib.renderer.Renderer"%>
<%@ page import="com.jaeksoft.searchlib.renderer.RendererSort"%>
<%@ page import="org.apache.commons.lang3.StringEscapeUtils"%>
<div class="osscmnrdr oss-input-div">
	<form class="form-horizontal" id="osssearchform" method="get"
		autocomplete="off" role="form" action="renderer">
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
		<div class="form-group form-group-lg has-feedback">
			<input class="form-control input-lg" type="text" id="osssearchbox"
				name="query" value="<%=StringEscapeUtils.escapeXml10(query)%>"
				onkeyup="OpenSearchServer.autosuggest(event, '<%=request.getAttribute("autocompUrl")%>&query=', 'osssearchform', 'osssearchbox', 'ossautocomplete')"
				autocomplete="off"
				placeholder="<%=renderer.getSearchButtonLabel()%>"> <span
				class="glyphicon glyphicon-search form-control-feedback"></span>
		</div>
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