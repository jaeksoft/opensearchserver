/*
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2017 Emmanuel Keller / Jaeksoft
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
 */
package com.qwazr.crawler.web.robotstxt;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class RobotsTxt2 extends RobotsTxt {

	public RobotsTxt2(InputStream input) throws IOException {
		super(input, StandardCharsets.UTF_8);
	}
}
