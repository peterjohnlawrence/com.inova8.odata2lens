package com.inova8.odata2lens;

public class PropertyTemplate {
	private String property;
	private String propertyType;
	private String range;
	private Float ordinal;
	private boolean visible = true;
	private String aggregate;
	private String formatOptions;
	public PropertyTemplate(String property, String propertyType, String range, Float ordinal, boolean visible, String aggregate, String formatOptions) {
		super();
		this.property = property;
		this.propertyType = propertyType;
		this.range = range;
		this.ordinal = ordinal;
		this.visible = visible;
		this.aggregate = aggregate;
		this.formatOptions= (formatOptions.isEmpty()||formatOptions.equals("\"\"") ? null:", formatOptions:" +  formatOptions.replace(";",","));
	}
	public String getProperty() {
		return property;
	}
	public String getPropertyType() {
		return propertyType;
	}
	public Float getOrdinal() {
		return ordinal;
	}
	public boolean getVisible() {
		return visible;
	}
	public String getAggregate() {
		return aggregate;
	}
	public String getRange() {
		return range;
	}
	public String getFormatOptions() {
		return formatOptions;
	}
	public void update(String property, String propertyType, String range, Float ordinal, boolean visible, String aggregate, String formatOptions) {
		this.property = property;
		this.propertyType = propertyType;
		this.range = range;
		this.ordinal = ordinal;
		this.visible = visible;
		this.aggregate = aggregate;		
		this.formatOptions= (formatOptions.isEmpty()||formatOptions.equals("\"\"")? null :", formatOptions:" +formatOptions.replace(";",","));
	}

}
