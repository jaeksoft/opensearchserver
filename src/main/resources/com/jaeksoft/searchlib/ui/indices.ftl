[#-- Copyright 2015 OpenSearchServer Inc. Licensed under the Apache
License, Version 2.0 (the "License"); you may not use this file except
in compliance with the License. You may obtain a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
law or agreed to in writing, software distributed under the License is
distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied. See the License for the specific
language governing permissions and limitations under the License. --]
<!DOCTYPE html>
<html lang="en">
<head>
	[#include "/inc/head.ftl"]
</head>
<body>
	[#include "/inc/nav.ftl"]
	[#include "/inc/tabs.ftl"]
	[#include "/inc/messages.ftl"]
	<div class="container-fluid">
		<table class="table table-condensed table-hover table-striped">
			<tr>
				<th>Index name</th>
				<th>Loaded</th>
				<th>Doc count</th>
				<th>Updated</th>
				<th>Size</th>
				<th>Threads</th>
				<th>Action</th>
			</tr>
			[#list indexlist as item]
			<tr>
				<td><a class="btn btn-default btn-xs"
					href="?select=${item.indexName?url}&page=${pagination.currentPage}">
					${item.indexName}</a>
				</td>
				<td>${item.loaded?c}</td>
				<td>${item.numDocs!}</td>
				<td>[#if
					item.lastModified??]${item.lastModified?datetime}[/#if]</td>
				<td>${item.humanSize!}</td>
				<td>${item.threadCount!}</td>
				<td><a class="btn btn-info btn-xs"
					href="?info=${item.indexName?url}&page=${pagination.currentPage}">
						info
					</a>
					<a class="btn btn-danger btn-xs"
						href="?del=${item.indexName?url}&page=${pagination.currentPage}">
						delete
					</a>
				</td>
			</tr>
			[/#list]
		</table>
		[#include "/inc/pagination.ftl"]
	</div>
	[#include "/inc/foot.ftl"]
</body>
</html>