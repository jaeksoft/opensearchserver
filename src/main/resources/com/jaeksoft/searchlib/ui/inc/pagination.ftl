[#-- Copyright 2015 OpenSearchServer Inc. Licensed under the Apache
License, Version 2.0 (the "License"); you may not use this file except
in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
law or agreed to in writing, software distributed under the License is
distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied. See the License for the specific
language governing permissions and limitations under the License. --]
<nav class="text-center">
	<ul class="pagination pagination-sm">
		[#if pagination.prev]
			<li><a href="?page=0" aria-label="First">
				<span aria-hidden="true">first</span></a></li>
			<li><a href="?page=${pagination.currentPage - 1}" aria-label="Previous">
				<span aria-hidden="true">prev</span></a></li>
		[/#if]
		[#list pagination.pages as page]
			[#if pagination.currentPage == page]
				<li class="active"><a href="?page=${page}">${page + 1}
					<span class="sr-only">(current)</span></a></li>
			[#else]
				<li><a href="?page=${page}">${page + 1}</a></li>
			[/#if]
		[/#list]
		[#if pagination.next]
			<li><a href="?page=${pagination.currentPage + 1}" aria-label="Next">
				<span aria-hidden="true">next</span></a></li>
			<li><a href="?page=${pagination.lastPageNumber}" aria-label="Last">
				<span aria-hidden="true">last</span></a></li>
		[/#if]
	</ul>
</nav>