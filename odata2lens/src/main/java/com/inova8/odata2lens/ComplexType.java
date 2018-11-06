package com.inova8.odata2lens;

import java.util.TreeMap;

public class ComplexType {
	String name;
	TreeMap<String, Property> properties = new TreeMap<String, Property>();
	TreeMap<String, NavigationProperty> navigationProperties = new TreeMap<String, NavigationProperty>();
	TreeMap<String, EntityNavigationSet> navigationSets = new TreeMap<String, EntityNavigationSet>();

	public String getName() {
		return name;
	}

	public TreeMap<String, Property> getProperties() {
		return properties;
	}

	public TreeMap<String, NavigationProperty> getNavigationProperties() {
		return navigationProperties;
	}

	public TreeMap<String, EntityNavigationSet> getNavigationSets() {
		return navigationSets;
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

	public String getTargetEntityType() {
		if (this.getIsNavigationProperty()) {
			return navigationProperties.firstEntry().getValue().getTargetEntityType();
		} else if (this.getIsNavigationSet()) {
			return navigationSets.firstEntry().getValue().getTargetEntityType() ;
		} else {
			return null;
		}
	}
	public String getTargetEntitySet() {
		if (this.getIsNavigationProperty()) {
			return navigationProperties.firstEntry().getValue().getRangeType().getEntitySet().getTarget()  ;
		} else if (this.getIsNavigationSet()) {
			return navigationSets.firstEntry().getValue().getRangeType().getEntitySet().getTarget() ;
		} else {
			return null;
		}
	}
	public String getTooltip() {
		if (this.getIsNavigationProperty()) {
			return navigationProperties.firstEntry().getValue().getTooltip() ;
		} else if (this.getIsNavigationSet()) {
			return navigationSets.firstEntry().getValue().getTooltip() ;
		} else {
			return null;
		}
	}
	public String getIcon() {
		if (this.getIsNavigationProperty()) {
			return navigationProperties.firstEntry().getValue().getIcon() ;
		} else if (this.getIsNavigationSet()) {
			return navigationSets.firstEntry().getValue().getIcon() ;
		} else {
			return null;
		}
	}
	public ComplexType(String name) {
		super();
		this.name = name;
	}

	public PropertyTemplate getPropertyTemplate(String subTypeName) {
		if (properties.containsKey(subTypeName)) {
			Property property = properties.get(subTypeName);
			return new PropertyTemplate(//String property, String propertyType, String range, Float ordinal, boolean visible, String aggregate, String formatOptions
					name + "/" + property.getName(), "D", property.getRange(), null, true, "[]",
					property.getFormatOptions());

		} else if (navigationProperties.containsKey(subTypeName)) {
			NavigationProperty navigationProperty = navigationProperties.get(subTypeName);
			return new PropertyTemplate(//String property, String propertyType, String range, Float ordinal, boolean visible, String aggregate, String formatOptions
					name + "/" + navigationProperty.getName(), "O", navigationProperty.getRange(), null, true, "[]",
					"");

		} else if (navigationSets.containsKey(subTypeName)) {
			EntityNavigationSet entityNavigationSet = navigationSets.get(subTypeName);
			return new PropertyTemplate(//String property, String propertyType, String range, Float ordinal, boolean visible, String aggregate, String formatOptions
					name + "/" + entityNavigationSet.getName(), "C", entityNavigationSet.getRange(), null, true, "[]",
					"");
		}
		return null;
	}
}
