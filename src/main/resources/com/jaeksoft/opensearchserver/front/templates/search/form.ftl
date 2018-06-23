<div class="container">
    <form id="oss-form" method="get">
        <div class="input-group mb-3">
            <input id="oss-keywords" type="text" class="form-control" aria-label="Search keywords" name="keywords"
                   value="${keywords!?html}" aria-describedby="search keywords">
            <div class="input-group-append">
            <#--
                <#if !lang??><#assign lang='en'></#if>
                <select id="oss-lang" class="custom-select" name="lang">
                    <option value="en" <#if lang == 'en'>selected</#if>>English</option>
                    <option value="de" <#if lang == 'de'>selected</#if>>German</option>
                    <option value="fr" <#if lang == 'fr'>selected</#if>>French</option>
                    <option value="it" <#if lang == 'it'>selected</#if>>Italian</option>
                </select>
                -->
            </div>
            <div class="input-group-append">
                <button class="btn btn-primary" type="submit">Search</button>
            </div>
        </div>
    </form>
</div>
<div class="container">
    <div id="oss-result"></div>
</div>
<#-- This javascript is used to load the search result in the oss-result div -->
<script>

    var opensearchserver = {

        search: function (start = 0) {
            var data = {
                keywords: $('#oss-keywords').val(),
                lang: $('#oss-lang').val()
            };
            console.log("OSS Search " + start);
            $('#oss-result').load('/search/${account.name?url}/${indexName?url}?start=' + start, data);
        }
    };

    $(function () {

        $('#oss-form').on('submit', function (e) {
            e.preventDefault();  //prevent form from submitting
            opensearchserver.search();
        });

    });
</script>