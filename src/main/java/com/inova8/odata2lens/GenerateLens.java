package com.inova8.odata2lens;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.request.retrieve.EdmMetadataRequest;
import org.apache.olingo.client.api.communication.response.ODataRetrieveResponse;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.commons.api.edm.Edm;
import org.apache.olingo.commons.api.edm.EdmAnnotation;
import org.apache.olingo.commons.api.edm.EdmComplexType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmFunction;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.EdmSchema;
import org.apache.olingo.commons.api.edm.annotation.EdmExpression;
import org.apache.olingo.commons.api.edm.annotation.EdmExpression.EdmExpressionType;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.inova8.odata2lens.NavigationProperty;
import com.inova8.uiTemplate.Form;
import com.inova8.uiTemplate.Grid;
import com.inova8.uiTemplate.Namespace;

@SuppressWarnings("unused")
public class GenerateLens {
	String workingPath;
	static TreeMap<String, EntityType> entityTypes = new TreeMap<String, EntityType>();
	static TreeMap<String, ComplexType> complexTypes = new TreeMap<String, ComplexType>();
	static String destinationPath;
	static String sourcePath;
	static String defaultNamespace;
	static HashSet<String> namespaceStrings;
	static TreeMap<String,String> namespaces = new TreeMap<String,String>();
	static TreeMap<String,String> prefixes = new TreeMap<String,String>();
	private static Boolean supportScripting;
	public static void main(String[] args) throws IOException, ODataException {
		generate(args[0], args[1], Arrays.copyOfRange(args, 2, args.length), getWorkingPath(), getWorkingPath());
	}

	public static boolean generate(String odataEndpoint, String apartmentName, String schemaNames[], String source,
			String destination) {
		System.out.println("Generating@ " + destination + "\\" + apartmentName);
		try {
			destinationPath = destination;
			sourcePath = source;
			Edm edm = null;
			edm = readEdm(odataEndpoint);

			Properties props = setTemplateLocation();

			Velocity.init(props);

			(new File(destinationPath + File.separator + apartmentName + File.separator + "model")).mkdirs();
			(new File(destinationPath + File.separator + apartmentName + File.separator + "view")).mkdirs();
			(new File(destinationPath + File.separator + apartmentName + File.separator + "i18n")).mkdirs();

			createMetadata(edm, schemaNames,apartmentName);
			deduceNamespaces();
			generateUITemplateJson(apartmentName);
			TreeMap<String, UITemplate> uiTemplates = readUITemplateJson(apartmentName);

			generateRouting(apartmentName);
			generateContextMenu(apartmentName, uiTemplates);
			generateManifest(apartmentName);
			generateUserPreference(apartmentName);
			StringWriter i18nWriter = generateI18n();
			generateEntitySet(apartmentName, i18nWriter, uiTemplates);
			generateEntity(apartmentName, i18nWriter, uiTemplates);
			//generateEntityNavigationSet(schemaName, i18nWriter, uiTemplates);

			FileWriter fw = new FileWriter(new File(
					getDestinationPath() + File.separator + apartmentName + File.separator + "i18n" + File.separator,
					"i18n.properties"));
			fw.write(i18nWriter.toString());
			fw.close();
			writeUITemplatesAsBuiltJson(apartmentName,uiTemplates);
			System.out.println(apartmentName + " generated");
			return true;
		} catch (Exception e) {
			System.out.println(apartmentName + " failed to generate\n" + e.toString());
			return false;
		}
	}
	public static void deduceNamespaces(){

		for(String namespacestring: namespaceStrings) {
			String[] splitNamespace = namespacestring.split(": <");
			String prefix = splitNamespace[0];
			String namespace = 	splitNamespace[1].substring(0, splitNamespace[1].length() - 1);
			namespaces.put(prefix, namespace);
			prefixes.put( namespace,prefix);
			String[] splitPrefix = prefix.split("\\.");
			if (splitPrefix.length>0) {
				String postfix = splitPrefix[splitPrefix.length - 1];
				EntityType entityType = entityTypes.get(postfix);
				if(entityType !=null) {
					entityType.getNamespaces().put(prefix, namespace);
				}
			}
		}		
	}

	@SuppressWarnings("unused")
	private static String extractUppercase(String prefix) {
		String namePrefix="";
		   for(int i=prefix.length()-1; i>=0; i--) {
		        if(Character.isUpperCase(prefix.charAt(i))) {
		        	namePrefix = prefix.charAt(i) + namePrefix ;
		        }
		    }	
		return namePrefix+"-";
	}

