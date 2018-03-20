<div class="form-group col-md-12">
    <label for="inclusion">Included URL patterns</label>
    <div class="input-group">
        <input id="inclusion" class="form-control" type="url" aria-label="Inclusion pattern"
               aria-describedby="inclusion-plus">
        <div class="input-group-append">
            <button id="inclusion-plus" class="btn btn-outline-secondary" type="button">
                <span class="oi oi-plus"></span>
            </button>
        </div>
    </div>
    <small>Wildcard pattern: ? = 1 character, * = many characters.
        E.g.: http://www.qwazr.com/*
    </small>
</div>
<div id="inclusion-list" class="col-md-12">
<#if webCrawlRecord.crawlDefinition.inclusionPatterns?has_content>
    <#list webCrawlRecord.crawlDefinition.inclusionPatterns as inclusion>
    <div class="form-group">
        <div class="input-group">
            <input class="form-control" readonly name="inclusion" type="url" value="${inclusion?html}">
            <div class="input-group-append">
                <button class="inclusion-minus btn btn-outline-secondary" type="button">
                    <span class="oi oi-minus"></span>
                </button>
            </div>
        </div>
    </div>
    </#list>
</#if>
</div>