<%@ page import="com.jaeksoft.searchlib.renderer.PagingSearchResult"%>
<div class="osscmnrdr oss-paging">
	<%
		PagingSearchResult paging = (PagingSearchResult) request
				.getAttribute("paging");
		String getUrl = (String) request.getAttribute("getUrl");
		for (int i = paging.getLeftPage(); i <= paging.getRightPage(); i++) {
	%>
	&nbsp;<a href="<%=getUrl.toString()%>&amp;page=<%=i%>"
		class="osscmnrdr<%if (i == paging.getCurrentPage()) {%> oss-currentpage<%}%>"><%=i%></a>&nbsp;
	<%
		}
	%>
</div>