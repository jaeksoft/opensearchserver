<%@ page import="com.jaeksoft.searchlib.result.AbstractResultSearch"%>
<%@ page import="com.jaeksoft.searchlib.facet.FacetField"%>
<%@ page import="com.jaeksoft.searchlib.renderer.Renderer"%>
<%@ page import="com.jaeksoft.searchlib.renderer.filter.RendererFilter"%>
<%@ page
	import="com.jaeksoft.searchlib.renderer.filter.RendererFilterItem"%>
<%@ page
	import="com.jaeksoft.searchlib.renderer.filter.RendererFilterQueries"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="java.util.Map"%>
<%@ page import="java.util.Set"%>
<%@ page import="java.util.List"%>
<%
	Renderer renderer = (Renderer) request.getAttribute("renderer");
	AbstractResultSearch facetResult = (AbstractResultSearch) request
			.getAttribute("facetResult");
	RendererFilterQueries filterQueries = (RendererFilterQueries) session
			.getAttribute("filterQueries");
	String getUrl = (String) request.getAttribute("getUrl");
%>
<div class="osscmnrdr oss-facet">
	<%
		if (filterQueries != null && !filterQueries.isEmpty()) {
	%>
	<div class="panel panel-default">
		<div class="panel-heading">
			<h3 class="panel-title"><%=renderer.getFiltersTitleText()%></h3>
		</div>
		<div class="panel-body">
			<ul style="list-style-type: none">
				<li><a href="<%=getUrl + "&fqc"%>"><%=renderer.getClearFiltersText()%></a></li>
				<%
					for (String fieldName : filterQueries.getTermsFilterSet()) {
							for (String term : filterQueries.getTermSet(fieldName)) {
								String filterUrl = getUrl
										+ filterQueries.getFilterParamTerm(true,
												fieldName, term);
				%>
				<li><a href="<%=filterUrl%>" title="<%=fieldName%>"><strong><%=term%></strong></a></li>
				<%
					}
						}
				%>
			</ul>
		</div>
	</div>
	<%
		}
	%>
	<%
		List<RendererFilter> filters = renderer.getFilters();
		if (filters != null && filters.size() > 0) {
	%>
	<%
		for (RendererFilter filter : renderer.getFilters()) {
	%>
	<div class="panel panel-default">
		<div class="panel-heading">
			<h3 class="panel-title"><%=filter.getPublicName()%></h3>
		</div>
		<div class="panel-body">
			<ul style="list-style-type: none">
				<%
					String fieldName = filter.getFieldName();
							for (RendererFilterItem filterItem : filter
									.getFilterItems(facetResult)) {
								boolean current = filterQueries.contains(fieldName,
										filterItem);
								String filterUrl = getUrl;
								filterUrl += filterQueries.getFilterParam(current,
										fieldName, filterItem);
				%>
				<li><a href="<%=filterUrl%>"> <%
 	if (current) {
 %><strong> <%
 	}
 %><%=filterItem.getLabel()%> <%
 	if (current) {
 %>
					</strong> <%
 	}
 %>
				</a></li>
				<%
					}
				%>
			</ul>
		</div>
	</div>
	<%
		}
		}
	%>
	<%
		Map<String, Map<String, Long>> facetList = null;
		if (facetResult != null)
			facetList = facetResult.getFacetResults();
		if (facetList != null && facetList.size() > 0) {
			for (Map.Entry<String, Map<String, Long>> facet : facetList
					.entrySet()) {
				if (renderer.isFilterListReplacement(facet))
					continue;
				String fieldName = facet.getKey();
	%>
	<div class="panel panel-default">
		<div class="panel-heading">
			<h3 class="panel-title text-capitalize"><%=fieldName%></h3>
		</div>
		<div class="panel-body">
			<ul style="list-style-type: none">
				<%
					for (Map.Entry<String, Long> facetItem : facet.getValue()
									.entrySet()) {
								boolean current = filterQueries.contains(fieldName,
										facetItem);
								String filterUrl = getUrl;
								filterUrl += filterQueries.getFilterParam(current,
										fieldName, facetItem);
				%>
				<li><a href="<%=filterUrl%>"> <%
 	if (current) {
 %><strong> <%
 	}
 %><%=facetItem.getKey()%> (<%=facetItem.getValue()%>) <%
 	if (current) {
 %>
					</strong> <%
 	}
 %>
				</a></li>
				<%
					}
				%>
			</ul>
		</div>
	</div>
	<%
		}
		}
	%>
</div>