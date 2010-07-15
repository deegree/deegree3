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

package org.deegree.cs.projections.azimuthal;

import javax.vecmath.Point2d;

import org.deegree.cs.CRSCodeType;
import org.deegree.cs.components.Axis;
import org.deegree.cs.components.Ellipsoid;
import org.deegree.cs.components.GeodeticDatum;
import org.deegree.cs.components.Unit;
import org.deegree.cs.coordinatesystems.GeographicCRS;
import org.deegree.cs.exceptions.ProjectionException;
import org.deegree.cs.projections.ProjectionBase;
import org.deegree.cs.transformations.helmert.Helmert;
import org.junit.Test;

/**
 * <code>StereographicAlternativeTest</code> test the stereographic azimuthal projection as it was defined by snyder.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class StereographicTest extends ProjectionBase {

    private static final Ellipsoid ellipsoid_7004 = new Ellipsoid(
                                                                   6377397.155,
                                                                   Unit.METRE,
                                                                   299.1528128,
                                                                   new CRSCodeType[] { new CRSCodeType( "7004", "EPSG" ) } );

    // the source and target crs are not correct, but what the hack
    private static final Helmert wgs_56 = new Helmert( 565.04, 49.91, 465.84, -0.40941295127179994, 0.3608190255680464,
                                                       -1.8684910003505757, 4.0772, GeographicCRS.WGS84,
                                                       GeographicCRS.WGS84,
                                                       new CRSCodeType[] { new CRSCodeType( "TOWGS_56" ) } );

    private static final GeodeticDatum datum_171 = new GeodeticDatum(
                                                                      ellipsoid_7004,
                                                                      wgs_56,
                                                                      new CRSCodeType[] { new CRSCodeType( "DATUM_171" ) } );

    private static final GeographicCRS geographic_204 = new GeographicCRS(
                                                                           datum_171,
                                                                           new Axis[] {
                                                                                       new Axis( "longitude",
                                                                                                 Axis.AO_EAST ),
                                                                                       new Axis( "latitude",
                                                                                                 Axis.AO_NORTH ) },
                                                                           new CRSCodeType[] { new CRSCodeType(
                                                                                                                "GEO_CRS_204" ) } );

    private static final StereographicAzimuthal projection_28992 = new StereographicAzimuthal(
                                                                                               geographic_204,
                                                                                               463000.0,
                                                                                               155000.0,
                                                                                               new Point2d(
                                                                                                            Math.toRadians( 5.38763888888889 ),
                                                                                                            Math.toRadians( 52.15616055555555 ) ),
                                                                                               Unit.METRE, 0.9999079 );

    /**
     * reference point created with proj4 command : <code>
     * proj -f "%.8f" +proj=stere +ellps=bessel +lon_0=5.38763888888889 +lat_0=52.15616055555555 +k=0.9999079 +x_0=155000 +y_0=463000.0
     * 6.610765 53.235916
     * 236660.95112449 583829.78324175
     * </code>
     * 
     * @throws IllegalArgumentException
     * @throws ProjectionException
     */
    @Test
    public void testAccuracy()
                            throws IllegalArgumentException, ProjectionException {

        Point2d sourcePoint = new Point2d( Math.toRadians( 6.610765 ), Math.toRadians( 53.235916 ) );
        Point2d targetPoint = new Point2d( 236660.95112449, 583829.78324175 );

        doForwardAndInverse( projection_28992, sourcePoint, targetPoint );
    }

    /**
     * reference point created with proj4 command : <code>
     * #<32661> +proj=stere +lat_0=90 +lat_ts=90 +lon_0=0 +k=0.994 +x_0=2000000 +y_0=2000000 +ellps=WGS84 +datum=WGS84 +units=m +no_defs  <>
     * proj -f "%.8f" +init=epsg:32166
     * 6.610765 53.235916
     * 2486063.23      -2194020.02
     * </code>
     * 
     * @throws IllegalArgumentException
     * @throws ProjectionException
     */
    public void testAccuracyNorthPole()
                            throws IllegalArgumentException, ProjectionException {

        StereographicAzimuthal projection_32166 = new StereographicAzimuthal( GeographicCRS.WGS84, 2000000, 2000000,
                                                                              new Point2d( Math.toRadians( 0 ),
                                                                                           Math.toRadians( 90 ) ),
                                                                              Unit.METRE, 0.994 );
        Point2d sourcePoint = new Point2d( Math.toRadians( 6.610765 ), Math.toRadians( 53.235916 ) );
        Point2d targetPoint = new Point2d( 2486063.23, -2194020.02 );

        doForwardAndInverse( projection_32166, sourcePoint, targetPoint );
    }

    /**
     * reference point created with proj4 command : <code>
     * #<32761> +proj=stere +lat_0=-90 +lat_ts=-90 +lon_0=0 +k=0.994 +x_0=2000000 +y_0=2000000 +ellps=WGS84 +datum=WGS84 +units=m +no_defs  <>
     * proj -f "%.8f" +init=epsg:32761
     * 6.610765 53.235916
     * 6354589.64      39573786.54
     * </code>
     * 
     * @throws IllegalArgumentException
     * @throws ProjectionException
     */
    public void testAccuracySouthPole()
                            throws IllegalArgumentException, ProjectionException {

        StereographicAzimuthal projection_32761 = new StereographicAzimuthal( GeographicCRS.WGS84, 2000000, 2000000,
                                                                              new Point2d( Math.toRadians( 0 ),
                                                                                           Math.toRadians( -90 ) ),
                                                                              Unit.METRE, 0.994 );
        Point2d sourcePoint = new Point2d( Math.toRadians( 6.610765 ), Math.toRadians( 53.235916 ) );
        Point2d targetPoint = new Point2d( 6354589.64, 39573786.54 );

        doForwardAndInverse( projection_32761, sourcePoint, targetPoint );
    }

    /**
     * tests the consistency of the {@link StereographicAlternative} projection.
     */
    @Test
    public void testConsistency() {
        consistencyTest( projection_28992, 463000, 155000, new Point2d( Math.toRadians( 5.38763888888889 ),
                                                                        Math.toRadians( 52.15616055555555 ) ),
                         Unit.METRE, 0.9999079, true, false, "stereographicAzimuthal" );
    }

}
