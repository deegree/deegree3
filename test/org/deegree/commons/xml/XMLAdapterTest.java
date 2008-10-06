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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import java.io.StringWriter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.jaxen.JaxenException;
import org.junit.Before;
import org.junit.Test;

/**
 * TODO add documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class XMLAdapterTest extends XMLAdapter {

    private static String WFS_NS = "http://www.opengis.net/wfs";

    private static String APP_NS = "http://www.deegree.org/app";

    private NamespaceContext nsContext;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp()
                            throws Exception {
        load( XMLAdapterTest.class.getResourceAsStream( "testdocument.xml" ) );
        nsContext = new NamespaceContext();
        nsContext.addNamespace( "ogc", "http://www.opengis.net/ogc" );
        nsContext.addNamespace( "gml", "http://www.opengis.net/gml" );
        nsContext.addNamespace( "wfs", "http://www.opengis.net/wfs" );
        nsContext.addNamespace( "app", "http://www.deegree.org/app" );
    }

    @Test
    public void testRootElement()
                            throws XMLStreamException {
        assertEquals( new QName( WFS_NS, "GetFeature" ), getRootElement().getQName() );
    }

    @Test
    public void testTypeNameAttribute()
                            throws JaxenException {

        OMElement root = getRootElement();
        AXIOMXPath xpath = new AXIOMXPath( "wfs:Query" );
        xpath.addNamespace( "wfs", WFS_NS );
        OMElement queryElement = (OMElement) xpath.selectSingleNode( root );
        OMAttribute typeNameAttr = queryElement.getAttribute( new QName( "typeName" ) );
        QName ftName = queryElement.resolveQName( typeNameAttr.getAttributeValue() );
        assertEquals( new QName( APP_NS, "Philosopher" ), ftName );
    }

    @Test
    public void testGetNode() {

        OMElement root = getRootElement();

        // select text node
        Object textNode = getNode( root, new XPath( "wfs:Query/ogc:Filter/ogc:BBOX/ogc:PropertyName/text()", nsContext ) );
        System.out.println( "textNode: " + textNode.getClass() );

        // select attribute node
        Object attributeNode = getNode( root, new XPath( "wfs:Query/@typeName", nsContext ) );
        System.out.println( "attributeNode: " + attributeNode.getClass() );
    }

    @Test
    public void testGetNodeAsString() {

        OMElement root = getRootElement();

        // select text node
        String textNode = getNodeAsString( root, new XPath( "wfs:Query/ogc:Filter/ogc:BBOX/ogc:PropertyName/text()",
                                                            nsContext ), null );
        System.out.println( "textNode: " + textNode );

        // select attribute node
        QName attributeNode = getNodeAsQName( root, new XPath( "wfs:Query/@typeName", nsContext ), null );
        System.out.println( "attributeNode: " + attributeNode );

        // select element node
        String elementNode = getNodeAsString( root,
                                              new XPath( "wfs:Query/ogc:Filter/ogc:BBOX/gml:Envelope/gml:coord/gml:X",
                                                         nsContext ), null );
        System.out.println( "elementNode: '" + elementNode + "'" );
    }

    @Test
    public void testGetRequiredNodeAsString() {

        OMElement root = getRootElement();
        String value = getRequiredNodeAsString( root,
                                                new XPath( "wfs:Query/ogc:Filter/ogc:BBOX/ogc:PropertyName/text()",
                                                           nsContext ) );
        assertEquals( "app:placeOfBirth/app:Place/app:country/app:Country/app:geom", value );

        try {
            value = getRequiredNodeAsString( root, new XPath( "wfs:Query/@doesNotExist", nsContext ) );
            fail();
        } catch ( XMLProcessingException e ) {
            // expected to be thrown (node does not exist)
        }
    }

    @Test
    public void testGetRequiredNodeAsQName() {

        OMElement root = getRootElement();
        QName value = getRequiredNodeAsQName( root, new XPath( "wfs:Query/@typeName", nsContext ) );
        assertEquals( new QName( APP_NS, "Philosopher" ), value );

        try {
            value = getRequiredNodeAsQName( root, new XPath( "wfs:Query/@doesNotExist", nsContext ) );
            fail();
        } catch ( XMLProcessingException e ) {
            // expected to be thrown (node does not exist)
        }
    }

    @Test
    public void testWriteElement()
                            throws XMLStreamException {

        System.out.println( getRootElement().getLocalName() );

        StringWriter stringWriter = new StringWriter();
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        factory.setProperty( XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE );
        XMLStreamWriter writer = new FormattingXMLStreamWriter( factory.createXMLStreamWriter( stringWriter ) );

        XMLStreamReader inputReader = getRootElement().getXMLStreamReaderWithoutCaching();
        inputReader.nextTag();

        getRootElement().serializeAndConsume( writer );

        System.out.println( "HUHU: '" + stringWriter.toString() + "'" );
    }
}
