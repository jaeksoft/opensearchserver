<#assign value = rendererWidget.getValue(rendererValue)!/>
<#if value?has_content>
    <#assign url = rendererField.getUrlField(resultDocument)!/>
    <#if url?has_content><a target="_top" href="<%=url%>"></#if>
    <img class="ossfieldrdr<%=fieldPos%>" src="<%=value%>">
    <#if url?has_content></a></#if>
</#if>