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
    <title>Accounts - OpenSearchServer</title>
    <#include '../includes/head.ftl'>
</head>
<body>
<#include '../includes/nav.ftl'>
<#include '../includes/messages.ftl'>
<div class="container">
    <nav aria-label="breadcrumb" role="navigation">
        <ol class="breadcrumb">
            <li class="breadcrumb-item"><a href="/admin">Admin</a></li>
            <li class="breadcrumb-item active" aria-current="page">Accounts</li>
        </ol>
    </nav>
</div>
<#if accounts?has_content>
    <div class="container">
        <h3>${accounts.count!0} account(s)</h3>
    <#if accounts.records?has_content>
        <table class="table table-hover">
            <thead class="thead-dark">
            <tr>
                <th>Id</th>
                <th>Name</th>
                <th>Creation</th>
                <th></th>
            </tr>
            </thead>
            <tbody>
        <#list accounts.records as account>
        <tr>
            <td>${account.id?html}</td>
            <td>${account.name?html}</td>
            <td>${account.creationTime?number_to_datetime}</td>
            <td align="right">
                <a href="/admin/accounts/${account.name?url}" class=" btn btn-sm btn-info">Edit</a>
            </td>
        </tr>
        </#list>
            </tbody>
        </table>
    </#if>
    </div>
</#if>
<div class="container">
    <form method="post">
        <div class="input-group">
            <input type="name" name="accountName" class="form-control" placeholder="Account name"
                   aria-label="Account name">
            <div class="input-group-append">
                <button class="btn btn-primary"
                        name="action" value="create" type="submit">
                    Create
                </button>
            </div>
        </div>
    </form>
</div>
<#include '../includes/foot.ftl'>
</body>
</html>