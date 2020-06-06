<#if isProduction>
<#-- In production we are using the min version, with precompiled JSX in /s/js/... -->
  <script src="/webjars/react/16.13.1/umd/react.production.min.js"></script>
  <script src="/webjars/react-dom/16.13.1/umd/react-dom.production.min.js"></script>
  <#list jsxs as jsx>
    <script src="/s/js/${jsx}.js"></script>
  </#list>
<#else>
<#-- In developement we are using inline JSX compilation with Babel -->
  <script src="/webjars/react/16.13.1/umd/react.development.js"></script>
  <script src="/webjars/react-dom/16.13.1/umd/react-dom.development.js"></script>
  <script type="module" src="https://cdn.jsdelivr.net/npm/react-ace@9.0.0/lib/index.min.js"></script>
  <#list jsxs as jsx>
    <script src="/s/js/${jsx}.js"></script>
  </#list>
  <script src="/s/js/bundle.js"></script>
</#if>
