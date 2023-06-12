/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.feature.xpath;

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.ElementNode;
import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.gml.GMLObject;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.feature.Feature;
import org.deegree.feature.xpath.node.GMLObjectNode;
import org.deegree.feature.xpath.node.PropertyNode;
import org.deegree.feature.xpath.node.XMLElementNode;
import org.deegree.feature.xpath.node.XPathNode;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.XPathEvaluator;
import org.deegree.filter.expression.ValueReference;
import org.jaxen.JaxenException;
import org.jaxen.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link XPathEvaluator} implementation for {@link TypedObjectNode} graphs.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class TypedObjectNodeXPathEvaluator implements XPathEvaluator<TypedObjectNode> {

	private static Logger LOG = LoggerFactory.getLogger(TypedObjectNodeXPathEvaluator.class);

	private Map<String, QName> bindings;

	public TypedObjectNodeXPathEvaluator() {
		// default constructor
	}

	/**
	 * @param bindings a mapping from local name to qname to use for repairing broken
	 * filters, may be null
	 */
	public TypedObjectNodeXPathEvaluator(Map<String, QName> bindings) {
		this.bindings = bindings;
	}

	@Override
	public TypedObjectNode[] eval(TypedObjectNode particle, ValueReference path) throws FilterEvaluationException {
		if (particle instanceof GMLObject) {
			return eval((GMLObject) particle, path);
		}
		if (particle instanceof ElementNode) {
			return eval((ElementNode) particle, path);
		}
		throw new FilterEvaluationException(
				"Evaluation of XPath expressions on '" + particle.getClass() + "' is not supported.");
	}

	public TypedObjectNode[] eval(GMLObject context, ValueReference propName) throws FilterEvaluationException {

		// simple property with just a simple element step?
		QName simplePropName = propName.getAsQName();
		if (bindings != null && simplePropName != null
				&& (simplePropName.getNamespaceURI() == null || simplePropName.getNamespaceURI().isEmpty())) {
			QName altName = bindings.get(simplePropName.getLocalPart());
			if (altName != null) {
				LOG.debug("Repairing namespace binding for property {}", simplePropName.getLocalPart());
				simplePropName = altName;
			}
		}
		if (simplePropName != null && context instanceof Feature) {
			List<Property> props = context.getProperties(simplePropName);
			TypedObjectNode[] propArray = new TypedObjectNode[props.size()];
			return props.toArray(propArray);
		}

		TypedObjectNode[] resultValues = null;
		try {
			synchronized (context) {
				XPath xpath = new GMLObjectXPath(propName.getAsText(), context);
				xpath.setNamespaceContext(propName.getNsContext());
				List<?> selectedNodes;
				selectedNodes = xpath.selectNodes(new GMLObjectNode<GMLObject, GMLObject>(null, context));
				resultValues = new TypedObjectNode[selectedNodes.size()];
				int i = 0;
				for (Object node : selectedNodes) {
					if (node instanceof XPathNode<?>) {
						resultValues[i++] = ((XPathNode<?>) node).getValue();
					}
					else if (node instanceof String || node instanceof Double || node instanceof Boolean) {
						resultValues[i++] = new PrimitiveValue(node);
					}
					else {
						throw new RuntimeException("Internal error. Encountered unexpected value of type '"
								+ node.getClass().getName() + "' (=" + node + ") during XPath-evaluation.");
					}
				}
			}
		}
		catch (JaxenException e) {
			e.printStackTrace();
			throw new FilterEvaluationException(e.getMessage());
		}
		return resultValues;
	}

	public TypedObjectNode[] eval(ElementNode element, ValueReference propName) throws FilterEvaluationException {

		TypedObjectNode[] resultValues = null;
		try {
			XPath xpath = new GMLObjectXPath(propName.getAsText(), null);
			xpath.setNamespaceContext(propName.getNsContext());
			List<?> selectedNodes;
			selectedNodes = xpath.selectNodes(new XMLElementNode(null, element));
			resultValues = new TypedObjectNode[selectedNodes.size()];
			int i = 0;
			for (Object node : selectedNodes) {
				if (node instanceof XPathNode<?>) {
					resultValues[i++] = ((XPathNode<?>) node).getValue();
				}
				else if (node instanceof String || node instanceof Double || node instanceof Boolean) {
					resultValues[i++] = new PrimitiveValue(node);
				}
				else {
					throw new RuntimeException("Internal error. Encountered unexpected value of type '"
							+ node.getClass().getName() + "' (=" + node + ") during XPath-evaluation.");
				}
			}
		}
		catch (JaxenException e) {
			throw new FilterEvaluationException(e.getMessage());
		}
		return resultValues;
	}

	public TypedObjectNode[] eval(Property element, ValueReference propName) throws FilterEvaluationException {

		TypedObjectNode[] resultValues = null;
		try {
			XPath xpath = new GMLObjectXPath(propName.getAsText(), null);
			xpath.setNamespaceContext(propName.getNsContext());
			List<?> selectedNodes;
			selectedNodes = xpath.selectNodes(new PropertyNode(null, element));
			resultValues = new TypedObjectNode[selectedNodes.size()];
			int i = 0;
			for (Object node : selectedNodes) {
				if (node instanceof XPathNode<?>) {
					resultValues[i++] = ((XPathNode<?>) node).getValue();
				}
				else if (node instanceof String || node instanceof Double || node instanceof Boolean) {
					resultValues[i++] = new PrimitiveValue(node);
				}
				else {
					throw new RuntimeException("Internal error. Encountered unexpected value of type '"
							+ node.getClass().getName() + "' (=" + node + ") during XPath-evaluation.");
				}
			}
		}
		catch (JaxenException e) {
			throw new FilterEvaluationException(e.getMessage());
		}
		return resultValues;
	}

	@Override
	public String getId(TypedObjectNode context) {
		if (context instanceof GMLObject) {
			return ((GMLObject) context).getId();
		}
		// TODO implement fallback to generic gml:id attribute
		return null;
	}

}
