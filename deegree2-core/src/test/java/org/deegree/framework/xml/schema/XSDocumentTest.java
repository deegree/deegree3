//$HeadURL: svn+ssh://developername@svn.wald.intevation.org/deegree/base/trunk/test/junit/org/deegree/framework/xml/schema/XSDocumentTest.java $
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

package org.deegree.framework.xml.schema;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import junit.framework.TestCase;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.XMLParsingException;

import alltests.Configuration;

public class XSDocumentTest extends TestCase {

    private static ILogger LOG = LoggerFactory.getLogger( XSDocumentTest.class );


    private static final String XSD_FILE = "xml/schema/Philosopher.xsd";

    private static URI NAMESPACE;
    static {
        try {
            NAMESPACE = new URI( "http://www.deegree.org/app" );
        } catch ( URISyntaxException e ) {
            LOG.logError( "Invalid namespace URI: " + e.getMessage(), e );
        }
    }

    private static final QualifiedName FT_PHILOSOPHER_NAME = new QualifiedName( "Philosopher", NAMESPACE );

    private static final QualifiedName FT_PHILOSOPHER_TYPE = new QualifiedName( "PhilosopherType", NAMESPACE );

    private static final QualifiedName FT_PLACE_TYPE = new QualifiedName( "PlaceType", NAMESPACE );

    private XSDocument doc;

    protected void setUp()
                            throws Exception {
        super.setUp();
        URL inputURL = new URL( Configuration.getResourceDir(), XSD_FILE );
        doc = new XSDocument();
        doc.load( inputURL );
    }

    public void testParseSchema()
                            throws XMLParsingException, XMLSchemaException {
        XMLSchema schema = doc.parseXMLSchema();

        ElementDeclaration[] elementDeclaration = schema.getElementDeclarations();
        assertNotNull( elementDeclaration );
        assertEquals( 4, elementDeclaration.length );

        ElementDeclaration singleElement = schema.getElementDeclaration( FT_PHILOSOPHER_NAME );
        assertNotNull( singleElement );
        assertEquals( "Philosopher", singleElement.getName().getLocalName() );
        assertEquals( "app:PhilosopherType", singleElement.getType().getName().getPrefixedName() );

        ComplexTypeDeclaration[] complexTypeDeclaration = schema.getComplexTypeDeclarations();
        assertNotNull( complexTypeDeclaration );
        assertEquals( 3, complexTypeDeclaration.length );

        ComplexTypeDeclaration singleComplexType = schema.getComplexTypeDeclaration( FT_PHILOSOPHER_TYPE );
        assertEquals( FT_PHILOSOPHER_TYPE, singleComplexType.getName() );

        singleComplexType = schema.getComplexTypeDeclaration( FT_PLACE_TYPE );
        assertNotNull( singleComplexType );
        assertEquals( FT_PLACE_TYPE, singleComplexType.getName() );

        ElementDeclaration[] subElements = singleComplexType.getExplicitElements();
        assertNotNull( subElements );
        assertEquals( 3, subElements.length );
        assertEquals( "id", subElements[0].getName().getLocalName() );
        assertEquals( "xsd:string", subElements[0].getType().getName().getPrefixedName() );

        SimpleTypeDeclaration[] simpleTypeDeclaration = schema.getSimpleTypeDeclarations();
        assertNotNull( simpleTypeDeclaration );
        assertEquals( 0, simpleTypeDeclaration.length );
    }
}
