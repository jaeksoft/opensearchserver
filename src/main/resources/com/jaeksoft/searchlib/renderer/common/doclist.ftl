<#if result?has_content>
    <#if result.documentCount gt 0>
        <#assign isJoin = renderer.fieldWithJoin/>
        <#assign searchRequest = result.request/>
        <#assign start = searchRequest.start/>
        <#assign end = searchRequest.start + result.documentCount - 1/>
    <div class="osscmnrdr oss-result">
        <#list start..end as i>
            <#assign mainResultDocument = result.getDocument(i) />
            <#if isJoin>
                <#assign joinResultDocuments = result.getJoinDocumentList(i)/>
            </#if>
            <#assign fieldPos = 0/>
            <#assign lastWasReplace = false/>
            <#assign lastFieldValue = []/>
            <#list renderer.fields as rendererField>
                <#if rendererField.replacePrevious>
                    <#if !lastWasReplace>
                        <#assign fieldPos = fieldPos - 1/>
                        <#assign lastWasReplace = true/>
                    </#if>
                    <#if lastFieldValues?has_content>
                        <#break/>
                    </#if>
                </#if>
                <#assign resultDocument = rendererField.getResultDocument(mainResultDocument,joinResultDocuments)/>
                <#assign fieldPos = fieldPos + 1/>
                <#assign widgetName = rendererField.widgetName />
                <#assign rendererWidget = rendererField.widget />
                <#assign fieldValues = rendererField.getFieldValue(resultDocument)! />
                <#assign lastFieldValues = fieldValues/>
                <#if fieldValues?has_content>
                    <#list fieldValues as  fieldValue>
                        <#assign rendererValue = fieldValue />
                        <div class="osscmnrdr ossfieldrdr${fieldPos}${rendererField.renderCssClass}">
                            <#include widgetName.templatePath />
                        </div>
                    </#list>
                <#else>
                    <#assign rendererValue = '' />
                    <div class="osscmnrdr ossfieldrdr${fieldPos}${rendererField.renderCssClass}">
                        <#include widgetName.templatePath />
                    </div>
                </#if>
            </#list>
            <br/>
        </#list>
        <#include 'paging.ftl'/>
    </div>
    </#if>
</#if>