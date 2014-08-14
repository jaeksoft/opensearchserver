<%@ page import="com.jaeksoft.searchlib.result.AbstractResultSearch"%>
<%@ page import="com.jaeksoft.searchlib.facet.FacetField"%>
<%@ page import="com.jaeksoft.searchlib.facet.FacetList"%>
<%@ page import="com.jaeksoft.searchlib.facet.Facet"%>
<%@ page import="com.jaeksoft.searchlib.facet.FacetItem"%>
<%@ page import="java.net.URLEncoder"%>
<%
	AbstractResultSearch facetResult = (AbstractResultSearch) request
			.getAttribute("facetResult");
	String getUrl = (String) request.getAttribute("getUrl");
	FacetList facetList = null;
	if (facetResult != null)
		facetList = facetResult.getFacetList();
	if (facetList != null && facetList.getList().size() > 0) {
%>

<div class="osscmnrdr oss-facet ">
	<%
		for (Facet facet : facetList) {
	%>
	<div class="panel panel-default">
		<div class="panel-heading">
			<h3 class="panel-title text-capitalize"><%=facet.getFacetField().getName()%></h3>
		</div>
		<div class="panel-body">
			<ul style="list-style-type: none">
				<%
					for (FacetItem facetItem : facet) {
								String fq = facet.getFacetField().getName() + ":\""
										+ facetItem.getTerm() + '"';
								boolean current = fq.equals(request.getParameter("fq"));
								String filterUrl = getUrl;
								if (!current)
									filterUrl += "&amp;fq="
											+ URLEncoder.encode(fq, "UTF-8");
				%>
				<li><a href="<%=filterUrl%>"><%if (current) {%><strong><%}%><%=facetItem.getTerm()%> (<%=facetItem.getCount()%>)
				<%if (current) {%></strong><%}%></a></li>
				<%
					}
				%>
			</ul>
		</div>
	</div>
	<%
		}
	%>
</div>
<%
	}
%>