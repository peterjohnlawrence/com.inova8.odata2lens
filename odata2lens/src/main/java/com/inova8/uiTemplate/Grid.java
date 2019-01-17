package com.inova8.uiTemplate;

public class Grid {
	String target;
	Boolean targetVisible;
	String targetIcon;
	String targetEntity;	
	Property[] properties;
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	public Boolean getTargetVisible() {
		return targetVisible;
	}
	public void setTargetVisible(Boolean targetVisible) {
		this.targetVisible = targetVisible;
	}
	public String getTargetIcon() {
		return targetIcon;
	}
	public void setTargetIcon(String targetIcon) {
		this.targetIcon = targetIcon;
	}
	public String getTargetEntity() {
		return targetEntity;
	}
	public void setTargetEntity(String targetEntity) {
		this.targetEntity = targetEntity;
	}
	public Property[] getProperties() {
		return properties;
	}
	public void setProperties(Property[] properties) {
		this.properties = properties;
	}
}
