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
	 String style;
	 Boolean nullable;

	public EntityNavigationSet(String name, String target, String label, String tooltip, String targetEntityType,
			String range, String icon, String height, String style, Boolean nullable) {
		this.name = name;
		this.target = target;
		this.label = label;
		this.tooltip = tooltip;
		this.targetEntityType = targetEntityType;
		this.range = range;
		this.icon = icon;
		this.height = height;
		this.style = style;
		this.nullable = nullable;
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
	public String getStyle() {
		return style;
	}
	public void setStyle( String style) {
		this.style=style;
	}
	public Boolean getNullable() {
		return nullable;
	}
}
