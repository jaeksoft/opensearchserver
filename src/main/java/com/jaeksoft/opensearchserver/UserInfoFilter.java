/*
 * Copyright 2017-2020 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.opensearchserver;

import java.io.IOException;
import java.security.Principal;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.representations.AccessToken;

class UserInfoFilter extends HttpFilter {

    private final static String AUTH_HEADER_USER = "X-Authenticated-User";
    private final static String AUTH_HEADER_NAME = "X-Authenticated-Name";

    protected void doFilter(final HttpServletRequest req, final HttpServletResponse res, final FilterChain chain)
        throws IOException, ServletException {
        final Principal principal = req.getUserPrincipal();
        if (principal != null) {
            if (principal instanceof KeycloakPrincipal) {
                final KeycloakPrincipal keycloakPrincipal = (KeycloakPrincipal) principal;
                final AccessToken accessToken = keycloakPrincipal.getKeycloakSecurityContext().getToken();
                res.addHeader(AUTH_HEADER_USER, accessToken.getPreferredUsername());
                res.addHeader(AUTH_HEADER_NAME, accessToken.getName());
            }
        }
        chain.doFilter(req, res);
    }
}
