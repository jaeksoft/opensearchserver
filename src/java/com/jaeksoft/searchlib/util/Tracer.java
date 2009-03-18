/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.util;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Tracer {

	public final static Tracer instance = new Tracer();

	private Logger logger;

	private Tracer() {
		logger = Logger.getLogger(getClass().getCanonicalName());
		logger.setLevel(Level.ALL);
	}

	public void method(String className, String... methods) {
		for (String method : methods) {
			logger.entering(className, method);
			logger.exiting(className, method);
		}
	}
}
