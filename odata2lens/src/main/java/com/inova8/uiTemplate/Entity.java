package com.inova8.uiTemplate;

public class Entity {
	String entity ;
	String icon ;
	String image ;
	Boolean entitySetVisible;
	Grid grid;
	Form form;
	Namespace[] namespaces;
	public Namespace[] getNamespaces() {
		return namespaces;
	}
	public String getEntity() {
		return entity;
	}
	public void setEntity(String entity) {
		this.entity = entity;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public Boolean getEntitySetVisible() {
		return entitySetVisible;
	}
	public void setEntitySetVisible(Boolean entitySetVisible) {
		this.entitySetVisible = entitySetVisible;
	}
	public Grid getGrid() {
		return grid;
	}
	public void setGrid(Grid grid) {
		this.grid = grid;
	}
	public Form getForm() {
		return form;
	}
	public void setForm(Form form) {
		this.form = form;
	} 

}
