<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@page
	import="net.gisiinteractive.gipublish.controller.request.filters.GisiFilter"%>
	
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="fr">
<head>
<link rel='stylesheet' class='user' type='text/css'
	href='<%=request.getContextPath()%>/styles/gisi.css' />
</head>



<body class="bodyForm">

<div id="zoneLogin" style="text-align: center;">

<br />
<div class="gisiException" style="text-align: center;"><%=request.getAttribute(GisiFilter.EXCEPTION)%>

</div>


</body>
</html>