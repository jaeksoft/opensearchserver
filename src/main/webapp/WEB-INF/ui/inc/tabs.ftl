[#-- Copyright 2015 OpenSearchServer Inc. Licensed under the Apache
License, Version 2.0 (the "License"); you may not use this file except
in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
law or agreed to in writing, software distributed under the License is
distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied. See the License for the specific
language governing permissions and limitations under the License. --]
<div class="container-fluid">
	<ul class="nav nav-tabs">
		<li role="presentation"[#if request.servletPath="/ui"] class="active"[/#if]>
			<a href="${request.contextPath}/ui">Indices</a></li>
		<li role="presentation"[#if request.servletPath="/ui/queries"] class="active"[/#if]>
			<a href="${request.contextPath}/ui/queries">Queries</a></li>
		<li role="presentation"[#if request.servletPath="/ui/analyzers"] class="active"[/#if]>
			<a href="${request.contextPath}/ui/analyzers">Analyzers</a></li>
		<li role="presentation"[#if request.servletPath="/ui/terms"] class="active"[/#if]>
			<a href="${request.contextPath}/ui/terms">Terms</a></li>
		<li role="presentation"
			[#if request.servletPath?starts_with('/ui/crawlers/')]class="dropdown active"[#else]class="dropdown"[/#if]>
			<a class="dropdown-toggle" data-toggle="dropdown" href="#" role="button"
				aria-expanded="false">Crawlers<span class="caret"></span></a>
			<ul class="dropdown-menu" role="menu">
				<li role="presentation"[#if request.servletPath="/ui/crawlers/web"] class="active"[/#if]>
					<a href="${request.contextPath}/ui/crawlers/web">Web</a></li>
				<li role="presentation"[#if request.servletPath="/ui/crawlers/files"] class="active"[/#if]>
					<a href="${request.contextPath}/ui/crawlers/files">Files</a></li>
				<li role="presentation"[#if request.servletPath="/ui/crawlers/databases"] class="active"[/#if]>
					<a href="${request.contextPath}/ui/crawlers/databases">Databases</a></li>
				<li role="presentation"[#if request.servletPath="/ui/crawlers/mailboxes"] class="active"[/#if]>
					<a href="${request.contextPath}/ui/crawlers/mailboxes">Mailboxes</a></li>
				<li role="presentation"[#if request.servletPath="/ui/crawlers/rest-ws"] class="active"[/#if]>
					<a href="${request.contextPath}/ui/crawlers/rest-ws">REST Web Services</a></li>
			</ul>
		</li>
		<li role="presentation"[#if request.servletPath="/ui/parsers"] class="active"[/#if]><a
			href="${request.contextPath}/ui/parsers">Parsers</a></li>
		<li role="presentation"[#if request.servletPath="/ui/replications"] class="active"[/#if]><a
			href="${request.contextPath}/ui/replications">Replications</a></li>
		<li role="presentation"[#if request.servletPath="/ui/jobs"] class="active"[/#if]><a
			href="${request.contextPath}/ui/jobs">Jobs</a></li>
	</ul>
	<br />
</div>