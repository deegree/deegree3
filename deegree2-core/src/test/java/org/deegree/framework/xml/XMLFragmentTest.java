// $HeadURL:
// /deegreerepository/deegree/test/org/deegree/framework/xml/XMLFragmentTest.java,v
// 1.2 2005/03/14 15:13:00 mschneider Exp $
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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;

/**
 *
 * @author last edited by: $Author: mschneider $
 *
 * @version 2.0, $Revision: 18195 $, $Date: 2009-06-18 17:55:39 +0200 (Do, 18 Jun 2009) $
 *
 * @since 2.0
 */
public class XMLFragmentTest extends TestCase {

    private static ILogger LOG = LoggerFactory.getLogger( XMLFragmentTest.class );

    private static final String SPECIAL_CHARS_FILE = "specialChars.xml";

    public static Test suite() {
        return new TestSuite( XMLFragmentTest.class );
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp()
                            throws Exception {
        super.setUp();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown()
                            throws Exception {
        super.tearDown();
    }

    // public void testCloning() throws Exception {
    // LOG.logInfo("\ntestCloning()...");
    // XMLDocument doc = new XMLDocument();
    // doc.load(XmlDocumentTest.class.getResourceAsStream (SPECIAL_CHARS_FILE));
    // Element root = doc.getDocumentElement();
    // Element german = (Element) XMLTools.getRequiredNode(root, "german",
    // XMLTools.getNamespaceNode(new String[0]));
    // Element umlauts = (Element) XMLTools.getRequiredNode(german, "umlauts",
    // XMLTools.getNamespaceNode(new String[0]));
    // XMLDocument clonedFragmentDoc = new XMLDocument(german);
    // umlauts.setAttribute("test", "yes");
    //
    // Element umlauts2 = (Element)
    // XMLTools.getRequiredNode(clonedFragmentDoc.getDocumentElement(),
    // "umlauts", XMLTools.getNamespaceNode(new String[0]));
    // assertNull (umlauts2.getAttributeNode("test"));
    //
    // clonedFragmentDoc = new XMLDocument(german);
    // umlauts2 = (Element)
    // XMLTools.getRequiredNode(clonedFragmentDoc.getDocumentElement(),
    // "umlauts", XMLTools.getNamespaceNode(new String[0]));
    // assertNotNull (umlauts.getAttributeNode("test"));
    // }

    public void testSpecialChars()
                            throws Exception {
        LOG.logInfo( "\ntestSpecialChars()..." );
        XMLFragment doc = new XMLFragment();
        doc.load( XMLFragmentTest.class.getResource( SPECIAL_CHARS_FILE ) );
        doc.prettyPrint( new PrintWriter( System.out ) );
    }
}
