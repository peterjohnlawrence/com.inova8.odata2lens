package com.inova8.odata2lens;

public class Property {
	String name;
	String label;
	String tooltip;
	String range;
	String formatOptions;

	public Property(String name, String label, String tooltip, String range, String formatOptions) {
		this.name = name;

		this.label = label;
		this.tooltip = tooltip;
		this.range = range;
		this.formatOptions = formatOptions;
	}

	public String getName() {
		return name;
	}

	public String getLabel() {
		return label;
	}

	public String getTooltip() {
		return tooltip;
	}
	
	public String getRange() {
		return range;
	}
	public String getFormatOptions() {
		return formatOptions;
	}}
