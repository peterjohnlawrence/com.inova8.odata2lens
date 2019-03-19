package com.inova8.odata2lens;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;

public class EntitySet {
	private String name;
	private String fqn;
	private String target;
	private String dashboardTarget;
	private String label;
	private String tooltip;
	private String image;
	private String entityIcon;
	private Boolean visible;
	private String gridStyle="entitySetTable";
	HashSet<String> baseTypes = new HashSet<String>();
	HashSet<EntitySet> parentEntitySets = new HashSet<EntitySet>();
	HashSet<EntitySet> childEntitySets = new HashSet<EntitySet>();

	public EntitySet(String name,String fqn, String target, String dashboardTarget, String label, String tooltip, String image,
			String entityIcon, Boolean visible, String baseType, HashSet<String> baseTypes) {
		this.name = name;
		this.fqn = fqn;
		this.target = target;
		this.dashboardTarget = dashboardTarget;
		this.label = label;
		this.tooltip = tooltip;
		this.image = image;
		this.entityIcon = entityIcon;
		this.visible = visible;
		if (baseType != null)
			this.addBaseType(baseType);
		this.baseTypes = baseTypes;
	}

	public String getName() {
		return name;
	}

	public String getFqn() {
		return fqn;
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
		if(parentEntitySets.isEmpty() )
			return visible;
		else
			return false;
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

	public ArrayList<EntitySet> getParentEntitySets() {
		ArrayList<EntitySet> parentEntitySetCollection = new ArrayList<EntitySet>(parentEntitySets);
		parentEntitySetCollection.sort(Comparator.comparing(EntitySet::getLabel));
		return parentEntitySetCollection;
	}

	public ArrayList<EntitySet> getChildEntitySets() {	
		ArrayList<EntitySet> childEntitySetCollection = new ArrayList<EntitySet>(childEntitySets);
		childEntitySetCollection.sort(Comparator.comparing(EntitySet::getLabel));
		return childEntitySetCollection;
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

	public String getGridStyle() {
		return gridStyle;
	}

	public void setGridStyle(String gridStyle) {
		this.gridStyle = gridStyle;
	}

}
