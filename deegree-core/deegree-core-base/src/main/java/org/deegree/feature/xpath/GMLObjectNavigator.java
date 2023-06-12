/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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

import static org.deegree.commons.xml.CommonNamespaces.GML3_2_NS;
import static org.deegree.commons.xml.CommonNamespaces.GMLNS;
import static org.jaxen.JaxenConstants.EMPTY_ITERATOR;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.genericxml.GenericXMLElement;
import org.deegree.commons.tom.gml.GMLObject;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.uom.Measure;
import org.deegree.feature.Feature;
import org.deegree.feature.xpath.node.AttributeNode;
import org.deegree.feature.xpath.node.DocumentNode;
import org.deegree.feature.xpath.node.ElementNode;
import org.deegree.feature.xpath.node.GMLObjectNode;
import org.deegree.feature.xpath.node.PrimitiveNode;
import org.deegree.feature.xpath.node.PropertyNode;
import org.deegree.feature.xpath.node.XMLElementNode;
import org.deegree.feature.xpath.node.XPathNode;
import org.jaxen.DefaultNavigator;
import org.jaxen.XPath;
import org.jaxen.saxpath.SAXPathException;
import org.jaxen.util.SingleObjectIterator;

/**
 * <a href="http://jaxen.codehaus.org/">Jaxen</a> {@link DefaultNavigator} implementation
 * for {@link GMLObject} objects.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 */
class GMLObjectNavigator extends DefaultNavigator {

	private static final long serialVersionUID = 5684363154723828577L;

	private DocumentNode documentNode;

	/**
	 * Creates a new {@link GMLObjectNavigator} instance with a {@link Feature} that acts
	 * as the root of the navigation hierarchy.
	 * @param root root of the navigation hierarchy (child of the document node), can be
	 * <code>null</code>
	 */
	GMLObjectNavigator(GMLObject root) {
		if (root != null) {
			this.documentNode = new DocumentNode(new GMLObjectNode<GMLObject, GMLObject>(null, root));
		}
	}

	/**
	 * Returns an iterator over the attributes of an {@link ElementNode}.
	 * @param node the context node for the attribute axis (an {@link ElementNode},
	 * otherwise returns emtpy iterator)
	 * @return a possibly empty iterator (never <code>null</code>)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Iterator<AttributeNode<? extends TypedObjectNode>> getAttributeAxisIterator(Object node) {
		if (node instanceof GMLObjectNode<?, ?>) {
			GMLObjectNode<GMLObject, ?> gmlObjectNode = (GMLObjectNode<GMLObject, ?>) node;
			GMLObject object = gmlObjectNode.getValue();
			if (object.getId() != null) {
				List<AttributeNode<?>> idAttrs = new ArrayList<AttributeNode<?>>(4);
				PrimitiveValue id = new PrimitiveValue(object.getId());
				idAttrs.add(new AttributeNode<GMLObject>(gmlObjectNode, new QName("fid"), id));
				idAttrs.add(new AttributeNode<GMLObject>(gmlObjectNode, new QName("gid"), id));
				idAttrs.add(new AttributeNode<GMLObject>(gmlObjectNode, new QName(GMLNS, "id"), id));
				idAttrs.add(new AttributeNode<GMLObject>(gmlObjectNode, new QName(GML3_2_NS, "id"), id));
				return idAttrs.iterator();
			}
		}
		else if (node instanceof PropertyNode) {
			Object value = ((PropertyNode) node).getValue().getValue();
			if (value instanceof Measure && ((Measure) value).getUomUri() != null) {
				PrimitiveValue uom = new PrimitiveValue(((Measure) value).getUomUri());
				return new SingleObjectIterator(
						new AttributeNode<Property>((PropertyNode) node, new QName("uom"), uom));
			}
			else if (value instanceof CodeType && ((CodeType) value).getCodeSpace() != null) {
				PrimitiveValue codeSpace = new PrimitiveValue(((CodeType) value).getCodeSpace());
				return new SingleObjectIterator(
						new AttributeNode<Property>((PropertyNode) node, new QName("codeSpace"), codeSpace));
			}
			else if (value instanceof GenericXMLElement) {
				XMLElementNode<Property> n = new XMLElementNode<Property>((PropertyNode) node,
						(GenericXMLElement) value);
				return getAttributeAxisIterator(n);
			}
			Map<QName, PrimitiveValue> attributes = ((PropertyNode) node).getValue().getAttributes();
			if (attributes != null) {
				List<AttributeNode<?>> attrNodes = new ArrayList<AttributeNode<?>>(attributes.size());
				for (Entry<QName, PrimitiveValue> attribute : attributes.entrySet()) {
					attrNodes.add(
							new AttributeNode<Property>((PropertyNode) node, attribute.getKey(), attribute.getValue()));
				}
				return attrNodes.iterator();
			}
		}
		else if (node instanceof XMLElementNode<?>) {
			org.deegree.commons.tom.ElementNode value = ((XMLElementNode<?>) node).getValue();
			Map<QName, PrimitiveValue> attributes = value.getAttributes();
			if (attributes != null) {
				List<AttributeNode<?>> attrNodes = new ArrayList<AttributeNode<?>>(attributes.size());
				for (Entry<QName, PrimitiveValue> attribute : attributes.entrySet()) {
					attrNodes.add(new AttributeNode<org.deegree.commons.tom.ElementNode>((XMLElementNode<?>) node,
							attribute.getKey(), attribute.getValue()));
				}
				return attrNodes.iterator();
			}
		}
		return EMPTY_ITERATOR;
	}

	/**
	 * Returns the local name of an attribute node.
	 * @param node attribute node, must not be null
	 * @return a string representing the unqualified local name if the node is an
	 * attribute, or <code>null</code> otherwise
	 */
	@Override
	public String getAttributeName(Object node) {
		String name = null;
		if (isAttribute(node)) {
			AttributeNode<?> attr = (AttributeNode<?>) node;
			name = attr.getLocalName();
		}
		return name;
	}

