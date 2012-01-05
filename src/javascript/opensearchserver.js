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

if (typeof (OpenSearchServer) == "undefined")
	OpenSearchServer = {};
OpenSearchServer.SearchControl = function(server_url, index_name, login,
		api_key, search_template) {
	this.server_url = server_url;
	this.index_name = index_name;
	this.login = login;
	this.api_key = api_key;
	this.search_template = search_template;
};
OpenSearchServer.SearchControl.prototype.getResults = function() {
	var q = document.getElementById('q');
	var url = this.server_url + "?q=" + q.value + "&format=json&use="
			+ this.index_name + "&qt=" + this.search_template;
	var request = null;
	if (window.XMLHttpRequest) {
		request = new XMLHttpRequest();
	} else if (window.ActiveXObject) {
		rquest = new ActiveXObject("Microsoft.XMLHTTP");
	} else {
		return null;
	}
	request.open("GET", url, true);
	request.send(null);

	request.onreadystatechange = function() {
		if (request.readyState == 4) {
			if (request.status == 200) {
				var numfound = document.getElementById('numfound');
				var result = document.getElementById('result');
				var json = eval('(' + request.responseText + ')');
				var found = json.response.result['numFound'];
				var time = json.response.result['time'] / 1000;
				result.innerHTML = '';
				numfound.innerHTML = '';
				numfound.innerHTML = found + ' documents found (' + time + ')';
				var i = 0;
				if (found < 1) {
					var title = json.response.result.doc.snippet[0]["value"];
					var content = json.response.result.doc.snippet[1]["value"];
					var url = json.response.result.doc.field[0]["value"];
					result.innerHTML += "<div> <a href=" + url + ">" + title
							+ "</a></div>";
					result.innerHTML += "<div>" + content + "</div>";
					result.innerHTML += "<div>" + url + "</div>";

				} else {
					for (i = 0; i < found; i++) {
						var title = json.response.result.doc[i].snippet[0]["value"];
						var content = json.response.result.doc[i].snippet[1]["value"];
						var url_value = json.response.result.doc[i].field[0]["value"];
						result.innerHTML += "<div> <a href=" + url_value + ">"
								+ title + "</a></div>";
						result.innerHTML += "<div>" + content + "</div>";
						result.innerHTML += "<div>" + url_value + "</div>";
						result.innerHTML += "<div style=\"margin-bottom:10px;\"></div>";
					}
				}
			} else
				alert(request.status);
		}
	};
};