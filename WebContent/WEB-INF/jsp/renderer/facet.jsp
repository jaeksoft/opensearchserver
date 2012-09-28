<%@ page import="com.jaeksoft.searchlib.result.AbstractResultSearch"%>
<%@ page import="com.jaeksoft.searchlib.facet.FacetField"%>
<%@ page import="com.jaeksoft.searchlib.facet.FacetList"%>
<%@ page import="com.jaeksoft.searchlib.facet.Facet"%>
<%@ page import="com.jaeksoft.searchlib.facet.FacetItem"%>
<%
	AbstractResultSearch facetResult = (AbstractResultSearch) request
			.getAttribute("facetResult");
	String getUrl = (String) request.getAttribute("getUrl");
	FacetList facetList = null;
	if (facetResult != null)
		facetList = facetResult.getFacetList();
	if (facetList != null && facetList.getList().size() > 0) {
%>
<div class="osscmnrdr oss-facet">
	<%
		for (Facet facet : facetList) {
	%>
	<ul style="margin: 0px; padding: 0px; list-style-type: none">
		<li style="text-transform: capitalize;"><%=facet.getFacetField().getName()%></li>
		<%
			for (FacetItem facetItem : facet) {
		%>
		<li><a
			href="<%=getUrl%>&fq=<%=facet.getFacetField().getName()%>:<%=facetItem.getTerm()%>">
				<%=facetItem.getTerm()%> (<%=facetItem.getCount()%>)
		</a>
		<li>
			<%
				}
			%>
		
	</ul>
	<%
		}
	%>
</div>
<%
	}
%>
