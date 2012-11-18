<%@ page import="com.jaeksoft.searchlib.renderer.Renderer"%>
<%@ page import="com.jaeksoft.searchlib.result.AbstractResultSearch"%>
<%
	AbstractResultSearch result = (AbstractResultSearch) request
			.getAttribute("result");
	if (result != null) {
		Renderer renderer = (Renderer) request.getAttribute("renderer");
		int count = result.getNumFound()
				- result.getCollapsedDocCount();
		float time = (float) (result.getTimer().duration());
%>
<div class="osscmnrdr ossnumfound"><%=renderer.getResultFoundText(count)%>
	(<%=time / 1000%>s)
</div>
<%
	}
%>
