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
    <title>Indexes</title>
    <#include 'includes/head.ftl'>
</head>
<body>
    <#include 'includes/nav.ftl'>
<br/>
<div class="container">
    <nav aria-label="breadcrumb" role="navigation">
        <ol class="breadcrumb">
            <li class="breadcrumb-item"><a href="/">Indexes</a></li>
            <li class="breadcrumb-item"><a href="/index/${indexName!?url}">${indexName!?html}</a></li>
            <li class="breadcrumb-item">Crawler</li>
            <li class="breadcrumb-item"><a href="/index/${indexName!?url}/crawler/web">Web</a></li>
            <li class="breadcrumb-item active" aria-current="page">
            ${webCrawlRecord.name!webCrawlRecord.uuid!?html}
            </li>
        </ol>
    </nav>
 <#include 'includes/messages.ftl'>
    <div class="card">
        <div class="card-body">
            <ul class="list-group list-group-flush">
                <li class="list-group-item">Entry URL : ${webCrawlRecord.entryUrl!?html}</li>
                <li class="list-group-item">Max depth: ${webCrawlRecord.maxDepth!}</li>
            </ul>
        </div>
        <div class="card-footer">
            <form method="post">
                <div class="input-group">
                    <input type="text" name="crawlName" class="form-control" placeholder="Crawl name"
                           aria-label="Crawl name">
                    <span class="input-group-btn">
                        <button class="btn btn-danger" name="action" value="delete" type="submit">Delete</button>
                    </span>
                </div>
            </form>
        </div>
    </div>
</div>
<#include 'includes/foot.ftl'>
</body>
</html>