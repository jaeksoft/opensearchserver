<div class="osscmnrdr oss-paging text-center">
<#assign getSort = request.getParameter("sort")!/>
<#list paging.leftPage..paging.rightPage as i>
    <#assign current =  (i == paging.currentPage)/>
    &nbsp;<#if current><strong></#if>
    <a href="${getUrl}&amp;page=${i} <#if getSort?has_content>&amp;sort=${getSort}</#if>"
       class="osscmnrdr<#if current> oss-currentpage</#if>">${i}</a><#if current></strong></#if>&nbsp;
</#list>
</div>