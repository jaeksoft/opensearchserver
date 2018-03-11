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
    <#include '../includes/head.ftl'>
</head>
<body>
<#include '../includes/nav.ftl'>
<#include '../includes/messages.ftl'>
<div class="container">
    <nav aria-label="breadcrumb" role="navigation">
        <ol class="breadcrumb">
            <li class="breadcrumb-item"><a href="/admin">Admin</a></li>
            <li class="breadcrumb-item"><a href="/admin/users">Users</a></li>
            <li class="breadcrumb-item active" aria-current="page">${userRecord.email!?html}</li>
        </ol>
    </nav>
</div>
<div class="container">
    <div class="card">
        <div class="card-body">
            <h5 class="card-title">User Record</h5>
            <table class="table">
                <tr>
                    <th>ID</th>
                    <td>${userRecord.id?html}</td>
                </tr>
                <tr>
                    <th>Email</th>
                    <td>${userRecord.email?html}</td>
                </tr>
                <tr>
                    <th>Session start time</th>
                    <td>${userRecord.creationTime?number_to_datetime}</td>
                </tr>
            </table>
        </div>
    </div>
    <br/>
</div>
<div class="container">
    <div class="card">
        <div class="card-body">
            <h5 class="card-title">Update status</h5>
            <p class="card-text">Current status: ${userRecord.status!'DISABLED'}</p>
            <form method="post">
                <input type="hidden" name="action" value="updateStatus">
                <div class="btn-group" role="group" aria-label="USer status">
                    <button type="submit" name="status" value="disabled" class="btn btn-danger">Disabled</button>
                    <button type="submit" name="status" value="enabled" class="btn btn-success">Enabled</button>
                </div>
            </form>
        </div>
    </div>
    <br/>
</div>
<div class="container">
    <div class="card">
        <div class="card-body">
            <h5 class="card-title">Account list</h5>
                <#if accounts?has_content>
                    <form method="post">
                        <table class="table">
                            <tr>
                                <th>Account ID</th>
                                <th>Account Name</th>
                                <th>Permission</th>
                                <th></th>
                            </tr>
                            <#list accounts as account,permission>
                            <tr>
                                <td>${account.id?html}</td>
                                <td>${account.name?html}</td>
                                <td>${permission.level?html}</td>
                                <td>
                                    <form method="post">
                                        <input type="hidden" name="accountId" value="${account.id?html}">
                                        <button class="btn btn-sm btn-danger" type="submit" name="action"
                                                value="removePermission">Remove
                                        </button>
                                    </form>
                                </td>
                            </tr>
                            </#list>
                        </table>
                    </form>
                </#if>
        </div>
        <div class="card-footer">
            <form method="post">
                <div class="input-group">
                    <input type="text" name="accountName" class="form-control" placeholder="Account name"
                           aria-label="Account name">
                    <div class="input-group-append">
                        <select class="custom-select" name="level">
                            <option value="owner" selected>Owner</option>
                            <option value="admin">Admin</option>
                            <option value="write">Write</option>
                            <option value="read">Read</option>
                        </select>
                        <button class="btn btn-info" name="action" value="setPermission" type="submit">Add</button>
                    </div>
                </div>
            </form>
        </div>
    </div>
    <br/>
</div>
<div class="container">
    <div class="card">
        <div class="card-body">
            <h5 class="card-title">Update password</h5>
            <form class="form-group row" method="post">
                <div class="form-group col-md-5">
                    <label for="inputPassword1" class="sr-only">Password</label>
                    <input name="password1" type="password" id="inputPassword1" class="form-control"
                           placeholder="Password" required autofocus>
                </div>
                <div class="form-group col-md-5">
                    <label for="inputPassword2" class="sr-only">Password confirmation</label>
                    <input name="password2" type="password" id="inputPassword2" class="form-control"
                           placeholder="Confirm password" required>
                </div>
                <div class="form-group col-md-2">
                    <button class="btn btn-primary btn-block" type="submit" name="action" value="updatePassword">
                        Update Password
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>
<#include '../includes/foot.ftl'>
</body>
</html>