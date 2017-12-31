<!DOCTYPE html>
<#--
   Copyright 2017 Emmanuel Keller / Jaeksoft
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
    <title>Indexes</title>
    <#include 'includes/head.ftl'>
</head>
<body>
    <#include 'includes/nav.ftl'>
<br/>
<div class="container">
<#include 'includes/messages.ftl'>
    <nav aria-label="breadcrumb" role="navigation">
        <ol class="breadcrumb">
            <li class="breadcrumb-item active" aria-current="page">Indexes</li>
        </ol>
    </nav>
</div>
<#if indexes?has_content>
    <div class="container">
        <table class="table table-hover">
            <tbody>
            <#list indexes as index>
            <tr>
                <th>${index!?html}</th>
                <td align="right">
                    <a href="/index/${index!?html}" class=" btn btn-sm btn-info">Edit</a>
                    <a href="/index/${index!?html}/crawler/web" class=" btn btn-sm btn-info">Web Crawl</a>
                </td>
            </tr>
            </#list>
            </tbody>
        </table>
    </div>
</#if>
<div class="container">
    <form method="post">
        <div class="input-group">
            <input type="text" name="indexName" class="form-control" placeholder="Index name"
                   aria-label="Index name">
            <span class="input-group-btn">
                        <button class="btn btn-primary" name="action" value="create" type="submit">Create</button>
                    </span>
        </div>
    </form>
</div>
    <#include 'includes/foot.ftl'>
</body>
</html>