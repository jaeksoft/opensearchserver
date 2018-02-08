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
    <title>Web Crawl</title>
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
<#-- WEB CRAWL STATUS -->
    <div class="card">
        <form method="post">
            <div class="card-body">
                <h5 class="card-title">Crawl status</h5>
                <#if webCrawlStatus??>
                <table class="table">
                    <tr>
                        <th>Start time</th>
                        <td><#if webCrawlStatus.startTime??>${webCrawlStatus.starTime?number_to_datetime}</#if></td>
                        <td></td>
                        <td></td>
                    </tr>
                    <tr>
                        <th>End time</th>
                        <td><#if webCrawlStatus.startTime??>${webCrawlStatus.endTime?number_to_datetime}</#if></td>
                        <td></td>
                        <td><</td>
                    </tr>
                </table>
                </#if>
            </div>
            <div class="card-footer text-right">
                    <#if webCrawlStatus?? && webCrawlStatus.endTime??>
                        <button class="btn btn-warning" name="action" value="stop" type="submit">Stop</button>
                    <#else>
                    <button class="btn btn-success" name="action" value="start" type="submit">Start</button>
                    </#if>
            </div>
        </form>
    </div>
    <br/>
<#-- WEB CRAWL EDITION -->
    <div class="card">
        <form method="post">
            <div class="card-body">
                <h5 class="card-title">Edit this web crawl</h5>
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
            </div>
            <div class="card-footer text-right">
                <button class="btn btn-primary" name="action" value="save" type="submit">Save</button>
            </div>
        </form>
    </div>
    <br/>

<#-- WEB CRAWL DELETION -->
    <div class="card">
        <div class="card-body">
            <h5 class="card-title">Delete this web crawl</h5>
            <p class="card-text text-danger">Once you delete a web crawl, there is no going back. Please be certain.</p>
            <form method="post">
                <div class="input-group">
                    <input type="text" name="crawlName" class="form-control"
                           placeholder="To delete it, enter the name of the crawl and click on the Delete button"
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