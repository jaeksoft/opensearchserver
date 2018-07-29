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
package com.jaeksoft.opensearchserver.front;

import com.jaeksoft.opensearchserver.Components;
import com.jaeksoft.opensearchserver.services.UsersService;
import com.qwazr.utils.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;

@WebServlet("/signin")
public class SigninServlet extends BaseServlet {

    private final Components components;

    private final static String TEMPLATE = "signin.ftl";

    public SigninServlet(final Components components) {
        this.components = components;
    }

    @Override
    protected ServletTransaction getServletTransaction(final HttpServletRequest request,
        final HttpServletResponse response) {
        final UsersService usersService = components.getUsersService();
        if (usersService.isSingleSignOn())
            return new SsoTransaction(usersService.getSingleSignOnRedirectUrl(), request, response);
        else
            return new FormTransaction(request, response);
    }

    private class FormTransaction extends ServletTransaction {

        private FormTransaction(final HttpServletRequest request, final HttpServletResponse response) {
            super(components.getFreemarkerTool(), request, response, false);
        }

        @Override
        protected String getTemplate() {
            return TEMPLATE;
        }

        public void signin() throws ServletException, IOException {
            request.login(request.getParameter("email"), request.getParameter("current-pwd"));
            final String url = request.getParameter("url");
            if (request.getUserPrincipal() != null) {
                addMessage(Message.Css.success, "Welcome Back !", null);
                response.sendRedirect(StringUtils.isBlank(url) ? "/accounts" : new URL(url).toString());
                return;
            }
            request.setAttribute("url", url);
            doGet();
        }
    }

    private class SsoTransaction extends ServletTransaction {

        final String ssoRedirect;

        private SsoTransaction(final String ssoRedirect, HttpServletRequest request, HttpServletResponse response) {
            super(null, request, response, false);
            this.ssoRedirect = ssoRedirect;
        }

        protected void doGet() throws IOException, ServletException {
            final String jwt = request.getParameter("jwt");
            if (!StringUtils.isBlank(jwt)) {
                request.login(request.getParameter("jwt"), StringUtils.EMPTY);
            }
            if (request.getUserPrincipal() != null) {
                final String url = request.getParameter("url");
                response.sendRedirect(StringUtils.isBlank(url) ? "/accounts" : new URL(url).toString());
                return;
            }
            response.sendRedirect(ssoRedirect);
        }
    }
}
