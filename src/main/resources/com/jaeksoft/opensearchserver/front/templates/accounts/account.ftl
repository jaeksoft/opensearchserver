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
    <title>Account - OpenSearchServer</title>
    <#include '../includes/head.ftl'>
</head>
<body>
<#include '../includes/nav.ftl'>
<#include '../includes/messages.ftl'>
<div class="container">
    <nav aria-label="breadcrumb" role="navigation">
        <ol class="breadcrumb">
            <li class="breadcrumb-item"><a href="/accounts">Accounts</a></li>
            <li class="breadcrumb-item active" aria-current="page">${account.name?html}</li>
        </ol>
    </nav>
    <div class="row">
        <div class="col-md-4">
            <div class="card">
                <div class="card-body">
                    <h5 class="card-title">Indexes</h5>
                    <p class="card-text">Manage your indexes here.</p>
                    <a href="/accounts/${account.name?url}/indexes" class="btn btn-primary">Indexes</a>
                </div>
            </div>
        </div>
        <div class="col-md-4">
            <div class="card">
                <div class="card-body">
                    <h5 class="card-title">Web crawls</h5>
                    <p class="card-text">Manage your Web crawls here.</p>
                    <a href="/accounts/${account.name?url}/crawlers/web" class="btn btn-primary">Web crawls</a>
                </div>
            </div>
        </div>
        <div class="col-md-4">
            <div class="card">
                <div class="card-body">
                    <h5 class="card-title">Tasks</h5>
                    <p class="card-text">Manage your running tasks here.</p>
                    <a href="/accounts/${account.name?url}/tasks" class="btn btn-primary">Tasks</a>
                </div>
            </div>
        </div>
    </div>
</div>
<#include '../includes/foot.ftl'>
</body>
</html>