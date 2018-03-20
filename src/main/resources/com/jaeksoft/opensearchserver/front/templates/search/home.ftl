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
    <title>Search ${account.name?html}/${indexName?html} - OpenSearchServer</title>
    <#include '../includes/head.ftl'>
</head>
<body>
<br/>
<#include '../includes/messages.ftl'>
<div class="container">
    <form method="get">
        <div class="input-group mb-3">
            <input type="text" class="form-control" aria-label="Search keywords" name="keywords"
                   value="${keywords!?html}" aria-describedby="search keywords">
            <div class="input-group-append">
                <select class="custom-select" name="lang">
                    <option value="en" <#if lang == 'en'>selected</#if>>English</option>
                    <option value="de"<#if lang == 'de'>selected</#if>>German</option>
                    <option value="fr"<#if lang == 'fr'>selected</#if>>French</option>
                    <option value="it"<#if lang == 'it'>selected</#if>>Italian</option>
                </select>
            </div>
            <div class="input-group-append">
                <button class="btn btn-primary" type="submit">Search</button>
            </div>
        </div>
    </form>
</div>
<div class="container">
<#if numDocs?has_content && totalTime?has_content>
    <p class="text-muted small">
        <#if numDocs == 0>No document found<#elseif numDocs == 1>1 results<#elseif numDocs gt 1>${numDocs} results</#if>
        (${totalTime?string["0.##"]} secs)
    </p>
</#if>
</div>
<#if results?has_content>
    <div class="container">
        <hr/>
    <#list results as result>
        <p><a href="${result.url}">${result.title!'No title'}</a><br/>
            <span class="text-success small">${result.urlDisplay!}</span><br/>
            <span class="text-muted small">${result.description!result.content!}</span>
        </p>
        <hr/>
    </#list>
    </div>
    <#if paging?has_content>
    <div class="container">
        <form>
            <input type="hidden" name="keywords" value="${keywords?html}">
            <input type="hidden" name="lang" value="${lang?html}">
            <#if rows?has_content><input type="hidden" name="rows" value="${rows?c}"></#if>
         <#include '../includes/paging.ftl'>
        </form>
    </div>
    </#if>
</#if>
<#include '../includes/foot.ftl'>
</body>
</html>