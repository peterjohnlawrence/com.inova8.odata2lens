package com.inova8.odata2lens;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

public class ComplexType {
	String name;
	TreeMap<String, TreeMap<String, Property>> properties = new TreeMap<String, TreeMap<String, Property>>();
	TreeMap<String, TreeMap<String, NavigationProperty>> navigationProperties = new TreeMap<String, TreeMap<String, NavigationProperty>>();
	TreeMap<String, TreeMap<String, EntityNavigationSet>> navigationSets = new TreeMap<String, TreeMap<String, EntityNavigationSet>>();

	public String getName() {
		return name;
	}

	public TreeMap<String, TreeMap<String, Property>> getProperties() {

		return properties;
	}
	public ArrayList<Property> getAllProperties() {
		ArrayList<Property> allProperties = new ArrayList<Property>();
		Collection<TreeMap<String, Property>> propertySets = getProperties().values();
		for(TreeMap<String, Property> properties : propertySets) {
			allProperties.addAll(properties.values());
		}	
		return allProperties;
	}
	public TreeMap<String, Property> getProperties(String subTypeName) {
		if (properties.containsKey(subTypeName)) {
			return properties.get(subTypeName);
		}
		return null;
	}

	public void putProperty(String subTypeName, Property property) {
		TreeMap<String, Property> subTypeProperties;
		if (properties.containsKey(subTypeName)) {
			subTypeProperties = properties.get(subTypeName);
		} else {
			subTypeProperties = new TreeMap<String, Property>();
			properties.put(subTypeName, subTypeProperties);
		}
		subTypeProperties.put(property.getName(), property);
	}

	public TreeMap<String, TreeMap<String, NavigationProperty>> getNavigationProperties() {
		return navigationProperties;
	}
	public ArrayList<NavigationProperty> getAllNavigationProperties() {
		ArrayList<NavigationProperty> allNavigationProperties = new ArrayList<NavigationProperty>();
		Collection<TreeMap<String, NavigationProperty>> navigationPropertySets = getNavigationProperties().values();
		for(TreeMap<String, NavigationProperty> navigationProperties : navigationPropertySets) {
			allNavigationProperties.addAll(navigationProperties.values()); 
		}	
		return allNavigationProperties;
	}
	public TreeMap<String, NavigationProperty> getNavigationProperties(String subTypeName) {
		if (navigationProperties.containsKey(subTypeName)) {
			return navigationProperties.get(subTypeName);
		}
		return null;
	}

	public void putNavigationProperty(String subTypeName, NavigationProperty navigationProperty) {
		TreeMap<String, NavigationProperty> subTypeNavigationProperties;
		if (navigationProperties.containsKey(subTypeName)) {
			subTypeNavigationProperties = navigationProperties.get(subTypeName);
		} else {
			subTypeNavigationProperties = new TreeMap<String, NavigationProperty>();
			navigationProperties.put(subTypeName, subTypeNavigationProperties);
		}
		subTypeNavigationProperties.put(navigationProperty.getName(), navigationProperty);
	}

	public TreeMap<String, TreeMap<String, EntityNavigationSet>> getNavigationSets() {
		return navigationSets;
	}
	public ArrayList<EntityNavigationSet> getAllNavigationSets() {
		ArrayList<EntityNavigationSet> allNavigationSets = new ArrayList<EntityNavigationSet>();
		Collection<TreeMap<String, EntityNavigationSet>> navigationSets = getNavigationSets().values();
		for(TreeMap<String, EntityNavigationSet> navigationSet : navigationSets) {
			allNavigationSets.addAll(navigationSet.values()); 
		}	
		return allNavigationSets;
	}
	public TreeMap<String, EntityNavigationSet> getNavigationSets(String subTypeName) {
		if (navigationSets.containsKey(subTypeName)) {
			return navigationSets.get(subTypeName);
		}
		return null;
	}