	private static TreeMap<String, UITemplate> readUITemplateJson(String apartmentName) throws IOException {

		TreeMap<String, UITemplate> uiTemplates;

		uiTemplates = readUITemplateJsonFile(
				getDestinationPath() + File.separator + apartmentName + File.separator + "uiTemplate.generated.json");
		try {
			//Overlay the template with the differences in the hand-crafted template file
			String uiTemplateFile = getSourcePath() + File.separator + apartmentName + File.separator
					+ "uiTemplate.json";
			JsonReader entitiesReader = new JsonReader(new FileReader(uiTemplateFile));
			Type targetClassType = new TypeToken<TreeMap<String, com.inova8.uiTemplate.Entity>>() {
			}.getType();
			TreeMap<String, com.inova8.uiTemplate.Entity> entities = new GsonBuilder().create().fromJson(entitiesReader,
					targetClassType);
			EntityType entityType;
			//	for (com.inova8.uiTemplate.Entity entity : entities.values()) {
			for (Entry<String, com.inova8.uiTemplate.Entity> entityEntry : entities.entrySet()) {
				com.inova8.uiTemplate.Entity entity = entityEntry.getValue();
				entityType = entityTypes.get(entityEntry.getKey());
				if (entityType != null) {
					if (entity.getIcon() != null && !entity.getIcon().isEmpty())
						entityType.getEntitySet().setEntityIcon(entity.getIcon());
					if (entity.getImage() != null && !entity.getImage().isEmpty())
						entityType.getEntitySet().setImage(entity.getImage());
					if (entity.getEntitySetVisible() != null && !entity.getEntitySetVisible().toString().isEmpty())
						entityType.setEntityTypeVisible(entity.getEntitySetVisible());

					if (entity.getNamespaces() != null ) {
						for( Namespace namespace : entity.getNamespaces()) {
							entityType.getNamespaces().put(namespace.getPrefix(), namespace.getNamespace());
						}
					}
					if (entity.getNamePattern() != null && !entity.getNamePattern().isEmpty())
						entityType.setNamePattern(entity.getNamePattern());	
					Form form = entity.getForm();
					if (form != null) {
						String formTarget = form.getTarget();
						UITemplate formTemplate = uiTemplates.get(formTarget);

						formTemplate.update(null, entity.getEntity(), entity.getIcon(), entity.getImage(),
								form.getTarget(), form.getTargetIcon(), "Form", form.getTargetEntity(),
								form.getTargetVisible(), form.getFormStyle(), null);
						if (form.getTargetIcon() != null && !form.getTargetIcon().isEmpty())
							entityType.getEntity().setTargetIcon(form.getTargetIcon());
						if (form.getTargetVisible() != null && !form.getTargetVisible().toString().isEmpty())
							entityType.getEntity().setVisible(form.getTargetVisible());
						if (form.getFormStyle() != null && !form.getFormStyle().toString().isEmpty())
							entityType.getEntity().setFormStyle(form.getFormStyle());
						if (form.getProperties() != null) {
							for (com.inova8.uiTemplate.Property property : form.getProperties()) {
								formTemplate.updateProperty(property.getProperty(), property.getPropertyType(),
										property.getPropertyRange(), property.getOrdinal(),
										property.getPropertyVisible(), property.getAggregate(),
										property.getFormatOptions(), property.getHeight(), property.getStyle(),
										property.getNullable(),property.getPropertyIRI(),property.getInverseProperty());
							}
						}
					}
					Grid grid = entity.getGrid();
					if (grid != null) {
						String gridTarget = grid.getTarget();
						UITemplate gridTemplate = uiTemplates.get(gridTarget);

						gridTemplate.update(null, entity.getEntity(), entity.getIcon(), entity.getImage(),
								grid.getTarget(), grid.getTargetIcon(), "Grid", grid.getTargetEntity(),
								grid.getTargetVisible(), null, grid.getGridStyle());
						if (grid.getTargetIcon() != null && !grid.getTargetIcon().isEmpty())
							entityType.getEntity().setTargetIcon(grid.getTargetIcon());
						if (grid.getTargetVisible() != null && !grid.getTargetVisible().toString().isEmpty())
							entityType.getEntitySet().setVisible(grid.getTargetVisible());
						if (grid.getGridStyle() != null && !grid.getGridStyle().toString().isEmpty())
							entityType.getEntitySet().setGridStyle(grid.getGridStyle());
						if (grid.getProperties() != null) {
							for (com.inova8.uiTemplate.Property property : grid.getProperties()) {
								gridTemplate.updateProperty(property.getProperty(), property.getPropertyType(),
										property.getPropertyRange(), property.getOrdinal(),
										property.getPropertyVisible(), property.getAggregate(),
										property.getFormatOptions(), property.getHeight(), property.getStyle(),
										property.getNullable(),property.getPropertyIRI(),property.getInverseProperty());
							}
						}
					}
				}else {
					System.out.println("UiTemplate refers to unrecognized entityType:" + entity.getEntity());
				}
			}

		} catch (FileNotFoundException e) {

		} finally {

		}
		return uiTemplates;
	}
	private static void writeUITemplatesAsBuiltJson(String apartmentName, TreeMap<String, UITemplate>  uiTemplates) throws IOException {

		Template uiTemplate = null;
		uiTemplate = Velocity.getTemplate("uiTemplate.asbuilt.json.vm");
		StringWriter uiWriter = new StringWriter();
		VelocityContext uiContext = new VelocityContext();
		uiContext.put("entityTypes", entityTypes);
		uiContext.put("uiTemplates", uiTemplates);

		uiTemplate.merge(uiContext, uiWriter);

		FileWriter fw = new FileWriter(new File(getDestinationPath() + File.separator + apartmentName + File.separator,
				"uiTemplate.asbuilt.json"));
		fw.write(uiWriter.toString());
		fw.close();
	}
	private static TreeMap<String, UITemplate> readUITemplateJsonFile(String uiTemplateFile) throws IOException {
		//Read the generated uitemplate file, derived from the metadata
		TreeMap<String, UITemplate> uiTemplates = new TreeMap<String, UITemplate>();
		try {
			JsonReader entitiesReader = new JsonReader(new FileReader(uiTemplateFile));
			Type targetClassType = new TypeToken<TreeMap<String, com.inova8.uiTemplate.Entity>>() {
			}.getType();
			TreeMap<String, com.inova8.uiTemplate.Entity> entities = new GsonBuilder().create().fromJson(entitiesReader,
					targetClassType);
			Integer sequence = 0;
			for (com.inova8.uiTemplate.Entity entity : entities.values()) {
				sequence++;
				Form form = entity.getForm();
				UITemplate formTemplate = new UITemplate(sequence.toString(), entity.getEntity(), entity.getIcon(),
						entity.getImage(), form.getTarget(), form.getTargetIcon(), "Form", form.getTargetEntity(),
						form.getTargetVisible().toString(), form.getFormStyle(), null);
				for (com.inova8.uiTemplate.Property property : form.getProperties()) {
					formTemplate.getProperties().put(property.getOrdinal(),
							new PropertyTemplate(property.getProperty(), property.getPropertyType(),
									property.getPropertyRange(), property.getOrdinal(), property.getPropertyVisible(),
									property.getAggregate(), property.getFormatOptions(), property.getHeight(),
									property.getStyle(), property.getNullable(), property.getPropertyIRI(), property.getInverseProperty()));
				}
				uiTemplates.put(form.getTarget(), formTemplate);
				sequence++;
				Grid grid = entity.getGrid();
				UITemplate gridTemplate = new UITemplate(sequence.toString(), entity.getEntity(), entity.getIcon(),
						entity.getImage(), grid.getTarget(), grid.getTargetIcon(), "Grid", grid.getTargetEntity(),
						grid.getTargetVisible().toString(), null, grid.getGridStyle());
				for (com.inova8.uiTemplate.Property property : grid.getProperties()) {
					gridTemplate.getProperties().put(property.getOrdinal(),
							new PropertyTemplate(property.getProperty(), property.getPropertyType(),
									property.getPropertyRange(), property.getOrdinal(), property.getPropertyVisible(),
									property.getAggregate(), property.getFormatOptions(), property.getHeight(),
									property.getStyle(), property.getNullable(), property.getPropertyIRI(), property.getInverseProperty()));
				}
				uiTemplates.put(grid.getTarget(), gridTemplate);
			}

		} catch (

		FileNotFoundException e) {

		} finally {

		}
		return uiTemplates;
	}

