<div class="osscmnrdr oss-input-div">
    <form class="form-horizontal" id="osssearchform" method="get"
          autocomplete="off" role="form" action="renderer">
    <#list hiddenParameterList as p>
        <#assign v = request.getParameter(p)!/>
        <#if v?has_content>
            <input type="hidden" name="${p}" value="${v}"/>
        </#if>
    </#list>
        <div class="form-group form-group-lg has-feedback">
            <input class="form-control input-lg" type="text" id="osssearchbox"
                   name="query" value="${query!?html}"
                   onkeyup="OpenSearchServer.autosuggest(event, '${autocompUrl}&query=', 'osssearchform', 'osssearchbox', 'ossautocomplete')"
                   autocomplete="off" placeholder="${renderer.searchButtonLabel}"> <span
                class="glyphicon glyphicon-search form-control-feedback"></span>
        </div>
    <#if renderer.sorts?has_content>
        <div id="osssort">
            <select name="sort" onchange="document.forms['osssearchform'].submit();">
                <#assign sort = request.getParameter("sort")!/>
                <#list renderer.sorts as rendererSort>
                    <#if sort.equals(rendererSort.sort)><#assign selected = "selected=selected"/></#if>
                    <option ${selected!} value="${rendererSort.sort}">${rendererSort.label}</option>
                </#list>
            </select>
        </div>
    </#if>
    </form>
    <div style="position: relative">
        <div id="ossautocomplete" class="osscmnrd" style="position: absolute;"></div>
    </div>
</div>