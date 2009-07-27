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

package org.deegree.crs.projections.cylindric;

import javax.vecmath.Point2d;

import org.deegree.crs.components.Unit;
import org.deegree.crs.exceptions.ProjectionException;
import org.deegree.crs.projections.ProjectionTest;
import org.deegree.crs.projections.conic.LambertConformalConic;
import org.deegree.crs.projections.cylindric.TransverseMercator;
import org.junit.Test;

/**
 * <code>StereographicAlternativeTest</code> tests the transverse mercator projection.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class TransverseMercatorTest extends ProjectionTest {

    private static final TransverseMercator projection_25832 = new TransverseMercator(
                                                                                       true,
                                                                                       geographic_4258,
                                                                                       0,
                                                                                       500000.0,
                                                                                       new Point2d(
                                                                                                    Math.toRadians( 9 ),
                                                                                                    0 ), Unit.METRE,
                                                                                       0.9996 );

    /**
     * reference point created with proj4 command : <code>
     * proj -f "%.5f" +proj=tmerc +ellps=GRS80 +lon_0=9 +lat_0=0 +k=0.9996 +x_0=500000
     * 6.610765 53.235916
     * 340545.99617007 5901178.79904923
     *  </code>
     *
     * @throws ProjectionException
     */
    @Test
    public void testAccuracy()
                            throws ProjectionException {

        Point2d sourcePoint = new Point2d( Math.toRadians( 6.610765 ), Math.toRadians( 53.235916 ) );
        Point2d targetPoint = new Point2d( 340545.99617007, 5901178.79904923 );

        doForwardAndInverse( projection_25832, sourcePoint, targetPoint );

    }

    /**
     * tests the consistency of the {@link LambertConformalConic} projection.
     */
    @Test
    public void testConsistency() {
        consistencyTest( projection_25832, 0, 500000, new Point2d( Math.toRadians( 9 ), 0 ), Unit.METRE, 0.9996, true,
                         false, "transverseMercator" );
        assertEquals( true, projection_25832.getHemisphere() );
        // should be 32 zone.
        assertEquals( 32, projection_25832.getZoneFromNearestMeridian( Math.toRadians( 9 ) ) );
    }

}
