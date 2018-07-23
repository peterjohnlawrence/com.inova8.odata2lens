package com.inova8.odata2lens;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.request.retrieve.EdmMetadataRequest;
import org.apache.olingo.client.api.communication.response.ODataRetrieveResponse;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.commons.api.edm.Edm;
import org.apache.olingo.commons.api.edm.EdmAnnotation;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmFunction;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.EdmSchema;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import com.inova8.odata2lens.NavigationProperty;

/**
 * Hello world!
 *
 */
public class GenerateLens {
	String workingPath;

	public static void main(String[] args) throws IOException, ODataException {
		String schemaName = args[0];//"northwind";
		Edm edm = null;
		edm = readEdm("http://localhost:8080/odata2sparql/" + schemaName);

		Properties props = setTemplateLocation();

		Velocity.init(props);

		(new File(getWorkingPath() + "\\" + schemaName + "\\" + "model")).mkdirs();
		(new File(getWorkingPath() + "\\" + schemaName + "\\" + "view")).mkdirs();
		(new File(getWorkingPath() + "\\" + schemaName + "\\" + "i18n")).mkdirs();

		TreeMap<String, EntityType> entityTypes = new TreeMap<String, EntityType>();
		createMetadata(edm, schemaName, entityTypes);
		generateUITemplate(schemaName, entityTypes);
		TreeMap<String, UITemplate> uiTemplates = readUITemplate(schemaName,entityTypes);
		generateRouting(schemaName, entityTypes);
		generateContextMenu(schemaName, entityTypes, uiTemplates);
		StringWriter i18nWriter = generateI18n();
		generateEntitySet(schemaName, entityTypes, i18nWriter, uiTemplates);
		generateEntity(schemaName, entityTypes, i18nWriter, uiTemplates);
		generateEntityNavigationSet(schemaName, entityTypes, i18nWriter, uiTemplates);

		FileWriter fw = new FileWriter(
				new File(getWorkingPath() + "\\" + schemaName + "\\" + "i18n\\", "i18n.properties"));
		fw.write(i18nWriter.toString());
		fw.close();
		//System.out.println(i18nWriter.toString());
	}

