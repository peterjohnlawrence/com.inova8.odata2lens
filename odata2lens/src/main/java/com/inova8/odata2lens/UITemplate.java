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
	private String targetVisible;
	private String image;
	private TreeMap<Float, PropertyTemplate> properties = new TreeMap<Float, PropertyTemplate>();

	public UITemplate(String sequence, String entity, String entityIcon, String image, String target, String targetIcon,
			String type, String targetEntity, String targetVisible) {
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

	public void update(String sequence, String entity, String entityIcon, String image, String target,
			String targetIcon, String type, String targetEntity, String targetVisible) {
		if (!sequence.isEmpty())
			this.sequence = sequence;
		if (!entity.isEmpty())
			this.entity = entity;
		if (!entityIcon.isEmpty())
			this.entityIcon = entityIcon;
		if (!image.isEmpty())
			this.image = image;
		if (!target.isEmpty())
			this.target = target;
		if (!targetIcon.isEmpty())
			this.targetIcon = targetIcon;
		if (!type.isEmpty())
			this.type = type;
		if (!targetEntity.isEmpty())
			this.targetEntity = targetEntity;
		if (!targetVisible.isEmpty())
			this.targetVisible = targetVisible;
	}

	public void updateProperty(String property, String propertyType, String range, Float ordinal, boolean visible,
			String aggregate, String formatOptions) {
		Float currentOrdinal = null;
		for (PropertyTemplate propertyTemplate : properties.values()) {
			if (propertyTemplate.getProperty().equals(property)) {
				currentOrdinal = propertyTemplate.getOrdinal();
				break;
			}
		}
		if (currentOrdinal != null) {
			PropertyTemplate propertyTemplate = new PropertyTemplate(property, propertyType, range, ordinal, visible,
					aggregate, formatOptions);
			properties.remove(currentOrdinal);
			properties.put(ordinal, propertyTemplate);
		}
	}
}
