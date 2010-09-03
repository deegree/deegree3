//$HeadURL$
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

import static org.jaxen.JaxenConstants.EMPTY_ITERATOR;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.genericxml.GenericXMLElement;
import org.deegree.commons.tom.genericxml.GenericXMLElementContent;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.uom.Measure;
import org.deegree.feature.Feature;
import org.deegree.feature.property.Property;
import org.deegree.gml.GMLObject;
import org.deegree.gml.GMLReference;
import org.deegree.gml.GMLVersion;
import org.jaxen.DefaultNavigator;
import org.jaxen.JaxenConstants;
import org.jaxen.XPath;
import org.jaxen.saxpath.SAXPathException;
import org.jaxen.util.SingleObjectIterator;

/**
 * <a href="http://jaxen.codehaus.org/">Jaxen</a> {@link DefaultNavigator} implementation for {@link Feature} objects.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
class FeatureNavigator extends DefaultNavigator {

    private static final long serialVersionUID = 5684363154723828577L;

    private DocumentNode documentNode;

    private GMLVersion version;

    private String gmlNs;

    /**
     * Creates a new {@link FeatureNavigator} instance with a {@link Feature} that acts as the root of the navigation
     * hierarchy.
     * 
     * @param rootFeature
     *            root of the navigation hierarchy (child of the document node), can be <code>null</code>
     * @param version
     *            determines the names and types of the standard GML properties, can be <code>null</code> (if no
     *            properties such as "gml:name" are used)
     */
    FeatureNavigator( Feature rootFeature, GMLVersion version ) {
        if ( rootFeature != null ) {
            this.documentNode = new DocumentNode( new GMLObjectNode<Feature>( null, rootFeature, version ) );
        }
        this.version = version;
        this.gmlNs = version.getNamespace();
    }

    /**
     * Returns an iterator over the attributes of an {@link ElementNode}.
     * 
     * @param node
     *            the context node for the attribute axis (an {@link ElementNode}, otherwise returns emtpy iterator)
     * @return a possibly empty iterator (never <code>null</code>)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Iterator<AttributeNode> getAttributeAxisIterator( Object node ) {
        if ( node instanceof GMLObjectNode ) {
            GMLObjectNode gmlObjectNode = (GMLObjectNode) node;
            GMLObject object = gmlObjectNode.getValue();
            if ( object.getId() != null ) {
                PrimitiveValue id = new PrimitiveValue( object.getId() );
                AttributeNode gmlIdAttrNode = new AttributeNode( gmlObjectNode, new QName( gmlNs, "id" ), id );
                return new SingleObjectIterator( gmlIdAttrNode );
            }
        } else if ( node instanceof PropertyNode ) {
            Object value = ( (PropertyNode) node ).getValue().getValue();
            if ( value instanceof GenericXMLElementContent ) {
                GenericXMLElementContent genericValue = (GenericXMLElementContent) value;
                Map<QName, PrimitiveValue> attributes = genericValue.getAttributes();
                List<AttributeNode> attrNodes = new ArrayList<AttributeNode>( attributes.size() );
                for ( Entry<QName, PrimitiveValue> attribute : attributes.entrySet() ) {
                    attrNodes.add( new AttributeNode( (PropertyNode) node, attribute.getKey(), attribute.getValue() ) );
                }
                return attrNodes.iterator();
            } else if ( value instanceof Measure && ( (Measure) value ).getUomUri() != null ) {
                PrimitiveValue uom = new PrimitiveValue( ( (Measure) value ).getUomUri() );
                return new SingleObjectIterator( new AttributeNode( (PropertyNode) node, new QName( "uom" ), uom ) );
            } else if ( value instanceof CodeType && ( (CodeType) value ).getCodeSpace() != null ) {
                PrimitiveValue codeSpace = new PrimitiveValue( ( (CodeType) value ).getCodeSpace() );
                return new SingleObjectIterator( new AttributeNode( (PropertyNode) node, new QName( "codeSpace" ),
                                                                    codeSpace ) );
            }
        } else if ( node instanceof XMLElementNode ) {
            GenericXMLElement value = ( (XMLElementNode) node ).getValue();
            Map<QName, PrimitiveValue> attributes = value.getAttributes();
            if ( attributes != null ) {
                List<AttributeNode> attrNodes = new ArrayList<AttributeNode>( attributes.size() );
                for ( Entry<QName, PrimitiveValue> attribute : attributes.entrySet() ) {
                    attrNodes.add( new AttributeNode( (XMLElementNode) node, attribute.getKey(), attribute.getValue() ) );
                }
                return attrNodes.iterator();
            }
        }
        return JaxenConstants.EMPTY_ITERATOR;
    }

    /**
     * Returns the local name of an attribute node.
     * 
     * @param node
     *            attribute node, must not be null
     * @return a string representing the unqualified local name if the node is an attribute, or <code>null</code>
     *         otherwise
     */
    @Override
    public String getAttributeName( Object node ) {
        String name = null;
        if ( isAttribute( node ) ) {
            AttributeNode attr = (AttributeNode) node;
            name = attr.getLocalName();
        }
        return name;
    }

    /**
     * Returns the namespace URI of an attribute node.
     * 
     * @param node
     *            attribute node, must not be null
     * @return namespace if the argument is an attribute, or <code>null</code> otherwise
     */
    @Override
    public String getAttributeNamespaceUri( Object node ) {
        String ns = null;
        if ( isAttribute( node ) ) {
            AttributeNode attr = (AttributeNode) node;
            ns = attr.getNamespaceUri();
        }
        return ns;
    }

    /**
     * Returns the qualified (=prefixed) name of an attribute node.
     * 
     * @param node
     *            attribute node, must not be null
     * @return a string representing the qualified (i.e. possibly prefixed) name if the argument is an attribute, or
     *         <code>null</code> otherwise
     */
    @Override
    public String getAttributeQName( Object node ) {
        String name = null;
        if ( isAttribute( node ) ) {
            AttributeNode attr = (AttributeNode) node;
            name = attr.getPrefixedName();
        }
        return name;
    }

    /**
     * Returns the string value of an attribute node.
     * 
     * @param node
     *            attribute node, must not be null
     * @return the text of the attribute value if the node is an attribute, <code>null</code> otherwise
     */
    @Override
    public String getAttributeStringValue( Object node ) {
        String value = null;
        if ( isAttribute( node ) ) {
            value = ( (AttributeNode) node ).getValue().getAsText();
        }
        return value;
    }

    /**
     * Returns an iterator over all children of the given node.
     * 
     * @param node
     *            the context node for the child axis, never <code>null</code>
     * @return a possibly empty iterator, never <code>null</code>
     */
    @SuppressWarnings("unchecked")
    @Override
    public Iterator<?> getChildAxisIterator( Object node ) {
        Iterator<?> iter = EMPTY_ITERATOR;
        if ( node instanceof GMLObjectNode ) {
            if ( ( (GMLObjectNode) node ).getValue() instanceof Feature ) {
                iter = new PropertyNodeIterator( (GMLObjectNode) node, version );
            }
        } else if ( node instanceof DocumentNode ) {
            iter = new SingleObjectIterator( ( (DocumentNode) node ).getRootNode() );
        } else if ( node instanceof PropertyNode ) {
            Property prop = ( (PropertyNode) node ).getValue();
            Object propValue = prop.getValue();
            if ( propValue instanceof GMLObject ) {
                // TODO clear strategy for GML object references (remote + local)
                if ( !( propValue instanceof GMLReference ) || ( (GMLReference) propValue ).isLocal() ) {
                    iter = new SingleObjectIterator( new GMLObjectNode( (PropertyNode) node, (GMLObject) propValue,
                                                                        version ) );
                }
            } else if ( propValue instanceof GenericXMLElementContent ) {
                List<TypedObjectNode> xmlNodes = ( (GenericXMLElementContent) propValue ).getChildren();
                List<XPathNode> xpathNodes = new ArrayList<XPathNode>( xmlNodes.size() );
                for ( TypedObjectNode xmlNode : xmlNodes ) {
                    if ( xmlNode instanceof GenericXMLElement ) {
                        xpathNodes.add( new XMLElementNode( (XPathNode) node, (GenericXMLElement) xmlNode ) );
                    } else if ( xmlNode instanceof GMLObject ) {
                        xpathNodes.add( new GMLObjectNode<GMLObject>( (XPathNode) node, (GMLObject) xmlNode, version ) );
                    } else if ( xmlNode instanceof PrimitiveValue ) {
                        xpathNodes.add( new TextNode( (PropertyNode) node, (PrimitiveValue) xmlNode ) );
                    }
                }
                iter = xpathNodes.iterator();
            } else if ( propValue instanceof PrimitiveValue ) {
                iter = new SingleObjectIterator( new TextNode( (PropertyNode) node, (PrimitiveValue) propValue ) );
            } else {
                // TODO remove this case
                iter = new SingleObjectIterator( new TextNode( (PropertyNode) node,
                                                               new PrimitiveValue( propValue.toString() ) ) );
            }
        } else if ( node instanceof XMLElementNode ) {
            List<TypedObjectNode> xmlNodes = ( (XMLElementNode) node ).getValue().getChildren();
            List<XPathNode> xpathNodes = new ArrayList<XPathNode>( xmlNodes.size() );
            for ( TypedObjectNode xmlNode : xmlNodes ) {
                if ( xmlNode instanceof GenericXMLElement ) {
                    xpathNodes.add( new XMLElementNode( (XPathNode) node, (GenericXMLElement) xmlNode ) );
                } else if ( xmlNode instanceof GMLObject ) {
                    xpathNodes.add( new GMLObjectNode<GMLObject>( (XPathNode) node, (GMLObject) xmlNode, version ) );
                } else if ( xmlNode instanceof PrimitiveValue ) {
                    xpathNodes.add( new TextNode( (ElementNode<TypedObjectNode>) node, (PrimitiveValue) xmlNode ) );
                }
            }
            iter = xpathNodes.iterator();
        }
        return iter;
    }

    @Override
    public String getCommentStringValue( Object contextNode ) {
        String msg = "getCommentStringValue(Object) called with argument (" + contextNode
                     + "), but method not implemented";
        throw new UnsupportedOperationException( msg );
    }

    /**
     * Returns the top-level document node.
     * 
     * @param contextNode
     *            any node in the document
     * @return the root node
     */
    @Override
    public Object getDocumentNode( Object contextNode ) {
        if ( documentNode == null ) {
            String msg = "getDocumentNode(Object) not possible, no document node provided";
            throw new UnsupportedOperationException( msg );
        }
        return documentNode;
    }

    /**
     * Returns the local name of an element node.
     * 
     * @param node
     *            the element node
     * @return a string representing the unqualified local name if the node is an element, or null otherwise
     */
    @Override
    public String getElementName( Object node ) {
        String name = null;
        if ( isElement( node ) ) {
            ElementNode el = (ElementNode) node;
            name = el.getLocalName();
        }
        return name;
    }

    /**
     * Returns the namespace URI of an element node.
     * 
     * @param node
     *            the element node
     * @return the namespace if the argument is an element, or null otherwise
     */
    @Override
    public String getElementNamespaceUri( Object node ) {
        String ns = null;
        if ( isElement( node ) ) {
            ElementNode el = (ElementNode) node;
            ns = el.getNamespaceUri();
        }
        return ns;
    }

    /**
     * Returns the qualified (=prefixed) name of an element node.
     * 
     * @param node
     *            the element node
     * @return a string representing the qualified (i.e. possibly prefixed) name if the argument is an element, or null
     *         otherwise
     */
    @Override
    public String getElementQName( Object node ) {
        String name = null;
        if ( isElement( node ) ) {
            ElementNode el = (ElementNode) node;
            name = el.getPrefixedName();
        }
        return name;
    }

    /**
     * Returns the string value of an element node.
     * 
     * @param node
     *            the target node
     * @return the text inside the node and its descendants if the node is an element, null otherwise
     */
    @SuppressWarnings("unchecked")
    @Override
    public String getElementStringValue( Object node ) {
        String value = null;
        if ( node instanceof PropertyNode ) {
            Property prop = ( (PropertyNode) node ).getValue();
            Object propValue = prop.getValue();
            // TODO check if conversion is feasible (e.g. Geometry.toString() may be expensive)
            value = propValue.toString();
        }
        return value;
    }

    @Override
    public String getNamespacePrefix( Object contextNode ) {
        String msg = "getNamespacePrefix(Object) called with argument (" + contextNode
                     + "), but method not implemented";
        throw new UnsupportedOperationException( msg );
    }

    @Override
    public String getNamespaceStringValue( Object contextNode ) {
        String msg = "getNamespaceStringValue(Object) called with argument (" + contextNode
                     + "), but method not implemented";
        throw new UnsupportedOperationException( msg );
    }

    /**
     * Returns a (single-member) iterator over this node's parent.
     * 
     * @param contextNode
     *            the context node for the parent axis
     * @return a possibly-empty iterator (not null)
     */
    @Override
    @SuppressWarnings("unchecked")
    public Iterator<XPathNode> getParentAxisIterator( Object contextNode ) {
        return new SingleObjectIterator( ( (XPathNode) contextNode ).getParent() );
    }

    @Override
    public String getTextStringValue( Object obj ) {
        String value = null;
        if ( obj instanceof TextNode ) {
            value = ( (TextNode) obj ).getValue().getAsText();
        }
        return value;
    }

    @Override
    public boolean isAttribute( Object obj ) {
        return obj instanceof AttributeNode;
    }

    @Override
    public boolean isComment( Object obj ) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isDocument( Object obj ) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isElement( Object obj ) {
        return obj instanceof ElementNode;
    }

    @Override
    public boolean isNamespace( Object obj ) {
        return false;
    }

    @Override
    public boolean isProcessingInstruction( Object obj ) {
        return false;
    }

    @Override
    public boolean isText( Object obj ) {
        return obj instanceof TextNode;
    }

    /**
     * Returns a parsed form of the given XPath string, which will be suitable for queries on <code>Feature</code>
     * objects.
     * 
     * @param xpath
     *            the XPath expression
     * @return a parsed form of the given XPath string
     * @throws SAXPathException
     *             if the string is syntactically incorrect
     */
    @Override
    public XPath parseXPath( String xpath )
                            throws SAXPathException {
        return new FeatureXPath( xpath, version );
    }

    /**
     * Translates a namespace prefix to a URI.
     * 
     * @param prefix
     *            the namespace prefix
     * @param element
     *            the namespace context
     * @return the namespace URI bound to the prefix in the scope of <code>element</code>; null if the prefix is not
     *         bound
     */
    @Override
    public String translateNamespacePrefixToUri( String prefix, Object element ) {
        String msg = "translateNamespacePrefixToUri(String,Object) called with arguments (" + prefix + "," + element
                     + "), but method not implemented";
        throw new UnsupportedOperationException( msg );
    }
}
