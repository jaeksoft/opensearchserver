<!DOCTYPE html>
<#if rendererUser??>
    <#assign login = rendererUser.username/>
</#if>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>OpenSearchServer</title>
    <link href="css/bootstrap.min.css" rel="stylesheet">
    <link href="css/renderer5.css" rel="stylesheet">
    <style type="text/css">
${renderer.css!}
</style>
</head>
<body>
<div class="container">
    <div id="oss-wrap">
        <div class="row">
            <div id="oss-header" class="col-xs-11">
            <#if renderer.authentication>
                <div id="oss-login" class="pull-right">
                    <br/>
                ${login}
                    <#if renderer.logout>
                        &nbsp;-&nbsp;<a href="${getUrl}>&logout">${renderer.logoutText!}</a>
                    </#if>
                </div>
            </#if>
            ${renderer.header!}
            </div>
            <div class="col-xs-1"></div>
        </div>
        <div class="row">
            <div class="col-xs-3">
                <div id="oss-facet">
                <#include 'facet.ftl'/>
                </div>
            </div>
            <div class="col-xs-8">
                <div id="oss-main">
                <#include 'form.ftl' />
				<#include 'numfound.ftl' />
				<#include 'doclist.ftl' />
                </div>
            </div>
            <div class="col-xs-1"></div>
        </div>
        <div id="oss-footer">
        ${renderer.footer!}
        </div>
        <div align="right" style="clear: both;">
            <a href="http://www.open-search-server.com/" target="_blank">
                <img alt="OpenSearchServer Logo" src="images/oss_logo_32.png" style="vertical-align: bottom"/>
            </a>
        </div>
    </div>
</div>
<script src="js/jquery-1.12.4.min.js"></script>
<script src="js/bootstrap.min.js"></script>
<script type="text/javascript" src="js/opensearchserver.js" charset="UTF-8"></script>
</body>
</html>