	private static TreeMap<String, UITemplate> readUITemplate(String schemaName , TreeMap<String, EntityType> entityTypes) throws IOException {

		BufferedReader uiTemplateReader = null;
		TreeMap<String, UITemplate> uiTemplates = new TreeMap<String, UITemplate>();
		
		uiTemplates = readUITemplateFile(getWorkingPath() + "\\" + schemaName + "\\uitemplate.generated.csv");
		try {
			String uiTemplateFile = getWorkingPath() + "\\" + schemaName + "\\uitemplate.csv";
			uiTemplateReader = new BufferedReader(new FileReader(uiTemplateFile));
			String line = "";
			String cvsSplitBy = ",";
			while ((line = uiTemplateReader.readLine()) != null) {
				String[] template = line.split(cvsSplitBy);
				if (template.length > 12) {
					String target = null;
					try {
						target = template[4].trim();
						UITemplate currentTemplate = uiTemplates.get(target);
						currentTemplate.update(template[0].trim(), template[1].trim(), template[2].trim(),
									template[3].trim(), template[4].trim(), template[5].trim(), template[6].trim(), template[7].trim());
						EntityType entityType = entityTypes.get(template[1].trim());
						if( !template[2].trim().isEmpty()) entityType.getEntitySet().setEntityIcon(template[2].trim());
						if( !template[3].trim().isEmpty()) entityType.getEntitySet().setImage(template[3].trim());
						if( !template[5].trim().isEmpty()) entityType.getEntity().setTargetIcon(template[5].trim());
						if(template.length == 15) currentTemplate.updateProperty(template[8].trim(), template[9].trim(), template[10].trim(),
								Float.parseFloat(template[11].trim()), Boolean.parseBoolean(template[12].trim()), template[13].trim(), template[14].trim());
						else currentTemplate.updateProperty(template[8].trim(), template[9].trim(), template[10].trim(),
								Float.parseFloat(template[11].trim()), Boolean.parseBoolean(template[12].trim()), template[13].trim(), "");
						

							
					} catch (Exception e) {
						int x=1;
					} finally {
					}
				}
			}
		} catch (FileNotFoundException e) {

		} finally {
			if (uiTemplateReader != null) {
				uiTemplateReader.close();
			}
		}
		return uiTemplates;
	}
	private static TreeMap<String, UITemplate> readUITemplateFile(String uiTemplateFile) throws IOException {

		BufferedReader uiTemplateReader = null;
		TreeMap<String, UITemplate> uiTemplates = new TreeMap<String, UITemplate>();
		try {
			uiTemplateReader = new BufferedReader(new FileReader(uiTemplateFile));
			String line = "";
			String cvsSplitBy = ",";
			String currentTarget = null;
			UITemplate currentTemplate = null;
			while ((line = uiTemplateReader.readLine()) != null) {
				String[] template = line.split(cvsSplitBy);
				if (template.length == 15) {
					String target = null;
					try {
						target = template[4].trim();
						if (!target.equals(currentTarget)) {
							currentTemplate = new UITemplate(template[0].trim(), template[1].trim(), template[2].trim(),
									template[3].trim(), template[4].trim(), template[5].trim(), template[6].trim(), template[7].trim());
							currentTarget = target;
							uiTemplates.put(target, currentTemplate);						
						}
						currentTemplate.getProperties().put(Float.parseFloat(template[11].trim()),
								new PropertyTemplate(template[8].trim(), template[9].trim(), template[10].trim(),
										Float.parseFloat(template[11].trim()), Boolean.parseBoolean(template[12].trim()), template[13].trim(), template[14].trim()));
					} catch (Exception e) {
					} finally {
					}
				}
			}
		} catch (FileNotFoundException e) {

		} finally {
			if (uiTemplateReader != null) {
				uiTemplateReader.close();
			}
		}
		return uiTemplates;
	}
	private static StringWriter generateI18n() {
		StringWriter i18nWriter = new StringWriter();
		Template i18nTemplate = Velocity.getTemplate("i18n.vm");
		StringWriter routingWriter = new StringWriter();
		VelocityContext i18nContext = new VelocityContext();
		i18nTemplate.merge(i18nContext, i18nWriter);
		return i18nWriter;
	}

	private static HashMap<String, String> getAnnotations(String defaultLabel, List<EdmAnnotation> annotations) {
		HashMap<String, String> annotationStrings = new HashMap<String, String>();
		String[] a = { "sap.label", "sap.heading", "sap.quickinfo" };
		int n = 0;
		for (EdmAnnotation annotation : annotations) {
			String expressionvalue = annotation.getExpression().asConstant().getValueAsString();
			if (expressionvalue != "") {
				annotationStrings.put(a[n], annotation.getExpression().asConstant().getValueAsString());
				defaultLabel = expressionvalue;
			} else {
				annotationStrings.put(a[n], defaultLabel);
			}
			n++;
		}
		return annotationStrings;
	}

