/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.analysis.filter;

import java.io.IOException;
import java.text.NumberFormat;

import org.apache.lucene.analysis.TokenStream;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.FilterFactory;

public class DegreesRadiansFilter extends FilterFactory {

	final private static NumberFormat getNumberFormat(int integer, int fraction) {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumIntegerDigits(integer);
		nf.setMinimumIntegerDigits(integer);
		nf.setMinimumFractionDigits(fraction);
		nf.setMaximumFractionDigits(fraction);
		return nf;
	}

	final private static NumberFormat getDegreesFormat() {
		return getNumberFormat(3, 5);
	}

	final private static NumberFormat getRadiansFormat() {
		return getNumberFormat(1, 7);
	}

	abstract private class CheckTokenFilter extends AbstractTermFilter {

		private NumberFormat nf;

		protected CheckTokenFilter(TokenStream input, NumberFormat nf) {
			super(input);
			this.nf = nf;
		}

		final protected Double checkValue(double min, double max) {
			try {
				Double number = new Double(getTerm());
				if (number < min || number > max)
					return null;
				return number;
			} catch (NumberFormatException e) {
				if (faultTolerant)
					return null;
				throw e;
			}
		}

		protected abstract Double checkNumber();

		final protected void createToken(double number) {
			createToken(nf.format(number));
		}

		@Override
		public boolean incrementToken() throws IOException {
			current = captureState();
			for (;;) {
				if (!input.incrementToken())
					return false;
				Double number = checkNumber();
				if (number == null)
					return false;
				createToken(number);
				return true;
			}
		}
	}

	public class CheckDegreesTokenFilter extends CheckTokenFilter {

		private CheckDegreesTokenFilter(TokenStream input, NumberFormat nf) {
			super(input, nf);
		}

		private CheckDegreesTokenFilter(TokenStream input) {
			super(input, getDegreesFormat());
		}

		@Override
		protected final Double checkNumber() {
			return checkValue(0, 360);
		}

	}

	public class DegreesToRadiansTokenFilter extends CheckDegreesTokenFilter {

		private DegreesToRadiansTokenFilter(TokenStream input) {
			super(input, getRadiansFormat());
		}

		@Override
		public final boolean incrementToken() throws IOException {
			current = captureState();
			for (;;) {
				if (!input.incrementToken())
					return false;
				Double degrees = checkNumber();
				if (degrees == null)
					return false;
				double radians = Math.toRadians(degrees);
				createToken(radians);
				return true;
			}
		}
	}

	public class CheckRadiansTokenFilter extends CheckTokenFilter {

		private double RADIANS_MAX = Math.PI * 2;

		private CheckRadiansTokenFilter(TokenStream input, NumberFormat nf) {
			super(input, nf);
		}

		private CheckRadiansTokenFilter(TokenStream input) {
			this(input, getRadiansFormat());
		}

		@Override
		protected final Double checkNumber() {
			return checkValue(0, RADIANS_MAX);
		}
	}

	public class RadiansToDegreeTokenFilter extends CheckRadiansTokenFilter {

		private RadiansToDegreeTokenFilter(TokenStream input) {
			super(input, getDegreesFormat());
		}

		@Override
		public final boolean incrementToken() throws IOException {
			current = captureState();
			for (;;) {
				if (!input.incrementToken())
					return false;
				Double radians = checkNumber();
				if (radians == null)
					return false;
				double degrees = Math.toDegrees(radians);
				createToken(degrees);
				return true;
			}
		}
	}

	private boolean faultTolerant = true;
	private int conversion = 0;

	@Override
	protected void initProperties() throws SearchLibException {
		super.initProperties();
		addProperty(ClassPropertyEnum.DEGREES_RADIANS_CONVERSION,
				ClassPropertyEnum.DEGREES_RADIANS_CONVERSION_LIST[0],
				ClassPropertyEnum.DEGREES_RADIANS_CONVERSION_LIST);
		addProperty(ClassPropertyEnum.FAULT_TOLERANT,
				ClassPropertyEnum.BOOLEAN_LIST[0],
				ClassPropertyEnum.BOOLEAN_LIST);
	}

	@Override
	protected void checkValue(ClassPropertyEnum prop, String value)
			throws SearchLibException {
		if (prop == ClassPropertyEnum.DEGREES_RADIANS_CONVERSION) {
			conversion = 0;
			int i = 0;
			for (String v : ClassPropertyEnum.DEGREES_RADIANS_CONVERSION_LIST) {
				if (value.equals(v)) {
					conversion = i;
					break;
				}
				i++;
			}
		} else if (prop == ClassPropertyEnum.FAULT_TOLERANT)
			faultTolerant = Boolean.parseBoolean(value);
	}

	@Override
	public TokenStream create(TokenStream tokenStream) {
		switch (conversion) {
		default:
			return new DegreesToRadiansTokenFilter(tokenStream);
		case 1:
			return new RadiansToDegreeTokenFilter(tokenStream);
		case 2:
			return new CheckDegreesTokenFilter(tokenStream);
		case 3:
			return new CheckRadiansTokenFilter(tokenStream);

		}
	}

}
