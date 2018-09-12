package com.inova8.odata2lens;

import java.util.HashSet;

public class EntitySet {
	String name;
	String target;
	String dashboardTarget;
	String label;
	String tooltip;
	String image;
	String entityIcon;
	Boolean visible;
	HashSet<String> baseTypes = new HashSet<String>();
	HashSet<EntitySet> parentEntitySets = new HashSet<EntitySet>();
	HashSet<EntitySet> childEntitySets = new HashSet<EntitySet>();

	public EntitySet(String name, String target, String dashboardTarget, String label, String tooltip, String image,
			String entityIcon, Boolean visible, String baseType) {
		this.name = name;
		this.target = target;
		this.dashboardTarget = dashboardTarget;
		this.label = label;
		this.tooltip = tooltip;
		this.image = image;
		this.entityIcon = entityIcon;
		this.visible = visible;
		if (baseType != null)
			this.addBaseType(baseType);
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

	public Boolean getVisible() {
		return visible;
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
	public HashSet<String> getBaseTypes() {
		return baseTypes;
	}

	public HashSet<EntitySet> getParentEntitySets() {
		return parentEntitySets;
	}

	public HashSet<EntitySet> getChildEntitySets() {
		return childEntitySets;
	}

	void addParentEntitySet(EntitySet entitySet) {
		parentEntitySets.add(entitySet);
	}

	void addBaseType(String baseType) {
		baseTypes.add(baseType);
	}

	void addChildEntitySet(EntitySet entitySet) {
		childEntitySets.add(entitySet);
	}

	void setImage(String image) {
		this.image = image;
	}

	void setEntityIcon(String entityIcon) {
		this.entityIcon = entityIcon;
	}

	void setVisible(Boolean visible) {
		this.visible = visible;
	}

}
