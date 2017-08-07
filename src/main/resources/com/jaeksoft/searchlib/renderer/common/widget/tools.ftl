<#assign originalUrl = rendererField.getOriginalUrl(resultDocument)! />
<#assign fieldUrl = rendererField.getUrlField(resultDocument)! />
<#assign parm = false/>
<#assign viewerUrl = rendererResult.getViewerUrl(resultDocument, originalUrl)! />
<#if viewerUrl?has_content>
<a target="_top" href="${viewerUrl}">Viewer</a>
    <#assign  parm = true />
</#if>
<#assign openFolderUrl = rendererResult.getOpenFolderUrl(resultDocument, fieldUrl)! />
<#if openFolderUrl?has_content>
    <#if parm>   </#if><a target="_top" href="${openFolderUrl}">Open folder</a>
    <#assign  parm = true />
</#if>
<#assign openMailboxUrl = rendererResult.getOpenMailboxUrl(rendererWidget, fieldValues, fieldUrl)! />
<#if openMailboxUrl?has_content>
    <#if parm>   </#if><a target="_top" href="${openMailboxUrl}">Open mailbox</a>
</#if>