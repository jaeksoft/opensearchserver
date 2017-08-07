<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#if rendererUser??>
    <#assign login = rendererUser.username/>
</#if>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-type" content="text/html; charset=UTF-8"/>
    <style type="text/css">
        body, html {
            margin: 0;
            padding: 0;
        }

        #oss-main {
            margin-left: ${renderer.facetWidth};
        }

        #oss-facet {
            float: left;
            width: ${renderer.facetWidth};
        }

        #oss-header {
            width: 100%;
        }

        #oss-footer {
            width: 100%;
            clear: both;
        }

        #oss-wrap {
            width: 100%;
            margin: 0 auto;
            min-width: 700px;
        }
        ${renderer.css!}
    </style>
    <title>OpenSearchServer</title>
</head>
<body>
<div id="oss-wrap">
    <div id="oss-header">
    ${renderer.header!}
    </div>
<#if renderer.authentication>
    <div id="oss-login">
    ${login}
        <#if renderer.logout>
            &nbsp;-&nbsp;<a href="${getUrl}>&logout">${renderer.logoutText!}</a>
        </#if>
    </div>
</#if>
    <div id="oss-facet">
    <#include 'facet.ftl'/>
    </div>
    <div id="oss-main">
    <#include 'form.ftl' />
    <#include 'numfound.ftl' />
    <#include 'doclist.ftl' />
    </div>
    <div id="oss-footer">
    ${renderer.footer!}
    </div>
    <div align="right" style="clear:both;">
        <a href="http://www.opensearchserver.com/" target="_blank">
            <img alt="OpenSearchServer Logo" src="images/oss_logo_32.png" style="vertical-align: bottom"/>
        </a>
    </div>
</div>
<script type="text/javascript" src="js/opensearchserver.js" charset="UTF-8"></script>
</body>
</html>