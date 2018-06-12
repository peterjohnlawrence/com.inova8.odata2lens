package com.inova8.odata2lens;

public class EntitySet {
	String name;
	String target;
	String dashboardTarget;
	String label;
	String tooltip;

	public EntitySet(String name, String target, String dashboardTarget, String label, String tooltip) {
		this.name = name;
		this.target = target;
		this.dashboardTarget = dashboardTarget;
		this.label = label;
		this.tooltip = tooltip;
	}

	public String getName() {
		return name;
	}
	public String getTarget() {
		return target;
	}
	public String getDashboardTarget() {
		return dashboardTarget;
	}
	public String getLabel() {
		return label;
	}

	public String getTooltip() {
		return tooltip;
	}

}
