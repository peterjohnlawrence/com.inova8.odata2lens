package com.inova8.odata2lens;

public class EntityNavigationSet {

	private String name;

	private String target;
	private String label;
	private String tooltip;
	private String targetEntityType;
	private String icon;
	private String range;
	private EntityType rangeType;
	private String subTypeName;
	private String height;

	public EntityNavigationSet(String name, String target, String label, String tooltip, String targetEntityType,
			String range, String icon, String height) {
		this.name = name;
		this.target = target;
		this.label = label;
		this.tooltip = tooltip;
		this.targetEntityType = targetEntityType;
		this.range = range;
		this.icon = icon;
		this.height = height;
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

	public String getIcon() {
		if (icon.isEmpty()) {
			if (rangeType != null) {
				return rangeType.getEntity().getTargetIcon();
			} else {
				return "";
			}
		} else {
			return icon;
		}
	}

	void setIcon(String icon) {
		this.icon = icon;
	}

	public void setRangeType(EntityType entityType) {
		this.rangeType = entityType;

	}

	public void setSubTypeName(String subTypeName) {
		this.subTypeName = subTypeName;
		
	}

	public String getSubTypeName() {
		return subTypeName;
	}

	public EntityType getRangeType() {
		return rangeType;
	}

	public String getHeight() {
		return height;
	}
}