	private static void createMetadata(Edm edm, String schemaName, TreeMap<String, EntityType> entityTypes)
			throws IOException {

		EdmSchema schema = edm.getSchema(schemaName);

		for (EdmEntityType edmEntityType : schema.getEntityTypes()) {
			if (!isFunctionImport(schema.getFunctions(), edmEntityType)) {
				//String value = edmEntityType.getAnnotations().get(0).getExpression().asConstant().getValueAsString();
				//This should work!! 
				// https://github.com/apache/olingo-odata4/blob/master/fit/src/test/java/org/apache/olingo/fit/tecsvc/client/BasicITCase.java
				//EdmAnnotation annotation =	edmEntityType.getAnnotation(edm.getTerm(new FullQualifiedName("sap", "label")), null);
				HashMap<String, String> entityTypeAnnotations = getAnnotations(edmEntityType.getName(),
						edmEntityType.getAnnotations());
				EntitySet entitySet = new EntitySet(edmEntityType.getName(), edmEntityType.getName() + "s",
						edmEntityType.getName() + "sDashBoard",
						entityTypeAnnotations.containsKey("sap.label") ? entityTypeAnnotations.get("sap.label") + "s"
								: edmEntityType.getName() + "s",
						entityTypeAnnotations.containsKey("sap.quickinfo")
								? entityTypeAnnotations.get("sap.quickinfo") + "s"
								: "Show " + edmEntityType.getName() + "s",
						"./images/icons/category.png", "sap-icon://expand-group"		
						);
				//value = edmEntityType.getAnnotations().get(0).getExpression().asConstant().getValueAsString()
				TreeMap<String, EntityNavigationSet> entityNavigationSets = new TreeMap<String, EntityNavigationSet>();
				TreeMap<String, NavigationProperty> navigationProperties = new TreeMap<String, NavigationProperty>();
				TreeMap<String, Property> properties = new TreeMap<String, Property>();
				for (String propertyName : edmEntityType.getPropertyNames()) {
					if (!propertyName.equals("subjectId") && !propertyName.equals("label")) {
						EdmProperty property = (EdmProperty) edmEntityType.getProperty(propertyName);
						HashMap<String, String> propertyAnnotations = getAnnotations(propertyName,
								property.getAnnotations());
						properties.put(propertyName,
								new Property(propertyName,
										propertyAnnotations.containsKey("sap.label")
												? propertyAnnotations.get("sap.label") : propertyName,
										propertyAnnotations.containsKey("sap.quickinfo")
												? propertyAnnotations.get("sap.quickinfo") : propertyName,property.getType().getFullQualifiedName().toString(),null)

								);
					}

				}

				for (String navigationPropertyName : edmEntityType.getNavigationPropertyNames()) {
					EdmNavigationProperty navigationProperty = edmEntityType
							.getNavigationProperty(navigationPropertyName);
					HashMap<String, String> navigationPropertyAnnotations = getAnnotations(navigationPropertyName,
							navigationProperty.getAnnotations());
					if (!isFunctionImport(schema.getFunctions(), navigationProperty.getType())) {
						if (navigationProperty.isCollection()) {
							entityNavigationSets.put(navigationPropertyName,
									new EntityNavigationSet(navigationPropertyName,
											edmEntityType.getName() + navigationPropertyName,
											navigationPropertyAnnotations.containsKey("sap.label")
													? navigationPropertyAnnotations.get("sap.label")
													: "Show " + edmEntityType.getName() + "s "
															+ navigationProperty.getType().getName(),
											navigationPropertyAnnotations.containsKey("sap.quickinfo")
													? navigationPropertyAnnotations.get("sap.quickinfo")
													: "Show " + edmEntityType.getName() + "s "
															+ navigationProperty.getType().getName(),
											navigationProperty.getType().getName(), navigationProperty.getType().getName(), ""));
						} else {
							navigationProperties.put(navigationPropertyName, new NavigationProperty(
									navigationPropertyName,
									navigationPropertyAnnotations.containsKey("sap.label")
											? navigationPropertyAnnotations.get("sap.label") : navigationPropertyName,
									navigationPropertyAnnotations.containsKey("sap.quickinfo")
											? navigationPropertyAnnotations.get("sap.quickinfo")
											: "Show " + navigationPropertyName,
									navigationProperty.getType().getName(), navigationProperty.getType().getName(), ""));
						}
					}
				}

				Entity entity = new Entity(edmEntityType.getName(), edmEntityType.getName(),
						entityTypeAnnotations.containsKey("sap.label") ? entityTypeAnnotations.get("sap.label")
								: edmEntityType.getName(),
						entityTypeAnnotations.containsKey("sap.quickinfo") ? entityTypeAnnotations.get("sap.quickinfo")
								: edmEntityType.getName(), "sap-icon://expand-group",
						entityNavigationSets, navigationProperties, properties);
				entityTypes.put(edmEntityType.getName(), new EntityType(entity, entitySet));
			}
		}
		for( EntityType entityType : entityTypes.values()) {
			for(NavigationProperty navigationProperty : entityType.getEntity().getNavigationProperties().values()) {
				
				navigationProperty.setRangeType(  entityTypes.get(navigationProperty.getRange()) );
				
			}
			for (EntityNavigationSet navigationSet : entityType.getEntity().getNavigationSet().values()) {
				navigationSet.setRangeType(  entityTypes.get(navigationSet.getRange()) );
			}
			
		}
	}

	private static boolean isFunctionImport(List<EdmFunction> edmFunctions, EdmEntityType edmEntityType) {
		// TODO Auto-generated method stub
		for (EdmFunction edmFunction : edmFunctions) {
			if (edmFunction.getReturnType().getType().getName().equals(edmEntityType.getName()))
				return true;
		}
		return false;
	}

