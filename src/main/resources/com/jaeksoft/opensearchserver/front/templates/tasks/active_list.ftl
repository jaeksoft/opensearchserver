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
    <title>Active Tasks - OpenSearchServer</title>
    <#include '../includes/head.ftl'>
</head>
<body>
    <#include '../includes/nav.ftl'>
<br/>
<div class="container">
    <nav aria-label="breadcrumb" role="navigation">
        <ol class="breadcrumb">
            <li class="breadcrumb-item active">Tasks</li>
        </ol>
    </nav>
 <#include '../includes/messages.ftl'>
</div>
<div class="container">
    <div class="card">
        <div class="card-body">
            <h5 class="card-title">Active tasks</h5>
                <#assign show_crawl = true>
        <#include 'includes/task_list.ftl'>
        </div>
    </div>
</div>
<#include '../includes/foot.ftl'>
</body>
</html>