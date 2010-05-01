package net.gisiinteractive.common.view.jsf.facelets;


public abstract class ProgressLabelHandler {

	protected int maxCount;
	private int count;

	public ProgressLabelHandler(int maxCount) {
		super();
		this.maxCount = maxCount;
	}

	public abstract String getValue();

	public int getCount() {
		String value = getValue();
		if (value == null)
			return 0;
		count = value.length();
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getMaxCount() {
		return maxCount;
	}

	public void setMaxCount(int maxCount) {
		this.maxCount = maxCount;
	}

	public boolean isSizeExeeded() {
		return getCount() > getMaxCount();
	}

	
	public static String cleanValue(String value) {
		String toReturn = null;
		
		if (value != null) {
			toReturn = value;
			toReturn = toReturn.replaceAll("<[^>]*>", "");
			toReturn = toReturn.replaceAll("(\\&nbsp;)", " ");
		}
		
		return toReturn;
	}
	
	public static int getCleanCount(String value) {
		if (value == null)
			return 0;
		value = cleanValue(value);

		return value.length();
	}
	
	public String getCleanValue() {
		return cleanValue(getValue());
	}
	
	public int getCleanCount() {
		return getCleanCount(getValue());
	}
}
