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
import com.jaeksoft.opensearchserver.front.BaseServlet;
import com.jaeksoft.opensearchserver.front.ServletTransaction;
import com.jaeksoft.opensearchserver.front.StaticTransaction;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/admin")
public class AdminHomeServlet extends BaseServlet {

    private final static String TEMPLATE = "admin/home.ftl";

    private final Components components;

    public AdminHomeServlet(final Components components) {
        this.components = components;
    }

    @Override
    protected ServletTransaction getServletTransaction(HttpServletRequest request, HttpServletResponse response) {
        return new StaticTransaction(components, TEMPLATE, request, response, false);
    }
}
