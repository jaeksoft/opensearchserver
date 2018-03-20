<!DOCTYPE html>
<#--
   Copyright 2017-2018 Emmanuel Keller / Jaeksoft
   <p>
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
   <p>
   http://www.apache.org/licenses/LICENSE-2.0
   <p>
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
-->
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Active Task planning - OpenSearchServer</title>
    <#include '../../includes/head.ftl'>
</head>
<body>
<#include '../../includes/nav.ftl'>
<#include '../../includes/messages.ftl'>
<div class="container">
    <nav aria-label="breadcrumb" role="navigation">
        <ol class="breadcrumb">
            <li class="breadcrumb-item"><a href="/accounts">Accounts</a></li>
            <li class="breadcrumb-item"><a href="/accounts/${account.name?url}">${account.name?html}</a></li>
            <li class="breadcrumb-item"><a href="/accounts/${account.name?url}/tasks">Tasks</a></li>
            <li class="breadcrumb-item active">Planning</li>
        </ol>
    </nav>
</div>
<div class="container">
    <ul class="nav nav-tabs">
        <li class="nav-item">
            <a class="nav-link" href="/accounts/${account.name?url}/tasks/">Active Tasks</a>
        </li>
        <li class="nav-item">
            <a class="nav-link active" href="/accounts/${account.name?url}/tasks/planning">Execution planning</a>
        </li>
    </ul>
    <br/>
    <#assign show_crawl = true>
    <#include 'includes/task_list.ftl'>
</div>
<#include '../../includes/foot.ftl'>
</body>
</html>