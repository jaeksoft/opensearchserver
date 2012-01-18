<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=utf-8"
	language="java"%>
<%@ page import="com.jaeksoft.searchlib.renderer.Renderer"%>
<%@ page import="com.jaeksoft.searchlib.renderer.RendererField"%>
<%@ page import="com.jaeksoft.searchlib.result.Result"%>
<%@ page import="com.jaeksoft.searchlib.request.SearchRequest"%>
<%@ page import="com.jaeksoft.searchlib.result.ResultDocument"%>
<%@ page import="com.jaeksoft.searchlib.schema.FieldValueItem"%>
<%@ page import="com.jaeksoft.searchlib.renderer.Paging"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="java.net.URLDecoder"%>
<%
	String[] hiddenParameterList = { "use", "name", "login", "key" };
	String query = request.getParameter("query");
	if (query == null)
		query = "";
	Renderer renderer = (Renderer) request.getAttribute("renderer");
	Result result = (Result) request.getAttribute("result");
	Paging paging = result == null ? null : new Paging(result, 10);
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<style type="text/css">
.osscmnrdr{
<%=renderer.getCommonStyle()==null?"":renderer.getCommonStyle()%>
}
.ossinputrdr{
<%=renderer.getInputStyle()==null?"":renderer.getInputStyle()%>
}
.ossbuttonrdr{
<%=renderer.getButtonStyle()==null?"":renderer.getButtonStyle()%>
}
<%
	int j = 0;
	for (RendererField rendererField : renderer.getFields()) {
		j++;
%>
.ossfieldrdr<%=j%>{
<%=rendererField.getStyle()==null?"":rendererField.getStyle()%>
}
<%
	}
%>
a:link{
<%=renderer.getAlink()==null?"":renderer.getAlink()%>
}
a:hover{
<%=renderer.getAhover()==null?"":renderer.getAhover()%>
}
a:visited{
<%=renderer.getAvisited()==null?"":renderer.getAvisited()%>
}
a:active{
<%=renderer.getAactive()==null?"":renderer.getAactive()%>
}
#ossautocomplete{
<%=renderer.getAutocompleteStyle()==null?"":renderer.getAutocompleteStyle()%>
}
#ossautocompletelist {
<%=renderer.getAutocompleteSelectedStyle()==null?"":renderer.getAutocompleteSelectedStyle()%>
}
.ossautocomplete_link {
<%=renderer.getAutocompleteLinkStyle()==null?"":renderer.getAutocompleteLinkStyle()%>
}
.ossautocomplete_link_over {
<%=renderer.getAutocompleteLinkHoverStyle()==null?"":renderer.getAutocompleteLinkHoverStyle()%>
}


</style>
</head>
<body>

	<form method="get">
		<%
			StringBuffer getUrl = new StringBuffer("?query=");
			getUrl.append(URLEncoder.encode(query, "UTF-8"));
			for (String p : hiddenParameterList) {
				String v = request.getParameter(p);
				if (v != null) {
					getUrl.append('&');
					getUrl.append(p);
					getUrl.append('=');
					getUrl.append(URLEncoder.encode(v, "UTF-8"));
		%>
		<input type="hidden" name="<%=p%>" value="<%=v%>" />
		<%
			}
			}
		%>
		
		 
		<input class="osscmnrdr ossinputrdr" size="60" type="text" id="query" name="query" value="<%=query%>" onkeyup="autosuggest(event)" autocomplete="off"/>
		<input class="osscmnrdr ossbuttonrdr"
			type="submit" value="<%=renderer.getSearchButtonLabel()%>" />
					<div id="ossautocomplete"></div>
	</form>
 
	<br/>
	<%
		if (result != null && result.getDocumentCount() > 0) {
			SearchRequest searchRequest = result.getSearchRequest();
			int start = searchRequest.getStart();
			int end = searchRequest.getStart() + result.getDocumentCount();
	%>
	<ul style="margin: 0px; padding: 0px; list-style-type: none">
		<%
			for (int i = start; i < end; i++) {
					ResultDocument resultDocument = result.getDocument(i);
					
		%>
		<li>
		<%
					j = 0;
					for (RendererField rendererField : renderer.getFields()) {
						j++;
						String url = rendererField.getUrlField(resultDocument);
						if (url != null)
							if (url.length() == 0)
								url = null;
						FieldValueItem[] fieldValueItems = rendererField.getFieldValue(resultDocument);
						for (FieldValueItem fieldValueItem : fieldValueItems) {
		%>
			<div class="osscmnrdr ossfieldrdr<%=j%>">
			<% if (url != null) { %>
				<a target="_top" href="<%=url%>">
			<% } %>
			<%=fieldValueItem.getValue()%>
			<% if (url != null) { %>
				</a>
			<% } %>
			</div>
		<%
						} // end loop fieldValueItem	
				} // (end loop renderer fields)
		%>
		<br/>
		</li>
		<%
			} // (end loop documents)
		%>
	</ul>
	<table>
		<tr>
			<%
				for (int i = paging.getLeftPage(); i <= paging.getRightPage(); i++) {
			%>
			<td><a href="<%=getUrl.toString()%>&page=<%=i%>" class="osscmnrdr"><%=i%></a></td>
			<%
				} // (end loop paging)
			%>
		</tr>
	</table>
	<%
		} // (if result != null)
	%>
	<hr />
	<div align="right">
		<a href="http://www.open-search-server.com/" target="_blank">
	 				<label class="osscmnrdr">Enterprise Search Made Yours.</label></a>
	 			<img alt="OPENSEARCHSERVER" src=" images/oss_logo_32.png" style="vertical-align:bottom" />
	 </div>
	 <script>
