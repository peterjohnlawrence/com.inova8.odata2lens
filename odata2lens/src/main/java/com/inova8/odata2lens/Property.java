package com.inova8.odata2lens;

public class Property {
	String name;
	String label;
	String tooltip;
	String range;
	Boolean isFK=false;
	private String subTypeName;
	ComplexType complexRange;
	String formatOptions="";
	String height="";
	String style="List";
	String styletag ="Input ";
	Boolean nullable = true;

	public Property(String name, String label, String tooltip, String range, String formatOptions, Boolean isFK, String height, String style,Boolean nullable) {
		this.name = name;

		this.label = label;
		this.tooltip = tooltip;
		this.range = range;
		
		if(formatOptions==null) {
			if(range.equals("Edm.Double")) {
				this.formatOptions = "{'style':'short';'maxFractionDigits':2}";
			}else {
				this.formatOptions ="";
			}
		}else {
			this.formatOptions = formatOptions;
		}
		this.isFK = isFK;	
		if(height==null|| height=="") {
			this.height="2rem";
		}else {
			this.height=height;
		}
		if(style==null|| style=="") {
			this.style="List";
		}else {
			this.style=style;
		}
		if(nullable==null) {
			this.nullable=true;
		}else {
			this.nullable=nullable;
		}

	}

	public String getName() {
		return name;
	}

	public String getLabel() {
		return label;
	}

	public String getTooltip() {
		return tooltip;
	}

	public String getSubTypeName() {
		return subTypeName;
	}

	public void setSubTypeName(String subTypeName) {
		this.subTypeName = subTypeName;
	}

	public String getRange() {
		return range;
	}

	public String getFormatOptions() {
		return formatOptions;
	}



	public ComplexType getComplexRange() {
		return complexRange;
	}

	public void setComplexRange(ComplexType complexRange) {
		this.complexRange = complexRange;
	}

	public Boolean getComplex() {
		if (this.complexRange != null)
			return true;
		else
			return false;
	}

	public Boolean getPrimitive() {
		if (this.complexRange != null)
			return false;
		else
			return true;
	}

	public Boolean getIsFK() {
		return isFK;
	}
	public Boolean getIsNotFK() {
		return !isFK;
	}
	public String getHeight() {
		return height;
	}
	public String getStyle() {
		return style;
	}
	public String getStyletag() {
		return styletag;
	}
	public Boolean getNullable() {
		return nullable;
	}
}
