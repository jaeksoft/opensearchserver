/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2013 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.webservice;

import java.util.TreeMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.jaeksoft.searchlib.util.InfoCallback;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "result")
@JsonInclude(Include.NON_NULL)
public class CommonResult implements InfoCallback {

	protected final static String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

	@XmlAttribute
	final public Boolean successful;

	@XmlElement
	public String info;

	@XmlElement
	public TreeMap<String, String> details;

	public CommonResult() {
		successful = null;
		info = null;
		details = null;
	}

	protected CommonResult(CommonResult result) {
		successful = result.successful;
		info = result.info;
		details = result.details == null ? null : new TreeMap<String, String>(
				result.details);
	}

	public CommonResult(Boolean successful, String info) {
		this.info = info;
		this.successful = successful;
	}

	@Override
	public void setInfo(String info) {
		this.info = info;
	}

	@Override
	@XmlTransient
	public String getInfo() {
		return info;
	}

	public CommonResult addDetail(String key, Object value) {
		if (value == null)
			return this;
		if (details == null)
			details = new TreeMap<String, String>();
		details.put(key, value.toString());
		return this;
	}

}
