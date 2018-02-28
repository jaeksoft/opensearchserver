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
    <title>Sign In - OpenSearchServer</title>
    <#include 'includes/head.ftl'>
</head>
<body>
    <#include 'includes/nav.ftl'>
<div class="container">
    <#include 'includes/messages.ftl'>
    <br/>
    <div class="card">
        <div class="card-body">
            <h5 class="card-title">Please sign in</h5>
            <form class="form-group row" method="post">
                <div class="form-group col-md-6">
                    <label for="inputEmail" class="sr-only">Email</label>
                    <input name="email" type="email" id="inputEmail" class="form-control"
                           placeholder="Email address" value="${email!?html}"
                           required autofocus>
                    <input type="hidden" name="url" value="${url!?url}">
                </div>
                <div class="form-group col-md-3">
                    <label for="inputPassword" class="sr-only">Password</label>
                    <input name="current-pwd" type="password" id="inputPassword" class="form-control"
                           placeholder="Password" required>
                </div>
                <div class="form-group col-md-2">
                    <button class="btn btn-primary btn-block" type="submit" name="action" value="signin">
                        Sign in
                    </button>
                </div>
            </form>
        </div>
    </div>
<#--
<br/>
<div class="row">
    <div class="col-md-6">
        <div class="card">
            <div class="card-body">
                <h5 class="card-title">No account yet ?</h5>
                <a class="btn btn-info" href="/account/signup">Sign up</a>
            </div>
        </div>
    </div>
    <div class="col-md-6">
        <div class="card">
            <div class="card-body">
                <h5 class="card-title">Password forgotten ?</h5>
                <a class="btn btn-warning" href="/account/reset">Reset your password</a>
            </div>
        </div>
    </div>
</div>
-->
</div>
<#include 'includes/foot.ftl'>
</body>
</html>