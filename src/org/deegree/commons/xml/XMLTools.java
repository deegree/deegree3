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

package org.deegree.commons.xml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.deegree.commons.types.QualifiedName;
import org.deegree.commons.utils.StringUtils;
import org.jaxen.JaxenException;
import org.jaxen.XPath;
import org.jaxen.dom.DOMXPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * XML Tools based on JAXP 1.1 for parsing documents and retrieving node values/node attributes. Furthermore this
 * utility class provides node retrieval based on XPath expressions.
 * 
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 * @see XMLAdapter
 * @deprecated replaced by {@link XMLAdapter}
 */
public final class XMLTools {

    private static final Logger LOG = LoggerFactory.getLogger( XMLTools.class );

    private XMLTools() {
        // hidden constructor to prevent instantiation
    }

    /**
     * 
     * Create a new and empty DOM document.
     * 
     * @return a new and empty DOM document.
     */
    public static Document create() {
        return getDocumentBuilder().newDocument();
    }

    /**
     * Create a new document builder with:
     * <UL>
     * <li>namespace awareness = true
     * <li>whitespace ignoring = false
     * <li>validating = false
     * <li>expand entity references = false
     * </UL>
     * 
     * @return new document builder
     */
    public static synchronized DocumentBuilder getDocumentBuilder() {
        DocumentBuilder builder = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware( true );
            factory.setExpandEntityReferences( false );
            factory.setIgnoringElementContentWhitespace( false );
            factory.setValidating( false );
            try {
                factory.setAttribute( "http://apache.org/xml/features/nonvalidating/load-external-dtd", false );
            } catch ( IllegalArgumentException _ ) {
                // ignore it, we just cannot set the feature
            }
            builder = factory.newDocumentBuilder();
        } catch ( Exception ex ) {
            LOG.error( ex.getMessage(), ex );
        }
        return builder;
    }

    // ------------------------------------------------------------------------
    // XPath based parsing methods
    // ------------------------------------------------------------------------

    /**
     * @param contextNode
     * @param xPathQuery
     * @param nsContext
     * @return Node
     * @throws XMLProcessingException
     */
    public static Node getNode( Node contextNode, String xPathQuery, NamespaceContext nsContext )
                            throws XMLProcessingException {
        Node node = null;
        try {
            XPath xpath = new DOMXPath( xPathQuery );
            xpath.setNamespaceContext( nsContext );
            node = (Node) xpath.selectSingleNode( contextNode );

            if ( xPathQuery.endsWith( "text()" ) ) {
                List<Node> nl = xpath.selectNodes( contextNode );
                int pos = xPathQuery.lastIndexOf( "/" );
                if ( pos > 0 ) {
                    xPathQuery = xPathQuery.substring( 0, pos );
                } else {
                    xPathQuery = ".";
                }
                xpath = new DOMXPath( xPathQuery );
                xpath.setNamespaceContext( nsContext );
                List<Node> nl_ = xpath.selectNodes( contextNode );
                List<String> tmp = new ArrayList<String>( nl_.size() );
                for ( int i = 0; i < nl_.size(); i++ ) {
                    tmp.add( getStringValue( nl_.get( i ) ) );
                }

                for ( int i = 0; i < nl.size(); i++ ) {
                    try {
                        nl.get( i ).getParentNode().removeChild( nl.get( i ) );
                    } catch ( Exception e ) {
                        // no exception thrown, why catch them?
                    }
                }

                Document doc = contextNode.getOwnerDocument();
                for ( int i = 0; i < tmp.size(); i++ ) {
                    Text text = doc.createTextNode( tmp.get( i ) );
                    nl_.get( i ).appendChild( text );
                    node = text;
                }
            }

        } catch ( JaxenException e ) {
            throw new XMLProcessingException( "Error evaluating XPath-expression '" + xPathQuery
                                              + "' from context node '" + contextNode.getNodeName() + "': "
                                              + e.getMessage(), e );
        }
        return node;
    }

    /**
     * @param contextNode
     * @param xpath
     * @param nsContext
     * @return the element
     * @throws XMLProcessingException
     * @throws ClassCastException
     *             if the node was not an element
     */
    public static Element getElement( Node contextNode, String xpath, NamespaceContext nsContext )
                            throws XMLProcessingException {
        Node node = getNode( contextNode, xpath, nsContext );
        return (Element) node;
    }

    /**
     * @param contextNode
     * @param xPathQuery
     * @param nsContext
     * @param defaultValue
     * @return the node's String value
     * @throws XMLProcessingException
     */
    public static String getNodeAsString( Node contextNode, String xPathQuery, NamespaceContext nsContext,
                                          String defaultValue )
                            throws XMLProcessingException {

        String value = defaultValue;
        Node node = getNode( contextNode, xPathQuery, nsContext );

        if ( node != null ) {
            value = getStringValue( node );
        }
        return value;
    }

    /**
     * @param contextNode
     * @param xPathQuery
     * @param nsContext
     * @param defaultValue
     * @return the node's boolean value
     * @throws XMLProcessingException
     */
    public static boolean getNodeAsBoolean( Node contextNode, String xPathQuery, NamespaceContext nsContext,
                                            boolean defaultValue )
                            throws XMLProcessingException {
        boolean value = defaultValue;
        Node node = getNode( contextNode, xPathQuery, nsContext );
        if ( node != null ) {
            String stringValue = getStringValue( node );

            if ( "true".equals( stringValue ) || "yes".equals( stringValue ) || "1".equals( stringValue ) ) {
                value = true;
            } else if ( "false".equals( stringValue ) || "no".equals( stringValue ) || "0".equals( stringValue ) ) {
                value = false;
            } else {
                throw new XMLProcessingException( "XPath-expression '" + xPathQuery + " ' from context node '"
                                                  + contextNode.getNodeName() + "' has an invalid value ('"
                                                  + stringValue + "'). Valid values are: 'true', 'yes', '1' "
                                                  + "'false', 'no' and '0'." );
            }
        }
        return value;
    }

    /**
     * @param contextNode
     * @param xPathQuery
     * @param nsContext
     * @param defaultValue
     * @return the node's integer value
     * @throws XMLProcessingException
     */
    public static int getNodeAsInt( Node contextNode, String xPathQuery, NamespaceContext nsContext, int defaultValue )
                            throws XMLProcessingException {
        int value = defaultValue;
        Node node = getNode( contextNode, xPathQuery, nsContext );
        if ( node != null ) {
            String stringValue = getStringValue( node );
            try {
                value = Integer.parseInt( stringValue );
            } catch ( NumberFormatException e ) {
                throw new XMLProcessingException( "Result '" + stringValue + "' of XPath-expression '" + xPathQuery
                                                  + "' from context node '" + contextNode.getNodeName()
                                                  + "' does not denote a valid integer value." );
            }
        }
        return value;
    }

    /**
     * @param contextNode
     * @param xPathQuery
     * @param nsContext
     * @param defaultValue
     * @return the node's double value
     * @throws XMLProcessingException
     */
    public static double getNodeAsDouble( Node contextNode, String xPathQuery, NamespaceContext nsContext,
                                          double defaultValue )
                            throws XMLProcessingException {
        double value = defaultValue;
        Node node = getNode( contextNode, xPathQuery, nsContext );
        if ( node != null ) {
            String stringValue = getStringValue( node );
            try {
                value = Double.parseDouble( stringValue );
            } catch ( NumberFormatException e ) {
                throw new XMLProcessingException( "Result '" + stringValue + "' of XPath-expression '" + xPathQuery
                                                  + "' from context node '" + contextNode.getNodeName()
                                                  + "' does not denote a valid double value." );
            }
        }
        return value;
    }

    /**
     * @param contextNode
     * @param xPathQuery
     * @param nsContext
     * @param defaultValue
     * @return the node as URI
     * @throws XMLProcessingException
     */
    public static String getNodeAsURI( Node contextNode, String xPathQuery, NamespaceContext nsContext,
                                       String defaultValue )
                            throws XMLProcessingException {
        String value = defaultValue;
        Node node = getNode( contextNode, xPathQuery, nsContext );
        if ( node != null ) {
            value = getStringValue( node );
        }
        return value;
    }

    /**
     * @param contextNode
     * @param xPathQuery
     * @param nsContext
     * @param defaultValue
     * @return the node as qualified name
     * @throws XMLProcessingException
     */
    public static QualifiedName getNodeAsQualifiedName( Node contextNode, String xPathQuery,
                                                        NamespaceContext nsContext, QualifiedName defaultValue )
                            throws XMLProcessingException {

        QualifiedName value = defaultValue;
        Node node = getNode( contextNode, xPathQuery, nsContext );

        if ( node != null ) {
            value = getQualifiedNameValue( node );
        }
        return value;

    }

    /**
     * Parses the value of the submitted <code>Node</code> as a <code>QualifiedName</code>.
     * <p>
     * To parse the text contents of an <code>Element</code> node, the actual text node must be given, not the
     * <code>Element</code> node itself.
     * </p>
     * 
     * @param node
     * @return object representation of the element
     * @throws XMLProcessingException
     */
    private static QualifiedName getQualifiedNameValue( Node node )
                            throws XMLProcessingException {

        String name = node.getNodeValue().trim();
        QualifiedName qName = null;
        if ( name.indexOf( ':' ) > -1 ) {
            String[] tmp = StringUtils.split( name, ":" );
            qName = new QualifiedName( tmp[0], tmp[1], XMLTools.getNamespaceForPrefix( tmp[0], node ) );
        } else {
            qName = new QualifiedName( name );
        }
        return qName;
    }

    /**
     * returns a list of nodes matching the passed XPath
     * 
     * @param contextNode
     * @param xPathQuery
     * @param nsContext
     * @return a list of nodes matching the passed XPath
     * @throws XMLProcessingException
     */
    public static List<Node> getNodes( Node contextNode, String xPathQuery, NamespaceContext nsContext )
                            throws XMLProcessingException {
        List<Node> nl = null;
        try {
            XPath xpath = new DOMXPath( xPathQuery );
            xpath.setNamespaceContext( nsContext );
            nl = xpath.selectNodes( contextNode );

            if ( xPathQuery.endsWith( "text()" ) ) {

                int pos = xPathQuery.lastIndexOf( "/" );
                if ( pos > 0 ) {
                    xPathQuery = xPathQuery.substring( 0, pos );
                } else {
                    xPathQuery = ".";
                }
                xpath = new DOMXPath( xPathQuery );
                xpath.setNamespaceContext( nsContext );
                List<?> nl_ = xpath.selectNodes( contextNode );
                List<String> tmp = new ArrayList<String>( nl_.size() );
                for ( int i = 0; i < nl_.size(); i++ ) {
                    tmp.add( getStringValue( (Node) nl_.get( i ) ) );
                }

                for ( int i = 0; i < nl.size(); i++ ) {
                    try {
                        nl.get( i ).getParentNode().removeChild( nl.get( i ) );
                    } catch ( Exception e ) {
                        // ignored, but why? Nothing is actually thrown here?
                    }
                }

                nl.clear();
                Document doc = contextNode.getOwnerDocument();
                for ( int i = 0; i < tmp.size(); i++ ) {
                    Text text = doc.createTextNode( tmp.get( i ) );
                    ( (Node) nl_.get( i ) ).appendChild( text );
                    nl.add( text );
                }
            }
        } catch ( JaxenException e ) {
            throw new XMLProcessingException( "Error evaluating XPath-expression '" + xPathQuery
                                              + "' from context node '" + contextNode.getNodeName() + "': "
                                              + e.getMessage(), e );
        }
        return nl;
    }

    /**
     * @param contextNode
     * @param xPathQuery
     * @param nsContext
     * @return the list of nodes as strings
     * @throws XMLProcessingException
     */
    public static String[] getNodesAsStrings( Node contextNode, String xPathQuery, NamespaceContext nsContext )
                            throws XMLProcessingException {
        String[] values = null;
        List<Node> nl = getNodes( contextNode, xPathQuery, nsContext );
        if ( nl != null ) {
            values = new String[nl.size()];
            for ( int i = 0; i < nl.size(); i++ ) {
                values[i] = getStringValue( nl.get( i ) );
            }
        } else {
            values = new String[0];
        }
        return values;
    }

    /**
     * @param contextNode
     *            to get the strings from
     * @param xPathQuery
     *            finding the nodes
     * @param nsContext
     *            to find the namespaces from.
     * @return the list of nodes as strings or an empty list, never <code>null</code>
     * @throws XMLProcessingException
     */
    public static List<String> getNodesAsStringList( Node contextNode, String xPathQuery, NamespaceContext nsContext )
                            throws XMLProcessingException {
        List<String> result = new ArrayList<String>();
        List<Node> nl = getNodes( contextNode, xPathQuery, nsContext );
        if ( nl != null ) {
            result = new ArrayList<String>( nl.size() );
            for ( int i = 0; i < nl.size(); i++ ) {
                result.add( getStringValue( nl.get( i ) ) );
            }
        }
        return result;
    }

    /**
     * @param contextNode
     * @param xPathQuery
     * @param nsContext
     * @return the nodes as qualified names
     * @throws XMLProcessingException
     */
    public static QualifiedName[] getNodesAsQualifiedNames( Node contextNode, String xPathQuery,
                                                            NamespaceContext nsContext )
                            throws XMLProcessingException {

        QualifiedName[] values = null;
        List<Node> nl = getNodes( contextNode, xPathQuery, nsContext );
        if ( nl != null ) {
            values = new QualifiedName[nl.size()];
            for ( int i = 0; i < nl.size(); i++ ) {
                values[i] = getQualifiedNameValue( nl.get( i ) );
            }
        } else {
            values = new QualifiedName[0];
        }
        return values;

    }

    /**
     * @param contextNode
     * @param xPathQuery
     * @param nsContext
     * @return the node
     * @throws XMLProcessingException
     */
    public static Node getRequiredNode( Node contextNode, String xPathQuery, NamespaceContext nsContext )
                            throws XMLProcessingException {
        Node node = getNode( contextNode, xPathQuery, nsContext );
        if ( node == null ) {
            throw new XMLProcessingException( "XPath-expression '" + xPathQuery + "' from context node '"
                                              + contextNode.getNodeName() + "' yields no result!" );
        }
        return node;
    }

    /**
     * @param contextNode
     * @param xpath
     * @param nsContext
     * @return the element
     * @throws XMLProcessingException
     * @throws ClassCastException
     *             if the node was not an element
     */
    public static Element getRequiredElement( Node contextNode, String xpath, NamespaceContext nsContext )
                            throws XMLProcessingException {
        Node node = getRequiredNode( contextNode, xpath, nsContext );
        return (Element) node;
    }

    /**
     * @param contextNode
     * @param xPathQuery
     * @param nsContext
     * @return the node as string
     * @throws XMLProcessingException
     */
    public static String getRequiredNodeAsString( Node contextNode, String xPathQuery, NamespaceContext nsContext )
                            throws XMLProcessingException {
        Node node = getRequiredNode( contextNode, xPathQuery, nsContext );
        return getStringValue( node );
    }

    /**
     * @param contextNode
     *            the parent of the requested node
     * @param xPathQuery
     *            the node to get out of the dom
     * @param nsContext
     *            context of the node
     * @param validValues
     *            the values that are valid for the required node
     * @return one of the String valid String values
     * @throws XMLProcessingException
     *             if no Node was found or the text of the Node was not present in the given valid strings.
     */
    public static String getRequiredNodeAsString( Node contextNode, String xPathQuery, NamespaceContext nsContext,
                                                  String[] validValues )
                            throws XMLProcessingException {
        String value = getRequiredNodeAsString( contextNode, xPathQuery, nsContext );
        boolean found = false;
        for ( int i = 0; i < validValues.length; i++ ) {
            if ( value.equals( validValues[i] ) ) {
                found = true;
                break;
            }
        }
        if ( !found ) {
            StringBuffer sb = new StringBuffer( "XPath-expression '" + xPathQuery + " ' from context node '"
                                                + contextNode.getNodeName()
                                                + "' has an invalid value. Valid values are: " );
            for ( int i = 0; i < validValues.length; i++ ) {
                sb.append( "'" ).append( validValues[i] ).append( "'" );
                if ( i != validValues.length - 1 ) {
                    sb.append( ", " );
                } else {
                    sb.append( "." );
                }
            }
            throw new XMLProcessingException( sb.toString() );
        }
        return value;
    }

    /**
     * Returns the parts of the targeted node value which are separated by the specified token.
     * 
     * @param contextNode
     * @param xPathQuery
     * @param nsContext
     * @param token
     * @return the parts of the targeted node value which are separated by the specified token.
     * @throws XMLProcessingException
     */
    public static String[] getRequiredNodeAsStrings( Node contextNode, String xPathQuery, NamespaceContext nsContext,
                                                     String token )
                            throws XMLProcessingException {
        Node node = getRequiredNode( contextNode, xPathQuery, nsContext );
        return StringUtils.split( getStringValue( node ), token );
    }

    /**
     * @param contextNode
     * @param xPathQuery
     * @param nsContext
     * @return the node as boolean
     * @throws XMLProcessingException
     */
    public static boolean getRequiredNodeAsBoolean( Node contextNode, String xPathQuery, NamespaceContext nsContext )
                            throws XMLProcessingException {
        boolean value = false;
        Node node = getRequiredNode( contextNode, xPathQuery, nsContext );
        String stringValue = getStringValue( node );
        if ( "true".equals( stringValue ) || "yes".equals( stringValue ) ) {
            value = true;
        } else if ( "false".equals( stringValue ) || "no".equals( stringValue ) ) {
            value = false;
        } else {
            throw new XMLProcessingException( "XPath-expression '" + xPathQuery + " ' from context node '"
                                              + contextNode.getNodeName() + "' has an invalid value ('" + stringValue
                                              + "'). Valid values are: 'true', 'yes', 'false' and 'no'." );
        }

        return value;
    }

    /**
     * @param contextNode
     * @param xPathQuery
     * @param nsContext
     * @return the node as integer
     * @throws XMLProcessingException
     */
    public static int getRequiredNodeAsInt( Node contextNode, String xPathQuery, NamespaceContext nsContext )
                            throws XMLProcessingException {

        int value = 0;
        String stringValue = getRequiredNodeAsString( contextNode, xPathQuery, nsContext );
        try {
            value = Integer.parseInt( stringValue );
        } catch ( NumberFormatException e ) {
            throw new XMLProcessingException( "Result '" + stringValue + "' of XPath-expression '" + xPathQuery
                                              + "' from context node '" + contextNode.getNodeName()
                                              + "' does not denote a valid integer value." );
        }
        return value;
    }

    /**
     * @param contextNode
     * @param xPathQuery
     * @param nsContext
     * @return the node as double
     * @throws XMLProcessingException
     */
    public static double getRequiredNodeAsDouble( Node contextNode, String xPathQuery, NamespaceContext nsContext )
                            throws XMLProcessingException {

        double value = 0;
        String stringValue = getRequiredNodeAsString( contextNode, xPathQuery, nsContext );
        try {
            value = Double.parseDouble( stringValue );
        } catch ( NumberFormatException e ) {
            throw new XMLProcessingException( "Result '" + stringValue + "' of XPath-expression '" + xPathQuery
                                              + "' from context node '" + contextNode.getNodeName()
                                              + "' does not denote a valid double value." );
        }
        return value;
    }

    /**
     * Returns the parts of the targeted node value which are separated by the specified regex. The string parts are
     * converted to doubles.
     * 
     * @param contextNode
     * @param xPathQuery
     * @param nsContext
     * @param regex
     * @return the parts of the targeted node value which are separated by the specified regex.
     * @throws XMLProcessingException
     */
    public static double[] getRequiredNodeAsDoubles( Node contextNode, String xPathQuery, NamespaceContext nsContext,
                                                     String regex )
                            throws XMLProcessingException {
        String[] parts = getRequiredNodeAsStrings( contextNode, xPathQuery, nsContext, regex );
        double[] doubles = new double[parts.length];
        for ( int i = 0; i < parts.length; i++ ) {
            try {
                doubles[i] = Double.parseDouble( parts[i] );
            } catch ( NumberFormatException e ) {
                throw new XMLProcessingException( "Value '" + parts[i] + "' does not denote a valid double value." );
            }
        }
        return doubles;
    }

    /**
     * @param contextNode
     * @param xPathQuery
     * @param nsContext
     * @return the node as qualified name
     * @throws XMLProcessingException
     */
    public static QualifiedName getRequiredNodeAsQualifiedName( Node contextNode, String xPathQuery,
                                                                NamespaceContext nsContext )
                            throws XMLProcessingException {
        Node node = getRequiredNode( contextNode, xPathQuery, nsContext );
        return getQualifiedNameValue( node );
    }

    /**
     * @param contextNode
     * @param xPathQuery
     * @param nsContext
     * @return the nodes
     * @throws XMLProcessingException
     */
    public static List<Node> getRequiredNodes( Node contextNode, String xPathQuery, NamespaceContext nsContext )
                            throws XMLProcessingException {
        List<Node> nl = getNodes( contextNode, xPathQuery, nsContext );
        if ( nl.size() == 0 ) {
            throw new XMLProcessingException( "XPath-expression: '" + xPathQuery + "' from context node '"
                                              + contextNode.getNodeName() + "' does not yield a result." );
        }

        return nl;
    }

    /**
     * @param contextNode
     * @param xpath
     * @param nsContext
     * @return a list of Elements
     * @throws XMLProcessingException
     * @throws ClassCastException
     *             if the resulting nodes of the xpath are not elements
     */
    public static List<Element> getRequiredElements( Node contextNode, String xpath, NamespaceContext nsContext )
                            throws XMLProcessingException {
        List<Node> nodes = getRequiredNodes( contextNode, xpath, nsContext );

        List<Element> list = new ArrayList<Element>( nodes.size() );
        for ( Node n : nodes ) {
            list.add( (Element) n );
        }

        return list;
    }

    /**
     * @param contextNode
     * @param xpath
     * @param nsContext
     * @return a list of Elements
     * @throws XMLProcessingException
     * @throws ClassCastException
     *             if the resulting nodes of the xpath are not elements
     */
    public static List<Element> getElements( Node contextNode, String xpath, NamespaceContext nsContext )
                            throws XMLProcessingException {
        List<Node> nodes = getNodes( contextNode, xpath, nsContext );

        List<Element> list = new ArrayList<Element>( nodes.size() );
        for ( Node n : nodes ) {
            list.add( (Element) n );
        }

        return list;
    }

    /**
     * Returns the content of the nodes matching the XPathQuery as a String array. At least one node must match the
     * query otherwise an exception will be thrown.
     * 
     * @param contextNode
     * @param xPathQuery
     * @param nsContext
     * @return the content of the nodes matching the XPathQuery as a String array.
     * @throws XMLProcessingException
     */
    public static String[] getRequiredNodesAsStrings( Node contextNode, String xPathQuery, NamespaceContext nsContext )
                            throws XMLProcessingException {

        List<Node> nl = getRequiredNodes( contextNode, xPathQuery, nsContext );

        String[] values = new String[nl.size()];
        for ( int i = 0; i < nl.size(); i++ ) {
            values[i] = getStringValue( nl.get( i ) );
        }

        return values;
    }

    /**
     * @param contextNode
     * @param xPathQuery
     * @param nsContext
     * @return the qualified names
     * @throws XMLProcessingException
     */
    public static QualifiedName[] getRequiredNodesAsQualifiedNames( Node contextNode, String xPathQuery,
                                                                    NamespaceContext nsContext )
                            throws XMLProcessingException {

        List<Node> nl = getRequiredNodes( contextNode, xPathQuery, nsContext );

        QualifiedName[] values = new QualifiedName[nl.size()];
        for ( int i = 0; i < nl.size(); i++ ) {
            values[i] = getQualifiedNameValue( nl.get( i ) );
        }

        return values;
    }

    /**
     * @param value
     * @param validValues
     * @throws XMLProcessingException
     */
    public static void checkValue( String value, String[] validValues )
                            throws XMLProcessingException {
        for ( int i = 0; i < validValues.length; i++ ) {
            if ( validValues[i].equals( value ) ) {
                return;
            }
        }
        StringBuffer sb = new StringBuffer( "Value '" ).append( value ).append( "' is invalid. Valid values are: " );
        for ( int i = 0; i < validValues.length; i++ ) {
            sb.append( "'" ).append( validValues[i] ).append( "'" );
            if ( i != validValues.length - 1 ) {
                sb.append( ", " );
            } else {
                sb.append( "." );
            }
        }
        throw new XMLProcessingException( sb.toString() );
    }

    // ------------------------------------------------------------------------
    // Node creation methods
    // ------------------------------------------------------------------------

    /**
     * Creates a new <code>Element</code> node from the given parameters and appends it to the also specified
     * <code>Element</code>.
     * 
     * @param element
     *            <code>Element</code> that the new <code>Element</code> is appended to
     * @param namespaceURI
     *            use null for default namespace
     * @param name
     *            qualified name
     * @return the appended <code>Element</code> node
     */
    public static Element appendElement( Element element, String namespaceURI, String name ) {
        return appendElement( element, namespaceURI, name, null );
    }

    /**
     * Creates a new <code>Element</code> node from the given parameters and appends it to the also specified
     * <code>Element</code>. Adds a text node to the newly generated <code>Element</code> as well.
     * 
     * @param element
     *            <code>Element</code> that the new <code>Element</code> is appended to
     * @param namespaceURI
     *            use null for default namespace
     * @param name
     *            qualified name
     * @param nodeValue
     *            value for a text node that is appended to the generated element
     * @return the appended <code>Element</code> node
     */
    public static Element appendElement( Element element, String namespaceURI, String name, String nodeValue ) {
        String namespace = namespaceURI;
        Element newElement = element.getOwnerDocument().createElementNS( namespace, name );
        if ( nodeValue != null && !nodeValue.equals( "" ) )
            newElement.appendChild( element.getOwnerDocument().createTextNode( nodeValue ) );
        element.appendChild( newElement );
        return newElement;
    }

    /**
     * Appends a namespace binding for the specified element that binds the given prefix to the given namespace using a
     * special attribute: xmlns:prefix=namespace
     * 
     * @param element
     * @param prefix
     * @param namespace
     */
    public static void appendNSBinding( Element element, String prefix, String namespace ) {
        Attr attribute = element.getOwnerDocument().createAttributeNS( CommonNamespaces.XMLNS,
                                                                       CommonNamespaces.XMLNS_PREFIX + ":" + prefix );
        attribute.setNodeValue( namespace );
        element.getAttributes().setNamedItemNS( attribute );
    }

    /**
     * Appends the default namespace binding for the specified element.
     * 
     * @param element
     * @param prefix
     * @param namespace
     */
    public static void appendNSDefaultBinding( Element element, String namespace ) {
        Attr attribute = element.getOwnerDocument().createAttributeNS( CommonNamespaces.XMLNS,
                                                                       CommonNamespaces.XMLNS_PREFIX );
        attribute.setNodeValue( namespace );
        element.getAttributes().setNamedItemNS( attribute );
    }

    /**
     * Appends the given namespace bindings to the specified element.
     * <p>
     * NOTE: The prebound prefix "xml" is skipped.
     * 
     * @param element
     * @param nsContext
     */
    public static void appendNSBindings( Element element, NamespaceContext nsContext ) {
        Map<String, String> namespaceMap = nsContext.getNamespaceMap();
        Iterator<String> prefixIter = namespaceMap.keySet().iterator();
        while ( prefixIter.hasNext() ) {
            String prefix = prefixIter.next();
            if ( !CommonNamespaces.XMLNS_PREFIX.equals( prefix ) ) {
                String namespace = namespaceMap.get( prefix );
                appendNSBinding( element, prefix, namespace );
            }
        }
    }

    /**
     * Returns the text contained in the specified element.
     * 
     * @param node
     *            current element
     * @return the textual contents of the element
     */
    public static String getStringValue( Node node ) {
        NodeList children = node.getChildNodes();
        StringBuffer sb = new StringBuffer( children.getLength() * 500 );
        if ( node.getNodeValue() != null ) {
            sb.append( node.getNodeValue().trim() );
        }
        if ( node.getNodeType() != Node.ATTRIBUTE_NODE ) {
            for ( int i = 0; i < children.getLength(); i++ ) {
                if ( children.item( i ).getNodeType() == Node.TEXT_NODE
                     || children.item( i ).getNodeType() == Node.CDATA_SECTION_NODE ) {
                    sb.append( children.item( i ).getNodeValue() );
                }
            }
        }
        return sb.toString();
    }

    /**
     * Returns the namespace URI that is bound to a given prefix at a certain node in the DOM tree.
     * 
     * @param prefix
     * @param node
     * @return namespace URI that is bound to the given prefix, null otherwise
     * @throws URISyntaxException
     */
    public static String getNamespaceForPrefix( String prefix, Node node ) {
        if ( node == null ) {
            return null;
        }
        if ( node.getNodeType() == Node.ELEMENT_NODE ) {
            NamedNodeMap nnm = node.getAttributes();
            if ( nnm != null ) {
                // LOG.logDebug( "(searching namespace for prefix (" + prefix
                // + "), resulted in a namedNodeMap for the currentNode: " + node.getNodeName() );
                for ( int i = 0; i < nnm.getLength(); i++ ) {
                    Attr a = (Attr) nnm.item( i );
                    // LOG.logDebug( "\t(searching namespace for prefix (" + prefix + "), resulted
                    // in an attribute: "
                    // + a.getName() );

                    if ( a.getName().startsWith( "xmlns:" ) && a.getName().endsWith( ':' + prefix ) ) {
                        a.getValue();
                    } else if ( prefix == null && a.getName().equals( "xmlns" ) ) {
                        a.getValue();
                    }
                }
            }
        } else if ( node.getNodeType() == Node.ATTRIBUTE_NODE ) {
            return getNamespaceForPrefix( prefix, ( (Attr) node ).getOwnerElement() );
        }
        return getNamespaceForPrefix( prefix, node.getParentNode() );
    }

}