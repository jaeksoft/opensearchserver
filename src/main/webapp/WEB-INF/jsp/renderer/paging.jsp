<%@ page import="com.jaeksoft.searchlib.renderer.PagingSearchResult"%>
<div class="osscmnrdr oss-paging text-center">
	<%
		PagingSearchResult paging = (PagingSearchResult) request
				.getAttribute("paging");
		String getUrl = (String) request.getAttribute("getUrlFq");
		for (int i = paging.getLeftPage(); i <= paging.getRightPage(); i++) {
			 boolean current =  i == paging.getCurrentPage();
	%>
	&nbsp;<%if (current) {%><strong><%}%><a href="<%=getUrl.toString()%>&amp;page=<%=i%>"
		class="osscmnrdr<%if (current) {%> oss-currentpage <%}%>"><%=i%></a><%if (current) {%></strong><%}%>&nbsp;
	<%
		}
	%>
</div>