	private static StringWriter generateI18n() {
		StringWriter i18nWriter = new StringWriter();
		Template i18nTemplate = Velocity.getTemplate("i18n.vm");
		VelocityContext i18nContext = new VelocityContext();
		i18nTemplate.merge(i18nContext, i18nWriter);
		return i18nWriter;
	}

	private static HashMap<String, String> getAnnotations(String defaultLabel, List<EdmAnnotation> annotations) {
		HashMap<String, String> annotationStrings = new HashMap<String, String>();
		for (EdmAnnotation annotation : annotations) {
			if (!annotation.getExpression().getExpressionType().equals(EdmExpressionType.Collection)) {
				String expressionvalue = annotation.getExpression().asConstant().getValueAsString();
				String termFQN = annotation.getTerm().getFullQualifiedName().toString();
				if (expressionvalue != "") {
					annotationStrings.put(termFQN, annotation.getExpression().asConstant().getValueAsString());
					defaultLabel = expressionvalue;
				} else {
					annotationStrings.put(termFQN, defaultLabel);
				}
			}
		}
		return annotationStrings;
	}
	private static HashSet<String> getNamespacesAnnotations(List<EdmAnnotation> annotations) {
		HashSet<String> annotationStrings = new HashSet<String>();
		for (EdmAnnotation annotation : annotations) {
			String termFQN = annotation.getTerm().getFullQualifiedName().toString();
			if (termFQN.equals("odata.namespaces")) {
				if (annotation.getExpression().getExpressionType().equals(EdmExpressionType.Collection)) {
					List<EdmExpression> items = annotation.getExpression().asDynamic().asCollection().getItems();
					for (EdmExpression item : items) {
						annotationStrings.add(item.asConstant().getValueAsString());
					}
				}
			}
		}
		return annotationStrings;
	}
	private static HashSet<String> getBaseTypesAnnotations(List<EdmAnnotation> annotations) {
		HashSet<String> annotationStrings = new HashSet<String>();
		for (EdmAnnotation annotation : annotations) {
			String termFQN = annotation.getTerm().getFullQualifiedName().toString();
			if (termFQN.equals("odata.baseType")) {
				if (annotation.getExpression().getExpressionType().equals(EdmExpressionType.Collection)) {
					List<EdmExpression> items = annotation.getExpression().asDynamic().asCollection().getItems();
					for (EdmExpression item : items) {
						annotationStrings.add(item.asConstant().getValueAsString());
					}
				}
			}
		}
		return annotationStrings;
	}

