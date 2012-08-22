//$HeadURL: svn+ssh://developername@svn.wald.intevation.org/deegree/base/trunk/test/junit/org/deegree/ogcwebservices/wass/common/URNTest.java $
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
package org.deegree.ogcwebservices.wass.common;

import junit.framework.TestCase;

/**
 * Test class for URN.
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 *
 * @version 2.0, $Revision: 18195 $, $Date: 2009-06-18 17:55:39 +0200 (Do, 18 Jun 2009) $
 *
 * @since 2.0
 */

public class URNTest extends TestCase {

    /**
     * Simple test cases.
     */
    public void testGetLastName() {
        assertNull( new URN( "urntest:nichts" ).getLastName() );
        assertNull( new URN( "urn" ).getLastName() );
        assertNotNull( new URN( "urn:" ).getLastName() );
        assertNotNull( new URN( "urn:eintest:einzweiter" ).getLastName() );
        assertNotNull( new URN( "urn:" ).getLastName() );
    }

    /**
     * Simple test cases.
     */
    public void testIsWellformedGDINRW() {
        assertTrue( new URN( "urn:x-gdi-nrw:authnMethod:1.0:password" ).isWellformedGDINRW() );
        assertTrue( new URN( "urn:x-gdi-nrw:authnMethod:1.0:was" ).isWellformedGDINRW() );
        assertTrue( new URN( "urn:x-gdi-nrw:authnMethod:1.0:session" ).isWellformedGDINRW() );
        assertTrue( new URN( "urn:x-gdi-nrw:authnMethod:1.0:anonymous" ).isWellformedGDINRW() );
        assertFalse( new URN( "urn:x-gdi-nrw:authnMethod:1.0:nichts" ).isWellformedGDINRW() );
        assertFalse( new URN( "urn:x-gdi-nrw:authnMethod:1.0:" ).isWellformedGDINRW() );
        assertFalse( new URN( "urn:x-gdi-nrw:authnMethod:1.0" ).isWellformedGDINRW() );
    }

    /**
     * Simple test cases.
     */
    public void testGetAuthenticationMethod() {
        assertTrue( new URN( "urn:x-gdi-nrw:authnMethod:1.0:password" ).getAuthenticationMethod().equalsIgnoreCase(
                                                                                                                    "password" ) );
        assertTrue( new URN( "urn:x-gdi-nrw:authnMethod:1.0:was" ).getAuthenticationMethod().equalsIgnoreCase(
                                                                                                               "was" ) );
        assertTrue( new URN( "urn:x-gdi-nrw:authnMethod:1.0:session" ).getAuthenticationMethod().equalsIgnoreCase(
                                                                                                                   "session" ) );
        assertTrue( new URN( "urn:x-gdi-nrw:authnMethod:1.0:anonymous" ).getAuthenticationMethod().equalsIgnoreCase(
                                                                                                                     "anonymous" ) );
        assertNull( new URN( "urn:x-gdi-nrw:authnMethod:1.0:nichts" ).getAuthenticationMethod() );
    }

}
