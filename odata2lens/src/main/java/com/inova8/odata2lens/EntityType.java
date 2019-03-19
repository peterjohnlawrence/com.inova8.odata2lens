package com.inova8.odata2lens;

public class EntityType {
	private Entity entity;
	private EntitySet entitySet;
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
}