	private static void createMetadata(Edm edm, String schemaNames[], String apartmentName) throws IOException {

		List<EdmEntitySet> entitySets = edm.getSchema("Instances").getEntityContainer().getEntitySets();
		defaultNamespace = getAnnotations("", edm.getSchema("Instances").getAnnotations())
				.get("odata.defaultNamespace");
		
		String supportScriptingText = getAnnotations("", edm.getSchema("Instances").getAnnotations())
				.get("odata.supportScripting");
		if(supportScriptingText.equals("true") )
			supportScripting=true;
		else
			supportScripting=false;
		namespaceStrings = getNamespacesAnnotations( edm.getSchema("Instances").getAnnotations());

		for (String schemaName : schemaNames) {
			EdmSchema schema = edm.getSchema(schemaName);
			if (schema != null) {
				if (schema.getComplexTypes() != null) {
					for (EdmComplexType edmComplexType : schema.getComplexTypes()) {
						ComplexType complexType = new ComplexType(edmComplexType.getName());
						complexTypes.put(edmComplexType.getFullQualifiedName().toString(), complexType);
						String subTypeName;
						for (String propertyName : edmComplexType.getPropertyNames()) {
							EdmProperty edmProperty = (EdmProperty) edmComplexType.getProperty(propertyName);
							//String name, String label, String tooltip, String range, String formatOptions
							subTypeName = getAnnotations("", edmProperty.getAnnotations()).get("odata.subType");

							HashMap<String, String> propertyAnnotations = getAnnotations(propertyName,
									edmProperty.getAnnotations());
							String type = edmProperty.getType().getFullQualifiedName().toString();
							type = propertyAnnotations.containsKey("odata.rdfType")
									? propertyAnnotations.get("odata.rdfType")
									: type;

							Property property = new Property(propertyName, propertyName, propertyName, type, null,
									false, "2rem", "Input", edmProperty.isNullable(),
									propertyAnnotations.containsKey("rdf.property")	? propertyAnnotations.get("rdf.property") : ""
										);
							if (subTypeName != null) {
								property.setSubTypeName(subTypeName);
								complexType.putProperty(subTypeName, property);
							}
						}
						for (String navigationPropertyName : edmComplexType.getNavigationPropertyNames()) {
							EdmNavigationProperty edmNavigationProperty = edmComplexType
									.getNavigationProperty(navigationPropertyName);
							subTypeName = getAnnotations("", edmNavigationProperty.getAnnotations())
									.get("odata.subType");
							HashMap<String, String> navigationPropertyAnnotations = getAnnotations(
									navigationPropertyName, edmNavigationProperty.getAnnotations());
							if (edmNavigationProperty.isCollection()) {
								//String name, String target, String label, String tooltip, String targetEntityType,String range, String icon
								EntityNavigationSet entityNavigationSet = new EntityNavigationSet(
										navigationPropertyName, navigationPropertyName, navigationPropertyName,
										navigationPropertyName, edmNavigationProperty.getType().getName(),
										edmNavigationProperty.getType().getName(), "", "2rem", "Multi",
										edmNavigationProperty.isNullable(),
										navigationPropertyAnnotations.containsKey("rdf.property")	? navigationPropertyAnnotations.get("rdf.property") : "",
										navigationPropertyAnnotations.containsKey("odata.inverseOf")	? navigationPropertyAnnotations.get("odata.inverseOf") : ""		
										);
								entityNavigationSet.setSubTypeName(subTypeName);
								//entityNavigationSet.setRangeType( );
								complexType.putNavigationSet(subTypeName, entityNavigationSet);
							} else {
								//String name, String label, String tooltip, String targetEntityType, String range,	String icon					
								NavigationProperty navigationProperty = new NavigationProperty(navigationPropertyName,
										navigationPropertyName, navigationPropertyName,
										edmNavigationProperty.getType().getName(),
										edmNavigationProperty.getType().getName(), null, "2rem", "Single",
										edmNavigationProperty.isNullable(),
										navigationPropertyAnnotations.containsKey("odata.isReifiedSubject")
										? (navigationPropertyAnnotations.get("odata.isReifiedSubject").equals("true"))
										: false,
										navigationPropertyAnnotations.containsKey("odata.isReifiedObject")
										? (navigationPropertyAnnotations.get("odata.isReifiedSubject").equals("true"))
										: false,
										navigationPropertyAnnotations.containsKey("odata.isReifiedPredicate")
										? (navigationPropertyAnnotations.get("odata.isReifiedPredicate").equals("true"))
										: false,
										navigationPropertyAnnotations.containsKey("rdf.property")	? navigationPropertyAnnotations.get("rdf.property") : "",
												navigationPropertyAnnotations.containsKey("odata.inverseOf")	? navigationPropertyAnnotations.get("odata.inverseOf") : ""		
										);
								
								navigationProperty.setSubTypeName(subTypeName);
								//navigationProperty.setRangeType(subTypeName);
								complexType.putNavigationProperty(subTypeName, navigationProperty);
							}
						}
					}
				}
				if (schema.getEntityTypes() != null) {
					for (EdmEntityType edmEntityType : schema.getEntityTypes()) {
						if (!isFunctionImport(schema.getFunctions(), edmEntityType)) {
							HashSet<String> baseTypes = getBaseTypesAnnotations(edmEntityType.getAnnotations());
							HashMap<String, String> entityTypeAnnotations = getAnnotations(edmEntityType.getName(),
									edmEntityType.getAnnotations());

							//					EdmEntitySet edmEntitySet = edm.getSchema("Instances").getEntityContainer()
							//							.getEntitySet(edmEntityType.getName());			

							EdmEntitySet edmEntitySet = entitySets.stream()
									.filter(entitySet -> edmEntityType.getFullQualifiedName()
											.equals(entitySet.getEntityType().getFullQualifiedName()))
									.findAny().orElse(null);

							HashMap<String, String> entitySetAnnotations = getAnnotations(edmEntityType.getName(),
									edmEntitySet.getAnnotations());

							EntitySet entitySet = new EntitySet(edmEntitySet.getName(),
									edmEntityType.getFullQualifiedName().toString().replace('.', '~'),
									edmEntityType.getName() + "s", edmEntityType.getName() + "sDashBoard",
									entitySetAnnotations.containsKey("sap.label")
											? entitySetAnnotations.get("sap.label")
											: (entityTypeAnnotations.containsKey("sap.label")
													? entityTypeAnnotations.get("sap.label") + "s"
													: edmEntityType.getName() + "s"),

									entitySetAnnotations.containsKey("sap.quickinfo")
											? entitySetAnnotations.get("sap.quickinfo")
											: (entityTypeAnnotations.containsKey("sap.quickinfo")
													? entityTypeAnnotations.get("sap.quickinfo") + "s"
													: "Show " + edmEntityType.getName() + "s"),

											"./apartment/"+ apartmentName + "/images/"+edmEntitySet.getName() +".svg", //"./images/icons/unknown.svg", 
									"sap-icon://group-2", true,
									entityTypeAnnotations.containsKey("odata.baseType")
											? entityTypeAnnotations.get("odata.baseType")
											: null,
									baseTypes,
									entityTypeAnnotations.containsKey("odata.isReifiedStatement")
									? entityTypeAnnotations.get("odata.isReifiedStatement").equals("true")
									: false

							);
							TreeMap<String, EntityNavigationSet> entityNavigationSets = new TreeMap<String, EntityNavigationSet>();
							TreeMap<String, NavigationProperty> navigationProperties = new TreeMap<String, NavigationProperty>();
							TreeMap<String, Property> properties = new TreeMap<String, Property>();
							for (String propertyName : edmEntityType.getPropertyNames()) {
								if (!propertyName.equals("subjectId") && !propertyName.equals("label")) {
									EdmProperty edmProperty = (EdmProperty) edmEntityType.getProperty(propertyName);
									HashMap<String, String> propertyAnnotations = getAnnotations(propertyName,
											edmProperty.getAnnotations());
									String type = edmProperty.getType().getFullQualifiedName().toString();
									type = propertyAnnotations.containsKey("odata.rdfType")
											? propertyAnnotations.get("odata.rdfType")
											: type;
									Property property = new Property(propertyName,
											propertyAnnotations.containsKey("sap.label")
													? propertyAnnotations.get("sap.label")
													: propertyName,
											propertyAnnotations.containsKey("sap.quickinfo")
													? propertyAnnotations.get("sap.quickinfo")
													: propertyName,
											type, null, propertyAnnotations.containsKey("odata.FK") ? true : false,
											"2rem", "Input", edmProperty.isNullable(),
											propertyAnnotations.containsKey("rdf.property")	? propertyAnnotations.get("rdf.property") : ""										
											);
									if (edmProperty.isPrimitive()) {
										//primitiveType = edmProperty.getType().getFullQualifiedName().toString();

									} else {
										ComplexType complexType = complexTypes.get(type);
										property.setComplexRange(complexType);
									}
									if(propertyAnnotations.containsKey("odata.isReifiedObject")&& (propertyAnnotations.get("odata.isReifiedObject").equals("true")) ) {
										entitySet.setReifiedObjectProperty(property);
									}
									properties.put(propertyName, property);
								}
							}

							for (String navigationPropertyName : edmEntityType.getNavigationPropertyNames()) {
								EdmNavigationProperty edmNavigationProperty = edmEntityType
										.getNavigationProperty(navigationPropertyName);
								HashMap<String, String> navigationPropertyAnnotations = getAnnotations(
										navigationPropertyName, edmNavigationProperty.getAnnotations());
								if (!isFunctionImport(schema.getFunctions(), edmNavigationProperty.getType())
										&& !isNavigationOutsideOfNamespace(schemaNames,
												edmNavigationProperty.getType())) {
									String range = "";
									if (edmNavigationProperty.getType().getNamespace().equals(schemaNames[0])) {
										range = edmNavigationProperty.getType().getName();
									} else {
										range = edmNavigationProperty.getType().getFullQualifiedName().toString()
												.replace(".", "_");
									}
									if (edmNavigationProperty.isCollection()) {
										EntityNavigationSet entityNavigationSet = new EntityNavigationSet(
												navigationPropertyName,
												edmEntityType.getName() + navigationPropertyName,
												navigationPropertyAnnotations.containsKey("sap.label")
														? navigationPropertyAnnotations.get("sap.label")
														: navigationPropertyName,
												navigationPropertyAnnotations.containsKey("sap.quickinfo")
														? navigationPropertyAnnotations.get("sap.quickinfo")
														: "Show " + edmEntityType.getName() + "s "
																+ edmNavigationProperty.getType().getName(),
												edmNavigationProperty.getType().getFullQualifiedName()
														.getFullQualifiedNameAsString(), //.getName(),
												range, "", "2rem", "Multi", edmNavigationProperty.isNullable(),
												navigationPropertyAnnotations.containsKey("rdf.property")	? navigationPropertyAnnotations.get("rdf.property") : "",
												navigationPropertyAnnotations.containsKey("odata.inverseOf")	? navigationPropertyAnnotations.get("odata.inverseOf") : ""		);

										entityNavigationSets.put(navigationPropertyName, entityNavigationSet);
									} else {
										NavigationProperty navigationProperty = new NavigationProperty(
												navigationPropertyName,
												navigationPropertyAnnotations.containsKey("sap.label")
														? navigationPropertyAnnotations.get("sap.label")
														: navigationPropertyName,
												navigationPropertyAnnotations.containsKey("sap.quickinfo")
														? navigationPropertyAnnotations.get("sap.quickinfo")
														: "Show " + navigationPropertyName,
												edmNavigationProperty.getType().getFullQualifiedName()
														.getFullQualifiedNameAsString(), //.getName(),
												range, "", "2rem", "Single", edmNavigationProperty.isNullable(),
												navigationPropertyAnnotations.containsKey("odata.isReifiedSubject")
												? (navigationPropertyAnnotations.get("odata.isReifiedSubject").equals("true"))
												: false,
												navigationPropertyAnnotations.containsKey("odata.isReifiedObject")
												? (navigationPropertyAnnotations.get("odata.isReifiedObject").equals("true"))
												: false,
												navigationPropertyAnnotations.containsKey("odata.isReifiedPredicate")
												? (navigationPropertyAnnotations.get("odata.isReifiedPredicate").equals("true"))
												: false,
												navigationPropertyAnnotations.containsKey("rdf.property")	? navigationPropertyAnnotations.get("rdf.property") : "",
												navigationPropertyAnnotations.containsKey("odata.inverseOf")	? navigationPropertyAnnotations.get("odata.inverseOf") : ""	);
										if(navigationPropertyAnnotations.containsKey("odata.isReifiedSubject")&& (navigationPropertyAnnotations.get("odata.isReifiedSubject").equals("true")) ) {
											entitySet.setReifiedSubjectPredicate(navigationProperty);
											if(navigationPropertyAnnotations.containsKey("odata.inverseOf"))entitySet.setReifiedInverseSubjectPredicate(navigationPropertyAnnotations.get("odata.inverseOf"));
										}
										if(navigationPropertyAnnotations.containsKey("odata.isReifiedObject")&& (navigationPropertyAnnotations.get("odata.isReifiedObject").equals("true")) ) {
											entitySet.setReifiedObjectPredicate(navigationProperty);
											if(navigationPropertyAnnotations.containsKey("odata.inverseOf"))entitySet.setReifiedInverseObjectPredicate(navigationPropertyAnnotations.get("odata.inverseOf"));
										}
										if(navigationPropertyAnnotations.containsKey("odata.isReifiedPredicate")&& (navigationPropertyAnnotations.get("odata.isReifiedPredicate").equals("true")) )entitySet.setReifiedPredicate(navigationProperty);

										navigationProperties.put(navigationPropertyName, navigationProperty);
									}
								}
							}

							Entity entity = new Entity(edmEntityType.getName(),
									edmEntityType.getFullQualifiedName().toString().replace('.', '~'),
									edmEntityType.getName(),
									entityTypeAnnotations.containsKey("sap.label")
											? entityTypeAnnotations.get("sap.label")
											: edmEntityType.getName(),
									entityTypeAnnotations.containsKey("sap.quickinfo")
											? entityTypeAnnotations.get("sap.quickinfo")
											: edmEntityType.getName(),
									"sap-icon://expand-group", entityNavigationSets, navigationProperties, properties);
							if (schema.getNamespace().equals(schemaNames[0])) {
								entityTypes.put(edmEntityType.getName(), new EntityType(entity, entitySet));
							} else {
								entityTypes.put(edmEntityType.getFullQualifiedName().toString().replace('.', '_'),
										new EntityType(entity, entitySet));
								//entityTypes.put(edmEntityType.getName(), new EntityType(entity, entitySet));
								//entityTypes.put(entity.getFqn(), new EntityType(entity, entitySet));
							}
						}
					}
				}
			}
		}

		for (String schemaName : schemaNames) {
			EdmSchema schema = edm.getSchema(schemaName);
			if (schema != null) {
				for (EntityType entityType : entityTypes.values()) {
					for (NavigationProperty navigationProperty : entityType.getEntity().getNavigationProperties()
							.values()) {
						navigationProperty.setRangeType(entityTypes.get(navigationProperty.getRange()));
					}
					for (EntityNavigationSet navigationSet : entityType.getEntity().getNavigationSet().values()) {
						navigationSet.setRangeType(entityTypes.get(navigationSet.getRange()));
					}
					for (Property property : entityType.getEntity().getProperties().values()) {
						if (property.getComplex()) {
							for (String subTypeName : property.getComplexRange().getProperties().keySet()) {
								entityType.getEntity().getSubTypeNames().add(subTypeName);
							}
							for (String subTypeName : property.getComplexRange().getNavigationProperties().keySet()) {
								entityType.getEntity().getSubTypeNames().add(subTypeName);
								//	complexNavigationProperty.setRangeType(entityType);
							}
							for (String subTypeName : property.getComplexRange().getNavigationSets().keySet()) {
								entityType.getEntity().getSubTypeNames().add(subTypeName);
								//	complexNavigationSet.setRangeType(entityTypes.get(complexNavigationSet.getRange()));
							}
						} else {
							entityType.getEntity().hasPrimitiveProperties(true);
						}
					}
					EntitySet entitySet = entityType.getEntitySet();
					if (!entitySet.getBaseTypes().isEmpty()) {
						for (String baseType : entitySet.getBaseTypes()) {
							String baseEntityTypeName = baseType;
							String[] namespacePrefix = baseType.split("~");
							if (namespacePrefix.length > 1) {
								if (namespacePrefix[0].equals(schemaNames[0])) {
									baseEntityTypeName = namespacePrefix[1];
								} else {
									baseEntityTypeName = namespacePrefix[0] + "_" + namespacePrefix[1];
								}
							}
							if (entityTypes.containsKey(baseEntityTypeName)) {
								EntityType baseEntityType = entityTypes.get(baseEntityTypeName);
								baseEntityType.getEntitySet().addChildEntitySet(entitySet);
								entitySet.addParentEntitySet(baseEntityType.getEntitySet());
							}
						}
					}
				}
			}
		}
		for (String schemaName : schemaNames) {
			EdmSchema schema = edm.getSchema(schemaName);
			if (schema != null) {
				for (EntityType entityType : entityTypes.values()) {
					entityType.setEntityTypeVisible(entityType.getEntitySet().getVisible());
				}
			}
		}
	}
 public static String toQName(String property) {
	 String qName;
	 int startOfLocalName = property.lastIndexOf("~");
	 if(startOfLocalName>=0) {
		 return property;
	 }else {
		 startOfLocalName = property.lastIndexOf("#");
		 if(startOfLocalName<=0)
		  startOfLocalName = property.lastIndexOf("/");
		 String namespace = property.substring(0, startOfLocalName+1);
		 String localname = property.substring(startOfLocalName+1);
		 qName = prefixes.get(namespace) + "~" + localname;
		 return qName;
	 }
 }
	private static boolean isNavigationOutsideOfNamespace(String schemaNames[], EdmEntityType entityType) {

		return !Arrays.stream(schemaNames).anyMatch(x -> entityType.getNamespace().equals(x));

	}

