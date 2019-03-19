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
import java.util.Properties;
import java.util.TreeMap;

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

public class GenerateLens {
	String workingPath;
	static TreeMap<String, EntityType> entityTypes = new TreeMap<String, EntityType>();
	static TreeMap<String, ComplexType> complexTypes = new TreeMap<String, ComplexType>();
	static String destinationPath;
	static String sourcePath;
	static String defaultNamespace;

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

			//		TreeMap<String, EntityType> entityTypes = new TreeMap<String, EntityType>();
			//		TreeMap<String, ComplexType> complexTypes = new TreeMap<String, ComplexType>();
			createMetadata(edm, schemaNames);
			//generateUITemplate(apartmentName);
			generateUITemplateJson(apartmentName);
			//TreeMap<String, UITemplate> uiTemplates = readUITemplate(schemaName);
			TreeMap<String, UITemplate> uiTemplates = readUITemplateJson(apartmentName);
			generateRouting(apartmentName);
			generateContextMenu(apartmentName, uiTemplates);
			StringWriter i18nWriter = generateI18n();
			generateEntitySet(apartmentName, i18nWriter, uiTemplates);
			generateEntity(apartmentName, i18nWriter, uiTemplates);
			//generateEntityNavigationSet(schemaName, i18nWriter, uiTemplates);

