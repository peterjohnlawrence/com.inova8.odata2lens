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
	private Boolean isReified;
	private NavigationProperty reifiedSubjectPredicate;
	private NavigationProperty reifiedPredicate;
	private NavigationProperty reifiedObjectPredicate;
	private Property reifiedObjectProperty;
	private String gridStyle="entitySetTable";
	HashSet<String> baseTypes = new HashSet<String>();
	HashSet<EntitySet> parentEntitySets = new HashSet<EntitySet>();
	HashSet<EntitySet> childEntitySets = new HashSet<EntitySet>();
	private String reifiedInverseSubjectPredicate;
	private String reifiedInverseObjectPredicate;

	public EntitySet(String name,String fqn, String target, String dashboardTarget, String label, String tooltip, String image,
			String entityIcon, Boolean visible, String baseType, HashSet<String> baseTypes, Boolean isReified) {
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
		this.isReified = isReified;
	}

	public String getName() {
		return name;
	}

	public String getFqn() {
		return fqn;
	}

	public Boolean getIsReified() {
		return isReified;
	}

//	public Boolean getReifiedSubject() {
//		return isReified;
//	}
	public void setIsReified(Boolean isReified) {
		this.isReified = isReified;
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
			return true;
		else
			return false;
	}
	public Boolean isVisible() {
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

	public void setReifiedSubjectPredicate(NavigationProperty reifiedSubjectPredicate) {
		this.reifiedSubjectPredicate = reifiedSubjectPredicate;
	}
	public void setReifiedPredicate(NavigationProperty reifiedPredicate) {
		this.reifiedPredicate = reifiedPredicate;
	}
	public void setReifiedObjectProperty(Property reifiedObjectProperty) {	
		this.reifiedObjectProperty = reifiedObjectProperty;
	}
	public void setReifiedObjectPredicate(NavigationProperty reifiedObjectPredicate) {
		this.reifiedObjectPredicate = reifiedObjectPredicate;
	}
	public void setReifiedInverseSubjectPredicate(String reifiedInverseSubjectPredicate) {
		this.reifiedInverseSubjectPredicate =  reifiedInverseSubjectPredicate;
	}

	public void setReifiedInverseObjectPredicate(String reifiedInverseObjectPredicate) {
		this.reifiedInverseObjectPredicate =  reifiedInverseObjectPredicate;		
	}
	public String getReifiedSubjectClass() {
		return this.reifiedSubjectPredicate.getRangeType().getEntitySet().getName();
	}
	public String getReifiedSubjectClassLabel() {
		return this.reifiedSubjectPredicate.getRangeType().getEntitySet().getLabel();
	}
	public String getReifiedPredicateClass() {
		if (this.reifiedPredicate!=null)
			return this.reifiedPredicate.getRangeType().getEntitySet().getName();
		else
			return null;
	}
	public String getReifiedPredicateClassLabel() {
		if (this.reifiedPredicate!=null)
			return this.reifiedPredicate.getRangeType().getEntitySet().getLabel();
		else
			return null;
	}
	public String getReifiedPredicateLabel() {
		if (this.reifiedPredicate!=null)
			return this.reifiedPredicate.getLabel();
		else
			return null;
	}
	public String getReifiedObjectClass() {
		if (this.reifiedObjectPredicate!=null)
			return this.reifiedObjectPredicate.getRangeType().getEntitySet().getName();
		else
			return null;
	}
	public String getReifiedObjectClassLabel() {
		if (this.reifiedObjectPredicate!=null)
			return this.reifiedObjectPredicate.getRangeType().getEntitySet().getLabel();
		else
			return null;
	}
	public String getReifiedSubjectPredicate() {	
		return this.reifiedSubjectPredicate.getName();
	}
	public String getReifiedSubjectPredicateLabel() {
		if (this.reifiedSubjectPredicate!=null)
			return this.reifiedSubjectPredicate.getLabel();
		else
			return null;	
	}
	public String getReifiedPredicate() {
		if (this.reifiedPredicate!=null)
			return this.reifiedPredicate.getName();
		else
			return null;
	}
	
	public String getReifiedInverseSubjectPredicate() {
		if(reifiedInverseSubjectPredicate!=null)
			return reifiedInverseSubjectPredicate.split("/")[1];
		else
			return null;
	}

	public String getReifiedInverseObjectPredicate() {
		if(reifiedInverseObjectPredicate!=null)
			return reifiedInverseObjectPredicate.split("/")[1];
		else
			return null;
	}

	public Boolean hasReifiedPredicate() {
		if (this.reifiedPredicate!=null)
			return true;
		else
			return false;
	}
	public String reificationType() {
		if (this.reifiedObjectProperty!=null)
			return "Data";
		else if (this.reifiedObjectPredicate!=null)
			return "Object";
		else
			return "";
	}
	public String getReifiedObjectPredicate() {
		if (this.reifiedObjectPredicate!=null)
			return this.reifiedObjectPredicate.getName();
		else
			return null;	
	}
	public String getReifiedObjectPredicateLabel() {
		if (this.reifiedObjectPredicate!=null)
			return this.reifiedObjectPredicate.getLabel();
		else
			return null;	
	}
	public String getReifiedObjectProperty() {
		if (this.reifiedObjectProperty!=null)
			return this.reifiedObjectProperty.getName();
		else
			return null;	
	}
	public String getReifiedObjectPropertyLabel() {
		if (this.reifiedObjectProperty!=null)
			return this.reifiedObjectProperty.getLabel();
		else
			return null;	
	}
	public String getReifiedObjectPropertyIRI() {
		if (this.reifiedObjectProperty!=null)
			return this.reifiedObjectProperty.getQName();
		else
			return null;	
	}

}
