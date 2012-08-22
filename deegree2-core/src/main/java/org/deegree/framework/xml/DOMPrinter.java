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

import java.io.PrintWriter;

import org.deegree.framework.util.StringTools;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <code>DOMPrinter</code> contains various methods to print DOM-XML-elements to a String,PrintWriter or Standard out.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class DOMPrinter {

    /**
     *
     * @param out
     * @param node
     */
    public static void printNode( PrintWriter out, Node node ) {
        switch ( node.getNodeType() ) {
        case Node.DOCUMENT_NODE: {
            out.print( "<?xml version=\"1.0\"?>" );
            Document doc = (Document) node;
            printNode( out, doc.getDocumentElement() );
            break;
        }
        case Node.ELEMENT_NODE: {
            String name = node.getNodeName();
            out.print( "<" + name );
            NamedNodeMap attributes = node.getAttributes();
            for ( int i = 0; i < attributes.getLength(); i++ ) {
                Node current = attributes.item( i );
                String value = current.getNodeValue();
                value = StringTools.replace( value, "&", "&amp;", true );
                out.print( " " + current.getNodeName() + "=\"" + value + "\"" );
            }
            out.print( ">" );

            // Kinder durchgehen
            NodeList children = node.getChildNodes();
            if ( children != null ) {
                for ( int i = 0; i < children.getLength(); i++ ) {
                    printNode( out, children.item( i ) );
                }
            }

            out.print( "</" + name + ">" );
            break;
        }
        case Node.TEXT_NODE:
        case Node.CDATA_SECTION_NODE: {
            String trimmed = node.getNodeValue().trim();
            if ( !trimmed.equals( "" ) )
                out.print( validateCDATA( trimmed ) );
            break;
        }
        case Node.PROCESSING_INSTRUCTION_NODE: {
            break;
        }
        case Node.ENTITY_REFERENCE_NODE: {
            break;
        }
        case Node.DOCUMENT_TYPE_NODE: {
            break;
        }
        }
    }

    /**
     *
     * @param node
     * @param indent
     */
    public static void printNode( Node node, String indent ) {
        if ( node == null ) {
            return;
        }

        switch ( node.getNodeType() ) {
        case Node.DOCUMENT_NODE: {
            System.out.println( "<?xml version=\"1.0\"?>" );
            Document doc = (Document) node;
            printNode( doc.getDocumentElement(), "" );
            break;
        }
        case Node.ELEMENT_NODE: {
            String name = node.getNodeName();
            System.out.print( indent + "<" + name );
            NamedNodeMap attributes = node.getAttributes();
            for ( int i = 0; i < attributes.getLength(); i++ ) {
                Node current = attributes.item( i );
                String value = current.getNodeValue();
                if ( value != null ) {
                    value = StringTools.replace( value, "&", "&amp;", true );
                    System.out.print( " " + current.getNodeName() + "=\"" + value + "\"" );
                }
            }
            // erwachsenen durchgehen
            NodeList children = node.getChildNodes();
            if ( children != null && children.getLength() != 0 ) {
                boolean complexContent = false;
                for ( int i = 0; i < children.getLength(); i++ ) {
                    if ( children.item( i ).getNodeType() != Node.TEXT_NODE
                         && children.item( i ).getNodeType() != Node.CDATA_SECTION_NODE ) {
                        complexContent = true;
                    }
                }
                if ( complexContent ) {
                    System.out.println( ">" );
                } else {
                    System.out.print( ">" );
                }

                for ( int i = 0; i < children.getLength(); i++ ) {
                    printNode( children.item( i ), indent + "  " );
                }

                if ( complexContent ) {
                    System.out.println( indent + "</" + name + ">" );
                } else {
                    System.out.println( "</" + name + ">" );
                }
            } else {
                System.out.println( "/>" );
            }
            break;
        }
        case Node.TEXT_NODE:
        case Node.CDATA_SECTION_NODE: {
            if ( node.getNodeValue() != null ) {
                String trimmed = node.getNodeValue().trim();
                if ( !trimmed.equals( "" ) )
                    System.out.print( trimmed );
            }
            break;
        }
        case Node.PROCESSING_INSTRUCTION_NODE: {
            break;
        }
        case Node.ENTITY_REFERENCE_NODE: {
            break;
        }
        case Node.DOCUMENT_TYPE_NODE: {
            break;
        }
        }
    }

    /**
     *
     * @param node
     * @param encoding
     * @return the String representation of the given node.
     */
    public static String nodeToString( Node node, String encoding ) {
        StringBuilder sb = new StringBuilder( 10000 );

        switch ( node.getNodeType() ) {
        case Node.DOCUMENT_NODE: {
            sb.append( "<?xml version=\"1.0\" encoding=\"" + encoding + "\" ?>" );
            Document doc = (Document) node;
            sb.append( nodeToString( doc.getDocumentElement(), "" ) );
            break;
        }
        case Node.ELEMENT_NODE: {
            String name = node.getNodeName();
            sb.append( "\n<" + name );
            NamedNodeMap attributes = node.getAttributes();
            for ( int i = 0; i < attributes.getLength(); i++ ) {
                Node current = attributes.item( i );
                String value = current.getNodeValue();
                if ( value != null ) {
                    value = StringTools.replace( value, "&", "&amp;", true );
                    sb.append( " " + current.getNodeName() + "=\"" + value + "\"" );
                }
            }
            sb.append( ">" );

            // Opas durchgehen
            NodeList children = node.getChildNodes();
            if ( children != null ) {
                for ( int i = 0; i < children.getLength(); i++ ) {
                    sb.append( nodeToString( children.item( i ), encoding ) );
                }
            }

            sb.append( "</" + name + ">" );
            break;
        }
        case Node.CDATA_SECTION_NODE: {
            String trimmed = node.getNodeValue().trim();
            if ( !trimmed.equals( "" ) )
                sb.append( "<![CDATA[" + trimmed + "]]>" );
            break;
        }
        case Node.TEXT_NODE: {
            String trimmed = node.getNodeValue();
            if ( trimmed != null ) {
                trimmed = trimmed.trim();
                if ( !trimmed.equals( "" ) ) {
                    sb.append( validateCDATA( trimmed ) );
                }
            }
            break;
        }
        case Node.PROCESSING_INSTRUCTION_NODE: {
            break;
        }
        case Node.ENTITY_REFERENCE_NODE: {
            break;
        }
        case Node.DOCUMENT_TYPE_NODE: {
            break;
        }
        }
        return sb.toString();
    }

    /**
     * Checks if a given CDATA-value has to be escaped if it is used as a text value in an XML element. If the submitted
     * string contains a character that have to be escaped or if the string is made of more than 1500 characters it is
     * encapsulated into a CDATA-section. Returns a version that is safe to be used.
     * <p>
     * The method is just proofed for the UTF-8 character encoding.
     *
     * @param cdata
     *            value to be used
     * @return the very same value (but escaped if necessary)
     * @todo refactoring required
     */
    public static StringBuffer validateCDATA( String cdata ) {
        StringBuffer sb = null;
        if ( cdata != null
             && ( cdata.length() > 1000 || cdata.indexOf( '<' ) >= 0 || cdata.indexOf( '>' ) >= 0
                  || cdata.indexOf( '&' ) >= 0 || cdata.indexOf( '"' ) >= 0 || cdata.indexOf( "'" ) >= 0 ) ) {
            sb = new StringBuffer( cdata.length() + 15 );
            sb.append( "<![CDATA[" ).append( cdata ).append( "]]>" );
        } else {
            if ( cdata != null ) {
                sb = new StringBuffer( cdata );
            }
        }
        return sb;
    }
}
