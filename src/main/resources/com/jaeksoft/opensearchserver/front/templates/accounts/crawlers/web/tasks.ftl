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
    <title>Web Crawl tasks - OpenSearchServer</title>
    <#include '../../../includes/head.ftl'>
</head>
<body>
<#include '../../../includes/nav.ftl'>
<#include '../../../includes/messages.ftl'>
<div class="container">
    <nav aria-label="breadcrumb" role="navigation">
        <ol class="breadcrumb">
            <li class="breadcrumb-item"><a href="/accounts/">Accounts</a></li>
            <li class="breadcrumb-item"><a href="/accounts/${account.id?url}">${account.name?html}</a></li>
            <li class="breadcrumb-item">Crawlers</li>
            <li class="breadcrumb-item"><a href="/accounts/${account.id?url}/crawlers/web">Web</a></li>
            <li class="breadcrumb-item">
                <a href="/accounts/${account.id?url}/crawlers/web/${webCrawlRecord.uuid?url}">
                ${webCrawlRecord.name!webCrawlRecord.uuid!?html}</a>
            </li>
            <li class=" breadcrumb-item active" aria-current="page">
                Tasks
            </li>
        </ol>
    </nav>
</div>
<#if tasks?has_content>
<div class="container">
    <div class="card">
        <div class="card-body">
            <h5 class="card-title">Active tasks</h5>
         <#assign show_crawl = false>
         <#include '../../tasks/includes/task_list.ftl'>
        </div>
    </div>
    <br/>
</div>
</#if>
<#if indexes?has_content>
<div class="container">
    <div class="card">
        <div class="card-body">
            <h5 class="card-title">Start a crawl</h5>
            <p class="card-text text-muted">Choose an index and click on the button to start the crawl
                process.</p>
        </div>
        <div class="card-footer">
            <form method="post" class="form-inline">
                <label for="indexList" class="sr-only">Index list</label>
                <select class="form-control mb-2 mr-sm-2" id="indexList" name="index">
                <#list indexes as index>
                    <option value="${index?html}">${index?html}</option>
                </#list>
                </select>
                <button class="btn btn-primary mb-2"
                        name="action" value="crawl" type="submit">
                    Start crawling
                </button>
            </form>
        </div>
    </div>
    <br/>
</div>
</#if>
<#-- START CRAWLING
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
    -->
<#include '../../../includes/foot.ftl'>
</body>
</html>