/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 *
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;


import org.apache.commons.net.util.SubnetUtils;
import org.apache.commons.net.util.SubnetUtils.SubnetInfo;

public class NetworksUtils {

	public final static SubnetInfo[] getSubnetArray(String ips)
			throws IOException {
		if (ips == null)
			return null;
		StringReader sr = new StringReader(ips);
		try {
			List<String> lines = IOUtils.readLines(sr);
			if (lines == null)
				return null;
			SubnetInfo[] subnetArray = new SubnetInfo[lines.size()];
			int i = 0;
			for (String line : lines) {
				line = line.trim();
				if (line.isEmpty())
					continue;
				if (line.indexOf('/') == -1)
					line = line + "/32";
				SubnetUtils subnetUtils = new SubnetUtils(line);
				subnetUtils.setInclusiveHostCount(true);
				subnetArray[i++] = subnetUtils.getInfo();
			}
			return subnetArray;
		} finally {
			IOUtils.closeQuietly(sr);
		}
	}

	public static void main(String[] args) throws IOException {
		for (SubnetInfo subnetInfo : getSubnetArray("78.235.189.98/32")) {
			System.out.println(subnetInfo.getAddressCount());
			System.out.println(subnetInfo.isInRange("78.235.189.98"));
		}
	}
}
