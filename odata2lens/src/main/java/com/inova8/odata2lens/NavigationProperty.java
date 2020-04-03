package com.inova8.odata2lens;

public class NavigationProperty {
	private String name;
	private String targetEntityType;
	private String label;
	private String tooltip;
	private String range;
	private String subTypeName;
	private String icon;
	private EntityType rangeType;
	 String height;
	 String style;
	Boolean nullable;
	Boolean isReifiedSubject =false;
	Boolean isReifiedObject =false;
	public NavigationProperty(String name, String label, String tooltip, String targetEntityType, String range,
			String icon, String height, String style, Boolean nullable,Boolean isReifiedSubject,Boolean isReifiedObject) {
		this.name = name;
		this.label = label;
		this.tooltip = tooltip;
		this.targetEntityType = targetEntityType;
		this.range = range;
		this.icon = icon;
		this.height = height;
		this.style = style;
		this.nullable = nullable;
		this.isReifiedSubject = isReifiedSubject;
		this.isReifiedObject = isReifiedObject;		
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

	public String getSubTypeName() {

		return subTypeName;
	}
	public void setSubTypeName(String subTypeName) {
		this.subTypeName = subTypeName;
	}

	public String getIcon() {
		if ( (icon == null || icon.isEmpty()) && rangeType != null) {
			return rangeType.getEntity().getTargetIcon();
		} else {
			return icon==null?"":icon;
		}
	}

	void setIcon(String icon) {
		this.icon = icon;
	}

	public void setRangeType(EntityType entityType) {
		this.rangeType = entityType;

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
	public Boolean getIsReifiedSubject() {
		return isReifiedSubject;
	}

	public Boolean getIsReifiedObject() {
		return isReifiedObject;
	}
}
