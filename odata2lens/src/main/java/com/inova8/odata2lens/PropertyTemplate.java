package com.inova8.odata2lens;

public class PropertyTemplate {
	private String property;
	private String propertyType;
	private String range;
	private Float ordinal;
	private boolean visible = true;
	private String aggregate;
	private String formatOptions;
	private String height;
	public PropertyTemplate(String property, String propertyType, String range, Float ordinal, Boolean visible, String aggregate, String formatOptions, String height) {
		super();
		this.property = property;
		this.propertyType = propertyType;
		this.range = range;
		this.ordinal = ordinal;
		if(visible!=null)this.visible = visible;
		this.aggregate = aggregate;
		this.formatOptions= (formatOptions==null || formatOptions.isEmpty()||formatOptions.equals("\"\"") ? null:", formatOptions:" +  formatOptions.replace(";",","));
		this.height=height;
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
	public String getHeight() {
		return height;
	}
	public void update(String property, String propertyType, String range, Float ordinal, Boolean visible, String aggregate, String formatOptions, String height) {
		this.property = property;
		if(propertyType!=null) this.propertyType = propertyType;
		if(range!=null) this.range = range;
		if(ordinal!=null)this.ordinal = ordinal;
		if(visible!=null)this.visible = visible;
		if(aggregate!=null)this.aggregate = aggregate;		
		if(formatOptions!=null) {
			this.formatOptions= (formatOptions.isEmpty()||formatOptions.equals("\"\"")? null :", formatOptions:" +formatOptions.replace(";",","));
		}
		if(height!=null) {
			this.height = height;
		}
	}

}
