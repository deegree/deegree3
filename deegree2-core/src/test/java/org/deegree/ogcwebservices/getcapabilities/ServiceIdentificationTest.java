//$HeadURL: svn+ssh://developername@svn.wald.intevation.org/deegree/base/trunk/test/junit/org/deegree/ogcwebservices/getcapabilities/ServiceIdentificationTest.java $
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
package org.deegree.ogcwebservices.getcapabilities;

import junit.framework.TestCase;

import org.deegree.datatypes.Code;
import org.deegree.model.metadata.iso19115.Keywords;

/**
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class ServiceIdentificationTest extends TestCase {

    public static void main(String[] args) {
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetServiceType() {
    }

    public static ServiceIdentification getTestInstance(String owsName,
            String owsVersion) {
        return new ServiceIdentification(new Code(owsName),
                new String[] { owsVersion },
                "deegree 2.0 " + owsName + " test", "abstract",
                new Keywords[] { new Keywords(new String[] { "CITE-2",
                        "JUnitTest " + System.currentTimeMillis() }) },
                "NO FEES - IT'S FREE", new String[] { "NONE" });
    }

}
