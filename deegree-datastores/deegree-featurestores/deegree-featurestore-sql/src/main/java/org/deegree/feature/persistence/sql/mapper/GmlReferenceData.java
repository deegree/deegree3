package org.deegree.feature.persistence.sql.mapper;

import org.deegree.commons.tom.ElementNode;
import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLVersion;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
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

	private Map<QName, List<Feature>> features;

	public GmlReferenceData(URL referenceData) throws IOException, XMLStreamException, UnknownCRSException {
		GMLStreamReader gmlStreamReader = GMLInputFactory.createGMLStreamReader(GMLVersion.GML_32, referenceData);
		FeatureCollection featureCollection = gmlStreamReader.readFeatureCollection();
		this.features = featureCollection.stream().collect(Collectors.groupingBy(Feature::getName));
	}

	@Override
	public boolean hasProperty(QName featureTypeName, List<QName> xpath) {
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
	public boolean isPropertyNilled(QName featureTypeName, List<QName> xpath) {
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
	public boolean hasZeroOrOneProperty(QName featureTypeName, List<QName> xpath) {
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

	private boolean hasProperty(Feature feature, List<QName> xpath) {
		if (xpath.isEmpty())
			return true;
		Iterator<QName> iterator = xpath.iterator();
		QName firstProperty = iterator.next();
		List<Property> properties = feature.getProperties(firstProperty);
		return hasProperty(iterator, properties);
	}

	private <T extends ElementNode> boolean hasProperty(Iterator<QName> iterator, List<T> properties) {
		if (!iterator.hasNext()) {
			if (properties.size() >= 1)
				return true;
			else
				return false;
		}
		else {
			QName next = iterator.next();
			for (ElementNode property : properties) {
				List<ElementNode> subProperties = getChildsByName(property, next);
				if (hasProperty(iterator, subProperties))
					return true;
			}
			return false;
		}
	}

	private boolean hasNilledPropertyOrIsMissing(Feature feature, List<QName> xpath) {
		if (xpath.isEmpty()) {
			return true;
		}
		Iterator<QName> iterator = xpath.iterator();
		QName firstProperty = iterator.next();
		List<Property> properties = feature.getProperties(firstProperty);
		return hasNilledPropertyOrIsMissing(iterator, properties);
	}

	private <T extends ElementNode> boolean hasNilledPropertyOrIsMissing(Iterator<QName> iterator, List<T> properties) {
		if (iterator.hasNext()) {
			QName next = iterator.next();
			for (ElementNode property : properties) {
				List<ElementNode> subProperties = getChildsByName(property, next);
				if (hasNilledPropertyOrIsMissing(iterator, subProperties))
					return true;
			}
		}
		else if (!properties.isEmpty()) {
			for (ElementNode property : properties) {
				if (!isNilTrue(property))
					return false;
			}
			return true;
		}
		return false;
	}

	private boolean isNilTrue(ElementNode property) {
		Map<QName, PrimitiveValue> attributes = property.getAttributes();
		PrimitiveValue nil = attributes.get(new QName(XSINS, "nil", XSI_PREFIX));
		if (nil == null)
			return false;
		return Boolean.parseBoolean(nil.getAsText());
	}

	private boolean hasMoreThanOne(Feature feature, List<QName> xpath) {
		if (xpath.isEmpty())
			return true;
		Iterator<QName> iterator = xpath.iterator();
		QName firstProperty = iterator.next();
		List<Property> properties = feature.getProperties(firstProperty);
		return hasMoreThanOne(iterator, properties);
	}

	private <T extends ElementNode> boolean hasMoreThanOne(Iterator<QName> iterator, List<T> properties) {
		if (!iterator.hasNext()) {
			if (properties.size() > 1)
				return true;
			else
				return false;
		}
		else {
			QName next = iterator.next();
			for (ElementNode property : properties) {
				List<ElementNode> subProperties = getChildsByName(property, next);
				if (hasMoreThanOne(iterator, subProperties))
					return true;
			}
			return false;
		}
	}

	private List<ElementNode> getChildsByName(ElementNode property, QName propertyName) {
		List<ElementNode> properties = new ArrayList<>();
		List<TypedObjectNode> children = property.getChildren();
		for (TypedObjectNode child : children) {
			if (child instanceof ElementNode) {
				QName name = ((ElementNode) child).getName();
				if (name.equals(propertyName))
					properties.add((ElementNode) child);
			}
		}
		return properties;
	}

}
