<div class="osscmnrdr oss-facet">
<#assign filterQueries =  session.getAttribute("filterQueries")/>
<#if filterQueries?has_content && filterQueries.termsFilterSet?has_content>
    <div class="panel panel-default">
        <div class="panel-heading">
            <h3 class="panel-title">${renderer.filtersTitleText!'Filters'}</h3>
        </div>
        <div class="panel-body">
            <ul style="list-style-type: none">
                <li><a href="${getUrl}&amp;fqc">${renderer.clearFiltersText!'Clear'}</a></li>
                <#list  filterQueries.termsFilterSet as fieldName>
                    <#list  filterQueries.getTermSet(fieldName) as term>
                        <#assign filterUrl = getUrl + filterQueries.getFilterParamTerm(true, fieldName, term)/>
                        <li><a href="${filterUrl}" title="${fieldName}"><strong>${term}</strong></a></li>
                    </#list>
                </#list>
            </ul>
        </div>
    </div>
</#if>
<#if renderer.filters?has_content>
    <#list renderer.filters as filter>
        <#assign fieldName = filter.fieldName!/>
        <div class="panel panel-default">
            <div class="panel-heading">
                <h3 class="panel-title">${filter.publicName!fieldName!}</h3>
            </div>
            <div class="panel-body">
                <ul style="list-style-type: none">
                    <#list filter.getFilterItems(facetResult) as filterItem>
                        <#assign current = filterQueries.contains(fieldName, filterItem) />
                        <#assign filterUrl = getUrl + filterQueries.getFilterParam(current, fieldName, filterItem) />
                        <li>
                            <a href="${filterUrl}">
                                <#if current><strong></#if>${filterItem.label}<#if current></strong></#if>
                            </a>
                        </li>
                    </#list>
                </ul>
            </div>
        </div>
    </#list>
</#if>
<#if facetResult??>
    <#assign facetList = facetResult.facetList.list/>
</#if>
<#if facetList?has_content>
    <#list facetList as facet>
        <#if !renderer.isFilterListReplacement(facet)>
            <#assign fieldName = facet.facetField.name/>
            <div class="panel panel-default">
                <div class="panel-heading">
                    <h3 class="panel-title text-capitalize">${fieldName}</h3>
                </div>
                <div class="panel-body">
                    <ul style="list-style-type: none">
                        <#list facet.list as facetItem>
                            <#assign current = filterQueries.contains(fieldName, facetItem)/>
                            <#assign filterUrl = getUrl + filterQueries.getFilterParam(current, fieldName, facetItem)/>
                            <li>
                                <a href="${filterUrl}">
                                    <#if current><strong></#if>${facetItem.key}
                                    (${facetItem.value.count})<#if current></strong></#if>
                                </a>
                            </li>
                        </#list>
                    </ul>
                </div>
            </div>
        </#if>
    </#list>
</#if>
</div>