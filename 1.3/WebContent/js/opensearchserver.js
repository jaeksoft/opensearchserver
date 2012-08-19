/**
 * License Agreement for OpenSearchServer
 * 
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 * 
 * OpenSearchServer is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * OpenSearchServer is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * OpenSearchServer. If not, see <http://www.gnu.org/licenses/>.
 */
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
	var key=getParameter("key");
	var build_url='autocompletion?use='+getParameter("use")+'&query=' + query;
	if(login!=null && typeof (login) != 'undefined') {
		 build_url +='&login='+login;
	}
	if(login!=null && typeof (key) != 'undefined') {
		 build_url +='&key='+key;
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