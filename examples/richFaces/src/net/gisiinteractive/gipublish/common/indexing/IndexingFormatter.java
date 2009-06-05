package net.gisiinteractive.gipublish.common.indexing;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import net.gisiinteractive.common.exceptions.SystemException;

import com.jaeksoft.searchlib.index.FieldContent;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.result.ResultDocument;

public class IndexingFormatter {
	public static final SimpleDateFormat DATEFORMAT = new SimpleDateFormat(
			"yyyyMMdd-HH:mm");
	public static final SimpleDateFormat DATETIMEFORMAT = new SimpleDateFormat(
			"yyyyMMddHHmmss");

	public static String format(Date date, DateFormat dateFormat) {
		if (date == null)
			return null;
		return dateFormat.format(date);
	}

	public static Date parse(String value, DateFormat dateFormat)
			throws ParseException {
		if (value == null)
			return null;
		return dateFormat.parse(value);
	}

	public static Date getDateValue(ResultDocument doc, String field) {
		try {
			return getDateValue(doc, field, true);
		} catch (ParseException e) {
			throw new SystemException(e);
		}
	}

	public static Date getDateValue(ResultDocument doc, String field,
			boolean lenient) throws ParseException {
		return getDateValue(doc, field, DATEFORMAT, lenient);
	}

	public static Date getDateValue(ResultDocument doc, String field,
			DateFormat dateFormat) {
		try {
			return getDateValue(doc, field, dateFormat, true);
		} catch (ParseException e) {
			throw new SystemException(e);
		}
	}

	public static Date getDateValue(ResultDocument doc, String field,
			DateFormat dateFormat, boolean lenient) throws ParseException {
		String value = getStringValue(doc, field, lenient);
		if (value == null)
			return null;

		return parse(getStringValue(doc, field, lenient), dateFormat);
	}

	public static long getLongValue(ResultDocument doc, String field) {
		return getLongValue(doc, field, true);
	}

	public static List<String> getMultipleStringValue(ResultDocument doc,
			String field) {
		try {
			return doc.getValueList(field);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static long getLongValue(ResultDocument doc, String field,
			boolean lenient) {
		String value = getStringValue(doc, field, lenient);
		if (value == null)
			return 0;
		return Long.parseLong(value);
	}

	public static String getStringValue(ResultDocument doc, String field) {
		return getStringValue(doc, field, true);
	}

	public static boolean getBooleanValue(ResultDocument doc, String field) {
		String stringValue = getStringValue(doc, field, true);
		if (stringValue == null)
			return false;
		return stringValue.equals(Boolean.TRUE.toString());
	}

	public static String getStringValue(ResultDocument doc, String field,
			boolean lenient) {
		try {
			List<String> values = doc.getValueList(field);
			if (values == null || values.size() == 0) {
				if (!lenient) {
					throw new IllegalArgumentException("Filed " + field
							+ " is mandatory and has no values.");
				} else {
					return null;
				}
			}

			if (values.size() > 1) {
				if (!lenient)
					throw new IllegalArgumentException("Filed " + field
							+ " is has many values: " + values);

			}

			return values.get(0);
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public static boolean add(IndexDocument document, String field, Date value) {
		if (value == null)
			return false;
		document.add(field, format(value, DATEFORMAT));
		return true;
	}

	public static boolean add(IndexDocument document, String field, Date value,
			SimpleDateFormat df) {
		if (value == null)
			return false;
		document.add(field, format(value, df));
		return true;
	}

	public static boolean add(IndexDocument document, String field, long value) {
		return add(document, field, value, new Long(0));
	}

	public static boolean add(IndexDocument document, String field, long value,
			Long skipValue) {
		if (skipValue != null && skipValue.intValue() == value)
			return false;
		document.add(field, Long.toString(value));
		return true;
	}

	public static boolean add(IndexDocument document, String field, String value) {
		if (value == null || value.length() == 0)
			return false;
		document.add(field, value);
		return true;
	}

	public static void add(IndexDocument toReturn, String field, boolean active) {
		add(toReturn, field, active ? Boolean.TRUE.toString() : Boolean.FALSE
				.toString());
	}

	public static String toString(IndexDocument document) {
		FieldContent[] fields = document.getFieldContentArray();
		StringBuffer toReturn = new StringBuffer();
		for (FieldContent fieldContent : fields) {
			toReturn.append(fieldContent.getField());
			toReturn.append(":");
			toReturn.append(fieldContent.getValues());
			toReturn.append("\n");
		}
		return toReturn.toString();
	}
}
