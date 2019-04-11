package com.inova8.uiTemplate;

public class Property {
	String property;
	Boolean propertyVisible;
	String propertyType;
	String propertyRange;
	Float ordinal;
	String aggregate;
	String formatOptions;
	private String height;
	public String getProperty() {
		return property;
	}
	public void setProperty(String property) {
		this.property = property;
	}
	public Boolean getPropertyVisible() {
		return propertyVisible;
	}
	public void setPropertyVisible(Boolean propertyVisible) {
		this.propertyVisible = propertyVisible;
	}
	public String getPropertyType() {
		return propertyType;
	}
	public void setPropertyType(String propertyType) {
		this.propertyType = propertyType;
	}
	public String getPropertyRange() {
		return propertyRange;
	}
	public void setPropertyRange(String propertyRange) {
		this.propertyRange = propertyRange;
	}
	public Float getOrdinal() {
		return ordinal;
	}
	public void setOrdinal(Float ordinal) {
		this.ordinal = ordinal;
	}
	public String getAggregate() {
		return aggregate;
	}
	public void setAggregate(String aggregate) {
		this.aggregate = aggregate;
	}
	public String getFormatOptions() {
		return formatOptions;
	}
	public void setFormatOptions(String formatOptions) {
		this.formatOptions = formatOptions;
	}
	public void setHeight(String height) {
		this.height=height;
	}
	public String getHeight() {
		return height;
	}
}
