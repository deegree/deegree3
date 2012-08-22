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

package org.deegree.framework.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.ogcbase.CommonNamespaces;
import org.jaxen.JaxenException;
import org.jaxen.XPath;
import org.jaxen.dom.DOMXPath;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * XML Tools based on JAXP 1.1 for parsing documents and retrieving node values/node attributes. Furthermore this
 * utility class provides node retrieval based on XPath expressions.
 *
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public final class XMLTools {

    private static final ILogger LOG = LoggerFactory.getLogger( XMLTools.class );

    private XMLTools() {
        // hidden constructor to prevent instantiation
    }

    // ------------------------------------------------------------------------
    // XPath based parsing methods
    // ------------------------------------------------------------------------

    /**
     * @param contextNode
     * @param xPathQuery
     * @param nsContext
     * @return Node
     * @throws XMLParsingException
     */
    public static Node getNode( Node contextNode, String xPathQuery, NamespaceContext nsContext )
                            throws XMLParsingException {
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
            throw new XMLParsingException( "Error evaluating XPath-expression '" + xPathQuery + "' from context node '"
                                           + contextNode.getNodeName() + "': " + e.getMessage(), e );
        }
        return node;
    }

    /**
     * @param contextNode
     * @param xpath
     * @param nsContext
     * @return the element
     * @throws XMLParsingException
     * @throws ClassCastException
     *             if the node was not an element
     */
    public static Element getElement( Node contextNode, String xpath, NamespaceContext nsContext )
                            throws XMLParsingException {
        Node node = getNode( contextNode, xpath, nsContext );
        return (Element) node;
    }

    /**
     * @param contextNode
     * @param xPathQuery
     * @param nsContext
     * @param defaultValue
     * @return the node's String value
     * @throws XMLParsingException
     */
    public static String getNodeAsString( Node contextNode, String xPathQuery, NamespaceContext nsContext,
                                          String defaultValue )
                            throws XMLParsingException {

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
     * @throws XMLParsingException
     */
    public static boolean getNodeAsBoolean( Node contextNode, String xPathQuery, NamespaceContext nsContext,
                                            boolean defaultValue )
                            throws XMLParsingException {
        boolean value = defaultValue;
        Node node = getNode( contextNode, xPathQuery, nsContext );
        if ( node != null ) {
            String stringValue = getStringValue( node );

            if ( "true".equals( stringValue ) || "yes".equals( stringValue ) || "1".equals( stringValue ) ) {
                value = true;
            } else if ( "false".equals( stringValue ) || "no".equals( stringValue ) || "0".equals( stringValue ) ) {
                value = false;
            } else {
                throw new XMLParsingException( "XPath-expression '" + xPathQuery + " ' from context node '"
                                               + contextNode.getNodeName() + "' has an invalid value ('" + stringValue
                                               + "'). Valid values are: 'true', 'yes', '1' " + "'false', 'no' and '0'." );
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
     * @throws XMLParsingException
     */
    public static int getNodeAsInt( Node contextNode, String xPathQuery, NamespaceContext nsContext, int defaultValue )
                            throws XMLParsingException {
        int value = defaultValue;
        Node node = getNode( contextNode, xPathQuery, nsContext );
        if ( node != null ) {
            String stringValue = getStringValue( node );
            try {
                value = Integer.parseInt( stringValue );
            } catch ( NumberFormatException e ) {
                throw new XMLParsingException( "Result '" + stringValue + "' of XPath-expression '" + xPathQuery
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
     * @throws XMLParsingException
     */
    public static double getNodeAsDouble( Node contextNode, String xPathQuery, NamespaceContext nsContext,
                                          double defaultValue )
                            throws XMLParsingException {
        double value = defaultValue;
        Node node = getNode( contextNode, xPathQuery, nsContext );
        if ( node != null ) {
            String stringValue = getStringValue( node );
            try {
                value = Double.parseDouble( stringValue );
            } catch ( NumberFormatException e ) {
                throw new XMLParsingException( "Result '" + stringValue + "' of XPath-expression '" + xPathQuery
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
     * @throws XMLParsingException
     */
    public static URI getNodeAsURI( Node contextNode, String xPathQuery, NamespaceContext nsContext, URI defaultValue )
                            throws XMLParsingException {
        URI value = defaultValue;
        Node node = getNode( contextNode, xPathQuery, nsContext );
        if ( node != null ) {
            String stringValue = getStringValue( node );
            try {
                value = new URI( stringValue );
            } catch ( URISyntaxException e ) {
                throw new XMLParsingException( "Result '" + stringValue + "' of XPath-expression '" + xPathQuery
                                               + "' from context node '" + contextNode.getNodeName()
                                               + "' does not denote a valid URI." );
            }
        }
        return value;
    }

    /**
     * @param contextNode
     * @param xPathQuery
     * @param nsContext
     * @param defaultValue
     * @return the node as qualified name
     * @throws XMLParsingException
     */
    public static QualifiedName getNodeAsQualifiedName( Node contextNode, String xPathQuery,
                                                        NamespaceContext nsContext, QualifiedName defaultValue )
                            throws XMLParsingException {

        QualifiedName value = defaultValue;
        Node node = getNode( contextNode, xPathQuery, nsContext );

        if ( node != null ) {
            value = getQualifiedNameValue( node );
        }
        return value;

    }

    /**
     * returns a list of nodes matching the passed XPath
     *
     * @param contextNode
     * @param xPathQuery
     * @param nsContext
     * @return a list of nodes matching the passed XPath
     * @throws XMLParsingException
     */
    public static List<Node> getNodes( Node contextNode, String xPathQuery, NamespaceContext nsContext )
                            throws XMLParsingException {
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
            throw new XMLParsingException( "Error evaluating XPath-expression '" + xPathQuery + "' from context node '"
                                           + contextNode.getNodeName() + "': " + e.getMessage(), e );
        }
        return nl;
    }

    /**
     * @param contextNode
     * @param xPathQuery
     * @param nsContext
     * @return the list of nodes as strings
     * @throws XMLParsingException
     */
    public static String[] getNodesAsStrings( Node contextNode, String xPathQuery, NamespaceContext nsContext )
                            throws XMLParsingException {
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
     * @throws XMLParsingException
     */
    public static List<String> getNodesAsStringList( Node contextNode, String xPathQuery, NamespaceContext nsContext )
                            throws XMLParsingException {
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
     * @return the nodes as URIs
     * @throws XMLParsingException
     */
    public static URI[] getNodesAsURIs( Node contextNode, String xPathQuery, NamespaceContext nsContext )
                            throws XMLParsingException {
        String[] values = getNodesAsStrings( contextNode, xPathQuery, nsContext );
        URI[] uris = new URI[values.length];
        for ( int i = 0; i < uris.length; i++ ) {
            try {
                uris[i] = new URI( values[i] );
            } catch ( URISyntaxException e ) {
                throw new XMLParsingException( "Result '" + values[i] + "' of XPath-expression '" + xPathQuery
                                               + "' from context node '" + contextNode.getNodeName()
                                               + "' does not denote a valid URI." );
            }
        }
        return uris;
    }

    /**
     * @param contextNode
     * @param xPathQuery
     * @param nsContext
     * @return the nodes as qualified names
     * @throws XMLParsingException
     */
    public static QualifiedName[] getNodesAsQualifiedNames( Node contextNode, String xPathQuery,
                                                            NamespaceContext nsContext )
                            throws XMLParsingException {

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
     * @throws XMLParsingException
     */
    public static Node getRequiredNode( Node contextNode, String xPathQuery, NamespaceContext nsContext )
                            throws XMLParsingException {
        Node node = getNode( contextNode, xPathQuery, nsContext );
        if ( node == null ) {
            throw new XMLParsingException( "XPath-expression '" + xPathQuery + "' from context node '"
                                           + contextNode.getNodeName() + "' yields no result!" );
        }
        return node;
    }

    /**
     * @param contextNode
     * @param xpath
     * @param nsContext
     * @return the element
     * @throws XMLParsingException
     * @throws ClassCastException
     *             if the node was not an element
     */
    public static Element getRequiredElement( Node contextNode, String xpath, NamespaceContext nsContext )
                            throws XMLParsingException {
        Node node = getRequiredNode( contextNode, xpath, nsContext );
        return (Element) node;
    }

    /**
     * @param contextNode
     * @param xPathQuery
     * @param nsContext
     * @return the node as string
     * @throws XMLParsingException
     */
    public static String getRequiredNodeAsString( Node contextNode, String xPathQuery, NamespaceContext nsContext )
                            throws XMLParsingException {
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
     * @throws XMLParsingException
     *             if no Node was found or the text of the Node was not present in the given valid strings.
     */
    public static String getRequiredNodeAsString( Node contextNode, String xPathQuery, NamespaceContext nsContext,
                                                  String[] validValues )
                            throws XMLParsingException {
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
            throw new XMLParsingException( sb.toString() );
        }
        return value;
    }

    /**
     * Returns the parts of the targeted node value which are separated by the specified regex.
     *
     * @param contextNode
     * @param xPathQuery
     * @param nsContext
     * @param regex
     * @return the parts of the targeted node value which are separated by the specified regex.
     * @throws XMLParsingException
     */
    public static String[] getRequiredNodeAsStrings( Node contextNode, String xPathQuery, NamespaceContext nsContext,
                                                     String regex )
                            throws XMLParsingException {
        Node node = getRequiredNode( contextNode, xPathQuery, nsContext );
        return StringTools.toArray( getStringValue( node ), regex, false );
    }

    /**
     * @param contextNode
     * @param xPathQuery
     * @param nsContext
     * @return the node as boolean
     * @throws XMLParsingException
     */
    public static boolean getRequiredNodeAsBoolean( Node contextNode, String xPathQuery, NamespaceContext nsContext )
                            throws XMLParsingException {
        boolean value = false;
        Node node = getRequiredNode( contextNode, xPathQuery, nsContext );
        String stringValue = getStringValue( node );
        if ( "true".equals( stringValue ) || "yes".equals( stringValue ) ) {
            value = true;
        } else if ( "false".equals( stringValue ) || "no".equals( stringValue ) ) {
            value = false;
        } else {
            throw new XMLParsingException( "XPath-expression '" + xPathQuery + " ' from context node '"
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
     * @throws XMLParsingException
     */
    public static int getRequiredNodeAsInt( Node contextNode, String xPathQuery, NamespaceContext nsContext )
                            throws XMLParsingException {

        int value = 0;
        String stringValue = getRequiredNodeAsString( contextNode, xPathQuery, nsContext );
        try {
            value = Integer.parseInt( stringValue );
        } catch ( NumberFormatException e ) {
            throw new XMLParsingException( "Result '" + stringValue + "' of XPath-expression '" + xPathQuery
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
     * @throws XMLParsingException
     */
    public static double getRequiredNodeAsDouble( Node contextNode, String xPathQuery, NamespaceContext nsContext )
                            throws XMLParsingException {

        double value = 0;
        String stringValue = getRequiredNodeAsString( contextNode, xPathQuery, nsContext );
        try {
            value = Double.parseDouble( stringValue );
        } catch ( NumberFormatException e ) {
            throw new XMLParsingException( "Result '" + stringValue + "' of XPath-expression '" + xPathQuery
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
     * @throws XMLParsingException
     */
    public static double[] getRequiredNodeAsDoubles( Node contextNode, String xPathQuery, NamespaceContext nsContext,
                                                     String regex )
                            throws XMLParsingException {
        String[] parts = getRequiredNodeAsStrings( contextNode, xPathQuery, nsContext, regex );
        double[] doubles = new double[parts.length];
        for ( int i = 0; i < parts.length; i++ ) {
            try {
                doubles[i] = Double.parseDouble( parts[i] );
            } catch ( NumberFormatException e ) {
                throw new XMLParsingException( "Value '" + parts[i] + "' does not denote a valid double value." );
            }
        }
        return doubles;
    }

    /**
     * @param contextNode
     * @param xPathQuery
     * @param nsContext
     * @return the node as URI
     * @throws XMLParsingException
     */
    public static URI getRequiredNodeAsURI( Node contextNode, String xPathQuery, NamespaceContext nsContext )
                            throws XMLParsingException {

        URI uri = null;
        String stringValue = getRequiredNodeAsString( contextNode, xPathQuery, nsContext );

        try {
            uri = new URI( stringValue );
        } catch ( URISyntaxException e ) {
            throw new XMLParsingException( "Result '" + stringValue + "' of XPath-expression '" + xPathQuery
                                           + "' from context node '" + contextNode.getNodeName()
                                           + "' does not denote a valid URI." );
        }
        return uri;
    }

    /**
     * @param contextNode
     * @param xPathQuery
     * @param nsContext
     * @return the node as qualified name
     * @throws XMLParsingException
     */
    public static QualifiedName getRequiredNodeAsQualifiedName( Node contextNode, String xPathQuery,
                                                                NamespaceContext nsContext )
                            throws XMLParsingException {
        Node node = getRequiredNode( contextNode, xPathQuery, nsContext );
        return getQualifiedNameValue( node );
    }

    /**
     * @param contextNode
     * @param xPathQuery
     * @param nsContext
     * @return the nodes
     * @throws XMLParsingException
     */
    public static List<Node> getRequiredNodes( Node contextNode, String xPathQuery, NamespaceContext nsContext )
                            throws XMLParsingException {
        List<Node> nl = getNodes( contextNode, xPathQuery, nsContext );
        if ( nl.size() == 0 ) {
            throw new XMLParsingException( "XPath-expression: '" + xPathQuery + "' from context node '"
                                           + contextNode.getNodeName() + "' does not yield a result." );
        }

        return nl;
    }

    /**
     * @param contextNode
     * @param xpath
     * @param nsContext
     * @return a list of Elements
     * @throws XMLParsingException
     * @throws ClassCastException
     *             if the resulting nodes of the xpath are not elements
     */
    public static List<Element> getRequiredElements( Node contextNode, String xpath, NamespaceContext nsContext )
                            throws XMLParsingException {
        List<Node> nodes = getRequiredNodes( contextNode, xpath, nsContext );

        ArrayList<Element> list = new ArrayList<Element>( nodes.size() );
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
     * @throws XMLParsingException
     * @throws ClassCastException
     *             if the resulting nodes of the xpath are not elements
     */
    public static List<Element> getElements( Node contextNode, String xpath, NamespaceContext nsContext )
                            throws XMLParsingException {
        List<Node> nodes = getNodes( contextNode, xpath, nsContext );

        ArrayList<Element> list = new ArrayList<Element>( nodes.size() );
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
     * @throws XMLParsingException
     */
    public static String[] getRequiredNodesAsStrings( Node contextNode, String xPathQuery, NamespaceContext nsContext )
                            throws XMLParsingException {

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
     * @throws XMLParsingException
     */
    public static QualifiedName[] getRequiredNodesAsQualifiedNames( Node contextNode, String xPathQuery,
                                                                    NamespaceContext nsContext )
                            throws XMLParsingException {

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
     * @throws XMLParsingException
     */
    public static void checkValue( String value, String[] validValues )
                            throws XMLParsingException {
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
        throw new XMLParsingException( sb.toString() );
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
    public static Element appendElement( Element element, URI namespaceURI, String name ) {
        return appendElement( element, namespaceURI, name, null );
    }

    /**
     * Appends a namespace binding for the specified element that binds the given prefix to the given namespace using a
     * special attribute: xmlns:prefix=namespace
     *
     * @param element
     * @param prefix
     * @param namespace
     */
    public static void appendNSBinding( Element element, String prefix, URI namespace ) {
        Attr attribute = element.getOwnerDocument().createAttributeNS( CommonNamespaces.XMLNS.toASCIIString(),
                                                                       CommonNamespaces.XMLNS_PREFIX + ":" + prefix );
        attribute.setNodeValue( namespace.toASCIIString() );
        element.getAttributes().setNamedItemNS( attribute );
    }

    /**
     * Appends the default namespace binding for the specified element.
     *
     * @param element
     * @param namespace
     */
    public static void appendNSDefaultBinding( Element element, URI namespace ) {
        Attr attribute = element.getOwnerDocument().createAttributeNS( CommonNamespaces.XMLNS.toASCIIString(),
                                                                       CommonNamespaces.XMLNS_PREFIX );
        attribute.setNodeValue( namespace.toASCIIString() );
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
        Map<String, URI> namespaceMap = nsContext.getNamespaceMap();
        Iterator<String> prefixIter = namespaceMap.keySet().iterator();
        while ( prefixIter.hasNext() ) {
            String prefix = prefixIter.next();
            if ( !CommonNamespaces.XMLNS_PREFIX.equals( prefix ) ) {
                URI namespace = namespaceMap.get( prefix );
                appendNSBinding( element, prefix, namespace );
            }
        }
    }

    // ------------------------------------------------------------------------
    // String value methods
    // ------------------------------------------------------------------------

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
     * Returns the text contained in the specified child element of the given element.
     *
     * @param name
     *            name of the child element
     * @param namespace
     *            namespace of the child element
     * @param node
     *            current element
     * @param defaultValue
     *            default value if element is missing
     * @return the textual contents of the element or the given default value, if missing
     */
    public static String getStringValue( String name, URI namespace, Node node, String defaultValue ) {

        String value = defaultValue;
        Element element = getChildElement( name, namespace, node );

        if ( element != null ) {
            value = getStringValue( element );
        }
        if ( value == null || value.equals( "" ) ) {
            value = defaultValue;
        }

        return value;
    }

    /**
     * Returns the text contained in the specified child element of the given element.
     *
     * @param name
     *            name of the child element
     * @param namespace
     *            namespace of the child element
     * @param node
     *            current element
     * @return the textual contents of the element or null, if it is missing
     * @throws XMLParsingException
     *             if the specified child element is missing
     */
    public static String getRequiredStringValue( String name, URI namespace, Node node )
                            throws XMLParsingException {
        Element element = getRequiredChildElement( name, namespace, node );
        return getStringValue( element );
    }

    /**
     * Returns the value of the specified node attribute.
     *
     * @param name
     *            name of attribute
     * @param namespaceURI
     *            namespace of attribute
     * @param node
     *            current element
     * @return the textual contents of the attribute
     * @throws XMLParsingException
     *             if specified attribute is missing
     */
    public static String getRequiredAttrValue( String name, URI namespaceURI, Node node )
                            throws XMLParsingException {

        String namespace = namespaceURI == null ? null : namespaceURI.toString();

        String value = null;
        NamedNodeMap atts = node.getAttributes();
        if ( atts != null ) {
            Attr attribute = null;
            if ( namespace == null ) {
                attribute = (Attr) atts.getNamedItem( name );
            } else {
                attribute = (Attr) atts.getNamedItemNS( namespace, name );
            }

            if ( attribute != null ) {
                value = attribute.getValue();
            }
        }
        if ( value == null ) {
            throw new XMLParsingException( "Required attribute " + name + '(' + namespaceURI + ") of element "
                                           + node.getNodeName() + " is missing." );
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
     * @throws XMLParsingException
     */
    public static QualifiedName getQualifiedNameValue( Node node )
                            throws XMLParsingException {

        String name = node.getTextContent().trim();
        QualifiedName qName = null;
        if ( name.indexOf( ':' ) > -1 ) {
            String[] tmp = StringTools.toArray( name, ":", false );
            try {
                qName = new QualifiedName( tmp[0], tmp[1], XMLTools.getNamespaceForPrefix( tmp[0], node ) );
            } catch ( URISyntaxException e ) {
                throw new XMLParsingException( e.getMessage(), e );
            }
        } else {
            qName = new QualifiedName( name );
        }
        return qName;
    }

    /**
     * Returns the namespace URI that is bound to a given prefix at a certain node in the DOM tree.
     *
     * @param prefix
     * @param node
     * @return namespace URI that is bound to the given prefix, null otherwise
     * @throws URISyntaxException
     */
    public static URI getNamespaceForPrefix( String prefix, Node node )
                            throws URISyntaxException {
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
                        return new URI( a.getValue() );
                    } else if ( prefix == null && a.getName().equals( "xmlns" ) ) {
                        return new URI( a.getValue() );
                    }
                }
            }
        } else if ( node.getNodeType() == Node.ATTRIBUTE_NODE ) {
            return getNamespaceForPrefix( prefix, ( (Attr) node ).getOwnerElement() );
        }
        return getNamespaceForPrefix( prefix, node.getParentNode() );
    }

    // ------------------------------------------------------------------------
    // Old code - deprecated
    // ------------------------------------------------------------------------

    /**
     * Returns the specified child element of the given element. If there is more than one element with that name, the
     * first one is returned.
     *
     * @deprecated
     * @param name
     *            name of the child element
     * @param namespaceURI
     *            namespaceURI of the child element
     * @param node
     *            current element
     * @return the element or null, if it is missing
     * @throws XMLParsingException
     *             if the specified child element is missing
     * @throws XMLParsingException
     * @todo refactoring required
     */
    @Deprecated
    public static Element getRequiredChildElement( String name, URI namespaceURI, Node node )
                            throws XMLParsingException {

        String namespace = namespaceURI == null ? null : namespaceURI.toString();

        NodeList nl = node.getChildNodes();
        Element element = null;
        Element childElement = null;

        if ( ( nl != null ) && ( nl.getLength() > 0 ) ) {
            for ( int i = 0; i < nl.getLength(); i++ ) {
                if ( nl.item( i ) instanceof Element ) {
                    element = (Element) nl.item( i );
                    String s = element.getNamespaceURI();
                    if ( ( s == null && namespace == null ) || ( namespace != null && namespace.equals( s ) ) ) {
                        if ( element.getLocalName().equals( name ) ) {
                            childElement = element;
                            break;
                        }
                    }
                }
            }
        }

        if ( childElement == null ) {
            throw new XMLParsingException( "Required child-element " + name + '(' + namespaceURI + ") of element "
                                           + node.getNodeName() + " is missing." );
        }

        return childElement;
    }

    /**
     * Returns the specified child element of the given element. If there is more than one with that name, the first one
     * is returned.
     *
     * @deprecated
     * @param name
     *            name of the child element
     * @param namespaceURI
     *            namespace of the child element
     * @param node
     *            current element
     * @return the element or null, if it is missing
     * @TODO refactoring required
     */
    @Deprecated
    public static Element getChildElement( String name, URI namespaceURI, Node node ) {

        String namespace = namespaceURI == null ? null : namespaceURI.toString();

        NodeList nl = node.getChildNodes();
        Element element = null;
        Element childElement = null;

        if ( ( nl != null ) && ( nl.getLength() > 0 ) ) {
            for ( int i = 0; i < nl.getLength(); i++ ) {
                if ( nl.item( i ) instanceof Element ) {
                    element = (Element) nl.item( i );
                    String s = element.getNamespaceURI();
                    if ( ( s == null && namespace == null ) || ( namespace != null && namespace.equals( s ) ) ) {
                        if ( element.getLocalName().equals( name ) ) {
                            childElement = element;
                            break;
                        }
                    }
                }
            }
        }
        return childElement;
    }

    /**
     * Returns the specified child elements of the given element.
     *
     * @deprecated
     * @param name
     *            name of the child elements
     * @param namespaceURI
     *            namespaceURI of the child elements
     * @param node
     *            current element
     * @return list of matching child elements
     */
    @Deprecated
    public static ElementList getChildElements( String name, URI namespaceURI, Node node ) {

        String namespace = namespaceURI == null ? null : namespaceURI.toString();

        NodeList nl = node.getChildNodes();
        Element element = null;
        ElementList elementList = new ElementList();

        if ( ( nl != null ) && ( nl.getLength() > 0 ) ) {
            for ( int i = 0; i < nl.getLength(); i++ ) {
                if ( nl.item( i ) instanceof Element ) {
                    element = (Element) nl.item( i );

                    String s = element.getNamespaceURI();

                    if ( ( s == null && namespace == null ) || ( namespace != null && namespace.equals( s ) ) ) {
                        if ( element.getLocalName().equals( name ) ) {
                            elementList.addElement( element );
                        }
                    }
                }
            }
        }
        return elementList;
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
     * <li>expandind entity references = false
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
            LOG.logError( ex.getMessage(), ex );
        }
        return builder;
    }

    /**
     * Returns the specified attribute value of the given node.
     *
     * @param node
     *            current element
     * @param attrName
     *            local name of the attribute
     *
     * @return the value of the attribute or null, if it is missing
     * @deprecated use
     * @see #getAttrValue(Node, URI, String, String) instead
     */
    @Deprecated
    public static String getAttrValue( Node node, String attrName ) {
        NamedNodeMap atts = node.getAttributes();
        if ( atts == null ) {
            return null;
        }
        Attr a = (Attr) atts.getNamedItem( attrName );
        if ( a != null ) {
            return a.getValue();
        }
        return null;
    }

    /**
     * Returns the specified attribute value of the given node.
     *
     * @param node
     *            current element
     * @param namespaceURI
     *            namespace of the attribute
     * @param attrName
     *            local name of the attribute
     * @param defaultVal
     *            default value to be returned if attribute is nat available
     *
     * @return the value of the attribute or null, if it is missing
     */
    public static String getAttrValue( Node node, URI namespaceURI, String attrName, String defaultVal ) {
        if ( node == null ) {
            return null;
        }
        String namespace = namespaceURI == null ? null : namespaceURI.toString();
        NamedNodeMap atts = node.getAttributes();
        if ( atts == null ) {
            return defaultVal;
        }
        Attr a = null;
        if ( namespace == null ) {
            a = (Attr) atts.getNamedItem( attrName );
        } else {
            a = (Attr) atts.getNamedItemNS( namespace, attrName );
        }
        if ( a != null ) {
            return a.getValue();
        }
        return defaultVal;
    }

    /**
     * Parses an XML document and returns a DOM object. The underlying input stream is closed at the end.
     *
     * @param reader
     *            accessing the resource to parse
     * @return a DOM object, if en error occurs the response is <code>null</code>
     *
     * @throws IOException
     * @throws SAXException
     */
    public static Document parse( Reader reader )
                            throws IOException, SAXException {
        javax.xml.parsers.DocumentBuilder parser = null;
        Document doc = null;
        try {
            DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
            fac.setNamespaceAware( true );
            fac.setValidating( false );
            fac.setIgnoringElementContentWhitespace( false );
            fac.setValidating( false );
            try {
                fac.setAttribute( "http://apache.org/xml/features/nonvalidating/load-external-dtd", false );
            } catch ( IllegalArgumentException _ ) {
                // ignore, we just can't set the feature
            }
            parser = fac.newDocumentBuilder();
            doc = parser.parse( new InputSource( reader ) );
        } catch ( ParserConfigurationException ex ) {
            throw new IOException( "Unable to initialize DocumentBuilder: " + ex.getMessage() );
        } catch ( Exception e ) {
            throw new SAXException( e.getMessage() );
        } finally {
            reader.close();
        }
        return doc;
    }

    /**
     * Parses an XML document and returns a DOM object.
     *
     * @deprecated
     * @param is
     *            accessing the resource to parse
     * @return a DOM object
     * @throws IOException
     * @throws SAXException
     */
    @Deprecated
    public static Document parse( InputStream is )
                            throws IOException, SAXException {
        return parse( new InputStreamReader( is ) );

    }

    /**
     * Copies one node to another node.
     *
     * @param source
     * @param dest
     * @return the copied node
     */
    public static Node copyNode( Node source, Node dest ) {
        if ( source.getNodeType() == Node.TEXT_NODE ) {
            Text tn = dest.getOwnerDocument().createTextNode( getStringValue( source ) );
            return tn;
        }
        NamedNodeMap attr = source.getAttributes();
        if ( attr != null ) {
            for ( int i = 0; i < attr.getLength(); i++ ) {
                ( (Element) dest ).setAttribute( attr.item( i ).getNodeName(), attr.item( i ).getNodeValue() );
            }
        }
        NodeList list = source.getChildNodes();
        for ( int i = 0; i < list.getLength(); i++ ) {
            if ( !( list.item( i ) instanceof Text ) ) {
                if ( !( list.item( i ) instanceof Comment ) ) {
                    Element en = dest.getOwnerDocument().createElementNS( list.item( i ).getNamespaceURI(),
                                                                          list.item( i ).getNodeName() );
                    if ( list.item( i ).getNodeValue() != null ) {
                        en.setNodeValue( list.item( i ).getNodeValue() );
                    }
                    Node n = copyNode( list.item( i ), en );
                    dest.appendChild( n );
                }
            } else if ( ( list.item( i ) instanceof CDATASection ) ) {
                CDATASection cd = dest.getOwnerDocument().createCDATASection( list.item( i ).getNodeValue() );
                dest.appendChild( cd );
            } else {
                Text tn = dest.getOwnerDocument().createTextNode( list.item( i ).getNodeValue() );
                dest.appendChild( tn );
            }
        }
        return dest;
    }

    /**
     * Appends a node to an element.
     * <p>
     * The node can be from the same document or a different one (it is automatically imported, if necessary).
     *
     * @param source
     * @param dest
     * @return the element that is appended to
     */
    public static Node insertNodeInto( Node source, Node dest ) {
        Node n = dest.getOwnerDocument().importNode( source, true );
        dest.appendChild( n );
        return dest;
    }

    /**
     * Returns the first child element of the submitted node.
     *
     * @param node
     * @return the first child element of the submitted node.
     */
    public static Element getFirstChildElement( Node node ) {
        NodeList nl = node.getChildNodes();
        Element element = null;
        if ( ( nl != null ) && ( nl.getLength() > 0 ) ) {
            for ( int i = 0; i < nl.getLength(); i++ ) {
                if ( nl.item( i ) instanceof Element ) {
                    element = (Element) nl.item( i );
                    break;
                }
            }
        }
        return element;
    }

    /**
     * @deprecated Returns the first child element of the submitted node that matches the given local name.
     *
     * @param node
     * @param name
     * @return the child element
     */
    @Deprecated
    public static Element getChildElement( Node node, String name ) {
        NodeList nl = node.getChildNodes();
        Element element = null;
        Element childElement = null;
        if ( ( nl != null ) && ( nl.getLength() > 0 ) ) {
            for ( int i = 0; i < nl.getLength(); i++ ) {
                if ( nl.item( i ) instanceof Element ) {
                    element = (Element) nl.item( i );

                    if ( element.getNodeName().equals( name ) ) {
                        childElement = element;

                        break;
                    }
                }
            }
        }
        return childElement;
    }

    /**
     * Returns all child elements of the given node.
     *
     * @param node
     * @return all child elements of the given node.
     */
    public static ElementList getChildElements( Node node ) {
        NodeList children = node.getChildNodes();
        ElementList list = new ElementList();
        for ( int i = 0; i < children.getLength(); i++ ) {
            if ( children.item( i ).getNodeType() == Node.ELEMENT_NODE ) {
                list.elements.add( (Element) children.item( i ) );
            }
        }
        return list;
    }

    /**
     * sets the value of an existing node
     *
     * @param target
     * @param nodeValue
     */
    public static void setNodeValue( Element target, String nodeValue ) {
        NodeList nl = target.getChildNodes();
        for ( int i = 0; i < nl.getLength(); i++ ) {
            target.removeChild( nl.item( i ) );
        }
        Text text = target.getOwnerDocument().createTextNode( nodeValue );
        target.appendChild( text );
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
    public static Element appendElement( Element element, URI namespaceURI, String name, String nodeValue ) {
        String namespace = namespaceURI == null ? null : namespaceURI.toString();
        Element newElement = element.getOwnerDocument().createElementNS( namespace, name );
        if ( nodeValue != null && !nodeValue.equals( "" ) )
            newElement.appendChild( element.getOwnerDocument().createTextNode( nodeValue ) );
        element.appendChild( newElement );
        return newElement;
    }

    /**
     * @param xml
     * @return the formatted string, or the original XML string if something went wrong
     */
    public static String getAsPrettyString( String xml ) {
        try {
            Element e = getStringFragmentAsElement( xml );
            return new XMLFragment( e ).getAsPrettyString();
        } catch ( SAXException e ) {
            LOG.logError( "Unknown error", e );
        } catch ( IOException e ) {
            LOG.logError( "Unknown error", e );
        }
        return xml;
    }

    /**
     * Converts an XML fragment string into a DOM node. The fragment should NOT contain the XML prolog. Appends ALL
     * default namespace bindings for ease of use with broken fragments.
     *
     * @param fragment
     * @return a DOM element
     * @throws IOException
     * @throws SAXException
     */
    public static Element getStringFragmentAsElement( String fragment )
                            throws SAXException, IOException {
        StringBuffer xml = new StringBuffer( "<?xml version=\"1.0\"?>" );

        xml.append( "<bogus " );
        // append ALL namespaces
        Map<String, URI> map = CommonNamespaces.getNamespaceContext().getNamespaceMap();
        for ( String pre : map.keySet() ) {
            if ( pre.equals( "xmlns" ) ) {
                continue;
            }
            xml.append( "xmlns:" ).append( pre ).append( "='" );
            xml.append( map.get( pre ).toString() ).append( "' " );
        }
        xml.append( ">" );
        xml.append( fragment );
        xml.append( "</bogus>" );

        StringReader in = new StringReader( xml.toString() );

        XMLFragment doc = new XMLFragment( in, "http://www.systemid.org" );
        return (Element) doc.getRootElement().getFirstChild();
    }

    /**
     * Works like {@link #getStringFragmentAsElement}, but does not throw exceptions and immediately imports the node
     * into another document.
     *
     * @param fragment
     * @param doc
     * @return an element in the new document
     */
    public static Element importStringFragment( String fragment, Document doc ) {
        try {
            Element e = getStringFragmentAsElement( fragment );
            return (Element) doc.importNode( e, true );
        } catch ( SAXException e ) {
            LOG.logError( "Could not convert String to XML.", e );
        } catch ( IOException e ) {
            LOG.logError( "Could not convert String to XML.", e );
        }

        return null;
    }

    /**
     * This method escapes Strings for XML by creating a DOM document, setting the text in an attribute, exporting it to
     * text and extracting the escaped string. That means that it's slow, and the method of property using DOM to create
     * the XML is a million times better. But if you're forced to use StringBuffers, here you have a little helper.
     *
     * @param str
     * @return the escaped string
     */
    public static String escape( String str ) {
        XMLFragment doc = new XMLFragment( new QualifiedName( "dummy" ) );
        doc.getRootElement().setAttribute( "dummy", str );
        String s = doc.getAsString().substring( 52 );
        return s.substring( 0, s.length() - 3 );
    }
    
    /**
     * This method escapes Strings for XML by creating a DOM document, setting the text in an attribute, exporting it to
     * text and extracting the escaped string. That means that it's slow, and the method of property using DOM to create
     * the XML is a million times better. But if you're forced to use StringBuffers, here you have a little helper.
     *
     * @param str
     * @return the escaped string
     */
    public static String escape( Node root) {
        return new XMLFragment( (Element)root ).getAsString();
    }

}
