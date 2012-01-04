// JavaScript Document
var server_url='http://localhost';
var path='oss1.3';
var server_port='8081';
var use='web';
var qt='search';
function getXmlHttpRequestObject() {
	if (window.XMLHttpRequest) {
		return new XMLHttpRequest();
	} else if (window.ActiveXObject) {
		return new ActiveXObject("Microsoft.XMLHTTP");
	} else {
		return null;
	}
}

function build_url() {
	return server_url+":"+server_port+"/"+path+"/select?format=json&use="+use+"&qt="+qt+"&q=";
}

function httpRequest() {
 var q = document.getElementById('q');
 var url=build_url()+q.value;
 var request = getXmlHttpRequestObject();
  request.open("GET", url, true);
  request.send(null);

  request.onreadystatechange = function() {
    if (request.readyState == 4 ) {
      if (request.status == 200) {
	 	display_result(request.responseText);
	}
	else
	alert(request.status);
    }
  };
}
function display_result(request) {
 var numfound = document.getElementById('numfound');
 var result = document.getElementById('result');
 result.innerHTML='';
 var json=eval('('+request+')');
 var found=json.response.result['numFound'];
 var time=json.response.result['time']/1000;
  numfound.innerHTML=found+' documents found ('+time+')';
	var i=0;
	if(found<1) {
			var title=json.response.result.doc.snippet[0]["value"];
	    	var content=json.response.result.doc.snippet[1]["value"];
			var url=json.response.result.doc.field[0]["value"];
			result.innerHTML+="<div> <a href="+url+">"+title+"</a></div>";
			result.innerHTML+="<div>"+content+"</div>";
			result.innerHTML+="<div>"+url+"</div>";
	}
	else {
	for(i=0;i<found;i++) {
			var title=json.response.result.doc[i].snippet[0]["value"];
	    	var content=json.response.result.doc[i].snippet[1]["value"];
			var url_value=json.response.result.doc[i].field[0]["value"];
			result.innerHTML+="<div> <a href="+url_value+">"+title+"</a></div>";
			result.innerHTML+="<div>"+content+"</div>";
			result.innerHTML+="<div>"+url_value+"</div>";
			result.innerHTML+="<div style=\"margin-bottom:10px;\"></div>";

		}

	}
}