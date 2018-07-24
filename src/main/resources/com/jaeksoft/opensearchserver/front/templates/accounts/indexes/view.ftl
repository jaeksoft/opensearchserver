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
    <title>Index - OpenSearchServer</title>
    <#include '../../includes/head.ftl'>
    <link rel="stylesheet" href="/webjars/highlightjs/9.12.0/styles/github.css">
</head>
<body>
<#include '../../includes/nav.ftl'>
<#include '../../includes/messages.ftl'>
<div class="container">
    <nav aria-label="breadcrumb" role="navigation">
        <ol class="breadcrumb">
            <li class="breadcrumb-item"><a href="/accounts">Accounts</a></li>
            <li class="breadcrumb-item"><a href="/accounts/${account.name?url}">${account.name?html}</a></li>
            <li class="breadcrumb-item"><a href="/accounts/${account.name?url}/indexes">Indexes</a></li>
            <li class="breadcrumb-item">${indexName!?html}</li>
            <li class="breadcrumb-item active" aria-current="page">view</li>
        </ol>
    </nav>
</div>
<div class="container">
    <div class="card">
        <div class="card-body">
            <h5 class="card-title">Get the code</h5>
            <p class="card-text">
                <button class="btn btn-sm btn-secondary float-right" data-clipboard-target="#oss-html">Copy</button>
                Copy the following code, and paste it into the search page in your site
            </p>
            <small>
                <pre><code class="html" id="oss-html">${htmlCode!?html}</code></pre>
            </small>
        </div>
    </div>
    <br/>
    <div id="oss">
        <#include '../../search/form.ftl'>
    </div>
    <script>
        $(function () {
            $('#oss').load('/search/${account.name?url}/${indexName?url}' + location.search);
        });
    </script>
<#include '../../includes/foot.ftl'>
    <script src="/webjars/clipboard/2.0.1/dist/clipboard.min.js"></script>
    <script src="/webjars/highlightjs/9.12.0/highlight.pack.js"></script>
    <script>
        $(function () {
            hljs.initHighlightingOnLoad();
            new ClipboardJS('button.btn');
        });
    </script>
</body>
</html>