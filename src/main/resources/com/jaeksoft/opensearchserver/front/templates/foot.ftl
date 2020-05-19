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
  <script src="/webjars/babel-standalone/6.14.0/babel.min.js"></script>
  <#list jsxs as jsx>
    <script src="/jsx/${jsx}.jsx" type="text/babel"></script>
  </#list>
</#if>