			FileWriter fw = new FileWriter(new File(
					getDestinationPath() + File.separator + apartmentName + File.separator + "i18n" + File.separator,
					"i18n.properties"));
			fw.write(i18nWriter.toString());
			fw.close();
			System.out.println(apartmentName + " generated");
			return true;
		} catch (Exception e) {
			System.out.println(apartmentName + " failed to generate\n" + e.toString());
			return false;
		}
	}

	private static TreeMap<String, UITemplate> readUITemplateJson(String apartmentName) throws IOException {

		TreeMap<String, UITemplate> uiTemplates;

		uiTemplates = readUITemplateJsonFile(
				getDestinationPath() + File.separator + apartmentName + File.separator + "uiTemplate.generated.json");
		try {
			String uiTemplateFile = getSourcePath() + File.separator + apartmentName + File.separator
					+ "uiTemplate.json";
			JsonReader entitiesReader = new JsonReader(new FileReader(uiTemplateFile));
			Type targetClassType = new TypeToken<TreeMap<String, com.inova8.uiTemplate.Entity>>() {
			}.getType();
			TreeMap<String, com.inova8.uiTemplate.Entity> entities = new GsonBuilder().create().fromJson(entitiesReader,
					targetClassType);
			EntityType entityType;
			for (com.inova8.uiTemplate.Entity entity : entities.values()) {
				entityType = entityTypes.get(entity.getEntity());
				if (entity.getIcon() != null && !entity.getIcon().isEmpty())
					entityType.getEntitySet().setEntityIcon(entity.getIcon());
				if (entity.getImage() != null && !entity.getImage().isEmpty())
					entityType.getEntitySet().setImage(entity.getImage());
				if (entity.getEntitySetVisible() != null && !entity.getEntitySetVisible().toString().isEmpty())
					entityType.getEntitySet().setVisible(entity.getEntitySetVisible());

				Form form = entity.getForm();
				if (form != null) {
					String formTarget = form.getTarget();
					UITemplate formTemplate = uiTemplates.get(formTarget);

					formTemplate.update(null, entity.getEntity(), entity.getIcon(), entity.getImage(), form.getTarget(),
							form.getTargetIcon(), "Form", form.getTargetEntity(), form.getTargetVisible(),
							form.getFormStyle(), null);
					if (form.getTargetIcon() != null && !form.getTargetIcon().isEmpty())
						entityType.getEntity().setTargetIcon(form.getTargetIcon());
					if (form.getTargetVisible() != null && !form.getTargetVisible().toString().isEmpty())
						entityType.getEntity().setVisible(form.getTargetVisible());
					if (form.getFormStyle() != null && !form.getFormStyle().toString().isEmpty())
						entityType.getEntity().setFormStyle(form.getFormStyle());
					for (com.inova8.uiTemplate.Property property : form.getProperties()) {
						formTemplate.updateProperty(property.getProperty(), property.getPropertyType(),
								property.getPropertyRange(), property.getOrdinal(), property.getPropertyVisible(),
								property.getAggregate(), property.getFormatOptions());
					}
				}
				Grid grid = entity.getGrid();
				if (grid != null) {
					String gridTarget = grid.getTarget();
					UITemplate gridTemplate = uiTemplates.get(gridTarget);

					gridTemplate.update(null, entity.getEntity(), entity.getIcon(), entity.getImage(), grid.getTarget(),
							grid.getTargetIcon(), "Grid", grid.getTargetEntity(), grid.getTargetVisible(), null,
							grid.getGridStyle());
					if (grid.getTargetIcon() != null && !grid.getTargetIcon().isEmpty())
						entityType.getEntity().setTargetIcon(grid.getTargetIcon());
					if (grid.getTargetVisible() != null && !grid.getTargetVisible().toString().isEmpty())
						entityType.getEntitySet().setVisible(grid.getTargetVisible());
					if (grid.getGridStyle() != null && !grid.getGridStyle().toString().isEmpty())
						entityType.getEntitySet().setGridStyle(grid.getGridStyle());
					for (com.inova8.uiTemplate.Property property : grid.getProperties()) {
						gridTemplate.updateProperty(property.getProperty(), property.getPropertyType(),
								property.getPropertyRange(), property.getOrdinal(), property.getPropertyVisible(),
								property.getAggregate(), property.getFormatOptions());
					}
				}
			}

		} catch (FileNotFoundException e) {

		} finally {

		}
		return uiTemplates;
	}

	private static TreeMap<String, UITemplate> readUITemplateJsonFile(String uiTemplateFile) throws IOException {
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
									property.getAggregate(), property.getFormatOptions()));
				}
				uiTemplates.put(form.getTarget(), formTemplate);
				sequence++;
				Grid grid = entity.getGrid();
				UITemplate gridTemplate = new UITemplate(sequence.toString(), entity.getEntity(), entity.getIcon(),
						entity.getImage(), form.getTarget(), form.getTargetIcon(), "Grid", form.getTargetEntity(),
						form.getTargetVisible().toString(), null, grid.getGridStyle());
				for (com.inova8.uiTemplate.Property property : grid.getProperties()) {
					gridTemplate.getProperties().put(property.getOrdinal(),
							new PropertyTemplate(property.getProperty(), property.getPropertyType(),
									property.getPropertyRange(), property.getOrdinal(), property.getPropertyVisible(),
									property.getAggregate(), property.getFormatOptions()));
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

	private static void createMetadata(Edm edm, String schemaNames[]) throws IOException {

		List<EdmEntitySet> entitySets = edm.getSchema("Instances").getEntityContainer().getEntitySets();
		defaultNamespace = getAnnotations("", edm.getSchema("Instances").getAnnotations())
				.get("odata.defaultNamespace");

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
							Property property = new Property(propertyName, propertyName, propertyName,
									edmProperty.getType().getFullQualifiedName().toString(), null, false);
							property.setSubTypeName(subTypeName);
							complexType.putProperty(subTypeName, property);
						}
						for (String navigationPropertyName : edmComplexType.getNavigationPropertyNames()) {
							EdmNavigationProperty edmNavigationProperty = edmComplexType
									.getNavigationProperty(navigationPropertyName);
							subTypeName = getAnnotations("", edmNavigationProperty.getAnnotations())
									.get("odata.subType");
							if (edmNavigationProperty.isCollection()) {
								//String name, String target, String label, String tooltip, String targetEntityType,String range, String icon
								EntityNavigationSet entityNavigationSet = new EntityNavigationSet(
										navigationPropertyName, navigationPropertyName, navigationPropertyName,
										navigationPropertyName, edmNavigationProperty.getType().getName(),
										edmNavigationProperty.getType().getName(), "");
								entityNavigationSet.setSubTypeName(subTypeName);
								//entityNavigationSet.setRangeType( );
								complexType.putNavigationSet(subTypeName, entityNavigationSet);
							} else {
								//String name, String label, String tooltip, String targetEntityType, String range,	String icon					
								NavigationProperty navigationProperty = new NavigationProperty(navigationPropertyName,
										navigationPropertyName, navigationPropertyName,
										edmNavigationProperty.getType().getName(),
										edmNavigationProperty.getType().getName(), null);
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

							EdmEntitySet edmEntitySet = entitySets.stream().filter(
									entitySet -> edmEntityType.getName().equals(entitySet.getEntityType().getName()))
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

									"./images/icons/unknown.svg", "sap-icon://group-2", true,
									entityTypeAnnotations.containsKey("odata.baseType")
											? entityTypeAnnotations.get("odata.baseType")
											: null,
											baseTypes

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

									Property property = new Property(propertyName,
											propertyAnnotations.containsKey("sap.label")
													? propertyAnnotations.get("sap.label")
													: propertyName,
											propertyAnnotations.containsKey("sap.quickinfo")
													? propertyAnnotations.get("sap.quickinfo")
													: propertyName,
											type, null, propertyAnnotations.containsKey("odata.FK") ? true : false);
									if (edmProperty.isPrimitive()) {
										//primitiveType = edmProperty.getType().getFullQualifiedName().toString();

									} else {
										ComplexType complexType = complexTypes.get(type);
										property.setComplexRange(complexType);
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
												edmNavigationProperty.getType().getName(),
												edmNavigationProperty.getType().getName(), "");

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
												edmNavigationProperty.getType().getName(),
												edmNavigationProperty.getType().getName(), "");
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
							entityTypes.put(edmEntityType.getName(), new EntityType(entity, entitySet));
						}
					}
					//Post process to identify parent and child entitySets, and sets of subTypes used in complex properties
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
								for (String subTypeName : property.getComplexRange().getNavigationProperties()
										.keySet()) {
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
								if (entityTypes.containsKey(baseType)) {
									EntityType baseEntityType = entityTypes.get(baseType);
									baseEntityType.getEntitySet().addChildEntitySet(entitySet);
									entitySet.addParentEntitySet(baseEntityType.getEntitySet());
								}
							}
						}
					}
				}
			}
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

	private static void generateContextMenu(String apartmentName, TreeMap<String, UITemplate> uiTemplates)
			throws IOException {

		Template contextMenuTemplate = null;

		contextMenuTemplate = Velocity.getTemplate("contextMenu.vm");
		StringWriter contextMenuWriter = new StringWriter();
		VelocityContext contextMenuContext = new VelocityContext();

		contextMenuContext.put("schema", apartmentName);
		contextMenuContext.put("defaultNamespace", defaultNamespace);
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
			FileWriter fw = new FileWriter(
					new File(getDestinationPath() + File.separator + apartmentName + File.separator + "view"
							+ File.separator + "entitySet" + File.separator + entityType.getEntitySet().getTarget()
							+ File.separator + entityType.getEntitySet().getTarget() + ".view.xml"));
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
			entityContext.put("entity", entityType.getEntity());
			entityContext.put("entitySet", entityType.getEntitySet());
			entityContext.put("complexTypes", complexTypes);
			entityContext.put("uiTemplate", uiTemplates.get(entityType.getEntity().getTarget()));
			entityTemplate.merge(entityContext, entityWriter);

			i18nTemplate.merge(entityContext, i18nWriter);

			(new File(getDestinationPath() + File.separator + apartmentName + File.separator + "view" + File.separator
					+ "entity" + File.separator + entityType.getEntity().getTarget())).mkdirs();
			FileWriter fw = new FileWriter(
					new File(getDestinationPath() + File.separator + apartmentName + File.separator + "view"
							+ File.separator + "entity" + File.separator + entityType.getEntity().getTarget()
							+ File.separator + entityType.getEntity().getTarget() + ".view.xml"));
			fw.write(entityWriter.toString());
			fw.close();
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
	//	@SuppressWarnings("unused")
	//	private static TreeMap<String, UITemplate> readUITemplate(String schemaName) throws IOException {
	//
	//		BufferedReader uiTemplateReader = null;
	//		TreeMap<String, UITemplate> uiTemplates = new TreeMap<String, UITemplate>();
	//
	//		uiTemplates = readUITemplateFile(
	//				getDestinationPath() + File.separator + schemaName + File.separator + "uitemplate.generated.csv");
	//		try {
	//			String uiTemplateFile = getSourcePath() + File.separator + schemaName + File.separator + "uitemplate.csv";
	//			uiTemplateReader = new BufferedReader(new FileReader(uiTemplateFile));
	//			String line = "";
	//			String cvsSplitBy = ",";
	//			Boolean firstLine = true;
	//			while ((line = uiTemplateReader.readLine()) != null) {
	//				if (firstLine) {
	//					firstLine = false;
	//				} else {
	//					String[] template = line.split(cvsSplitBy);
	//					if (template.length > 13) {
	//						String target = null;
	//						try {
	//							target = template[4].trim();
	//							UITemplate currentTemplate = uiTemplates.get(target);
	//							currentTemplate.update(template[0].trim(), template[1].trim(), template[2].trim(),
	//									template[3].trim(), template[4].trim(), template[5].trim(), template[6].trim(),
	//									template[7].trim(), template[8].trim());
	//							EntityType entityType = entityTypes.get(template[1].trim());
	//							if (!template[2].trim().isEmpty())
	//								entityType.getEntitySet().setEntityIcon(template[2].trim());
	//							if (!template[3].trim().isEmpty())
	//								entityType.getEntitySet().setImage(template[3].trim());
	//							if (!template[5].trim().isEmpty())
	//								entityType.getEntity().setTargetIcon(template[5].trim());
	//							if (!template[8].trim().isEmpty())
	//								entityType.getEntitySet().setVisible(Boolean.parseBoolean(template[8].trim()));
	//							if (template.length == 16)
	//								currentTemplate.updateProperty(template[9].trim(), template[10].trim(),
	//										template[11].trim(), Float.parseFloat(template[12].trim()),
	//										Boolean.parseBoolean(template[13].trim()), template[14].trim(),
	//										template[15].trim());
	//							else
	//								currentTemplate.updateProperty(template[9].trim(), template[10].trim(),
	//										template[11].trim(), Float.parseFloat(template[12].trim()),
	//										Boolean.parseBoolean(template[13].trim()), template[14].trim(), "");
	//						} catch (Exception e) {
	//						} finally {
	//						}
	//					}
	//				}
	//			}
	//		} catch (FileNotFoundException e) {
	//
	//		} finally {
	//			if (uiTemplateReader != null) {
	//				uiTemplateReader.close();
	//			}
	//		}
	//		return uiTemplates;
	//	}
	//	@SuppressWarnings("unused")
	//	private static TreeMap<String, UITemplate> readUITemplateFile(String uiTemplateFile) throws IOException {
	//
	//		BufferedReader uiTemplateReader = null;
	//		TreeMap<String, UITemplate> uiTemplates = new TreeMap<String, UITemplate>();
	//		try {
	//			uiTemplateReader = new BufferedReader(new FileReader(uiTemplateFile));
	//			String line = "";
	//			String cvsSplitBy = ",";
	//			Boolean firstLine = true;
	//			String currentTarget = null;
	//			UITemplate currentTemplate = null;
	//			while ((line = uiTemplateReader.readLine()) != null) {
	//				if (firstLine) {
	//					firstLine = false;
	//				} else {
	//					String[] template = line.split(cvsSplitBy);
	//					if (template.length == 16) {
	//						String target = null;
	//						try {
	//							target = template[4].trim();
	//							if (!target.equals(currentTarget)) {
	//								currentTemplate = new UITemplate(template[0].trim(), template[1].trim(),
	//										template[2].trim(), template[3].trim(), template[4].trim(), template[5].trim(),
	//										template[6].trim(), template[7].trim(), template[8].trim());
	//								currentTarget = target;
	//								uiTemplates.put(target, currentTemplate);
	//							}
	//							currentTemplate.getProperties().put(Float.parseFloat(template[12].trim()),
	//									new PropertyTemplate(template[9].trim(), template[10].trim(), template[11].trim(),
	//											Float.parseFloat(template[12].trim()),
	//											Boolean.parseBoolean(template[13].trim()), template[14].trim(),
	//											template[15].trim()));
	//						} catch (Exception e) {
	//						} finally {
	//						}
	//					}
	//				}
	//			}
	//		} catch (FileNotFoundException e) {
	//
	//		} finally {
	//			if (uiTemplateReader != null) {
	//				uiTemplateReader.close();
	//			}
	//		}
	//		return uiTemplates;
	//	}
	//	@SuppressWarnings("unused")
	//	private static void generateUITemplate(String schemaName) throws IOException {
	//
	//		Template uiTemplate = null;
	//
	//		uiTemplate = Velocity.getTemplate("uiTemplate.vm");
	//		StringWriter uiWriter = new StringWriter();
	//		VelocityContext uiContext = new VelocityContext();
	//
	//		uiContext.put("schema", schemaName);
	//		uiContext.put("entityTypes", entityTypes);
	//
	//		uiTemplate.merge(uiContext, uiWriter);
	//
	//		FileWriter fw = new FileWriter(new File(getDestinationPath() + File.separator + schemaName + File.separator,
	//				"uiTemplate.generated.csv"));
	//		fw.write(uiWriter.toString());
	//		fw.close();
	//	}

}
