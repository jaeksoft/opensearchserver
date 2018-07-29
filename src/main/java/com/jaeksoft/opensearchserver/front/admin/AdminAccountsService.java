/*
 * Copyright 2017-2018 Emmanuel Keller / Jaeksoft
 *  <p>
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  <p>
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  <p>
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.jaeksoft.opensearchserver.front.admin;

import com.jaeksoft.opensearchserver.model.AccountRecord;
import com.jaeksoft.opensearchserver.model.PermissionLevel;
import com.jaeksoft.opensearchserver.model.PermissionRecord;
import com.jaeksoft.opensearchserver.services.AccountsService;
import com.jaeksoft.opensearchserver.services.PermissionsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Api
@Path("/accounts")
@RolesAllowed("admin")
@SwaggerDefinition(basePath = "/admin/ws", info = @Info(title = "Accounts administration", version = "v2.0.0"))
public class AdminAccountsService {

    private final AccountsService accountsService;
    private final PermissionsService permissionsService;

    public AdminAccountsService(final AccountsService accountsService, final PermissionsService permissionsService) {
        this.accountsService = accountsService;
        this.permissionsService = permissionsService;
    }

    @Path("/{account_name}")
    @POST
    @Produces({ MediaType.TEXT_PLAIN })
    public UUID createAccount(@PathParam("account_name") String name) {
        return accountsService.createAccount(name);
    }

    @Path("/{account_name}")
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    public AccountRecord getAccount(@PathParam("account_name") String name) {
        return accountsService.getExistingAccount(name);
    }

    @Path("/{account_name}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public boolean updateAccount(@PathParam("account_name") String name, final AccountRecord record) {
        final AccountRecord account = accountsService.getExistingAccount(name);
        return accountsService.update(account.getId(), b -> b.crawlNumberLimit(record.getCrawlNumberLimit())
            .recordNumberLimit(record.getRecordNumberLimit())
            .indexNumberLimit(record.getIndexNumberLimit())
            .storageLimit(record.getStorageLimit())
            .tasksNumberLimit(record.getTasksNumberLimit())
            .status(record.getStatus()));
    }

    @Path("/{account_name}/permissions")
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    public List<PermissionRecord> getPermissions(@PathParam("account_name") String name,
        @QueryParam("start") Integer start, @QueryParam("rows") Integer rows) {
        final AccountRecord account = accountsService.getExistingAccount(name);
        final List<PermissionRecord> records = new ArrayList<>();
        permissionsService.getPermissionsByAccount(account.getId(), start == null ? 0 : start,
            rows == null ? 100 : rows, records::add);
        return Collections.unmodifiableList(records);
    }

    @Path("/{account_name}/permissions/{user_id}/level/{permission_level}")
    @POST
    @Produces({ MediaType.TEXT_PLAIN })
    public boolean setPermissions(@PathParam("account_name") String name, @PathParam("user_id") UUID userId,
        @PathParam("permission_level") PermissionLevel level) {
        final AccountRecord account = accountsService.getExistingAccount(name);
        return permissionsService.setPermission(userId, account.getId(), level);
    }
}
