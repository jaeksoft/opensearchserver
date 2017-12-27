<!DOCTYPE html>
<!--
  ~ Copyright 2017 Emmanuel Keller / Jaeksoft
  ~ <p>
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~ <p>
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~ <p>
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
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
    <form class="form-inline" method="post">
        <div class="form-group mx-sm-3">
            <label for="indexName" class="sr-only">Index name</label>
            <input type="text" name="indexName" class="form-control" id="indexName" placeholder="index name">
        </div>
        <div class="form-group">
            <button type="submit" name="action" value="create" class="btn btn-primary">Create</button>
        </div>
    </form>
    <br/>
    <#if indexes?has_content>
        <table class="table">
            <thead>
            <tr>
                <th scope="col">#</th>
                <th scope="col">Index</th>
            </tr>
            </thead>
            <tbody>
            <#list indexes as index>
            <tr>
                <th scope="row">${index?counter}</th>
                <td><a href="/index/${index!?html}">${index!?html}</a></td>
            </tr>
            </#list>
            </tbody>
        </table>
    </#if>
</div>
    <#include 'includes/foot.ftl'>
</body>
</html>