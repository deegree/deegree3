//$HeadURL: $
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.model.crs.projections.azimuthal;

import javax.vecmath.Point2d;

import org.deegree.model.crs.components.Unit;
import org.deegree.model.crs.exceptions.ProjectionException;
import org.deegree.model.crs.projections.ProjectionTest;
import org.junit.Test;

/**
 * <code>StereographicAlternativeTest</code> test the lambert azimuthal equal area projection.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class LambertAzimuthalTest extends ProjectionTest {

    private static final LambertAzimuthalEqualArea projection_3035 = new LambertAzimuthalEqualArea(
                                                                                                    geographic_4258,
                                                                                                    3210000.0,
                                                                                                    4321000.0,
                                                                                                    new Point2d(
                                                                                                                 Math.toRadians( 10 ),
                                                                                                                 Math.toRadians( 52 ) ),
                                                                                                    Unit.METRE );

    /**
     * reference point created with proj4 command : <code>
     * proj -f "%.8f" +proj=laea +ellps=GRS80 +lon_0=10 +lat_0=52 +k=1 +x_0=4321000 +y_0=3210000
     * 6.610765 53.235916
     * 4094775.23791324        3352810.22470640
     * </code>
     * 
     * @throws ProjectionException
     */
    @Test
    public void testAccuracy()
                            throws ProjectionException {

        Point2d sourcePoint = new Point2d( Math.toRadians( 6.610765 ), Math.toRadians( 53.235916 ) );
        Point2d targetPoint = new Point2d( 4094775.23791324, 3352810.22470640 );

        doForwardAndInverse( projection_3035, sourcePoint, targetPoint );
    }

    /**
     * tests the consistency of the {@link LambertAzimuthalEqualArea} projection.
     */
    @Test
    public void testConsistency() {
        consistencyTest( projection_3035, 3210000, 4321000, new Point2d( Math.toRadians( 10 ), Math.toRadians( 52 ) ),
                         Unit.METRE, 1, false, true, "lambertAzimuthalEqualArea" );
    }

}
