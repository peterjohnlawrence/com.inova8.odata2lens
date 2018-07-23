package com.inova8.odata2lens;

public class EntitySet {
	String name;
	String target;
	String dashboardTarget;
	String label;
	String tooltip;
	String image;
	String entityIcon;
	

	public EntitySet(String name, String target, String dashboardTarget, String label, String tooltip, String image,String entityIcon) {
		this.name = name;
		this.target = target;
		this.dashboardTarget = dashboardTarget;
		this.label = label;
		this.tooltip = tooltip;
		this.image = image;
		this.entityIcon = entityIcon;
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
	public String getImage() {
		return image;
	}
	public String getEntityIcon() {
		return entityIcon;
	}
	void setImage(String image) {
		this.image = image;
	}
	void setEntityIcon(String entityIcon) {
		this.entityIcon = entityIcon;
	}

}
