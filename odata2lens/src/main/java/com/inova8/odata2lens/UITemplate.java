package com.inova8.odata2lens;

import java.util.TreeMap;

public class UITemplate {
	private String sequence;
	private String entity;
	private String entityIcon;
	private String target;
	private String targetIcon;
	private String type;
	private String targetEntity;
	private TreeMap<Float, PropertyTemplate> properties = new TreeMap<Float, PropertyTemplate>();
	public UITemplate(String sequence, String entity, String entityIcon, String target, String targetIcon, String type,
			String targetEntity) {
		super();
		this.sequence = sequence;
		this.entity = entity;
		this.entityIcon = entityIcon;
		this.target = target;
		this.targetIcon = targetIcon;
		this.type = type;
		this.targetEntity = targetEntity;

	}
	public String getSequence() {
		return sequence;
	}
	public String getEntity() {
		return entity;
	}
	public String getEntityIcon() {
		return entityIcon;
	}
	public String getTarget() {
		return target;
	}
	public String getTargetIcon() {
		return targetIcon;
	}
	public String getType() {
		return type;
	}
	public String getTargetEntity() {
		return targetEntity;
	}
	public TreeMap<Float, PropertyTemplate> getProperties() {
		return properties;
	}
	public void update(String sequence, String entity, String entityIcon, String target, String targetIcon, String type,
			String targetEntity) {
		this.sequence = sequence;
		this.entity = entity;
		this.entityIcon = entityIcon;
		this.target = target;
		this.targetIcon = targetIcon;
		this.type = type;
		this.targetEntity = targetEntity;
		
	}
	public void updateProperty(String property, String propertyType, String range, Float ordinal, boolean hidden, String aggregate) {
		Float currentOrdinal = null;
		for(PropertyTemplate propertyTemplate : properties.values()){
			if(propertyTemplate.getProperty().equals(property)){
				currentOrdinal=propertyTemplate.getOrdinal();
				break;
			}		
		}
		if(currentOrdinal!=null){
			PropertyTemplate propertyTemplate = new PropertyTemplate( property,  propertyType,  range,  ordinal,  hidden,  aggregate);
			properties.remove(currentOrdinal);
			properties.put(ordinal,propertyTemplate);
		}
	}
}