	private static void generateRouting(String schemaName, TreeMap<String, EntityType> entityTypes) throws IOException {

		Template routingTemplate = null;

		routingTemplate = Velocity.getTemplate("routing.vm");
		StringWriter routingWriter = new StringWriter();
		VelocityContext routingContext = new VelocityContext();

		routingContext.put("schema", schemaName);
		routingContext.put("entityTypes", entityTypes.values());

		routingTemplate.merge(routingContext, routingWriter);

		FileWriter fw = new FileWriter(
				new File(getWorkingPath() + "\\" + schemaName + "\\" + "model\\", "routing.json"));
		fw.write(routingWriter.toString());
		fw.close();
		System.out.println(routingWriter.toString());

	}

	private static void generateUITemplate(String schemaName, TreeMap<String, EntityType> entityTypes)
			throws IOException {

		Template uiTemplate = null;

		uiTemplate = Velocity.getTemplate("uiTemplate.vm");
		StringWriter uiWriter = new StringWriter();
		VelocityContext uiContext = new VelocityContext();

		uiContext.put("schema", schemaName);
		uiContext.put("entityTypes", entityTypes);

		uiTemplate.merge(uiContext, uiWriter);

		FileWriter fw = new FileWriter(
				new File(getWorkingPath() + "\\" + schemaName + "\\", "uiTemplate.generated.csv"));
		fw.write(uiWriter.toString());
		fw.close();
		System.out.println(uiWriter.toString());
	}

	private static void generateContextMenu(String schemaName, TreeMap<String, EntityType> entityTypes,TreeMap<String, UITemplate>  uiTemplates)
			throws IOException {

		Template contextMenuTemplate = null;

		contextMenuTemplate = Velocity.getTemplate("contextMenu.vm");
		StringWriter contextMenuWriter = new StringWriter();
		VelocityContext contextMenuContext = new VelocityContext();

		contextMenuContext.put("schema", schemaName);
		contextMenuContext.put("entityTypes", entityTypes.values());

		contextMenuTemplate.merge(contextMenuContext, contextMenuWriter);

		FileWriter fw = new FileWriter(
				new File(getWorkingPath() + "\\" + schemaName + "\\" + "model\\", "contextMenu.json"));
		fw.write(contextMenuWriter.toString());
		fw.close();
		System.out.println(contextMenuWriter.toString());
	}

	private static void generateEntitySet(String schemaName, TreeMap<String, EntityType> entityTypes,
			StringWriter i18nWriter, TreeMap<String, UITemplate> uiTemplates) throws IOException {

		Template entitySetTemplate = null;
		Template i18nTemplate = null;

		entitySetTemplate = Velocity.getTemplate("entitySet.vm");
		i18nTemplate = Velocity.getTemplate("i18n.entitySet.vm");

		for (EntityType entityType : entityTypes.values()) {
			StringWriter entitySetWriter = new StringWriter();
			VelocityContext entitySetContext = new VelocityContext();

			entitySetContext.put("schema", schemaName);
			entitySetContext.put("entity", entityType.getEntity());
			entitySetContext.put("entitySet", entityType.getEntitySet());
			entitySetContext.put("uiTemplate", uiTemplates.get(entityType.getEntitySet().getTarget()));
			entitySetTemplate.merge(entitySetContext, entitySetWriter);

			i18nTemplate.merge(entitySetContext, i18nWriter);

			(new File(getWorkingPath() + "\\" + schemaName + "\\view\\" + entityType.getEntitySet().getTarget()))
					.mkdirs();
			FileWriter fw = new FileWriter(
					new File(getWorkingPath() + "\\" + schemaName + "\\view\\" + entityType.getEntitySet().getTarget()
							+ "\\" + entityType.getEntitySet().getTarget() + ".view.xml"));
			fw.write(entitySetWriter.toString());
			fw.close();
			System.out.println(entitySetWriter.toString());
			System.out.println(i18nWriter.toString());
		}

	}