	/**
	 * Returns the namespace URI of an attribute node.
	 * @param node attribute node, must not be null
	 * @return namespace if the argument is an attribute, or <code>null</code> otherwise
	 */
	@Override
	public String getAttributeNamespaceUri(Object node) {
		String ns = null;
		if (isAttribute(node)) {
			AttributeNode<?> attr = (AttributeNode<?>) node;
			ns = attr.getNamespaceUri();
		}
		return ns;
	}

	/**
	 * Returns the qualified (=prefixed) name of an attribute node.
	 * @param node attribute node, must not be null
	 * @return a string representing the qualified (i.e. possibly prefixed) name if the
	 * argument is an attribute, or <code>null</code> otherwise
	 */
	@Override
	public String getAttributeQName(Object node) {
		String name = null;
		if (isAttribute(node)) {
			AttributeNode<?> attr = (AttributeNode<?>) node;
			name = attr.getPrefixedName();
		}
		return name;
	}

	/**
	 * Returns the string value of an attribute node.
	 * @param node attribute node, must not be null
	 * @return the text of the attribute value if the node is an attribute,
	 * <code>null</code> otherwise
	 */
	@Override
	public String getAttributeStringValue(Object node) {
		String value = null;
		if (isAttribute(node)) {
			value = ((AttributeNode<?>) node).getValue().getAsText();
		}
		return value;
	}

