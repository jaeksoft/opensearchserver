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
    <title>Home admin - OpenSearchServer</title>
    <#include '../includes/head.ftl'>
</head>
<body>
<#include '../includes/nav.ftl'>
<#include '../includes/messages.ftl'>
<div class="container">
    <nav aria-label="breadcrumb" role="navigation">
        <ol class="breadcrumb">
            <li class="breadcrumb-item active" aria-current="page">Admin</li>
        </ol>
    </nav>
</div>
<div class="container">
    <div class="row">
        <div class="col-md-4">
            <div class="card">
                <div class="card-body">
                    <h5 class="card-title">Accounts</h5>
                    <p class="card-text">Manage accounts</p>
                    <a href="/admin/accounts" class="btn btn-primary">Accounts</a>
                </div>
            </div>
        </div>
        <div class="col-md-4">
            <div class="card">
                <div class="card-body">
                    <h5 class="card-title">Users</h5>
                    <p class="card-text">Manage users</p>
                    <a href="/admin/users" class="btn btn-primary">Users</a>
                </div>
            </div>
        </div>
        <div class="col-md-4">
            <div class="card">
                <div class="card-body">
                    <h5 class="card-title">Web Service</h5>
                    <p class="card-text">JSON API</p>
                    <a href="/admin/service" class="btn btn-primary">Web API</a>
                </div>
            </div>
        </div>
    </div>
</div>
<#include '../includes/foot.ftl'>
</body>
</html>