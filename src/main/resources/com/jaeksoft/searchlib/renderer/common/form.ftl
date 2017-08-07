<div class="osscmnrdr oss-input-div">
    <form id="osssearchform" method="get" autocomplete="off"
          action="renderer">
    <#list hiddenParameterList as p>
        <#assign v = request.getParameter(p)!/>
        <#if v?has_content>
            <input type="hidden" name="${p}" value="${v}"/>
        </#if>
    </#list>
        <input class="osscmnrdr ossinputrdr" size="60" type="text"
               id="osssearchbox" name="query" value="${query!?html}"
               onkeyup="OpenSearchServer.autosuggest(event, '${autocompUrl}&query=', 'osssearchform', 'osssearchbox', 'ossautocomplete')"
               autocomplete="off"/> <input class="osscmnrdr ossbuttonrdr"
                                           type="submit" value="${renderer.searchButtonLabel}"/>
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