	private static void generateEntity(String schemaName, TreeMap<String, EntityType> entityTypes,
			StringWriter i18nWriter, TreeMap<String, UITemplate> uiTemplates) throws IOException {

		Template entityTemplate = null;
		Template i18nTemplate = null;

		entityTemplate = Velocity.getTemplate("entity.vm");
		i18nTemplate = Velocity.getTemplate("i18n.entity.vm");

		for (EntityType entityType : entityTypes.values()) {
			StringWriter entityWriter = new StringWriter();
			VelocityContext entityContext = new VelocityContext();

			entityContext.put("schema", schemaName);
			entityContext.put("entity", entityType.getEntity());
			entityContext.put("entitySet", entityType.getEntitySet());
			entityContext.put("uiTemplate", uiTemplates.get(entityType.getEntity().getTarget()));
			entityTemplate.merge(entityContext, entityWriter);

			i18nTemplate.merge(entityContext, i18nWriter);

			(new File(getWorkingPath() + "\\" + schemaName + "\\view\\" + entityType.getEntity().getTarget())).mkdirs();
			FileWriter fw = new FileWriter(new File(getWorkingPath() + "\\" + schemaName + "\\view\\"
					+ entityType.getEntity().getTarget() + "\\" + entityType.getEntity().getTarget() + ".view.xml"));
			fw.write(entityWriter.toString());
			fw.close();
			System.out.println(entityWriter.toString());
			System.out.println(i18nWriter.toString());
		}

	}

	private static void generateEntityNavigationSet(String schemaName, TreeMap<String, EntityType> entityTypes,
			StringWriter i18nWriter, TreeMap<String, UITemplate> uiTemplates) throws IOException {

		Template entityNavigationSetTemplate = null;
		Template i18nTemplate = null;

		entityNavigationSetTemplate = Velocity.getTemplate("entityNavigationSet.vm");
		i18nTemplate = Velocity.getTemplate("i18n.entityNavigationSet.vm");

		for (EntityType entityType : entityTypes.values()) {
			for (EntityNavigationSet entityNavigationSet : entityType.getEntity().getNavigationSet().values()) {
				StringWriter entityNavigationSetWriter = new StringWriter();
				VelocityContext entityNavigationSetContext = new VelocityContext();

				entityNavigationSetContext.put("schema", schemaName);
				entityNavigationSetContext.put("entity",
						entityTypes.get(entityNavigationSet.getTargetEntityType()).getEntity());
				entityNavigationSetContext.put("entitySet",
						entityTypes.get(entityNavigationSet.getTargetEntityType()).getEntitySet());
				entityNavigationSetContext.put("entityNavigationSet", entityNavigationSet);
				entityNavigationSetContext.put("uiTemplate", uiTemplates.get(entityNavigationSet.getTarget()));
				entityNavigationSetTemplate.merge(entityNavigationSetContext, entityNavigationSetWriter);

				i18nTemplate.merge(entityNavigationSetContext, i18nWriter);

				(new File(getWorkingPath() + "\\" + schemaName + "\\view\\" + entityNavigationSet.getTarget()))
						.mkdirs();
				FileWriter fw = new FileWriter(new File(getWorkingPath() + "\\" + schemaName + "\\view\\"
						+ entityNavigationSet.getTarget() + "\\" + entityNavigationSet.getTarget() + ".view.xml"));
				fw.write(entityNavigationSetWriter.toString());
				fw.close();
				System.out.println(entityNavigationSetWriter.toString());
				System.out.println(i18nWriter.toString());
			}
		}

	}

	private static Properties setTemplateLocation() {
		Properties props = new Properties();
		String sPath = getWorkingPath();
		props.put("file.resource.loader.path", sPath);
		props.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogSystem");
		return props;
	}

	private static String getWorkingPath() {
		URL main = GenerateLens.class.getResource("GenerateLens.class");
		File file = new File(main.getPath());
		Path path = Paths.get(file.getPath());
		String sPath = path.getParent().toString();
		return sPath;
	}

	public static Edm readEdm(String serviceUrl) throws IOException, ODataException {
		ODataClient client = ODataClientFactory.getClient();
		EdmMetadataRequest request = client.getRetrieveRequestFactory().getMetadataRequest(serviceUrl);
		ODataRetrieveResponse<Edm> response = request.execute();
		Edm edm = response.getBody();
		return edm;

	}
}
