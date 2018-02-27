<nav class="navbar navbar-expand-lg navbar-light bg-light">

    <a class="navbar-brand" href="/">
        <img src="/s/images/oss_logo_32.png" width="32" height="32" class="d-inline-block align-top"
             alt="OpenSearchServer 2.0 Alpha">
        OpenSearchServer 2.0&alpha;
    </a>

    <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarNav"
            aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
        <span class="navbar-toggler-icon"></span>
    </button>
    <div class="collapse navbar-collapse" id="navbarNav">
        <ul class="navbar-nav">
            <li class="nav-item<#if request.servletPath=='/accounts'> active</#if>">
                <a class="nav-link" href="/accounts">Accounts
                    <#if request.servletPath=='/accounts'><span class="sr-only">(current)</span></#if></a>
            </li>
        </ul>
    </div>
</nav>