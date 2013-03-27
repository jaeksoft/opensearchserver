/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.collapse;

public class CollapseParameters {

	public enum Mode {

		OFF(0, "off"),

		ADJACENT(2, "adjacent"),

		CLUSTER(3, "cluster");

		final public int code;
		final private String label;

		private Mode(int code, String label) {
			this.code = code;
			this.label = label;
		}

		public String getLabel() {
			return label;
		}

		public static Mode valueOf(int code) {
			for (Mode mode : values())
				if (mode.code == code)
					return mode;
			return OFF;
		}

		public static Mode valueOfLabel(String label) {
			if (label == null)
				return OFF;
			for (Mode mode : values())
				if (label.equals(mode.label))
					return mode;
			return OFF;
		}
	}

	public enum Type {

		OPTIMIZED(0, "optimized"),

		FULL(1, "full");

		final public int code;
		final private String label;

		private Type(int code, String label) {
			this.code = code;
			this.label = label;
		}

		public String getLabel() {
			return label;
		}

		public static Type valueOf(int code) {
			for (Type type : values())
				if (type.code == code)
					return type;
			return OPTIMIZED;
		}

		public static Type valueOfLabel(String label) {
			if (label == null)
				return OPTIMIZED;
			for (Type type : values())
				if (label.equals(type.label))
					return type;
			return OPTIMIZED;
		}

	}
}
