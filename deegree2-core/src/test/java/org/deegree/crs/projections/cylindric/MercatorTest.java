//$HeadURL: svn+ssh://developername@svn.wald.intevation.org/deegree/base/trunk/test/junit/org/deegree/crs/projections/cylindric/MercatorTest.java $
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

import org.deegree.crs.Identifiable;
import org.deegree.crs.components.Unit;
import org.deegree.crs.coordinatesystems.GeographicCRS;
import org.deegree.crs.exceptions.ProjectionException;
import org.deegree.crs.projections.ProjectionTest;

/**
 * <code>MercatorTest</code> tests the mercator projection.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author: rbezema $
 * 
 * @version $Revision: 18229 $, $Date: 2009-06-23 10:50:45 +0200 (Di, 23 Jun 2009) $
 * 
 */
public class MercatorTest extends ProjectionTest {

    private static final Point2d sourcePoint = new Point2d( Math.toRadians( 6.610765 ), Math.toRadians( 53.235916 ) );

    /**
     * reference point created with proj4 command : <code>
     * proj -f "%.5f" +proj=merc +ellps=WGS84 +k=0.99997
     * 6.610765 53.235916
     * 340545.99617007 5901178.79904923
     *  </code>
     * 
     * @throws ProjectionException
     */
    public void testAccuracyScale()
                            throws ProjectionException {
        Mercator projection_3395 = new Mercator( GeographicCRS.WGS84, 0, 0, new Point2d( 0, 0 ), Unit.METRE, 0.99997,
                                                 new Identifiable( "EPSG:3395" ) );

        // +k=0.99997
        Point2d targetPoint = new Point2d( 735884.91634419, 6992291.49820715 );

        // +lon_0=9 +lat_0=0 +k=1 +x_0=500
        // Point2d targetPoint = new Point2d( -265468.42359, 6992501.27325 );

        doForwardAndInverse( projection_3395, sourcePoint, targetPoint );

    }

    /**
     * reference point created with proj4 command : <code>
     * proj -f "%.5f" +proj=merc +ellps=WGS84 +lon_0=3 +lat_0=9 +k=0.99997 +x_0=300 +y_0=500
     * 6.610765 53.235916
     * 402236.46272    6992791.49821
     *  </code>
     * 
     * @throws ProjectionException
     */
    public void testAccuracyAll()
                            throws ProjectionException {
        Mercator projection_3395 = new Mercator( GeographicCRS.WGS84, 500, 300, new Point2d( Math.toRadians( 3 ),
                                                                                             Math.toRadians( 9 ) ),
                                                 Unit.METRE, 0.99997, new Identifiable( "EPSG:3395" ) );
        Point2d targetPoint = new Point2d( 402236.46272, 6992791.49821 );

        doForwardAndInverse( projection_3395, sourcePoint, targetPoint );
    }

    /**
     * reference point created with proj4 command : <code>
     * proj -f "%.5f" +proj=merc +ellps=WGS84 +lon_0=0 +lat_0=9 +k=1 +y_0=500
     * 6.610765 53.235916
     * 735906.99355    6993001.27325
     *  </code>
     * 
     * @throws ProjectionException
     */
    public void testAccuracyNorthingLatitudeNoScale()
                            throws ProjectionException {
        Mercator projection_3395 = new Mercator( GeographicCRS.WGS84, 500, 0, new Point2d( 0, Math.toRadians( 9 ) ),
                                                 Unit.METRE, 1, new Identifiable( "EPSG:3395" ) );
        Point2d targetPoint = new Point2d( 735906.99355, 6993001.27325 );

        doForwardAndInverse( projection_3395, sourcePoint, targetPoint );
    }

    /**
     * reference point created with proj4 command : <code>
     * proj -f "%.5f" +proj=merc +ellps=WGS84 +lon_0=0 +lat_0=9 +k=0.99997 +y_0=500
     * 6.610765 53.235916
     * 735884.91634    6992791.49821
     *
     *  </code>
     * 
     * @throws ProjectionException
     */
    public void testAccuracyNorthingLatitudeScale()
                            throws ProjectionException {
        Mercator projection_3395 = new Mercator( GeographicCRS.WGS84, 500, 0, new Point2d( 0, Math.toRadians( 9 ) ),
                                                 Unit.METRE, 0.99997, new Identifiable( "EPSG:3395" ) );
        Point2d targetPoint = new Point2d( 735884.91634, 6992791.49821 );

        doForwardAndInverse( projection_3395, sourcePoint, targetPoint );
    }

    /**
     * reference point created with proj4 command : <code>
     * proj -f "%.5f" +proj=merc +ellps=WGS84 +lon_0=9 +lat_0=0 +k=1 +x_0=500
     * 6.610765 53.235916
     * -265468.42359, 6992501.27325
     *  </code>
     * 
     * @throws ProjectionException
     */
    public void testAccuracyEastingLongitudeNoScale()
                            throws ProjectionException {
        Mercator projection_3395 = new Mercator( GeographicCRS.WGS84, 0, 500, new Point2d( Math.toRadians( 9 ), 0 ),
                                                 Unit.METRE, 1, new Identifiable( "EPSG:3395" ) );
        // +lon_0=9 +lat_0=0 +k=1 +x_0=500
        Point2d targetPoint = new Point2d( -265468.42359, 6992501.27325 );

        doForwardAndInverse( projection_3395, sourcePoint, targetPoint );
    }

    /**
     * reference point created with proj4 command : <code>
     * proj -f "%.5f" +proj=merc +ellps=WGS84 +lon_0=9 +lat_0=0 +k=0.99997 +x_0=500
     * 6.610765 53.235916
     * -265460.44453, 6992291.49821
     *  </code>
     * 
     * @throws ProjectionException
     */
    public void testAccuracyEastingLongitudeScale()
                            throws ProjectionException {
        Mercator projection_3395 = new Mercator( GeographicCRS.WGS84, 0, 500, new Point2d( Math.toRadians( 9 ), 0 ),
                                                 Unit.METRE, 0.99997, new Identifiable( "EPSG:3395" ) );
        // +k=0.99997
        // Point2d targetPoint = new Point2d( 735884.91634419, 6992291.49820715 );

        // +lon_0=9 +lat_0=0 +k=1 +x_0=500
        // Point2d targetPoint = new Point2d( -265468.42359, 6992501.27325 );

        Point2d targetPoint = new Point2d( -265460.44453, 6992291.49821 );

        doForwardAndInverse( projection_3395, sourcePoint, targetPoint );

    }

    /**
     * reference point created with proj4 command : <code>
     * proj -f "%.5f" +proj=merc +ellps=WGS84 +k=1
     * 6.610765 53.235916
     * 735906.99355400, 6992501.27324534
     *  </code>
     * 
     * @throws ProjectionException
     */
    public void testAccuracy()
                            throws ProjectionException {
        Mercator projection_3395 = new Mercator( GeographicCRS.WGS84, 0, 0, new Point2d( 0, 0 ), Unit.METRE, 1,
                                                 new Identifiable( "EPSG:3395" ) );

        Point2d targetPoint = new Point2d( 735906.99355400, 6992501.27324534 );

        doForwardAndInverse( projection_3395, sourcePoint, targetPoint );

    }
}
