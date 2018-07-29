<!DOCTYPE html>
<#--
   Copyright 2017-2018 Emmanuel Keller / Jaeksoft
   <p>
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
   <p>
   http://www.apache.org/licenses/LICENSE-2.0
   <p>
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
-->
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>User - OpenSearchServer</title>
    <link rel="stylesheet" href="/webjars/swagger-ui/3.17.4/swagger-ui.css">
    <script src="/webjars/swagger-ui/3.17.4/swagger-ui-bundle.js"></script>
</head>
<body>
<div id="swagger-ui"></div>
<script>
    window.onload = function () {
        // Build a system
        const ui = SwaggerUIBundle({
            url: "/admin/ws/swagger.json",
            dom_id: '#swagger-ui',
            deepLinking: true,
            presets: [
                SwaggerUIBundle.presets.apis,
            ],
            plugins: [
                SwaggerUIBundle.plugins.DownloadUrl
            ],
            layout: "BaseLayout"
        })
        window.ui = ui
    }
</script>
</body>
</html>