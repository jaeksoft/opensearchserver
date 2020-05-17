<#if config.production>
  <script src="/webjars/react/16.13.1/umd/react.production.min.js"></script>
  <script src="/webjars/react-dom/16.13.1/umd/react-dom.production.min.js"></script>
  <script src="/s/js/navbar.js"></script>
  <script src="/s/js/app.js"></script>
<#else>
  <script src="/webjars/react/16.13.1/umd/react.development.js"></script>
  <script src="/webjars/react-dom/16.13.1/umd/react-dom.development.js"></script>
  <script src="/webjars/babel-standalone/6.14.0/babel.min.js"></script>
  <script src="/jsx/navbar.jsx" type="text/babel"></script>
  <script src="/jsx/app.jsx" type="text/babel"></script>
</#if>