	private static boolean isFunctionImport(List<EdmFunction> edmFunctions, EdmEntityType edmEntityType) {
		for (EdmFunction edmFunction : edmFunctions) {
			if (edmFunction.getReturnType().getType().getName().equals(edmEntityType.getName()))
				return true;
		}
		return false;
	}

	private static void generateRouting(String schemaName) throws IOException {

		Template routingTemplate = null;

		routingTemplate = Velocity.getTemplate("routing.vm");
		StringWriter routingWriter = new StringWriter();
		VelocityContext routingContext = new VelocityContext();

		routingContext.put("schema", schemaName);
		routingContext.put("entityTypes", entityTypes.values());

		routingTemplate.merge(routingContext, routingWriter);

		FileWriter fw = new FileWriter(
				new File(getDestinationPath() + File.separator + schemaName + File.separator + "model" + File.separator,
						"routing.json"));
		fw.write(routingWriter.toString());
		fw.close();
	}

	private static void generateUITemplateJson(String apartmentName) throws IOException {

		Template uiTemplate = null;
		uiTemplate = Velocity.getTemplate("uiTemplate.json.vm");
		StringWriter uiWriter = new StringWriter();
		VelocityContext uiContext = new VelocityContext();

		uiContext.put("schema", apartmentName);
		uiContext.put("entityTypes", entityTypes);

		uiTemplate.merge(uiContext, uiWriter);

		FileWriter fw = new FileWriter(new File(getDestinationPath() + File.separator + apartmentName + File.separator,
				"uiTemplate.generated.json"));
		fw.write(uiWriter.toString());
		fw.close();
	}
	private static void generateManifest(String apartmentName)
			throws IOException {

		Template manifestTemplate = null;

		manifestTemplate = Velocity.getTemplate("manifest.vm");
		StringWriter manifestWriter = new StringWriter();
		VelocityContext manifestContext = new VelocityContext();

		manifestContext.put("schema", apartmentName);

		manifestTemplate.merge(manifestContext, manifestWriter);

		FileWriter fw = new FileWriter(new File(
				getDestinationPath() + File.separator + apartmentName  + File.separator,
				"manifest.json"));
		fw.write(manifestWriter.toString());
		fw.close();
	}
	private static void generateUserPreference(String apartmentName)
			throws IOException {

		Template userPreferenceTemplate = null;

		userPreferenceTemplate = Velocity.getTemplate("userPreference.vm");
		StringWriter userPreferenceWriter = new StringWriter();
		VelocityContext userPreferenceContext = new VelocityContext();

		userPreferenceContext.put("schema", apartmentName);

		userPreferenceTemplate.merge(userPreferenceContext, userPreferenceWriter);

		FileWriter fw = new FileWriter(new File(
				getDestinationPath() + File.separator + apartmentName  + File.separator,
				"userPreference.json"));
		fw.write(userPreferenceWriter.toString());
		fw.close();
	}
	private static void generateContextMenu(String apartmentName, TreeMap<String, UITemplate> uiTemplates)
			throws IOException {

		Template contextMenuTemplate = null;

		contextMenuTemplate = Velocity.getTemplate("contextMenu.vm");
		StringWriter contextMenuWriter = new StringWriter();
		VelocityContext contextMenuContext = new VelocityContext();

		contextMenuContext.put("schema", apartmentName);
		contextMenuContext.put("defaultNamespace", defaultNamespace);
		contextMenuContext.put("namespaces", namespaces);
		ArrayList<EntityType> entityTypesCollection = new ArrayList<EntityType>(entityTypes.values());
		entityTypesCollection.sort(Comparator.comparing(EntityType::getEntitySetLabel));
		contextMenuContext.put("entityTypes", entityTypesCollection);

		contextMenuTemplate.merge(contextMenuContext, contextMenuWriter);

		FileWriter fw = new FileWriter(new File(
				getDestinationPath() + File.separator + apartmentName + File.separator + "model" + File.separator,
				"contextMenu.json"));
		fw.write(contextMenuWriter.toString());
		fw.close();
	}

