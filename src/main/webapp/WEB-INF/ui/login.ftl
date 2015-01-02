<#-- Copyright 2015 OpenSearchServer Inc. Licensed under the Apache
License, Version 2.0 (the "License"); you may not use this file except
in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
law or agreed to in writing, software distributed under the License is
distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied. See the License for the specific
language governing permissions and limitations under the License. -->
<!DOCTYPE html>
<html lang="en">
<head>
	<#include "/inc/head.ftl">
</head>
<body>
	<#include "/inc/nav.ftl"/>
	<#include "/inc/messages.ftl"/>
	<div class="container">
		<div class="login text-center">
			<form id="form" name="form" method="post" role="form"
				class="vertical-form">
				<img alt="OpenSearchServer Logo"
					src="${request.contextPath}/images/oss_logo.png" />
				<h4 class="text-center">
					WELCOME TO <span class="text-info">OPENSEARCHSERVER</span>
				</h4>
				<br /> <input value="" name="login" placeholder="Login" type="text"
					autofocus="autofocus" class="form-control text-center"
					required="required" /><input name="password"
					placeholder="Password" type="password"
					class="form-control text-center" required="required" />
				<button type="submit" class="btn btn-primary btn-block">LOG
					IN</button>
			</form>
		</div>
	</div>
	<#include "/inc/foot.ftl"/>
</body>
</html>