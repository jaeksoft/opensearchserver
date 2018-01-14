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
    <title>Web Crawl Status</title>
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
            <li class="breadcrumb-item">
                <a href="/index/${indexName!?url}/crawler/web/${webCrawlRecord.uuid!?url}">
                ${webCrawlRecord.name!webCrawlRecord.uuid!?html}
                </a>
            </li>
            <li class="breadcrumb-item active" aria-current="page">Status</li>
        </ol>
    </nav>
 <#include 'includes/messages.ftl'>
    <div class="card">
        <div class="card-body">
            <form method="post">
                <div class="form-row">
                    <div class="form-group col-md-9">
                        <label for="webCrawlName">Name</label>
                        <input type="text" class="form-control" id="webCrawlName" name="crawlName"
                               placeholder="Crawl name" value="${webCrawlRecord.name!?html}">
                    </div>
                    <div class="form-group col-md-3">
                        <label for="maxDepth">Maximum depth</label>
                        <input type="number" id="maxDepth" class="form-control" name="maxDepth"
                               placeholder="Maximum depth" aria-label="Max depth"
                               value="${webCrawlRecord.crawlDefinition.maxDepth!?html}">
                    </div>
                </div>
                <div class="form-group">
                    <label for="entryUrl">Entry URL</label>
                    <input type="url" class="form-control" id="group" name="entryUrl"
                           placeholder="Entry URL" value="${webCrawlRecord.crawlDefinition.entryUrl!?html}">
                </div>
                <div class="input-group">
                       <span class="input-group-btn">
                        <button class="btn btn-primary" name="action" value="save" type="submit">Save</button>
                    </span>
                </div>
            </form>
        </div>
        <div class="card-footer">
            <form method="post">
                <div class="input-group">
                    <input type="text" name="crawlName" class="form-control"
                           placeholder="Enter the name of the crawl to delete it"
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