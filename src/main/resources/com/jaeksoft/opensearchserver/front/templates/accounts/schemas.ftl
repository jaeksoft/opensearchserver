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
    <title>Schemas - OpenSearchServer</title>
    <#include '../includes/head.ftl'>
</head>
<body>
    <#include '../includes/nav.ftl'>
<br/>
<div class="container">
<#include '../includes/messages.ftl'>
    <nav aria-label="breadcrumb" role="navigation">
        <ol class="breadcrumb">
            <li class="breadcrumb-item active" aria-current="page">Schemas</li>
        </ol>
    </nav>
</div>
<#if schemas?has_content>
    <div class="container">
        <table class="table table-hover">
            <thead class="thead-dark">
            <tr>
                <th>Schema</th>
                <th></th>
            </tr>
            </thead>
            <tbody>
        <#list schemas as schema>
        <tr>
            <th>${accountId?html}</th>
            <td align="right">
                <a href="/accounts/${accountId?url}" class=" btn btn-sm btn-info">View</a>
            </td>
        </tr>
        </#list>
            </tbody>
        </table>
    </div>
</#if>
<#include '../includes/foot.ftl'>
</body>
</html>