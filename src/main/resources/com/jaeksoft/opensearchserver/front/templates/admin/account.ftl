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
            <li class="breadcrumb-item"><a href="/admin">Admin</a></li>
            <li class="breadcrumb-item"><a href="/admin/accounts">Accounts</a></li>
            <li class="breadcrumb-item active" aria-current="page">${accountRecord.name!?html}</li>
        </ol>
    </nav>
</div>
<div class="container">
    <div class="card">
        <div class="card-body">
            <h5 class="card-title">Account Record</h5>
            <table class="table">
                <tr>
                    <th>ID</th>
                    <td>${accountRecord.id?html}</td>
                </tr>
                <tr>
                    <th>Name</th>
                    <td>${accountRecord.name?html}</td>
                </tr>
                <tr>
                    <th>Creation time</th>
                    <td>${accountRecord.creationTime?number_to_datetime}</td>
                </tr>
            </table>
        </div>
    </div>
    <br/>
</div>
<div class="container">
    <div class="card">
        <div class="card-body">
            <h5 class="card-title">Account status</h5>
            <p class="card-text">Current status: ${accountRecord.status!'DISABLED'}</p>
            <form method="post">
                <input type="hidden" name="action" value="updateStatus">
                <div class="btn-group" role="group" aria-label="USer status">
                    <button type="submit" name="status" value="disabled" class="btn btn-danger">Disabled</button>
                    <button type="submit" name="status" value="enabled" class="btn btn-success">Enabled</button>
                </div>
            </form>
        </div>
    </div>
    <br/>
</div>
<div class="container">
    <div class="card">
        <div class="card-body">
            <h5 class="card-title">Users</h5>
                <#if accountRecord.accountIds?has_content>
                    <form method="post">
                        <input type="hidden" name="action" value="removeAccount">
                        <table class="table">
                            <tr>
                                <th>Account ID</th>
                                <th>Action</th>
                            </tr>
                            <#list userRecord.accountIds as accountId>
                            <tr>
                                <td>${accountId?html}</td>
                                <td>
                                    <button class="btn btn-sm btn-danger" type="submit" name="account"
                                            value="${accountId?html}">Remove
                                    </button>
                                </td>
                            </tr>
                            </#list>
                        </table>
                    </form>
                </#if>
        </div>
        <div class="card-footer">
            <form method="post">
                <div class="input-group">
                    <input type="text" name="account" class="form-control" placeholder="Account ID"
                           aria-label="Account ID">
                    <div class="input-group-append">
                        <button class="btn btn-info" name="action" value="addAccount" type="submit">Add</button>
                    </div>
                </div>
            </form>
        </div>
    </div>
    <br/>
</div>
<div class="container">
    <div class="card">
        <div class="card-body">
            <h5 class="card-title">Account name</h5>
            <form class="form-group row" method="post">
                <div class="form-group col-md-10">
                    <label for="inputName" class="sr-only">Name</label>
                    <input name="accountName" type="text" id="inputName" class="form-control"
                           placeholder="Account name" required autofocus value="${accountRecord.name?html}">
                </div>
                <div class="form-group col-md-2">
                    <button class="btn btn-primary btn-block" type="submit" name="action" value="updateName">
                        Update Name
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>
<div class="container">
    <div class="card">
        <div class="card-body">
            <h5 class="card-title">Account limits</h5>
            <form method="post">
                <div class="form-row">
                    <div class="form-group col-md-2">
                        <label for="inputName">Crawl</label>
                        <input name="crawlNumberLimit" type="number" id="crawlNumberLimit" class="form-control"
                               value="${accountRecord.crawlNumberLimit?c}">
                    </div>
                    <div class="form-group col-md-2">
                        <label for="inputName">Tasks</label>
                        <input name="tasksNumberLimit" type="number" id="tasksNumberLimit" class="form-control"
                               value="${accountRecord.tasksNumberLimit?c}">
                    </div>
                    <div class="form-group col-md-2">
                        <label for="inputName">Indexes</label>
                        <input name="indexNumberLimit" type="number" id="indexNumberLimit" class="form-control"
                               value="${accountRecord.indexNumberLimit?c}">
                    </div>
                    <div class="form-group col-md-2">
                        <label for="inputName">Records</label>
                        <input name="recordNumberLimit" type="number" id="recordNumberLimit" class="form-control"
                               value="${accountRecord.recordNumberLimit?c}">
                    </div>
                    <div class="form-group col-md-2">
                        <label for="inputName">Storage</label>
                        <input name="storageLimit" type="number" id="storageLimit" class="form-control"
                               value="${accountRecord.storageLimitMb?c}">
                    </div>
                </div>
                <div class="form-group">
                    <button class="btn btn-primary" type="submit" name="action" value="setLimits">
                        Update limits
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>
<#include '../includes/foot.ftl'>
</body>
</html>