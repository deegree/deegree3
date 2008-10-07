//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth  
 lat/lon GmbH 
 Aennchenstr. 19
 53115 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de


 ---------------------------------------------------------------------------*/
package org.deegree.model.feature.xpath;

import java.util.Arrays;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.deegree.model.feature.Feature;
import org.deegree.model.feature.Property;
import org.jaxen.DefaultNavigator;
import org.jaxen.JaxenConstants;
import org.jaxen.Navigator;
import org.jaxen.XPath;
import org.jaxen.saxpath.SAXPathException;
import org.jaxen.util.SingleObjectIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO add documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class FeatureNavigator extends DefaultNavigator {

    private static final long serialVersionUID = 5684363154723828577L;

    private static final Logger LOG = LoggerFactory.getLogger( FeatureNavigator.class );

    // Singleton implementation.
    private static class Singleton {

        // Singleton instance.
        static FeatureNavigator instance = new FeatureNavigator();
    }

    /**
     * Returns the single <code>FeatureNavigator</code> instance.
     * 
     * @return the single <code>FeatureNavigator</code> instance
     */
    public static Navigator getInstance() {
        return Singleton.instance;
    }

    /**
     * Returns an iterator over all attributes of an element node.
     * 
     * @param node
     *            the context node for the attribute axis
     * @return a possibly-empty iterator (not null)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Iterator<AttributeNode> getAttributeAxisIterator( Object node ) {
        if ( node instanceof FeatureNode ) {
            FeatureNode featureNode = (FeatureNode) node;
            if ( featureNode.getFeature().getId() != null ) {
                AttributeNode gmlIdAttrNode = new AttributeNode( featureNode, new QName( "http://www.opengis.net/gml",
                                                                                         "id" ),
                                                                 featureNode.getFeature().getId() );
                return new SingleObjectIterator( gmlIdAttrNode );
            }
        }
        return JaxenConstants.EMPTY_ITERATOR;
    }

    /**
     * Returns the local name of an attribute node.
     * 
     * @param node
     *            the attribute node
     * @return a string representing the unqualified local name if the node is an attribute, or null otherwise
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
     *            the attribute node
     * @return the namespace if the argument is an attribute, or null otherwise
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
     *            the attribute node
     * @return a string representing the qualified (i.e. possibly prefixed) name if the argument is an attribute, or
     *         null otherwise
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
     *            the attribute node
     * @return the text of the attribute value if the node is an attribute, null otherwise
     */
    @Override
    public String getAttributeStringValue( Object node ) {
        String value = null;
        if ( isAttribute( node ) ) {
            value = ( (AttributeNode) node ).getValue();
        }
        return value;
    }

    /**
     * Returns an iterator over all children of the given node.
     * 
     * @param node
     *            the context node for the child axis
     * @return a possibly-empty iterator (not null)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Iterator<?> getChildAxisIterator( Object node ) {
        Iterator<?> iter = JaxenConstants.EMPTY_ITERATOR;
        if ( node instanceof FeatureNode ) {
            Feature feature = ( (FeatureNode) node ).getFeature();
            iter = new PropertyNodeIterator (feature, Arrays.asList( feature.getProperties() ).iterator());
        } else if ( node instanceof PropertyNode ) {
            Property prop = ( (PropertyNode) node ).getProperty();
            Object propValue = prop.getValue();
            if (propValue instanceof Feature) {
                iter = new SingleObjectIterator(new FeatureNode ((PropertyNode) node, (Feature) propValue));
            }
            if (propValue instanceof String) {
                iter = new SingleObjectIterator(new TextNode ((PropertyNode) node, (String) propValue));
            }
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
        String msg = "getDocumentNode(Object) called with argument (" + contextNode + "), but method not implemented";
        throw new UnsupportedOperationException( msg );
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
            Property prop = ( (PropertyNode) node ).getProperty();
            Object propValue = prop.getValue();
            if ( propValue instanceof String ) {
                value = (String) propValue;
            }
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
    public Iterator<Node> getParentAxisIterator( Object contextNode ) {
        return new SingleObjectIterator( ( (Node) contextNode ).getParent() );
    }

    @Override
    public String getTextStringValue( Object obj ) {
        String value = null;
        if ( obj instanceof TextNode ) {
            value = ( (TextNode) obj ).getValue();
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
        return obj instanceof NamespaceNode;
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
        return new FeatureXPath( xpath );
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