	/**
	 * Returns an iterator over all children of the given node.
	 * @param node the context node for the child axis, never <code>null</code>
	 * @return a possibly empty iterator, never <code>null</code>
	 */
	@Override
	public Iterator<?> getChildAxisIterator(Object node) {
		Iterator<?> iter = EMPTY_ITERATOR;
		if (node instanceof GMLObjectNode<?, ?>) {
			GMLObjectNode<GMLObject, GMLObject> gmlObjectNode = (GMLObjectNode<GMLObject, GMLObject>) node;
			if (gmlObjectNode.getValue() != null) {
				iter = new PropertyNodeIterator(gmlObjectNode);
			}
		}
		else if (node instanceof DocumentNode) {
			iter = new SingleObjectIterator(((DocumentNode) node).getRootNode());
		}
		else if (node instanceof PropertyNode) {
			PropertyNode propNode = (PropertyNode) node;
			Property prop = propNode.getValue();
			if (!prop.getChildren().isEmpty()) {
				List<XPathNode> xpathNodes = new ArrayList<XPathNode>(prop.getChildren().size());
				for (TypedObjectNode xmlNode : prop.getChildren()) {
					if (xmlNode instanceof org.deegree.commons.tom.ElementNode) {
						xpathNodes
							.add(new XMLElementNode<Property>(propNode, (org.deegree.commons.tom.ElementNode) xmlNode));
					}
					else if (xmlNode instanceof GMLObject) {
						xpathNodes.add(new GMLObjectNode<GMLObject, Property>(propNode, (GMLObject) xmlNode));
					}
					else if (xmlNode instanceof PrimitiveValue) {
						xpathNodes.add(new PrimitiveNode<Property>(propNode, (PrimitiveValue) xmlNode));
					}
				}
				iter = xpathNodes.iterator();
			}
			else {
				final Object propValue = prop.getValue();
				if (propValue instanceof GMLObject) {
					GMLObject castNode = (GMLObject) propValue;
					iter = new SingleObjectIterator(new GMLObjectNode<GMLObject, Property>(propNode, castNode));
				}
				else if (propValue instanceof PrimitiveValue) {
					iter = new SingleObjectIterator(
							new PrimitiveNode<Property>((PropertyNode) node, (PrimitiveValue) propValue));
				}
				else if (propValue == null) {
					iter = EMPTY_ITERATOR;
				}
				else {
					// TODO remove this case
					iter = new SingleObjectIterator(
							new PrimitiveNode<Property>((PropertyNode) node, new PrimitiveValue(propValue.toString())));
				}
			}
		}
		else if (node instanceof XMLElementNode<?>) {
			XMLElementNode<?> xmlElementNode = (XMLElementNode<?>) node;
			List<TypedObjectNode> xmlNodes = xmlElementNode.getValue().getChildren();
			List<XPathNode<?>> xpathNodes = new ArrayList<XPathNode<?>>(xmlNodes.size());
			for (TypedObjectNode xmlNode : xmlNodes) {
				if (xmlNode instanceof org.deegree.commons.tom.ElementNode) {
					xpathNodes.add(new XMLElementNode<org.deegree.commons.tom.ElementNode>(xmlElementNode,
							(org.deegree.commons.tom.ElementNode) xmlNode));
				}
				else if (xmlNode instanceof GMLObject) {
					xpathNodes.add(new GMLObjectNode<GMLObject, org.deegree.commons.tom.ElementNode>(xmlElementNode,
							(GMLObject) xmlNode));
				}
				else if (xmlNode instanceof PrimitiveValue) {
					xpathNodes.add(new PrimitiveNode<org.deegree.commons.tom.ElementNode>(xmlElementNode,
							(PrimitiveValue) xmlNode));
				}
			}
			iter = xpathNodes.iterator();
		}
		return iter;
	}

	@Override
	public String getCommentStringValue(Object contextNode) {
		String msg = "getCommentStringValue(Object) called with argument (" + contextNode
				+ "), but method not implemented";
		throw new UnsupportedOperationException(msg);
	}

	/**
	 * Returns the top-level document node.
	 * @param contextNode any node in the document
	 * @return the root node
	 */
	@Override
	public Object getDocumentNode(Object contextNode) {
		if (documentNode == null) {
			String msg = "getDocumentNode(Object) not possible, no document node provided";
			throw new UnsupportedOperationException(msg);
		}
		return documentNode;
	}

	/**
	 * Returns the local name of an element node.
	 * @param node the element node
	 * @return a string representing the unqualified local name if the node is an element,
	 * or null otherwise
	 */
	@Override
	public String getElementName(Object node) {
		String name = null;
		if (isElement(node)) {
			ElementNode<?> el = (ElementNode<?>) node;
			name = el.getLocalName();
		}
		return name;
	}

