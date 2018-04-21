<#-- Display messages if any -->
<#if messages?has_content>
<div class="container">
    <#list messages as message>
        <div class="alert alert-${message.css} alert-dismissible fade show" role="alert">
        <#if message.title?has_content><h4 class="alert-heading">${message.title!?html}</h4></#if>
        <#if message.message?has_content><p class="mb-0">${message.message?html}</p></#if>
            <button type="button" class="close" data-dismiss="alert" aria-label="Close">
                <span aria-hidden="true">&times;</span>
            </button>
        </div>
    </#list>
</div>
</#if>
<#-- Result number -->
<div class="container">
<#if numDocs?has_content && totalTime?has_content>
    <p class="text-muted small">
        <#if numDocs == 0>No document found<#elseif numDocs == 1>1 results<#elseif numDocs gt 1>${numDocs} results</#if>
        (${totalTime?string["0.##"]} secs)
    </p>
</#if>
</div>
<#-- Result list -->
<#if results?has_content>
<div class="container">
    <hr/>
    <#list results as result>
    <p><a href="${result.url}">${result.title!'No title'}</a><br/>
        <span class="text-success small">${result.urlDisplay!}</span><br/>
        <span class="text-muted small">${result.description!result.content!}</span>
    </p>
    <hr/>
    </#list>
</div>
</#if>
<#-- Pagination -->
<#if paging?has_content>
<div class="container">
    <form>
        <input type="hidden" name="keywords" value="${keywords?html}">
        <input type="hidden" name="lang" value="${lang?html}">
        <#if rows?has_content><input type="hidden" name="rows" value="${rows?c}"></#if>
        <nav aria-label="Pagination">
            <ul class="pagination justify-content-center">
                <#if paging.prev?has_content>
                <li class="page-item">
                    <button class="page-link" type="submit" name="start" value="${paging.prev.start?c}">
                        Previous
                    </button>
                </li>
                </#if>
                <#if paging.pages??>
                    <#list paging.pages as page>
                    <li class="page-item<#if page.current> active</#if>">
                        <button class="page-link" type="submit" name="start" value="${page.start?c}">
                            ${page.number?c}
                        </button>
                    </li>
                    </#list>
                </#if>
                <#if paging.next?has_content>
                <li class="page-item">
                    <button class="page-link" type="submit" name="start" value="${paging.next.start?c}">
                        Next
                    </button>
                </li>
                </#if>
            </ul>
        </nav>
    </form>
</div>
</#if>