function getXmlHttpRequestObject() {
	if (window.XMLHttpRequest) {
		return new XMLHttpRequest();
	} else if (window.ActiveXObject) {
		return new ActiveXObject("Microsoft.XMLHTTP");
	} else {
		return null;
	}
}

var xmlHttp = getXmlHttpRequestObject();

function setAutocomplete(value) {
	var ac = document.getElementById('ossautocomplete');
	ac.innerHTML = value;
	return ac;
}

var selectedAutocomplete = 0;
var autocompleteSize = 0;

function getselectedautocompletediv(n) {
	return document.getElementById('autocompleteitem' + n);
}

function autosuggest(event) {
	var keynum  = null;
	if(window.event) { // IE
		keynum = event.keyCode;
	} else if(event.which) { // Netscape/Firefox/Opera
		keynum = event.which;
	}
	if (keynum == 38 || keynum == 40) {
		if (selectedAutocomplete > 0) {
			autocompleteLinkOut(selectedAutocomplete);
		}
		if (keynum == 38) {
			if (selectedAutocomplete > 0) {
				selectedAutocomplete--;
			}	
		} else if (keynum == 40) {
			if (selectedAutocomplete < autocompleteSize) {
				selectedAutocomplete++;
			}
		}
		if (selectedAutocomplete > 0) {
			var dv= getselectedautocompletediv(selectedAutocomplete);
			autocompleteLinkOver(selectedAutocomplete);
			setKeywords(dv.innerHTML);
		}
		return false;
	}
	
	if (xmlHttp.readyState != 4 && xmlHttp.readyState != 0)
		return;
	var str = escape(document.getElementById('query').value);
	if (str.length == 0) {
		setAutocomplete('');
		return;
	}
	var request_url = build_url(str);
	xmlHttp.open("GET", request_url, true);
	xmlHttp.onreadystatechange = handleAutocomplete; 
	xmlHttp.send(null);
	return true;
}
function build_url(query) {
	var login=getParameter("login");
	var build_url='autocompletion?use='+getParameter("use")+'&query=' + query;
	if(login !=null) {
		 build_url +='&login='+login;
	}
	return build_url;
}
function handleAutocomplete() {
	if (xmlHttp.readyState != 4)
		return;
	var ac = setAutocomplete('');
	var resp = xmlHttp.responseText;
	if (resp == null) {
		return;
	}
	if (resp.length == 0) {
		return;
	}
	var str = resp.split("\n");
	var content = '<div id="ossautocompletelist">';
	var i = 0;
	var end=(str.length)-1;
	for (i = 0; i < end; i++) {
		var j = i + 1;
		content += '<div id="autocompleteitem' + j + '" ';
		content += 'onmouseover="javascript:autocompleteLinkOver(' + j + ');" ';
		content += 'onmouseout="javascript:autocompleteLinkOut(' + j + ');" ';
		content += 'onclick="javascript:setsetKeywords_onClick(this.innerHTML);" ';
		content += 'class="ossautocomplete_link">' + str[i] + '</div>';
	}
	content += '</div>';
	ac.innerHTML = content;
	selectedAutocomplete = 0;
	autocompleteSize = str.length-1;
}
	
function autocompleteLinkOver(n) {
	if (selectedAutocomplete > 0) {
		autocompleteLinkOut(selectedAutocomplete);
	}
	var dv = getselectedautocompletediv(n);
	dv.className = 'ossautocomplete_link_over';
	selectedAutocomplete = n;
}

function autocompleteLinkOut(n) {
	var dv = getselectedautocompletediv(n);
	if(dv!=null) {
	dv.className = 'ossautocomplete_link';
	}
}
function setsetKeywords_onClick(value) {
	var dv = document.getElementById('query');
	if(dv !=null) {
		dv.value = value;
		dv.focus();
		setAutocomplete('');
	}
}
function setKeywords(value) {
	var dv = document.getElementById('query');
	if(dv !=null) {
	dv.value = value;
	dv.focus();
	}
}
function getParameter(name){
	name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
	var regexS = "[\\?&]" + name + "=([^&#]*)";
	var regex = new RegExp(regexS);
	var results = regex.exec(window.location.href);
	if (results == null)
		return "";
	else
		return decodeURIComponent(results[1].replace(/\+/g, " "));
}
</script>
</body>
</html>