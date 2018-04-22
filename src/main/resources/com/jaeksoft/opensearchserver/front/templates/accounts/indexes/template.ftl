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
    <style type="text/css" media="screen">
        #editor {
            position: relative;
            left: 0;
            right: 0;
            height: 1000px;
        }
    </style>
</head>
<body>
<#include '../../includes/nav.ftl'>
<#include '../../includes/messages.ftl'>
<div class="container-fluid">
    <nav aria-label="breadcrumb" role="navigation">
        <ol class="breadcrumb">
            <li class="breadcrumb-item"><a href="/accounts">Accounts</a></li>
            <li class="breadcrumb-item"><a href="/accounts/${account.name?url}">${account.name?html}</a></li>
            <li class="breadcrumb-item"><a href="/accounts/${account.name?url}/indexes">Indexes</a></li>
            <li class="breadcrumb-item">${indexName!?html}</li>
            <li class="breadcrumb-item">Templates</li>
            <li class="breadcrumb-item active" aria-current="page">${templatePath}</li>
        </ol>
    </nav>
</div>
<div class="container-fluid">
    <ul class="nav nav-pills nav-fill">
        <#list templates as tempPath, tempName>
            <#assign active = tempPath == templatePath>
            <li class="nav-item">
                <a class="nav-link<#if active> active</#if>"
                   href="/accounts/${account.name?url}/indexes/${indexName!?url}/templates/${tempPath?url}">${tempName?capitalize}</a>
            </li>
        </#list>
    </ul>
    <br/>
</div>
<div class="container-fluid">
    <form id="formEditor" method="post">
        <div id="editor" class="border">${htmlTemplate!?html}</div>
        <textarea name="editor" style="display: none;"></textarea>
        <br/>
        <div class="form-row">
            <div class="form-group col-md-2">
                <button class="btn btn-primary btn-block" name="action" value="template" type="submit">
                    Save
                </button>
            </div>
            <div class="col-md-3"></div>
            <div class="col-md-2">
                <a href="/accounts/${account.name?url}/indexes/${indexName?url}/view"
                   class="btn btn-info btn-block">View</a>
            </div>
            <div class="col-md-3"></div>
            <div class="form-group col-md-2">
                <button class="btn btn-warning btn-block" name="action" value="revertDefault" type="submit">
                    Revert default
                </button>
            </div>
        </div>
    </form>
    <br/>
</div>
<#include '../../includes/foot.ftl'>
<script src="/webjars/ace-builds/1.3.1/src-noconflict/ace.js" type="text/javascript" charset="utf-8"></script>
<script>
    var editor = ace.edit("editor");
    editor.setTheme("ace/theme/xcode");
    editor.session.setMode("ace/mode/ftl");
    $("#formEditor").submit(function (event) {
        var textarea = $('textarea[name="editor"]');
        textarea.val(editor.getSession().getValue());
    });
</script>
</body>
</html>