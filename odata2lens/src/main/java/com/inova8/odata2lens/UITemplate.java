package com.inova8.odata2lens;

import java.util.TreeMap;

public class UITemplate {
	private String sequence;
	private String entity;
	private String entityIcon;
	private String target;
	private String targetIcon;
	private String type;
	private String formStyle;
	private String gridStyle;
	private String targetEntity;
	private String targetVisible;
	private String image;
	private TreeMap<Float, PropertyTemplate> properties = new TreeMap<Float, PropertyTemplate>();

	public UITemplate(String sequence, String entity, String entityIcon, String image, String target, String targetIcon,
			String type, String targetEntity, String targetVisible, String formStyle, String gridStyle) {
		super();
		this.sequence = sequence;
		this.entity = entity;
		this.entityIcon = entityIcon;
		this.image = image;
		this.target = target;
		this.targetIcon = targetIcon;
		this.type = type;
		this.targetEntity = targetEntity;
		this.targetVisible = targetVisible;
		if (formStyle != null && !formStyle.isEmpty())
			this.formStyle = formStyle;
		if (gridStyle != null && !gridStyle.isEmpty())
			this.gridStyle = gridStyle;
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

	public String getImage() {
		return image;
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

	public String getTargetVisible() {
		return targetVisible;
	}

	public TreeMap<Float, PropertyTemplate> getProperties() {
		return properties;
	}

	public String getFormStyle() {
		return formStyle;
	}

	public void setFormStyle(String formStyle) {
		this.formStyle = formStyle;
	}

	public String getGridStyle() {
		return gridStyle;
	}

	public void setGridStyle(String gridStyle) {
		this.gridStyle = gridStyle;
	}

	public void update(String sequence, String entity, String entityIcon, String image, String target,
			String targetIcon, String type, String targetEntity, String targetVisible, String formStyle,
			String gridStyle) {
		if (sequence != null && !sequence.isEmpty())
			this.sequence = sequence;
		if (entity != null && !entity.isEmpty())
			this.entity = entity;
		if (entityIcon != null && !entityIcon.isEmpty())
			this.entityIcon = entityIcon;
		if (image != null && !image.isEmpty())
			this.image = image;
		if (target != null && !target.isEmpty())
			this.target = target;
		if (targetIcon != null && !targetIcon.isEmpty())
			this.targetIcon = targetIcon;
		if (type != null && !type.isEmpty())
			this.type = type;
		if (targetEntity != null && !targetEntity.isEmpty())
			this.targetEntity = targetEntity;
		if (targetVisible != null && !targetVisible.isEmpty())
			this.targetVisible = targetVisible;
		if (formStyle != null && !formStyle.isEmpty())
			this.formStyle = formStyle;
		if (gridStyle != null && !gridStyle.isEmpty())
			this.gridStyle = gridStyle;
	}

	public void update(String sequence, String entity, String entityIcon, String image, String target,
			String targetIcon, String type, String targetEntity, Boolean targetVisible,String formStyle,
			String gridStyle) {
		if (sequence != null && !sequence.isEmpty())
			this.sequence = sequence;
		if (entity != null && !entity.isEmpty())
			this.entity = entity;
		if (entityIcon != null && !entityIcon.isEmpty())
			this.entityIcon = entityIcon;
		if (image != null && !image.isEmpty())
			this.image = image;
		if (target != null && !target.isEmpty())
			this.target = target;
		if (targetIcon != null && !targetIcon.isEmpty())
			this.targetIcon = targetIcon;
		if (type != null && !type.isEmpty())
			this.type = type;
		if (targetEntity != null && !targetEntity.isEmpty())
			this.targetEntity = targetEntity;
		if (targetVisible != null && !targetEntity.isEmpty())
			this.targetVisible = targetVisible.toString();
		if (formStyle != null && !formStyle.isEmpty())
			this.formStyle = formStyle;
		if (gridStyle != null && !gridStyle.isEmpty())
			this.gridStyle = gridStyle;
	}

	public void updateProperty(String property, String propertyType, String range, Float ordinal, Boolean visible,
			String aggregate, String formatOptions, String height, String listStyle) {
		if (ordinal != null) {
			Float currentOrdinal = null;
			PropertyTemplate currentTemplate = null;
			for (PropertyTemplate propertyTemplate : properties.values()) {
				if (propertyTemplate.getProperty().equals(property)) {
					currentOrdinal = propertyTemplate.getOrdinal();
					currentTemplate = propertyTemplate;
					break;
				}
			}
			Float increment = (float) 0.0;
			while (properties.containsKey(ordinal + increment)) {
				increment = (float) (increment + 0.1);
			}
			if (currentOrdinal != null && currentTemplate != null) {
				PropertyTemplate propertyTemplate = new PropertyTemplate(property,
						(propertyType == null) ? currentTemplate.getPropertyType() : propertyType,
						(range == null) ? currentTemplate.getRange() : range, ordinal + increment,
						(visible == null) ? currentTemplate.getVisible() : visible,
						(aggregate == null) ? currentTemplate.getAggregate() : aggregate,
						(formatOptions == null) ? currentTemplate.getFormatOptions() : formatOptions,
						(height == null) ? currentTemplate.getHeight() : height	,	
						(listStyle == null) ? currentTemplate.getListStyle() : listStyle					
						);
				properties.remove(currentOrdinal);
				properties.put(ordinal + increment, propertyTemplate);
			}
		} else {
			for (PropertyTemplate propertyTemplate : properties.values()) {
				if (propertyTemplate.getProperty().equals(property)) {
					propertyTemplate.update(property, propertyType, range, ordinal, visible, aggregate, formatOptions, height,listStyle);
					break;
				}
			}
		}
	}
}