	/**
	 * Returns the namespace URI of an element node.
	 * @param node the element node
	 * @return the namespace if the argument is an element, or null otherwise
	 */
	@Override
	public String getElementNamespaceUri(Object node) {
		String ns = null;
		if (isElement(node)) {
			ElementNode<?> el = (ElementNode<?>) node;
			ns = el.getNamespaceUri();
		}
		return ns;
	}

	/**
	 * Returns the qualified (=prefixed) name of an element node.
	 * @param node the element node
	 * @return a string representing the qualified (i.e. possibly prefixed) name if the
	 * argument is an element, or null otherwise
	 */
	@Override
	public String getElementQName(Object node) {
		String name = null;
		if (isElement(node)) {
			ElementNode<?> el = (ElementNode<?>) node;
			name = el.getPrefixedName();
		}
		return name;
	}

	/**
	 * Returns the string value of an element node.
	 * @param node the target node
	 * @return the text inside the node and its descendants if the node is an element,
	 * null otherwise
	 */
	@Override
	public String getElementStringValue(Object node) {
		String value = null;
		if (node instanceof PropertyNode) {
			Property prop = ((PropertyNode) node).getValue();
			Object propValue = prop.getValue();
			// TODO check if conversion is feasible (e.g. Geometry.toString() may be
			// expensive)
			value = propValue.toString();
		}
		return value;
	}

	@Override
	public String getNamespacePrefix(Object contextNode) {
		String msg = "getNamespacePrefix(Object) called with argument (" + contextNode
				+ "), but method not implemented";
		throw new UnsupportedOperationException(msg);
	}

	@Override
	public String getNamespaceStringValue(Object contextNode) {
		String msg = "getNamespaceStringValue(Object) called with argument (" + contextNode
				+ "), but method not implemented";
		throw new UnsupportedOperationException(msg);
	}

	/**
	 * Returns a (single-member) iterator over this node's parent.
	 * @param contextNode the context node for the parent axis
	 * @return a possibly-empty iterator (not null)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Iterator<XPathNode<?>> getParentAxisIterator(Object contextNode) {
		return new SingleObjectIterator(((XPathNode<?>) contextNode).getParent());
	}

	@Override
	public String getTextStringValue(Object obj) {
		String value = null;
		if (obj instanceof PrimitiveNode<?>) {
			value = ((PrimitiveNode<?>) obj).getValue().getAsText();
		}
		return value;
	}

	@Override
	public boolean isAttribute(Object obj) {
		return obj instanceof AttributeNode<?>;
	}

	@Override
	public boolean isComment(Object obj) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDocument(Object obj) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isElement(Object obj) {
		return obj instanceof ElementNode<?>;
	}

	@Override
	public boolean isNamespace(Object obj) {
		return false;
	}

	@Override
	public boolean isProcessingInstruction(Object obj) {
		return false;
	}

	@Override
	public boolean isText(Object obj) {
		return obj instanceof PrimitiveNode<?>;
	}

	/**
	 * Returns a parsed form of the given XPath string, which will be suitable for queries
	 * on <code>Feature</code> objects.
	 * @param xpath the XPath expression
	 * @return a parsed form of the given XPath string
	 * @throws SAXPathException if the string is syntactically incorrect
	 */
	@Override
	public XPath parseXPath(String xpath) throws SAXPathException {
		return new GMLObjectXPath(xpath, null);
	}

	/**
	 * Translates a namespace prefix to a URI.
	 * @param prefix the namespace prefix
	 * @param element the namespace context
	 * @return the namespace URI bound to the prefix in the scope of <code>element</code>;
	 * null if the prefix is not bound
	 */
	@Override
	public String translateNamespacePrefixToUri(String prefix, Object element) {
		String msg = "translateNamespacePrefixToUri(String,Object) called with arguments (" + prefix + "," + element
				+ "), but method not implemented";
		throw new UnsupportedOperationException(msg);
	}

}
