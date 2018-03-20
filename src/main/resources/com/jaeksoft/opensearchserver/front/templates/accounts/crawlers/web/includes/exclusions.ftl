<div class="form-group col-md-12">
    <label for="exclusion">Excluded URL patterns</label>
    <div class="input-group">
        <input id="exclusion" class="form-control" type="url" aria-label="Exclusion pattern"
               aria-describedby="exclusion-plus">
        <div class="input-group-append">
            <button id="exclusion-plus" class="btn btn-outline-secondary" type="button">
                <span class="oi oi-plus"></span>
            </button>
        </div>
    </div>
    <small>Wildcard pattern: ? = 1 character, * = many characters.
        E.g.: http://www.qwazr.com/apidocs/*
    </small>
</div>
<div id="exclusion-list" class="col-md-12">
<#if webCrawlRecord.crawlDefinition.exclusionPatterns?has_content>
    <#list webCrawlRecord.crawlDefinition.exclusionPatterns as exclusion>
        <div class="form-group">
            <div class="input-group">
                <input class="form-control" readonly name="exclusion" type="url" value="${exclusion?html}">
                <div class="input-group-append">
                    <button class="exclusion-minus btn btn-outline-secondary" type="button">
                        <span class="oi oi-minus"></span>
                    </button>
                </div>
            </div>
        </div>
    </#list>
</#if>
</div>