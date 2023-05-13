package org.deegree.feature.xpath.node;

import static org.deegree.commons.tom.primitive.BaseType.STRING;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.genericxml.GenericXMLElement;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.feature.property.GenericProperty;
import org.deegree.time.position.TimePosition;

public class TimePositionAdapter {

	public Property getAsXMLElement(final QName name, final TimePosition timeInstant) {
		final Map<QName, PrimitiveValue> attrs = new HashMap<QName, PrimitiveValue>();
		if (timeInstant.getIndeterminatePosition() != null) {
			final String indeterminateValue = timeInstant.getIndeterminatePosition().name().toLowerCase();
			final PrimitiveValue pv = new PrimitiveValue(indeterminateValue, new PrimitiveType(STRING));
			attrs.put(new QName("indeterminatePosition"), pv);
		}
		final PrimitiveValue value = new PrimitiveValue(timeInstant.getValue());
		final List<TypedObjectNode> children = new ArrayList<TypedObjectNode>();
		children.add(value);
		final GenericXMLElement xmlElement = new GenericXMLElement(name, attrs, children);
		return new GenericProperty(null, name, xmlElement, attrs, children);
	}

}
