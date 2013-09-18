/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2013 Emmanuel Keller / Jaeksoft
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

import com.jaeksoft.searchlib.collapse.CollapseFunction.FunctionConcat;
import com.jaeksoft.searchlib.collapse.CollapseFunction.FunctionCount;
import com.jaeksoft.searchlib.collapse.CollapseFunction.FunctionExecutor;
import com.jaeksoft.searchlib.collapse.CollapseFunction.FunctionMaximum;
import com.jaeksoft.searchlib.collapse.CollapseFunction.FunctionMinimum;

public class CollapseParameters {

	public static enum Mode {

		OFF("off"),

		ADJACENT("adjacent"),

		CLUSTER("cluster");

		final private String label;

		private Mode(String label) {
			this.label = label;
		}

		public String getLabel() {
			return label;
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

	public static enum Type {

		OPTIMIZED("optimized"),

		FULL("full");

		final private String label;

		private Type(String label) {
			this.label = label;
		}

		public String getLabel() {
			return label;
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

	public static enum Function {

		MIN("minimum", FunctionMinimum.class),

		MAX("maximum", FunctionMaximum.class),

		CONCAT("concatenate", FunctionConcat.class),

		COUNT("count", FunctionCount.class);

		final private String label;

		final private Class<? extends FunctionExecutor> executorClass;

		private Function(String label,
				Class<? extends FunctionExecutor> executorClass) {
			this.label = label;
			this.executorClass = executorClass;
		}

		public String getLabel() {
			return label;
		}

		public FunctionExecutor newExecutor() throws InstantiationException,
				IllegalAccessException {
			return executorClass.newInstance();
		}
	}

}
