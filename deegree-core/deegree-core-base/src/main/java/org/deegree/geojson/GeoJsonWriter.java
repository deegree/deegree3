package org.deegree.geojson;

import static org.deegree.commons.xml.CommonNamespaces.XSINS;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.gson.stream.JsonWriter;
import org.deegree.commons.tom.ElementNode;
import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.genericxml.GenericXMLElement;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.GenericFeature;
import org.deegree.feature.property.GenericProperty;
import org.deegree.feature.property.SimpleProperty;
import org.deegree.feature.types.property.CustomPropertyType;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.geometry.Geometry;
import org.deegree.gml.reference.FeatureReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stream-based writer for GeoJSON documents.
 * <p>
 * Instances of this class are not thread-safe.
 * </p>
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class GeoJsonWriter extends JsonWriter implements GeoJsonFeatureWriter, GeoJsonSingleFeatureWriter {

	private static final Logger LOG = LoggerFactory.getLogger(GeoJsonWriter.class);

	private static final QName XSI_NIL = new QName(XSINS, "nil", "xsi");

	private QName geometryPropertyToExport;

	private boolean skipExportAsWkt = true;

	private GeoJsonGeometryWriter geoJsonGeometryWriter;

	private final ICRS crs;

	private boolean isStarted = false;

	/**
	 * Instantiates a new {@link GeoJsonWriter}.
	 * @param writer the writer to write the GeoJSON into, never <code>null</code>
	 * @param crs the target crs of the geometries, may be <code>null</code>, then
	 * "EPSG:4326" will be used
	 * @throws UnknownCRSException if "crs:84" is not known as CRS (should never happen)
	 */
	public GeoJsonWriter(Writer writer, ICRS crs) throws UnknownCRSException {
		this(writer, crs, false);
	}

	/**
	 * Instantiates a new {@link GeoJsonWriter}.
	 * @param writer the writer to write the GeoJSON into, never <code>null</code>
	 * @param crs the target crs of the geometries, may be <code>null</code>, then
	 * "EPSG:4326" will be used
	 * @param skipGeometries <code>true</code> if geometries should not be exported,
	 * <code>false</code> otherwise
	 * @throws UnknownCRSException if "crs:84" is not known as CRS (should never happen)
	 */
	public GeoJsonWriter(Writer writer, ICRS crs, boolean skipGeometries) throws UnknownCRSException {
		super(writer);
		setIndent("  ");
		setHtmlSafe(true);
		if (!skipGeometries) {
			this.geoJsonGeometryWriter = new GeoJsonGeometryWriter(this, crs);
		}
		this.crs = crs;
	}

	/**
	 * Instantiates a new {@link GeoJsonWriter}.
	 * @param writer the writer to write the GeoJSON into, never <code>null</code>
	 * @param crs the target crs of the geometries, may be <code>null</code>, then
	 * "EPSG:4326" will be used
	 * @param geometryPropertyToExport the name of the geometry to export as GeoJSON
	 * geometry, required if multiple geometries available
	 * @param skipExportAsWkt <code>false</code> if geometries should be exported as WKT,
	 * <code>true</code> otherwise (default: true)
	 * @throws UnknownCRSException if "crs:84" is not known as CRS (should never happen)
	 */
	public GeoJsonWriter(Writer writer, ICRS crs, QName geometryPropertyToExport, boolean skipExportAsWkt)
			throws UnknownCRSException {
		this(writer, crs, false);
		this.geometryPropertyToExport = geometryPropertyToExport;
		this.skipExportAsWkt = skipExportAsWkt;
	}

	@Override
	public void close() throws IOException {
		this.isStarted = false;
		super.close();
	}

	@Override
	public void startFeatureCollection() throws IOException {
		beginObject();
		name("type").value("FeatureCollection");
	}

	@Override
	public void endFeatureCollection() throws IOException {
		if (isStarted)
			endArray();
		endObject();
	}

	@Override
	public void startSingleFeature() throws IOException {
		isStarted = true;
		beginObject();
		name("type").value("Feature");
	}

	@Override
	public void endSingleFeature() throws IOException {
		endObject();
	}

	@Override
	public void write(Feature feature) throws IOException, TransformationException, UnknownCRSException {
		QName featureName = feature.getName();
		LOG.debug("Exporting Feature {} with ID {}", featureName, feature.getId());

		if (!isStarted) {
			name("features").beginArray();
			isStarted = true;
		}
		startSingleFeature();
		writeSingleFeature(feature);
		endSingleFeature();
	}

	@Override
	public void writeSingleFeature(Feature feature) throws IOException, UnknownCRSException, TransformationException {
		name("id").value(feature.getId());
		Property exportedGeometryProperty = writeGeometry(feature);
		writeProperties(feature, exportedGeometryProperty);
		writeCrs();
	}

	private Property writeGeometry(Feature feature) throws IOException, UnknownCRSException, TransformationException {
		if (geoJsonGeometryWriter == null)
			return null;
		List<Property> geometryProperties = findGeometryProperties(feature);
		if (geometryProperties.isEmpty()) {
			name("geometry").nullValue();
			return null;
		}
		else if (geometryProperties.size() == 1) {
			return exportGeometry(geometryProperties.get(0));
		}
		else if (geometryPropertyToExport != null) {
			Optional<Property> mainGeometryFromTunable = geometryProperties.stream()
				.filter(geomProp -> geomProp.getName().equals(geometryPropertyToExport))
				.findFirst();
			if (mainGeometryFromTunable.isPresent()) {
				return exportGeometry(mainGeometryFromTunable.get());
			}
			else {
				throw new IOException("Could not write Feature as GeoJSON. Geometry to export ("
						+ geometryPropertyToExport + ") is not available.");
			}
		}
		else {
			throw new IOException("Could not write Feature as GeoJSON. The feature contains more than one geometry.");
		}
	}

	private Property exportGeometry(Property property)
			throws IOException, TransformationException, UnknownCRSException {
		name("geometry");
		Geometry value = (Geometry) property.getValue();
		geoJsonGeometryWriter.writeGeometry(value);
		return property;
	}

	private List<Property> findGeometryProperties(Feature feature) {
		// try default geometry properties
		List<Property> result = feature.getGeometryProperties();
		if (!result.isEmpty()) {
			return result;
		}

		// search for geometries in the properties recursively
		result = new ArrayList<>();

		// XXX would be nice to have a nice visitor pattern implementation for this model
		// instead of implementing traversal logic everywhere
		for (Property property : feature.getProperties()) {
			findGeometryPropertiesInProperty(property, result);
		}

		return result;
	}

	private void findGeometryPropertiesInProperty(Property property, List<Property> result) {
		if (property.getValue() instanceof Geometry) {
			result.add(property);
		}
		else {
			PropertyType propertyType = property.getType();
			if (propertyType instanceof CustomPropertyType) {
				findGeometryPropertiesInChildren(property, result);
			}
			else if (property instanceof GenericProperty) {
				findGeometryProperties(property.getValue(), result);
			}
		}
	}

	private void findGeometryProperties(TypedObjectNode node, List<Property> result) {
		if (node == null) {
			return;
		}
		if (node instanceof Property) {
			findGeometryPropertiesInProperty((Property) node, result);
		}
		else if (node instanceof GenericXMLElement) {
			findGeometryPropertiesInChildren((GenericXMLElement) node, result);
		}
		else if (node instanceof GenericFeature) {
			result.addAll(findGeometryProperties((GenericFeature) node));
		}
	}

	private void findGeometryPropertiesInChildren(ElementNode property, List<Property> result) {
		for (TypedObjectNode child : property.getChildren()) {
			if (child instanceof Property) {
				findGeometryPropertiesInProperty((Property) child, result);
			}
			else if (child instanceof GenericXMLElement) {
				findGeometryPropertiesInChildren((GenericXMLElement) child, result);
			}
		}
	}

	private void writeProperties(Feature feature, Property exportedGeometryProperty)
			throws IOException, TransformationException, UnknownCRSException {
		List<Property> properties = feature.getProperties();
		name("properties");
		if (properties.isEmpty()) {
			nullValue();
		}
		else {
			exportProperties(feature, exportedGeometryProperty);
		}
	}

	private void writeCrs() throws IOException {
		if (crs != null) {
			name("srsName");
			value(crs.getAlias());
		}
	}

	private void exportProperties(Feature feature, Property exportedGeometryProperty)
			throws IOException, UnknownCRSException, TransformationException {
		beginObject();

		List<QName> propertyNames = feature.getProperties()
			.stream()
			.map(property -> property.getName())
			.distinct()
			.collect(Collectors.toList());
		for (QName propertyName : propertyNames) {
			List<Property> properties = feature.getProperties(propertyName);
			exportPropertyByName(propertyName, properties, exportedGeometryProperty);
		}
		endObject();
	}

	private void exportPropertyByName(QName propertyName, List<Property> properties, Property exportedGeometryProperty)
			throws IOException, TransformationException, UnknownCRSException {
		if (properties.isEmpty()) {
			return;
		}
		Property firstProperty = properties.get(0);
		if (firstProperty.getType() instanceof GeometryPropertyType && (skipExportAsWkt
				|| propertyName.equals(exportedGeometryProperty != null ? exportedGeometryProperty.getName() : null))) {
			return;
		}
		if (properties.size() == 1 && !isNilledAndHasNoOtherAttributesOrProperties(firstProperty)) {
			name(propertyName.getLocalPart());
			export(firstProperty);
		}
		else if (properties.size() > 1) {
			name(propertyName.getLocalPart());
			beginArray();
			for (Property property : properties) {
				export(property);
			}
			endArray();
		}
	}

	private void export(Property property) throws IOException, TransformationException, UnknownCRSException {
		PropertyType propertyType = property.getType();
		if (property instanceof SimpleProperty) {
			exportValue(((SimpleProperty) property).getValue());
		}
		else if (propertyType instanceof CustomPropertyType) {
			exportAttributesAndChildren(property);
		}
		else if (propertyType instanceof FeaturePropertyType) {
			exportFeaturePropertyType(property);
		}
		else if (propertyType instanceof GeometryPropertyType) {
			exportGeometryPropertyTypeAsWkt(property);
		}
		else if (property instanceof GenericProperty) {
			exportGenericProperty((GenericProperty) property);
		}
		else {
			throw new IOException("Unhandled property type '" + property.getClass() + "' (property name "
					+ property.getName() + " )");
		}
	}

	private void exportGenericProperty(GenericProperty property)
			throws IOException, TransformationException, UnknownCRSException {
		TypedObjectNode value = property.getValue();
		if (value instanceof PrimitiveValue) {
			exportValue((PrimitiveValue) value);
		}
		else {
			export(value);
		}
	}

	private void exportFeaturePropertyType(Property property)
			throws IOException, UnknownCRSException, TransformationException {
		QName propertyName = property.getName();
		LOG.debug("Exporting feature property '{}'", propertyName);
		if (property instanceof Feature) {
			if (property == null) {
				nullValue();
			}
			else {
				write((Feature) property);
			}
		}
		else if (property instanceof FeatureReference) {
			beginObject();
			name("href");
			value(((FeatureReference) property).getURI());
			endObject();
		}
		else {
			Map<QName, PrimitiveValue> attributes = property.getAttributes();
			if (attributes.size() > 0) {
				beginObject();
				for (Map.Entry<QName, PrimitiveValue> attribute : attributes.entrySet()) {
					name(attribute.getKey().getLocalPart());
					exportValue(attribute.getValue());
				}
				endObject();
			}
			else {
				nullValue();
			}
		}
	}

	private void exportGeometryPropertyTypeAsWkt(Property property)
			throws IOException, TransformationException, UnknownCRSException {
		TypedObjectNode value = property.getValue();
		if (value instanceof Geometry) {
			geoJsonGeometryWriter.writeWktGeometry((Geometry) value);
		}
	}

	private void exportAttributesAndChildren(ElementNode elementNode)
			throws IOException, TransformationException, UnknownCRSException {
		Map<QName, PrimitiveValue> attributes = retrieveAttributes(elementNode);
		List<TypedObjectNode> children = retrieveChildren(elementNode);
		Map<QName, List<TypedObjectNode>> attributesAndChildren = children.stream()
			.collect(Collectors.groupingBy(childNode -> {
				if (childNode instanceof Property) {
					return ((Property) childNode).getName();
				}
				else if (childNode instanceof GenericXMLElement) {
					return ((GenericXMLElement) childNode).getName();
				}
				else if (childNode instanceof FeatureReference) {
					return new QName("href");
				}
				return new QName("value");
			}));
		attributes.forEach(
				(qName, primitiveValue) -> attributesAndChildren.put(qName, Collections.singletonList(primitiveValue)));

		if (attributesAndChildren.isEmpty()) {
			nullValue();
		}
		else if (attributesAndChildren.size() == 1) {
			QName key = attributesAndChildren.keySet().iterator().next();
			List<TypedObjectNode> values = attributesAndChildren.get(key);
			if (values.size() == 1 && values.get(0) instanceof PrimitiveValue) {
				exportValue((PrimitiveValue) values.get(0));
			}
			else if (values.size() == 1) {
				beginObject();
				export(values.get(0));
				endObject();
			}
			else if (values.size() > 1) {
				beginObject();
				name(key.getLocalPart());
				beginArray();
				for (TypedObjectNode value : values) {
					exportValue(value);
				}
				endArray();
				endObject();
			}
		}
		else {
			beginObject();
			for (Map.Entry<QName, List<TypedObjectNode>> childNode : attributesAndChildren.entrySet()) {
				List<TypedObjectNode> values = childNode.getValue();
				if (values.size() == 0) {
					nullValue();
				}
				else if (values.size() == 1) {
					TypedObjectNode value = values.get(0);
					if (value instanceof PrimitiveValue) {
						name(childNode.getKey().getLocalPart());
						exportValue((PrimitiveValue) value);
					}
					else {
						export(value);
					}
				}
				else {
					name(childNode.getKey().getLocalPart());
					beginArray();
					for (TypedObjectNode value : values) {
						exportValue(value);
					}
					endArray();
				}
			}
			endObject();
		}
	}

	private void export(TypedObjectNode node) throws IOException, UnknownCRSException, TransformationException {
		if (node == null) {
			LOG.warn("Null node found.");
			return;
		}
		if (node instanceof PrimitiveValue) {
			PrimitiveValue primitiveValue = (PrimitiveValue) node;
			exportValue(primitiveValue);
		}
		else if (node instanceof Property) {
			Property prop = (Property) node;
			PropertyType type = prop.getType();

			if (type instanceof GeometryPropertyType) {
				// don't write geometry properties, geometry is written before
			}
			else if (!isNilledAndHasNoOtherAttributesOrProperties(node)) {
				name(((Property) node).getName().getLocalPart());
				export((Property) node);
			}
		}
		else if (node instanceof GenericXMLElement) {
			if (!isNilledAndHasNoOtherAttributesOrProperties(node)) {
				name(((GenericXMLElement) node).getName().getLocalPart());
				exportAttributesAndChildren((GenericXMLElement) node);
			}
		}
		else if (node instanceof FeatureReference) {
			name("href");
			value(((FeatureReference) node).getURI());
		}
		else {
			throw new IOException("Unhandled node type '" + node.getClass());
		}
	}

	private void exportValue(TypedObjectNode node) throws IOException, UnknownCRSException, TransformationException {
		if (node == null) {
			LOG.warn("Null node found.");
			return;
		}
		if (node instanceof PrimitiveValue) {
			PrimitiveValue primitiveValue = (PrimitiveValue) node;
			exportValue(primitiveValue);
		}
		else if (node instanceof Property) {
			export((Property) node);
		}
		else if (node instanceof GenericXMLElement) {
			exportAttributesAndChildren((GenericXMLElement) node);
		}
		else if (node instanceof FeatureReference) {
			value(((FeatureReference) node).getURI());
		}
		else {
			throw new IOException("Unhandled node type '" + node.getClass());
		}
	}

	private void exportValue(PrimitiveValue value) throws IOException {
		if (value == null) {
			nullValue();
		}
		else {
			switch (value.getType().getBaseType()) {
				case BOOLEAN:
					value((Boolean) value.getValue());
					break;
				case INTEGER:
					value((BigInteger) value.getValue());
					break;
				case DOUBLE:
					value((Double) value.getValue());
					break;
				case DECIMAL:
					value((BigDecimal) value.getValue());
					break;
				default:
					value(value.getAsText());
					break;
			}
		}
	}

	private Map<QName, PrimitiveValue> retrieveAttributes(ElementNode elementNode) {
		Map<QName, PrimitiveValue> attributes = elementNode.getAttributes();
		if (attributes != null)
			return attributes;
		return Collections.emptyMap();
	}

	private List<TypedObjectNode> retrieveChildren(ElementNode elementNode) {
		List<TypedObjectNode> children = elementNode.getChildren();
		if (children != null)
			return children;
		return Collections.emptyList();
	}

	private boolean isNilledAndHasNoOtherAttributesOrProperties(TypedObjectNode node) {
		if (node instanceof Property) {
			Property property = (Property) node;
			return isNilledAndHasNoOtherAttributesOrProperties(property.getChildren(), property.getAttributes());
		}
		else if (node instanceof GenericXMLElement) {
			GenericXMLElement genericXMLElement = (GenericXMLElement) node;
			return isNilledAndHasNoOtherAttributesOrProperties(genericXMLElement.getChildren(),
					genericXMLElement.getAttributes());
		}
		return false;
	}

	private static boolean isNilledAndHasNoOtherAttributesOrProperties(List<TypedObjectNode> children,
			Map<QName, PrimitiveValue> attributes) {
		if ((children == null || children.isEmpty()) && attributes != null && attributes.size() == 1) {
			TypedObjectNode nil = attributes.get(XSI_NIL);
			if (nil instanceof PrimitiveValue) {
				return Boolean.TRUE.equals(((PrimitiveValue) nil).getValue());
			}
		}
		return false;
	}

	void setSkipWktExport(boolean skipWktExport) {
		this.skipExportAsWkt = skipWktExport;
	}

	void setGeometryPropertyToExport(QName geometryPropertyToExport) {
		this.geometryPropertyToExport = geometryPropertyToExport;
	}

}
