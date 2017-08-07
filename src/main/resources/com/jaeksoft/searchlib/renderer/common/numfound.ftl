<#if result??>
    <#assign  count = result.numFound - result.collapsedDocCount/>
    <#assign  time =  result.timer.duration/>
<div class="osscmnrdr ossnumfound">${renderer.getResultFoundText(count)}
    (${time / 1000}s)
</div>
<br/>
</#if>