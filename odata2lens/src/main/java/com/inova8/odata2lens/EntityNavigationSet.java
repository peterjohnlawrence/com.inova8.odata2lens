package com.inova8.odata2lens;

public class EntityNavigationSet {

	private String name;

	private String target;
	private String label;
	private String tooltip;
	private String targetEntityType;
	String range;
	
	public EntityNavigationSet(String name, String target, String label, String tooltip, String targetEntityType,	String range) {
		this.name = name;
		this.target = target;
		this.label = label;
		this.tooltip = tooltip;
		this.targetEntityType =targetEntityType;
		this.range = range;
	}

	public String getName() {
		return name;
	}

	public String getTarget() {
		return target;
	}

	public String getLabel() {
		return label;
	}

	public String getTooltip() {
		return tooltip;
	}
	public String getTargetEntityType() {
		return targetEntityType;
	}

	public String getRange() {
		return range;
	}

}
