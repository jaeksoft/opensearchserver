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
	this.opensearchserver_server_url = server_url;
	this.opensearchserver_index_name = index_name;
	this.opensearchserver_login = login;
	this.opensearchserver_api_key = api_key;
	this.opensearchserver_search_template = search_template;
	var opensearchserver_div = document.getElementById('opensearchserver');
	OpenSearchServer.SearchControl.prototype
			.createSearchForm(opensearchserver_div);
	if (typeof OpenSearchServer.SearchControl.prototype.getParameter("q") != 'undefined') {
		this.opensearchserver_searchterm = OpenSearchServer.SearchControl.prototype
				.getParameter("q");
		OpenSearchServer.SearchControl.prototype
				.getRequest(opensearchserver_div);
	}

};
OpenSearchServer.SearchControl.prototype.createSearchForm = function(
		opensearchserver_div) {
	var q = null;
	if (typeof document.getElementById('q') != 'undefined')
		q = null;
	else
		q = document.getElementById('q').value;
	if (q == null
			&& typeof OpenSearchServer.SearchControl.prototype
					.getParameter("q") != 'undefined')
		q = OpenSearchServer.SearchControl.prototype.getParameter("q");
	var search_form = document.createElement('form');
	search_form.setAttribute('id', 'opensearchserver_form');
	search_form.setAttribute('name', 'opensearchserver_form');
	search_form.setAttribute('action', '');

	var search_input = document.createElement('input');
	search_input.setAttribute('type', 'text');
	search_input.setAttribute('class', 'span6');
	search_input.setAttribute('id', 'q');
	search_input.setAttribute('name', 'q');
	search_input.setAttribute('placeholder', 'OpenSearchServer search');
	search_input.setAttribute('style', 'height:30px;');
	if (q != null)
		search_input.setAttribute('value', q);

	var search_submit = document.createElement('input');
	search_submit.setAttribute('type', 'submit');
	search_submit.setAttribute('class', 'btn primary');
	search_submit.setAttribute('id', 'submit');
	search_submit.setAttribute('name', 'submit');
	search_submit.setAttribute('value', 'Submit');

	opensearchserver_div.appendChild(search_form);
	search_form.appendChild(search_input);
	search_form.appendChild(search_submit);
	search_form.onsubmit = function() {
		return true;
	};
};
OpenSearchServer.SearchControl.prototype.getParameter = function(name) {
	name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
	var regexS = "[\\?&]" + name + "=([^&#]*)";
	var regex = new RegExp(regexS);
	var results = regex.exec(window.location.href);
	if (results == null)
		return "";
	else
		return decodeURIComponent(results[1].replace(/\+/g, " "));
};
OpenSearchServer.SearchControl.prototype.getRequest = function(
		opensearchserver_div) {
	var q = document.getElementById('q').value;
	var start = null;
	if (!q)
		q = OpenSearchServer.SearchControl.prototype.getParameter("q");

	if (typeof OpenSearchServer.SearchControl.prototype.getParameter("p") != 'undefined') {
		start = OpenSearchServer.SearchControl.prototype.getParameter("p");
		start = start ? Math.max(0, start - 1) * 10 : 0;
	} else
		start = 0;
	var url = server_url + "/select?q=" + q + "&format=json&use=" + index_name
			+ "&qt=" + search_template + "&login=" + login + "&key=" + api_key
			+ "&start=" + start;

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

				OpenSearchServer.SearchControl.prototype.getResults(request,
						opensearchserver_div);

			} else
				alert(request.status);
		}
	};
};
OpenSearchServer.SearchControl.prototype.getResults = function(request,
		opensearchserver_div) {
	opensearchserver_div.innerHTML = '';
	OpenSearchServer.SearchControl.prototype
			.createSearchForm(opensearchserver_div);
	var json = eval('(' + request.responseText + ')');
	var found = json.response.result['numFound'];
	var collapsed_count = json.response.result['collapsedDocCount'];
	var time = json.response.result['time'] / 1000;
	var start = json.response.result['start'];
	var rows = json.response.result['rows'];
	var num_found = document.createElement('div');
	var end = rows;
	num_found.setAttribute('id', 'opensearchserver_numfound');
	num_found.innerHTML = found + ' documents found (' + time + ')';
	opensearchserver_div.appendChild(num_found);
	var i = 0;
	if (found < 1) {
		var title = json.response.result.doc.snippet[0]["value"];
		var content = json.response.result.doc.snippet[1]["value"];
		var url = json.response.result.doc.field[0]["value"];
		OpenSearchServer.SearchControl.prototype.createResultDivs(title,
				content, url, opensearchserver_div);
	} else {
		if (found - start <= rows) {
			end = found - start;
		}
		for (i = 0; i < end; i++) {
			var title = json.response.result.doc[i].snippet[0]["value"];
			var content = json.response.result.doc[i].snippet[1]["value"];
			var url = json.response.result.doc[i].field[0]["value"];
			OpenSearchServer.SearchControl.prototype.createResultDivs(title,
					content, url, opensearchserver_div);

		}

	}

	var paging_div = document.createElement('div');
	paging_div.setAttribute('id', 'opensearchserver_paging');
	opensearchserver_div.appendChild(paging_div);

	OpenSearchServer.SearchControl.prototype.createPaging(found, start, rows,
			paging_div, collapsed_count);
};

OpenSearchServer.SearchControl.prototype.createResultDivs = function(title,
		content, url, opensearchserver_div) {
	var div_title = document.createElement('div');
	div_title.setAttribute('id', 'opensearchserver_title');
	opensearchserver_div.appendChild(div_title);

	var hyperlink_url = document.createElement('a');
	hyperlink_url.setAttribute('id', 'opensearchserver_hyperlink_url');
	hyperlink_url.setAttribute('href', url);
	hyperlink_url.innerHTML = title;
	div_title.appendChild(hyperlink_url);

	var div_content = document.createElement('div');
	div_content.setAttribute('id', 'opensearchserver_content');
	div_content.innerHTML = content;
	opensearchserver_div.appendChild(div_content);

	var div_url = document.createElement('div');
	div_url.setAttribute('id', 'opensearchserver_url');
	div_url.innerHTML = url;
	opensearchserver_div.appendChild(div_url);

};
OpenSearchServer.SearchControl.prototype.createPaging = function(found, start,
		rows, paging_div, collapsed_count) {
	var total_result = found - collapsed_count;
	var num_rows = rows;
	var start_page = start;
	var q = OpenSearchServer.SearchControl.prototype.getParameter("q");
	var current_page = Math.floor(start_page / num_rows);
	var result_total = Math.ceil(total_result / num_rows);
	if (result_total > 1) {
		var low = current_page - (10 / 2);
		var high = current_page + (10 / 2 - 1);
		if (low < 0)
			high += low * -1;
		if (high > result_total) {
			low -= high - result_total;
		}
		var result_low = Math.max(low, 1);
		var result_high = Math.min(result_total, high);
		var result_prev = Math.max(current_page - 1, 0);

		if (current_page + 1 < result_high) {
			var result_next = Math.min(current_page + 1, result_total);
		}
		var i;
		for (i = result_low; i <= result_high; i++) {
			var url = (document.URL).split("?");
			var base_url = url[0];
			var paging_link = base_url + "?q=" + q + "&p=" + i + "&r=" + rows;
			var paging_url = document.createElement('a');
			paging_url.setAttribute('id', 'opensearchserver_paging_url');
			paging_url.setAttribute('href', paging_link);
			paging_url.innerHTML = i;
			paging_div.appendChild(paging_url);
		}
	}
};