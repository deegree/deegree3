//$HeadURL: svn+ssh://developername@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.model.spatialschema;


import junit.framework.TestCase;

import org.deegree.model.crs.UnknownCRSException;

/**
 * 
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author: admin $
 * 
 * @version $Revision: $, $Date: $
 */
public class SurfacePatchImplTestCase extends TestCase {
    
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

    /**
     * 
     * @throws UnknownCRSException
     * @throws GeometryException
     */
    public void testEqualsObject() throws UnknownCRSException, GeometryException {
        Position[] exterior = new Position[5];
        exterior[0] = GeometryFactory.createPosition(0, 0);
        exterior[1] = GeometryFactory.createPosition(10, 0);
        exterior[2] = GeometryFactory.createPosition(10, 10);
        exterior[3] = GeometryFactory.createPosition(0, 10);
        exterior[4] = GeometryFactory.createPosition(0, 0);
        SurfacePatchImpl surface = new SurfacePatchImpl(new SurfaceInterpolationImpl(), exterior, null, null) {
           
            /**
             * Comment for <code>serialVersionUID</code>
             */
            private static final long serialVersionUID = -3726145326117878873L;

            public boolean intersects(Geometry gmo) {
                return false;
            }
           
            public boolean contains(Geometry gmo) {
                return false;
            }
        };
        assertTrue(surface.equals(surface));
    }

} 