	private static void generateEntitySet(String apartmentName, StringWriter i18nWriter,
			TreeMap<String, UITemplate> uiTemplates) throws IOException {

		//		Template entitySet360Template = null;
		//		Template entitySetTableTemplate = null;
		//		Template i18nTemplate = null;

		Template entitySet360Template = Velocity.getTemplate("entitySet360.vm");
		Template entitySetTableTemplate = Velocity.getTemplate("entitySetTable.vm");
		Template i18nTemplate = Velocity.getTemplate("i18n.entitySet360.vm");

		for (EntityType entityType : entityTypes.values()) {
			StringWriter entitySetWriter = new StringWriter();
			VelocityContext entitySetContext = new VelocityContext();

			entitySetContext.put("schema", apartmentName);
			entitySetContext.put("supportScripting", supportScripting);
			entitySetContext.put("entity", entityType.getEntity());
			entitySetContext.put("entitySet", entityType.getEntitySet());
			entitySetContext.put("complexTypes", complexTypes);
			entitySetContext.put("uiTemplate", uiTemplates.get(entityType.getEntitySet().getTarget()));
			if (entityType.getEntitySet().getGridStyle().equals("entitySet360")) {
				entitySet360Template.merge(entitySetContext, entitySetWriter);
				i18nTemplate.merge(entitySetContext, i18nWriter);
			} else if (entityType.getEntitySet().getGridStyle() == null
					|| entityType.getEntitySet().getGridStyle().equals("entitySetTable")) {
				entitySetTableTemplate.merge(entitySetContext, entitySetWriter);
				i18nTemplate.merge(entitySetContext, i18nWriter);
			} else {

				System.out.println("Invalid template " + entityType.getEntitySet().getGridStyle());
			}
			(new File(getDestinationPath() + File.separator + apartmentName + File.separator + "view" + File.separator
					+ "entitySet" + File.separator + entityType.getEntitySet().getTarget())).mkdirs();
			
			String fileName = getDestinationPath() + File.separator + apartmentName + File.separator + "view"
					+ File.separator + "entitySet" + File.separator + entityType.getEntitySet().getTarget()
					+ File.separator + entityType.getEntitySet().getTarget() ;		
			
			formCreator(entitySetWriter, fileName);
		}

	}

