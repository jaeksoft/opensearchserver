<nav aria-label="Pagination">
    <ul class="pagination justify-content-center">
    <#if paging.prev?has_content>
    <li class="page-item">
        <button class="page-link" type="submit" name="start" value="${paging.prev.start?c}">
            Previous
        </button>
    </li>
    </#if>
    <#list paging.pages as page>
    <li class="page-item<#if page.current> active</#if>">
        <button class="page-link" type="submit" name="start" value="${page.start?c}">
            ${page.number?c}
        </button>
    </li>
    </#list>
    <#if paging.next?has_content>
    <li class="page-item">
        <button class="page-link" type="submit" name="start" value="${paging.next.start?c}">
            Next
        </button>
    </li>
    </#if>
    </ul>
</nav>