	public void putNavigationSet(String subTypeName, EntityNavigationSet navigationSet) {
		TreeMap<String, EntityNavigationSet> subTypeNavigationSets;
		if (navigationSets.containsKey(subTypeName)) {
			subTypeNavigationSets = navigationSets.get(subTypeName);
		} else {
			subTypeNavigationSets = new TreeMap<String, EntityNavigationSet>();
			navigationSets.put(subTypeName, subTypeNavigationSets);
		}
		subTypeNavigationSets.put(navigationSet.getName(), navigationSet);
	}

	public Boolean getIsProperty() {
		return navigationProperties.isEmpty() && navigationSets.isEmpty();
	}

	public Boolean getIsNavigationProperty() {
		return properties.isEmpty() && navigationSets.isEmpty();
	}

	public Boolean getIsNavigationSet() {
		return properties.isEmpty() && navigationProperties.isEmpty();
	}

	//	public String getTargetEntityType() {
	//		if (this.getIsNavigationProperty()) {
	//			return navigationProperties.firstEntry().getValue().getTargetEntityType();
	//		} else if (this.getIsNavigationSet()) {
	//			return navigationSets.firstEntry().getValue().getTargetEntityType();
	//		} else {
	//			return null;
	//		}
	//	}
	//
	//	public String getTargetEntitySet() {
	//		if (this.getIsNavigationProperty()) {
	//			return navigationProperties.firstEntry().getValue().getRangeType().getEntitySet().getTarget();
	//		} else if (this.getIsNavigationSet()) {
	//			return navigationSets.firstEntry().getValue().getRangeType().getEntitySet().getTarget();
	//		} else {
	//			return null;
	//		}
	//	}
	//
	//	public String getTooltip() {
	//		if (this.getIsNavigationProperty()) {
	//			return navigationProperties.firstEntry().getValue().getTooltip();
	//		} else if (this.getIsNavigationSet()) {
	//			return navigationSets.firstEntry().getValue().getTooltip();
	//		} else {
	//			return null;
	//		}
	//	}
	//
	//	public String getIcon() {
	//		if (this.getIsNavigationProperty()) {
	//			return navigationProperties.firstEntry().getValue().getIcon();
	//		} else if (this.getIsNavigationSet()) {
	//			return navigationSets.firstEntry().getValue().getIcon();
	//		} else {
	//			return null;
	//		}
	//	}

	public ComplexType(String name) {
		super();
		this.name = name;
	}

	public ArrayList<PropertyTemplate> getPropertyTemplates(String subTypeName) {
		ArrayList<PropertyTemplate> propertyTemplates = new ArrayList<PropertyTemplate>();
		if (properties.containsKey(subTypeName)) {
			Collection<Property> subTypeProperties = getProperties(subTypeName).values();
			for (Property property : subTypeProperties) {
				propertyTemplates.add(new PropertyTemplate(//String property, String propertyType, String range, Float ordinal, boolean visible, String aggregate, String formatOptions
						name + "/" + property.getName(), "D", property.getRange(), null, true, "[]",
						property.getFormatOptions(), property.getHeight()));
			}

		} else if (navigationProperties.containsKey(subTypeName)) {
			Collection<NavigationProperty> subTypeNavigationProperties = getNavigationProperties(subTypeName).values();
			for (NavigationProperty navigationProperty : subTypeNavigationProperties) {
				propertyTemplates.add(new PropertyTemplate(//String property, String propertyType, String range, Float ordinal, boolean visible, String aggregate, String formatOptions
						name + "/" + navigationProperty.getName(), "O", navigationProperty.getRange(), null, true, "[]",
						"","4rem"));
			}

		} else if (navigationSets.containsKey(subTypeName)) {
			Collection<EntityNavigationSet> subTypeNavigationSets = getNavigationSets(subTypeName).values();
			for (EntityNavigationSet entityNavigationSet : subTypeNavigationSets) {
				propertyTemplates.add(new PropertyTemplate(//String property, String propertyType, String range, Float ordinal, boolean visible, String aggregate, String formatOptions
						name + "/" + entityNavigationSet.getName(), "C", entityNavigationSet.getRange(), null, true,
						"[]", "","4rem"));
			}
		}
		if (propertyTemplates.size() > 0) {
			return propertyTemplates;
		} else {
			return null;
		}
	}
}
