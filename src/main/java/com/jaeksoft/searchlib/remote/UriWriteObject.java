/**
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2008-2020 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.remote;

import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;

import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;

public class UriWriteObject extends UriHttp {

    public UriWriteObject(final int timeOut, URI uri, Externalizable object)
            throws IOException {
        super(timeOut);
        final HttpPut httpPut = new HttpPut(uri.toASCIIString());
        httpPut.setConfig(requestConfig);
        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (final ObjectOutputStream swo = new ObjectOutputStream(baos)) {
                swo.writeObject(object);
                swo.flush();
            }
            httpPut.setEntity(new ByteArrayEntity(baos.toByteArray()));
        }
        execute(httpPut);
    }

    public Externalizable getResponseObject() throws IOException, ClassNotFoundException {
        try (final ObjectInputStream sro = new ObjectInputStream(httpResponse.getEntity().getContent())) {
            return (Externalizable) sro.readObject();
        }
    }

    @Override
    public void close() {
        super.close();
    }
}