	protected static void formCreator(StringWriter entitySetWriter, String fileName) throws IOException {
		File handCodedFile = new File(fileName + ".hc.view.xml");
		File targetFile= new File(fileName + ".view.xml");
		File generatedFile= new File(fileName + ".generated.view.xml");
		if(handCodedFile.exists()) {
			FileWriter fw = new FileWriter(	generatedFile);
			fw.write(entitySetWriter.toString());
			handCodedFile.renameTo(targetFile);
			fw.close();
		}else {
			FileWriter fw = new FileWriter(	targetFile);
			fw.write(entitySetWriter.toString());
			fw.close();
		}
	}

	private static void generateEntity(String apartmentName, StringWriter i18nWriter,
			TreeMap<String, UITemplate> uiTemplates) throws IOException {

		Template entityTemplate = null;
		Template i18nTemplate = null;

		entityTemplate = Velocity.getTemplate("entity360.vm");
		i18nTemplate = Velocity.getTemplate("i18n.entity360.vm");

		for (EntityType entityType : entityTypes.values()) {
			StringWriter entityWriter = new StringWriter();
			VelocityContext entityContext = new VelocityContext();

			entityContext.put("schema", apartmentName);
			entityContext.put("supportScripting", supportScripting);
			entityContext.put("entity", entityType.getEntity());
			entityContext.put("entitySet", entityType.getEntitySet());
			entityContext.put("complexTypes", complexTypes);
			entityContext.put("uiTemplate", uiTemplates.get(entityType.getEntity().getTarget()));
			entityTemplate.merge(entityContext, entityWriter);

			i18nTemplate.merge(entityContext, i18nWriter);

			(new File(getDestinationPath() + File.separator + apartmentName + File.separator + "view" + File.separator
					+ "entity" + File.separator + entityType.getEntity().getTarget())).mkdirs();
			
			String fileName = getDestinationPath() + File.separator + apartmentName + File.separator + "view"
					+ File.separator + "entity" + File.separator + entityType.getEntity().getTarget()
					+ File.separator + entityType.getEntity().getTarget() ;
			formCreator(entityWriter, fileName);
			
		}
	}

	private static Properties setTemplateLocation() {
		Properties props = new Properties();
		props.put(RuntimeConstants.RESOURCE_LOADER, "classpath");
		props.put("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
		props.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogSystem");

		return props;
	}

	private static String getWorkingPath() {
		URL main = GenerateLens.class.getResource("GenerateLens.class");
		File file = new File(main.getPath());
		Path path = Paths.get(file.getPath());
		String sPath = path.getParent().getParent().getParent().getParent().toString();
		return sPath.replace("%20", " ");
	}

	private static String getDestinationPath() {
		return destinationPath;
	}

	private static String getSourcePath() {
		return sourcePath;

	}

	public static Edm readEdm(String serviceUrl) throws IOException, ODataException {
		ODataClient client = ODataClientFactory.getClient();
		EdmMetadataRequest request = client.getRetrieveRequestFactory().getMetadataRequest(serviceUrl);
		ODataRetrieveResponse<Edm> response = request.execute();
		Edm edm = response.getBody();
		return edm;

	}

}
