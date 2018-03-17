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
    <#include '../../../includes/head.ftl'>
</head>
<body>
<#include '../../../includes/nav.ftl'>
<#include '../../../includes/messages.ftl'>
<div class="container">
    <nav aria-label="breadcrumb" role="navigation">
        <ol class="breadcrumb">
            <li class="breadcrumb-item"><a href="/accounts">Accounts</a></li>
            <li class="breadcrumb-item"><a href="/accounts/${account.name?url}">${account.name?html}</a></li>
            <li class="breadcrumb-item">Crawler</li>
            <li class="breadcrumb-item active">Web</li>
        </ol>
    </nav>
</div>
<#if webCrawlRecords?has_content>
<div class="container">
    <form method="post">
        <table class="table table-hover">
            <thead class="thead-dark">
            <tr>
                <th>#</th>
                <th>Name</th>
                <th>Entry URL</th>
                <th>Depth</th>
                <th>Max</th>
                <th></th>
            </tr>
            </thead>
            <tbody>
       <#list webCrawlRecords as webCrawlRecord>
       <tr>
           <th>
               <div class="form-check">
                   <input class="form-check-input" type="checkbox" name="c"
                          value="${webCrawlRecord.uuid?html}">
               </div>
           </th>
           <th>${webCrawlRecord.name!?html}</th>
           <td>${webCrawlRecord.crawlDefinition.entryUrl!?html}</td>
           <td>${webCrawlRecord.crawlDefinition.maxDepth!}</td>
           <td>${webCrawlRecord.crawlDefinition.maxUrlNumber!}</td>
           <td align="right">
               <a href="/accounts/${account.name?url}/crawlers/web/${webCrawlRecord.uuid!}"
                  class=" btn btn-sm btn-secondary">Edit</a>
               <a href="/accounts/${account.name?url}/crawlers/web/${webCrawlRecord.uuid!}/tasks"
                  class=" btn btn-sm btn-info">Tasks</a>
           </td>
       </tr>
       </#list>
            </tbody>
        </table>
        <div class="card">
            <div class="card-body">
                <div class="form-inline">
                    <div class="form-group mb-2 mr-sm-2">
                        <label for="crawlAction" class="sr-only">Grouped crawl action</label>
                        <select id="crawlAction" class="form-control" name="action">
                            <option>Choose an action</option>
                            <option value="activate">ACTIVATE</option>
                            <option value="pause">PAUSE</option>
                            <option value="stop">STOP</option>
                        </select>
                    </div>
                    <div class="form-group mb-2 mr-sm-2">
                        <label for="indexList" class="sr-only">Index list</label>
                        <select class="form-control" id="indexList" name="index">
                            <option>Choose an index</option>
                     <#list indexes as index>
                            <option value="${index?html}">${index?html}</option>
                     </#list>
                        </select>
                    </div>
                    <button type="submit" class="btn btn-primary mb-2">Apply</button>
                </div>
            </div>
        </div>
    </form>
</div>
<br/>
</#if>
<div class="container">
    <form method="post">
        <div class="card">
            <div class="card-body">
                <h5 class="card-title">Add Web crawl</h5>
                <div class="form-row">
                    <div class="form-group col-md-2">
                        <input type="text" name="crawlName" class="form-control" placeholder="Crawl name"
                               aria-label="Crawl name">
                    </div>
                    <div class="form-group col-md-5">
                     <textarea class="form-control" name="entryUrl" rows="1" placeholder="Entry URL"
                               aria-label="Entry URL"></textarea>
                    </div>
                    <div class="form-group col-md-2">
                        <input type="number" name="maxDepth" class="form-control" placeholder="maxDepth"
                               aria-label="Max depth">
                    </div>
                    <div class="form-group col-md-2">
                        <input type="number" name="maxUrlNumber" class="form-control" placeholder="maxUrlNumber"
                               aria-label="Max URL number">
                    </div>
                    <div class="form-group col-md-1">
                        <button class="btn btn-primary" name="action" value="create" type="submit">Create</button>
                    </div>
                </div>
            </div>
        </div>
    </form>
</div>
<#include '../../../includes/foot.ftl'>
</body>
</html>