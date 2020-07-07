package com.inova8.odata2lens;

import java.util.TreeMap;

public class EntityType {
	private String namePattern ="";
	private Entity entity;
	private EntitySet entitySet;
	private Boolean entityTypeVisible;
	private  TreeMap<String,String> namespaces = new TreeMap<String,String>();
	public EntityType(Entity entity, EntitySet entitySet ) {
		this.entity = entity;
		this.entitySet = entitySet;
	}
	public Entity getEntity() {
		return entity;
	}
	public EntitySet getEntitySet() {
		return entitySet;
	}
	public String getEntitySetLabel() {
		return entitySet.getLabel();
	}
	public String getEntityLabel() {
		return entity.getLabel();
	}
	public TreeMap<String,String> getNamespaces() {
		return namespaces;
	}
	public void setNamespaces(TreeMap<String,String> namespaces) {
		this.namespaces = namespaces;
	}
	public Boolean getEntityTypeVisible() {
		return entityTypeVisible;
	}
	public void setEntityTypeVisible(Boolean entityTypeVisible) {
		this.entityTypeVisible = entityTypeVisible;
	}
	public String getNamePattern() {
		return namePattern;
	}
	public void setNamePattern(String namePattern) {
		this.namePattern = namePattern;
	}
}
