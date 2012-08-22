//$HeadURL: svn+ssh://developername@svn.wald.intevation.org/deegree/base/trunk/test/junit/org/deegree/ogcbase/PropertyPathTest.java $
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
package org.deegree.ogcbase;

import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

public class PropertyPathTest extends TestCase {

    private static final String TEST_FILE = "PropertyPathTest.xml";

    private PropertyPath pPath = null;

    protected void setUp()
                            throws Exception {
        super.setUp();

    }

    protected void tearDown()
                            throws Exception {
        super.tearDown();
    }

    /*
     * Test method for 'org.deegree.ogcbase.PropertyPath.PropertyPath(PropertyPathStep[])'
     */
    public void testPropertyPathPropertyPathStepArray() {
    }

    /*
     * Test method for 'org.deegree.ogcbase.PropertyPath.PropertyPath(QualifiedName)'
     */
    public void testPropertyPathQualifiedName() {
        QualifiedName name = new QualifiedName( "lastName" );
        pPath = PropertyPathFactory.createPropertyPath( name );
        assertNotNull( pPath );
        assertEquals( 1, pPath.getSteps() );
    }

    /*
     * Test method for 'org.deegree.ogcbase.PropertyPath.PropertyPath(Text)'
     */
    public void testPropertyPathText()
                            throws IOException, SAXException, XMLParsingException {
        XMLFragment doc = new XMLFragment();
        doc.load( this.getClass().getResource( TEST_FILE ) );
        Element root = doc.getRootElement();
        List list = XMLTools.getNodes( root, "WFSSpecification/PropertyName/text()",
                                       CommonNamespaces.getNamespaceContext() );
        for ( int i = 0; i < list.size(); i++ ) {
            Text node = (Text) list.get( i );
            pPath = OGCDocument.parsePropertyPath( node );
            assertNotNull( pPath );
        }

        Text text = (Text) XMLTools.getRequiredNode( root,
                                                     "WFSSpecification/PropertyName[3]/text()",
                                                     CommonNamespaces.getNamespaceContext() );
        pPath = OGCDocument.parsePropertyPath( text );

        list = XMLTools.getNodes( root, "BrokenWFSSpecification/PropertyName/text()",
                                  CommonNamespaces.getNamespaceContext() );
        for ( int i = 0; i < list.size(); i++ ) {
            Text node = (Text) list.get( i );
            try {
                pPath = OGCDocument.parsePropertyPath( node );
                fail( "Could be parsed as PropertyPath, but shouldn't: " + pPath );
            } catch ( XMLParsingException e ) {
            }
        }
    }
}

/***************************************************************************************************
 * <code>
 Changes to this class. What the people have been up to:

 $Log$
 Revision 1.4  2007/02/12 10:12:42  wanhoff
 added footer, corrected header

 </code>
 **************************************************************************************************/
