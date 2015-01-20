[#-- Copyright 2015 OpenSearchServer Inc. Licensed under the Apache
License, Version 2.0 (the "License"); you may not use this file except
in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
law or agreed to in writing, software distributed under the License is
distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied. See the License for the specific
language governing permissions and limitations under the License. --]
<nav class="navbar navbar-inverse">
	<div class="container-fluid">
		<div class="navbar-header">
			<button type="button" class="navbar-toggle collapsed"
				data-toggle="collapse" data-target="#oss-navbar">
				<span class="sr-only">Toggle navigation</span>
				<span class="icon-bar"></span>
				<span class="icon-bar"></span>
				<span class="icon-bar"></span>
			</button>
			<img alt="Brand" src="${request.contextPath}/images/oss_logo_48.png" />
		</div>
		<div class="collapse navbar-collapse" id="oss-navbar">
			<ul class="nav navbar-nav">
				<li[#if request.servletPath="/ui" ] class="active"[/#if]>
					<a href="${request.contextPath}/ui">${version}</a>
				</li>
				<li>
					<a href="http://www.opensearchserver.com/documentation">Documentation</a>
				</li>
				<li>
					<a href="http://www.opensearchserver.com/#support">Support</a>
				</li>
			</ul>
			[#if !session.noUsers]
			<ul class="nav navbar-nav navbar-right">
				[#if !session.logged]
				<li[#if request.servletPath="/ui/login"] class="active"[/#if]>
					<a href="${request.contextPath}/ui/login">Log in</a></li>
				[#else]
				<li>
					<a href="${request.contextPath}/ui/logout">Log out</a>
				</li>
				[/#if]
			</ul>
			[/#if]
		</div>
	</div>
</nav>