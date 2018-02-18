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
    <title>Web crawlers - OpenSearchServer</title>
    <#include '../includes/head.ftl'>
</head>
<body>
    <#include '../includes/nav.ftl'>
<br/>
<div class="container">
    <nav aria-label="breadcrumb" role="navigation">
        <ol class="breadcrumb">
            <li class="breadcrumb-item">Crawler</li>
            <li class="breadcrumb-item active">Web</li>
        </ol>
    </nav>
 <#include '../includes/messages.ftl'>
</div>
<#if webCrawlRecords?has_content>
    <div class="container">
        <table class="table table-hover">
            <thead class="thead-dark">
            <tr>
                <th>Name</th>
                <th>Entry URL</th>
                <th>Depth</th>
                <th></th>
            </tr>
            </thead>
            <tbody>
           <#list webCrawlRecords as webCrawlRecord>
           <tr>
               <th>${webCrawlRecord.name!?html}</th>
               <td>${webCrawlRecord.crawlDefinition.entryUrl!?html}</td>
               <td>${webCrawlRecord.crawlDefinition.maxDepth!}</td>
               <td align="right">
                   <a href="/crawlers/web/${webCrawlRecord.uuid!}"
                      class=" btn btn-sm btn-secondary">Edit</a>
                   <a href="/crawlers/web/${webCrawlRecord.uuid!}/status"
                      class=" btn btn-sm btn-info">Status</a>
               </td>
           </tr>
           </#list>
            </tbody>
        </table>
    </div>
</#if>
<div class="container">
    <form method="post">
        <div class="form-row">
            <div class="form-group col-md-2">
                <input type="text" name="crawlName" class="form-control" placeholder="Crawl name"
                       aria-label="Crawl name">
            </div>
            <div class="form-group col-md-7">
                <input type="url" name="entryUrl" class="form-control" placeholder="Entry URL"
                       aria-label="Entry URL">
            </div>
            <div class="form-group col-md-2">
                <input type="number" name="maxDepth" class="form-control" placeholder="maxDepth"
                       aria-label="Max depth">
            </div>
            <div class="form-group col-md-1">
                <button class="btn btn-primary" name="action" value="create" type="submit">Create</button>
            </div>
        </div>
    </form>
</div>
<#include '../includes/foot.ftl'>
</body>
</html>