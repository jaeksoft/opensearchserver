<#assign value = rendererWidget.getValue(rendererValue)/>
<#assign url = rendererField.getUrlField(resultDocument)/>
<#if value?has_content>
    <#if url?has_content>
    <a target="_top" href="${url}">${rendererValue}</a>
    <#else>
    ${value}
    </#if>
</#if>