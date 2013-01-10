<%@ page contentType="text/html; charset=UTF-8" %> 
<%@ page import="com.jaeksoft.searchlib.renderer.Renderer"%>
<%@ page import="com.jaeksoft.searchlib.renderer.Viewer"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="java.net.URLDecoder"%>
<%
Renderer renderer = (Renderer) request.getAttribute("renderer");
Viewer viewer = (Viewer) request.getAttribute("viewer");
String uri = (String)request.getAttribute("uri");
%>
<!doctype html>
<html>
<head>
<title>OpenSearchServer</title>
<script type="text/javascript" src="js/pdf.js" charset="UTF-8"></script>
<script type="text/javascript">PDFJS.workerSrc = 'js/pdf.js';</script>
<script type="text/javascript">
PDFJS.getDocument('<%=uri%>').then(function(pdf) {
	  // Using promise to fetch the page
	  pdf.getPage(1).then(function(page) {
	    var scale = 1.5;
	    var viewport = page.getViewport(scale);

	    //
	    // Prepare canvas using PDF page dimensions
	    //
	    var canvas = document.getElementById('the-canvas');
	    var context = canvas.getContext('2d');
	    canvas.height = viewport.height;
	    canvas.width = viewport.width;

	    //
	    // Render PDF page into canvas context
	    //
	    var renderContext = {
	      canvasContext: context,
	      viewport: viewport
	    };
	    page.render(renderContext);
	  });
	});
</script>
</head>
<body>
	<canvas id="the-canvas" style="border:1px solid black;"/>
</body>
</html>