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
package org.deegree.coverage.raster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileWriter;

import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.io.WorldFileAccess;
import org.deegree.geometry.Envelope;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Reading of worldfiles.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$ }
 */
public class WorldFileAccessTest {

    private double delta = 0.0001;

    private static File wld;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUp()
                            throws Exception {
        wld = File.createTempFile( "deegree3-junit-test", ".wld" );
        wld.deleteOnExit();
        String wldString = "8.0\n0.0\n0.0\n-8.0\n420000.0\n4519999.0";
        FileWriter writer = new FileWriter( wld );
        writer.write( wldString );
        writer.close();
    }

    /**
     * Test method for {@link WorldFileAccess#readWorldFile(File, RasterIOOptions)} .
     */
    @Test
    public void testReadWorldFile() {
        try {
            RasterIOOptions options = new RasterIOOptions();
            options.add( RasterIOOptions.GEO_ORIGIN_LOCATION, OriginLocation.CENTER.name() );
            RasterGeoReference rRefCenter = WorldFileAccess.readWorldFile( wld, options );

            // center
            double[] origin = rRefCenter.getOrigin();
            assertEquals( 420000.000, origin[0], delta );
            assertEquals( 4519999.000, origin[1], delta );

            assertEquals( 8.0, rRefCenter.getResolutionX(), delta );
            assertEquals( -8.0, rRefCenter.getResolutionY(), delta );

            Envelope env = rRefCenter.getEnvelope( 500, 500, null );
            assertEquals( 420000.0, env.getMin().get0(), delta );
            assertEquals( 4515999.0, env.getMin().get1(), delta );
            assertEquals( 424000.000, env.getMax().get0(), delta );
            assertEquals( 4519999.000, env.getMax().get1(), delta );

            int[] size = rRefCenter.getSize( env );
            assertEquals( 501, size[0] );
            assertEquals( 501, size[1] );

            // outer
            options.add( RasterIOOptions.GEO_ORIGIN_LOCATION, OriginLocation.OUTER.name() );
            RasterGeoReference rRefOuter = WorldFileAccess.readWorldFile( wld, options );
            origin = rRefOuter.getOrigin();
            assertEquals( 420000.000, origin[0], delta );
            assertEquals( 4519999.000, origin[1], delta );

            // test the outer size
            size = rRefOuter.getSize( env );
            assertEquals( 500, size[0] );
            assertEquals( 500, size[1] );

        } catch ( Exception e ) {
            fail( "unexpected exception thrown: " + e.getMessage() );
        }

    }
}
