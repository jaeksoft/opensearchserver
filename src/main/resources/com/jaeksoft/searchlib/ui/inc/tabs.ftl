[#-- Copyright 2015 OpenSearchServer Inc. Licensed under the Apache
License, Version 2.0 (the "License"); you may not use this file except
in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
law or agreed to in writing, software distributed under the License is
distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied. See the License for the specific
language governing permissions and limitations under the License. --]
<div class="container-fluid">
	<ul class="nav nav-tabs nav-pills">
		<li role="presentation"[#if request.servletPath="/ui"] class="active"[/#if]>
			<a href="${request.contextPath}/ui">Cluster</a></li>
		<li role="presentation"[#if request.servletPath="/ui/indices"] class="active"[/#if]>
			<a href="${request.contextPath}/ui/indices">Indices</a></li>
		<li role="presentation"[#if request.servletPath="/ui/queries"] class="active"[/#if]>
			<a href="${request.contextPath}/ui/queries">Queries</a></li>
		<li role="presentation"[#if request.servletPath="/ui/analyzers"] class="active"[/#if]>
			<a href="${request.contextPath}/ui/analyzers">Analyzers</a></li>
		<li role="presentation"[#if request.servletPath="/ui/dictionaries"] class="active"[/#if]>
			<a href="${request.contextPath}/ui/dictionaries">Dictionaries</a></li>
		<li role="presentation"[#if request.servletPath="/ui/crawlers"] class="active"[/#if]>
			<a href="${request.contextPath}/ui/crawlers">Crawlers</a></li>
		<li role="presentation"[#if request.servletPath="/ui/parsers"] class="active"[/#if]><a
			href="${request.contextPath}/ui/parsers">Parsers</a></li>
		<li role="presentation"[#if request.servletPath="/ui/replications"] class="active"[/#if]><a
			href="${request.contextPath}/ui/replications">Replications</a></li>
		<li role="presentation"[#if request.servletPath="/ui/renderers"] class="active"[/#if]><a
			href="${request.contextPath}/ui/renderers">Renderers</a></li>
		<li role="presentation"[#if request.servletPath="/ui/jobs"] class="active"[/#if]><a
			href="${request.contextPath}/ui/jobs">Jobs</a></li>
		<li role="presentation"[#if request.servletPath="/ui/classifiers"] class="active"[/#if]><a
			href="${request.contextPath}/ui/classifiers">Classifiers</a></li>
		<li role="presentation"[#if request.servletPath="/ui/affinities"] class="active"[/#if]><a
			href="${request.contextPath}/ui/affinities">Affinities</a></li>
		<li role="presentation"[#if request.servletPath="/ui/users"] class="active"[/#if]>
			<a href="${request.contextPath}/ui/users">Users</a></li>
	</ul>
	<br />
</div>