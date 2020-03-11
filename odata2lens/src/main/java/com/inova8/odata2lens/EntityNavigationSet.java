package com.inova8.odata2lens;

public class EntityNavigationSet {

	 String name;

	 String target;
	 String label;
	 String tooltip;
	 String targetEntityType;
	 String icon;
	 String range;
	 EntityType rangeType;
	 String subTypeName;
	 String height;
	 String listStyle;

	public EntityNavigationSet(String name, String target, String label, String tooltip, String targetEntityType,
			String range, String icon, String height, String listStyle) {
		this.name = name;
		this.target = target;
		this.label = label;
		this.tooltip = tooltip;
		this.targetEntityType = targetEntityType;
		this.range = range;
		this.icon = icon;
		this.height = height;
		this.listStyle = listStyle;
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
	public String getListStyle() {
		return listStyle;
	}
	public void setListStyle( String listStyle) {
		this.listStyle=listStyle;
	}
}
