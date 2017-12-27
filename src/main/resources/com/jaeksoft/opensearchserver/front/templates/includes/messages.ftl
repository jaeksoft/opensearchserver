<#if messages?has_content>
    <#list messages as message>
    <div class="alert alert-${message.css} alert-dismissible fade show" role="alert">
        <#if message.title?has_content><h4 class="alert-heading">${message.title!?html}</h4></#if>
        <#if message.message?has_content><p class="mb-0">${message.message?html}</p></#if>
        <button type="button" class="close" data-dismiss="alert" aria-label="Close">
            <span aria-hidden="true">&times;</span>
        </button>
    </div>
    </#list>
</#if>