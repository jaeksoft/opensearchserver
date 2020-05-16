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

import com.jaeksoft.opensearchserver.Components;
import com.jaeksoft.opensearchserver.front.ServletTransaction;
import com.jaeksoft.opensearchserver.model.UserRecord;
import com.jaeksoft.opensearchserver.services.UsersService;
import com.qwazr.utils.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AdminUsersTransaction extends ServletTransaction {

    private final static String TEMPLATE = "admin/users.ftl";

    private final UsersService usersService;

    AdminUsersTransaction(final Components components,
                          final HttpServletRequest request,
                          final HttpServletResponse response) {
        super(components.getFreemarkerTool(), components.getConfigService(), request, response, false);
        usersService = components.getUsersService();
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    public String create() {
        final String userEmail = request.getParameter("userEmail");
        if (!StringUtils.isBlank(userEmail)) {
            final UUID userId = usersService.createUser(userEmail);
            return "/admin/users/" + userId;
        }
        return null;
    }

    @Override
    protected void doGet() throws IOException, ServletException {
        final int start = getRequestParameter("start", 0, null, null);
        final List<UserRecord> users = new ArrayList<>();
        request.setAttribute("count", usersService.getUsers(start, 20, users::add));
        request.setAttribute("users", users);
        super.doGet();
    }
}
