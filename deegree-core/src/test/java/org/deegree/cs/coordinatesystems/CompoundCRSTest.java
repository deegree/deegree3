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

package org.deegree.cs.coordinatesystems;

import javax.vecmath.Point2d;

import junit.framework.TestCase;

import org.deegree.cs.CRSCodeType;
import org.deegree.cs.CRSIdentifiable;
import org.deegree.cs.components.Axis;
import org.deegree.cs.components.Ellipsoid;
import org.deegree.cs.components.GeodeticDatum;
import org.deegree.cs.components.Unit;
import org.deegree.cs.projections.Projection;
import org.deegree.cs.projections.azimuthal.StereographicAlternative;
import org.deegree.cs.projections.cylindric.TransverseMercator;
import org.deegree.cs.transformations.helmert.Helmert;
import org.junit.Test;

/**
 * <code>CompoundCRSTest</code> test the instantiation of two compound crs's.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class CompoundCRSTest extends TestCase {
    /**
     * Test the construction of two compound crs's
     *
     * @throws IllegalArgumentException
     */
    @Test
    public void testInstantiation()
                            throws IllegalArgumentException {

        // Source crs espg:28992
        Ellipsoid sourceEllipsoid = new Ellipsoid( 6377397.155, Unit.METRE, 299.1528128, new CRSCodeType[] { new CRSCodeType( "7004", "EPSG" ) } );
        GeodeticDatum sourceDatum = new GeodeticDatum( sourceEllipsoid, null, new CRSCodeType[] { new CRSCodeType( "DATUM_171" ) } );
        GeographicCRS geoSource = new GeographicCRS( sourceDatum, new Axis[] { new Axis( "longitude", Axis.AO_EAST ),
                                                                              new Axis( "latitude", Axis.AO_NORTH ) },
                                                     new CRSCodeType[] { new CRSCodeType( "GEO_CRS_204" ) } );
        Helmert sourceWGS = new Helmert( 565.04, 49.91, 465.84, -0.40941295127179994, 0.3608190255680464,
                                         -1.8684910003505757, 4.0772, geoSource, GeographicCRS.WGS84,
                                         new CRSCodeType[] { new CRSCodeType( "TOWGS_56" ) } );
        sourceDatum.setToWGS84( sourceWGS );
        Projection sourceProj = new StereographicAlternative( geoSource, 463000.0, 155000.0,
                                                              new Point2d( Math.toRadians( 5.38763888888889 ),
                                                                           Math.toRadians( 52.15616055555555 ) ),
                                                              Unit.METRE, 0.9999079 );
        ProjectedCRS sourceCRS = new ProjectedCRS( sourceProj, new Axis[] { new Axis( "x", Axis.AO_EAST ),
                                                                           new Axis( "y", Axis.AO_NORTH ) },
                                                   new CRSCodeType[] { new CRSCodeType( "28992", "EPSG" ) } );

        CompoundCRS sourceCompound = new CompoundCRS( new Axis( "z", Axis.AO_UP ), sourceCRS, 100,
                                                      new CRSIdentifiable( new CRSCodeType[] { new CRSCodeType( "test_case" ) } ) );
        assertEquals( 100.0, sourceCompound.getDefaultHeight() );
        // standard projection values.
        assertEquals( Math.toRadians( 5.38763888888889 ),
                      ( (ProjectedCRS) sourceCompound.getUnderlyingCRS() ).getProjection().getProjectionLongitude() );
        assertEquals( Math.toRadians( 52.15616055555555 ),
                      ( (ProjectedCRS) sourceCompound.getUnderlyingCRS() ).getProjection().getProjectionLatitude() );

        // wgs84
        assertEquals( 565.04,
                      ( (ProjectedCRS) sourceCompound.getUnderlyingCRS() ).getGeodeticDatum().getWGS84Conversion().dx );
        assertEquals( 49.91,
                      ( (ProjectedCRS) sourceCompound.getUnderlyingCRS() ).getGeodeticDatum().getWGS84Conversion().dy );
        assertEquals( 465.84,
                      ( (ProjectedCRS) sourceCompound.getUnderlyingCRS() ).getGeodeticDatum().getWGS84Conversion().dz );
        assertEquals( -0.40941295127179994,
                      ( (ProjectedCRS) sourceCompound.getUnderlyingCRS() ).getGeodeticDatum().getWGS84Conversion().ex );
        assertEquals( 0.3608190255680464,
                      ( (ProjectedCRS) sourceCompound.getUnderlyingCRS() ).getGeodeticDatum().getWGS84Conversion().ey );
        assertEquals( -1.8684910003505757,
                      ( (ProjectedCRS) sourceCompound.getUnderlyingCRS() ).getGeodeticDatum().getWGS84Conversion().ez );
        assertEquals( 4.0772,
                      ( (ProjectedCRS) sourceCompound.getUnderlyingCRS() ).getGeodeticDatum().getWGS84Conversion().ppm );

        // the datum.equals method
        assertEquals( sourceDatum, sourceCompound.getGeodeticDatum() );

        // and the equals method.
        assertEquals( sourceCRS, sourceCompound.getUnderlyingCRS() );

        // Target crs espg:25832
        Ellipsoid targetEllipsoid = new Ellipsoid( 6378137.0, Unit.METRE, 298.257222101, new CRSCodeType[] { new CRSCodeType( "1188", "ESPG" ) } );
        Helmert targetWGS = new Helmert( GeographicCRS.WGS84, GeographicCRS.WGS84, new CRSCodeType[] { new CRSCodeType( "1188", "ESPG" ) } );
        GeodeticDatum targetDatum = new GeodeticDatum( targetEllipsoid, targetWGS, new CRSCodeType[] { new CRSCodeType( "6258", "ESPG" ) } );
        GeographicCRS targetGEO = new GeographicCRS( targetDatum, new Axis[] { new Axis( "longitude", Axis.AO_EAST ),
                                                                              new Axis( "latitude", Axis.AO_NORTH ) },
                                                     new CRSCodeType[] { new CRSCodeType( "4258", "ESPG" ) } );
        Projection targetProj = new TransverseMercator( true, targetGEO, 0, 500000.0, new Point2d( Math.toRadians( 9 ),
                                                                                                   0 ), Unit.METRE,
                                                        0.9996 );
        ProjectedCRS targetCRS = new ProjectedCRS( targetProj, new Axis[] { new Axis( "x", Axis.AO_EAST ),
                                                                           new Axis( "y", Axis.AO_NORTH ) },
                                                   new CRSCodeType[] { new CRSCodeType( "28992", "ESPG" ) } );
        CompoundCRS targetCompound = new CompoundCRS( new Axis( "z", Axis.AO_UP ), targetCRS, 2,
                                                      new CRSIdentifiable( new CRSCodeType[] { new CRSCodeType( "test_case_2" ) } ) );
        assertEquals( 2., targetCompound.getDefaultHeight() );
        assertEquals( Math.toRadians( 9 ),
                      ( (ProjectedCRS) targetCompound.getUnderlyingCRS() ).getProjection().getProjectionLongitude() );
        assertEquals( 0., ( (ProjectedCRS) targetCompound.getUnderlyingCRS() ).getProjection().getProjectionLatitude() );

        // wgs84
        assertEquals(
                      false,
                      ( (ProjectedCRS) targetCompound.getUnderlyingCRS() ).getGeodeticDatum().getWGS84Conversion().hasValues() );

        // the datum.equals method
        assertEquals( targetDatum, targetCompound.getGeodeticDatum() );

        // and the equals method.
        assertEquals( targetCRS, targetCompound.getUnderlyingCRS() );

    }
}
