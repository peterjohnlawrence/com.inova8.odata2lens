package com.inova8.odata2lens;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

public class Entity {

	private String name;
	private String target;
	private String label;
	private String tooltip;
	private String targetIcon;
	private  TreeMap<String,EntityNavigationSet> navigationSet;
	private  TreeMap<String,NavigationProperty> navigationProperties;
	private  TreeMap<String,Property> properties;
	private Set<String> subTypeNames = new HashSet<String>();
	private boolean hasPrimitiveProperties =false;
	public Entity(String name, String target, String label, String tooltip, String targetIcon, TreeMap<String,EntityNavigationSet> entityNavigationSets,TreeMap<String,NavigationProperty> navigationProperties,TreeMap<String,Property> properties) {
		this.name = name;
		this.target = target;
		this.label = label;
		this.tooltip = tooltip;
		this.targetIcon = targetIcon;
		this.navigationSet = entityNavigationSets;
		this.navigationProperties = navigationProperties;
		this.properties = properties;
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
	public String getTargetIcon() {
		return targetIcon;
	}
	void setTargetIcon(String targetIcon) {
		this.targetIcon = targetIcon;
	}
	public TreeMap<String,EntityNavigationSet> getNavigationSet() {
		return navigationSet;
	}
	public TreeMap<String,NavigationProperty> getNavigationProperties() {
		return navigationProperties;
	}
	public TreeMap<String,Property> getProperties() {
		return properties;
	}

	public Set<String> getSubTypeNames() {
		return subTypeNames;
	}
	public Boolean getHasMultipleColumns() {
		if ( subTypeNames.size() > 1)
			return true;
		else if(subTypeNames.size()==1 && this.hasPrimitiveProperties )
			return true;
		else 
			return false;
	}

	public void hasPrimitiveProperties(boolean b) {
		this.hasPrimitiveProperties = b;
		
	}
	public Boolean getOwnProperties() {
		return !(navigationSet.isEmpty() && navigationProperties.isEmpty() && properties.isEmpty() && !hasPrimitiveProperties );
		
	}
	
}
