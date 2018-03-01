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
<br/>
<div class="container">
<#include '../includes/messages.ftl'>
</div>
<div class="container">
    <div class="card">
        <div class="card-body">
            <h4 class="card-title">Account : ${account.name?html}</h4>
            <h6 class="card-subtitle mb-2 text-muted">Discover below the main sections</h6>
        </div>
    </div>
    <br/>
    <div class="row">
        <div class="col-md-4">
            <div class="card">
                <div class="card-body">
                    <h5 class="card-title">Indexes</h5>
                    <p class="card-text">Manage your indexes here.</p>
                    <a href="/accounts/${account.id?url}/indexes" class="btn btn-primary">Indexes</a>
                </div>
            </div>
        </div>
        <div class="col-md-4">
            <div class="card">
                <div class="card-body">
                    <h5 class="card-title">Web crawls</h5>
                    <p class="card-text">Manage your Web crawls here.</p>
                    <a href="/accounts/${account.id?url}/crawlers/web" class="btn btn-primary">Web crawls</a>
                </div>
            </div>
        </div>
        <div class="col-md-4">
            <div class="card">
                <div class="card-body">
                    <h5 class="card-title">Tasks</h5>
                    <p class="card-text">Manage your running tasks here.</p>
                    <a href="/accounts/${account.id?url}/tasks" class="btn btn-primary">Tasks</a>
                </div>
            </div>
        </div>
    </div>
</div>
    <#include '../includes/foot.ftl'>
</body>
</html>