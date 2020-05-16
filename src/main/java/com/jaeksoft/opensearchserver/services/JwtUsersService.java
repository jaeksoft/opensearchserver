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

package com.jaeksoft.opensearchserver.services;

import com.jaeksoft.opensearchserver.model.ActiveStatus;
import com.jaeksoft.opensearchserver.model.PermissionRecord;
import com.jaeksoft.opensearchserver.model.UserRecord;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.qwazr.utils.LoggerUtils;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.concurrent.ThreadUtils;
import io.undertow.security.idm.Account;
import io.undertow.security.idm.Credential;

import javax.ws.rs.NotSupportedException;
import java.net.URI;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JwtUsersService implements UsersService {

    private final static Logger LOGGER = LoggerUtils.getLogger(JwtUsersService.class);

    private final JWSSigner signer;
    private final String sharedSecret;
    private final URI jwtUri;

    public JwtUsersService(ConfigService configService) throws KeyLengthException {
        sharedSecret =
            Objects.requireNonNull(configService.getJwtKey(), "The JwtKey configuration parameter is missing.");
        jwtUri =
            Objects.requireNonNull(configService.getJwtUri(), "The JwtUri configuration parameter is missing.");
        signer = new MACSigner(sharedSecret);
    }

    @Override
    public UserRecord getUserById(UUID userId) {
        return new JwtUserRecord(userId, null, null);
    }

    @Override
    public Map<UserRecord, PermissionRecord> getUsersByIds(List<PermissionRecord> permissions) {
        if (permissions == null || permissions.isEmpty())
            return Collections.emptyMap();
        final Map<UserRecord, PermissionRecord> results = new LinkedHashMap<>();
        permissions.forEach(
            permission -> results.put(new JwtUserRecord(permission.getUserId(), null, null), permission));
        return results;
    }

    @Override
    public boolean isSingleSignOn() {
        return true;
    }

    @Override
    public String getSingleSignOnRedirectUrl() {
        return jwtUri.toString();
    }

    @Override
    public Account verify(Account account) {
        return account;
    }

    @Override
    public Account verify(String token, Credential credential) {
        if (StringUtils.isBlank(token))
            return null;
        try {

            final SignedJWT signedJWT = SignedJWT.parse(token);

            final JWSVerifier verifier = new MACVerifier(sharedSecret);

            if (!signedJWT.verify(verifier)) {
                ThreadUtils.sleep(2, TimeUnit.SECONDS);
                return null;
            }

            final JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();

            final Date expirationTime = claimsSet.getExpirationTime();
            if (expirationTime == null || new Date().after(expirationTime))
                return null;

            final String email = claimsSet.getStringClaim("email");
            final String name = claimsSet.getStringClaim("name");
            final String id = claimsSet.getStringClaim("id");
            if (StringUtils.isBlank(id))
                return null;

            return new UserAccount(new JwtUserRecord(UUID.fromString(id), name, email));
        } catch (ParseException | JOSEException | IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, e, e::getMessage);
            return null;
        }
    }

    @Override
    public Account verify(Credential credential) {
        return null;
    }

    private static class JwtUserRecord implements UserRecord {

        private final UUID id;
        private final String name;
        private final String email;

        private JwtUserRecord(UUID id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        @Override
        public UUID getId() {
            return id;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getEmail() {
            return email;
        }

        @Override
        public ActiveStatus getStatus() {
            return ActiveStatus.ENABLED;
        }

        @Override
        public boolean matchPassword(String applicationSalt, String clearPassword) {
            throw new NotSupportedException("Matching password is not supported.");
        }

    }
}
