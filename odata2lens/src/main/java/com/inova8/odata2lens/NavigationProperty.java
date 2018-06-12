package com.inova8.odata2lens;

public class NavigationProperty {
	String name;
	String targetEntityType;
	String label;
	String tooltip;
	String range;

	public NavigationProperty(String name, String label, String tooltip, String targetEntityType, String range) {
		this.name = name;
		this.label = label;
		this.tooltip = tooltip;
		this.targetEntityType = targetEntityType;
		this.range = range;
	}

	public String getName() {
		return name;
	}

	public String getTargetEntityType() {
		return targetEntityType;
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

}
