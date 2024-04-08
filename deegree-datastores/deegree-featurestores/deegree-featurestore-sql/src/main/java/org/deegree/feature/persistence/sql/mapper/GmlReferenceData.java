package org.deegree.feature.persistence.sql.mapper;

import org.deegree.commons.tom.ElementNode;
import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.GenericFeature;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLVersion;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.deegree.commons.xml.CommonNamespaces.XSINS;
import static org.deegree.commons.xml.CommonNamespaces.XSI_PREFIX;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class GmlReferenceData implements ReferenceData {

	private Map<QName, List<Feature>> features = new HashMap<>();

	public GmlReferenceData(URL referenceData) throws IOException, XMLStreamException, UnknownCRSException {
		GMLStreamReader gmlStreamReader = GMLInputFactory.createGMLStreamReader(GMLVersion.GML_32, referenceData);
		FeatureCollection featureCollection = gmlStreamReader.readFeatureCollection();
		addFeatures(featureCollection);
	}

	private void addFeatures(FeatureCollection featureCollection) {
		Iterator<Feature> iterator = featureCollection.iterator();
		while (iterator.hasNext()) {
			Feature feature = iterator.next();
			addFeature(feature);
			List<Property> properties = feature.getProperties();
			for (Property prop : properties) {
				// add inline features
				if (prop.getValue() instanceof GenericFeature) {
					Feature inlineFeature = (Feature) prop.getValue();
					addFeature(inlineFeature);
				}
			}
		}
	}

	private void addFeature(Feature feature) {
		QName name = feature.getName();
		if (!features.containsKey(name)) {
			features.put(name, new ArrayList<>());
		}
		features.get(name).add(feature);
	}

	@Override
	public boolean hasProperty(QName featureTypeName, List<PathStep> xpath) {
		List<Feature> featuresOfType = this.features.get(featureTypeName);
		if (featuresOfType != null && !featuresOfType.isEmpty()) {
			for (Feature feature : featuresOfType) {
				if (hasProperty(feature, xpath))
					return true;
			}
		}
		return false;
	}

	@Override
	public boolean isPropertyNilled(QName featureTypeName, List<PathStep> xpath) {
		List<Feature> featuresOfType = this.features.get(featureTypeName);
		if (featuresOfType != null && !featuresOfType.isEmpty()) {
			for (Feature feature : featuresOfType) {
				if (!hasNilledPropertyOrIsMissing(feature, xpath))
					return false;
			}
		}
		return true;
	}

	@Override
	public boolean hasZeroOrOneProperty(QName featureTypeName, List<PathStep> xpath) {
		List<Feature> featuresOfType = this.features.get(featureTypeName);
		if (featuresOfType != null && !featuresOfType.isEmpty()) {
			for (Feature feature : featuresOfType) {
				if (hasMoreThanOne(feature, xpath))
					return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean shouldFeatureTypeMapped(QName featureTypeName) {
		return features.containsKey(featureTypeName);
	}

	private boolean hasProperty(Feature feature, List<PathStep> xpath) {
		if (xpath.isEmpty())
			return true;
		Iterator<PathStep> iterator = xpath.iterator();
		PathStep firstProperty = getNext(iterator);
		List<Property> properties = feature.getProperties(firstProperty.getName());
		return hasProperty(iterator, properties);
	}

	private <T extends TypedObjectNode> boolean hasProperty(Iterator<PathStep> iterator, List<T> properties) {
		PathStep next = getNext(iterator);
		if (next == null) {
			if (properties.size() >= 1)
				return true;
			else
				return false;
		}
		else {
			for (TypedObjectNode property : properties) {
				List<TypedObjectNode> subProperties = getChildsByName(property, next);
				if (hasProperty(iterator, subProperties))
					return true;
			}
			return false;
		}
	}

	private boolean hasNilledPropertyOrIsMissing(Feature feature, List<PathStep> xpath) {
		if (xpath.isEmpty()) {
			return true;
		}
		Iterator<PathStep> iterator = xpath.iterator();
		PathStep firstProperty = getNext(iterator);
		List<Property> properties = feature.getProperties(firstProperty.getName());
		return hasNilledPropertyOrIsMissing(iterator, properties);
	}

	private <T extends TypedObjectNode> boolean hasNilledPropertyOrIsMissing(Iterator<PathStep> iterator,
			List<T> properties) {
		PathStep next = getNext(iterator);
		if (next != null) {
			for (TypedObjectNode property : properties) {
				List<TypedObjectNode> subProperties = getChildsByName(property, next);
				if (hasNilledPropertyOrIsMissing(iterator, subProperties))
					return true;
			}
		}
		else if (!properties.isEmpty()) {
			for (TypedObjectNode property : properties) {
				if (!isNilTrue(property))
					return false;
			}
			return true;
		}
		return false;
	}

	private boolean isNilTrue(TypedObjectNode property) {
		if (!(property instanceof ElementNode))
			return false;
		Map<QName, PrimitiveValue> attributes = ((ElementNode) property).getAttributes();
		PrimitiveValue nil = attributes.get(new QName(XSINS, "nil", XSI_PREFIX));
		if (nil == null)
			return false;
		return Boolean.parseBoolean(nil.getAsText());
	}

	private boolean hasMoreThanOne(Feature feature, List<PathStep> xpath) {
		if (xpath.isEmpty())
			return true;
		Iterator<PathStep> iterator = xpath.iterator();
		PathStep firstProperty = getNext(iterator);
		List<Property> properties = feature.getProperties(firstProperty.getName());
		return hasMoreThanOne(iterator, properties);
	}

	private <T extends TypedObjectNode> boolean hasMoreThanOne(Iterator<PathStep> iterator, List<T> properties) {
		PathStep next = getNext(iterator);
		if (next == null) {
			if (properties.size() > 1)
				return true;
			else
				return false;
		}
		else {
			for (TypedObjectNode property : properties) {
				List<TypedObjectNode> subProperties = getChildsByName(property, next);
				if (hasMoreThanOne(iterator, subProperties))
					return true;
			}
			return false;
		}
	}

	private List<TypedObjectNode> getChildsByName(TypedObjectNode property, PathStep pathStep) {
		if (property instanceof ElementNode)
			return getChildsByName((ElementNode) property, pathStep);
		if (property instanceof GenericFeature)
			return getChildsByName((GenericFeature) property, pathStep);
		return Collections.emptyList();
	}

	private List<TypedObjectNode> getChildsByName(ElementNode property, PathStep pathStep) {
		List<TypedObjectNode> properties = new ArrayList<>();
		for (TypedObjectNode child : property.getChildren()) {
			if (child instanceof ElementNode) {
				QName name = ((ElementNode) child).getName();
				if (name.equals(pathStep.getName()))
					properties.add(child);
			}
			else if (child instanceof GenericFeature) {
				QName name = ((GenericFeature) child).getName();
				if (name.equals(pathStep.getName()))
					properties.add(child);
			}
		}
		return properties;
	}

	private List<TypedObjectNode> getChildsByName(GenericFeature property, PathStep pathStep) {
		return property.getProperties(pathStep.getName())
			.stream()
			.map(prop -> (TypedObjectNode) prop)
			.collect(Collectors.toList());
	}

	private PathStep getNext(Iterator<PathStep> iterator) {
		while (iterator.hasNext()) {
			PathStep next = iterator.next();
			if (!next.isTypeDefinition())
				return next;
		}
		return null;
	}

}
