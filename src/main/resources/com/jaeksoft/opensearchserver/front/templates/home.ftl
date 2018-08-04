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
    <title>OpenSearchServer</title>
    <#include 'includes/head.ftl'>
</head>
<body>
    <#include 'includes/nav.ftl'>
<#include 'includes/messages.ftl'>
<div class="container">
    <h2>Welcome to OpenSearchServer 2.0 Alpha</h2>
    <p class="lead">Here are the current implemented features</p>
    <div class="row">
        <div class="col-md-4">
            <div class="card">
                <div class="card-body">
                    <h4 class="card-title">Search features</h4>
                </div>
                <ul class="list-group list-group-flush">
                    <li class="list-group-item text-success"><span class="oi oi-check"></span>
                        Multilingual Full Text Search
                    </li>
                    <li class="list-group-item text-success"><span class="oi oi-check"></span>
                        Text snippets with highlighting
                    </li>
                    <li class="list-group-item text-danger"><span class="oi oi-x"></span>
                        Facets &amp; filters
                    </li>
                    <li class="list-group-item text-danger"><span class="oi oi-x"></span>
                        Auto-completion with suggestions
                    </li>
                    <li class="list-group-item text-danger"><span class="oi oi-x"></span>
                        Spelling correction
                    </li>
                </ul>
            </div>
        </div>
        <div class="col-md-4">
            <div class="card">
                <div class="card-body">
                    <h4 class="card-title">WEB integration</h4>
                </div>
                <ul class="list-group list-group-flush">
                    <li class="list-group-item text-success"><span class="oi oi-check"></span>
                        HTML rendering with Javascript
                    </li>
                    <li class="list-group-item text-success"><span class="oi oi-check"></span>
                        Online template editor
                    </li>
                    <li class="list-group-item text-danger"><span class="oi oi-x"></span>
                        JSON/WebService
                    </li>
                </ul>
            </div>
        </div>
        <div class="col-md-4">
            <div class="card">
                <div class="card-body">
                    <h4 class="card-title">Crawling &amps Indexing</h4>
                </div>
                <ul class="list-group list-group-flush">
                    <li class="list-group-item text-success"><span class="oi oi-check"></span>
                        Web crawling with support of semantic tagging (JSON-LD)
                    </li>
                    <li class="list-group-item text-danger"><span class="oi oi-x"></span>
                        File crawling (FTP, FileSystems, Cloud vendors)
                    </li>
                    <li class="list-group-item text-danger"><span class="oi oi-x"></span>
                        Database crawling
                    </li>
                    <li class="list-group-item text-danger"><span class="oi oi-x"></span>
                        GitHub project crawling
                    </li>
                </ul>
            </div>
        </div>
    </div>
    <br/>
    <p class="text-center">
        <em><sup>*</sup> Missing features will be implemented in August</em>
    </p>
</div>
<#include 'includes/foot.ftl'>
</body>
</html>