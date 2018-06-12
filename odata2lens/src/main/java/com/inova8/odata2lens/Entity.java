package com.inova8.odata2lens;

import java.util.TreeMap;

public class Entity {

	private String name;
	private String target;
	private String label;
	private String tooltip;
	private  TreeMap<String,EntityNavigationSet> navigationSet;
	private  TreeMap<String,NavigationProperty> navigationProperties;
	private  TreeMap<String,Property> properties;
	public Entity(String name, String target, String label, String tooltip, TreeMap<String,EntityNavigationSet> entityNavigationSets,TreeMap<String,NavigationProperty> navigationProperties,TreeMap<String,Property> properties) {
		this.name = name;
		this.target = target;
		this.label = label;
		this.tooltip = tooltip;
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
	public TreeMap<String,EntityNavigationSet> getNavigationSet() {
		return navigationSet;
	}
	public TreeMap<String,NavigationProperty> getNavigationProperties() {
		return navigationProperties;
	}
	public TreeMap<String,Property> getProperties() {
		return properties;
	}
}
