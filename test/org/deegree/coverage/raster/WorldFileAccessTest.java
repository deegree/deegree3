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

import org.deegree.coverage.raster.geom.RasterReference;
import org.deegree.geometry.Envelope;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
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
     * Test method for
     * {@link org.deegree.dataaccess.raster.WorldFileAccess#readWorldFile(java.io.File, org.deegree.dataaccess.raster.WorldFileAccess.TYPE)}.
     */
    @Test
    public void testReadWorldFile() {
        try {
            RasterReference renv = WorldFileAccess.readWorldFile( wld, WorldFileAccess.TYPE.CENTER );

            assertEquals( 419996.000, renv.getX0( RasterReference.Type.OUTER ), delta );
            assertEquals( 4520003.000, renv.getY0( RasterReference.Type.OUTER ), delta );
            assertEquals( 420000.000, renv.getX0( RasterReference.Type.CENTER ), delta );
            assertEquals( 4519999.000, renv.getY0( RasterReference.Type.CENTER ), delta );

            assertEquals( 8.0, renv.getXRes(), delta );
            assertEquals( -8.0, renv.getYRes(), delta );

            Envelope env = renv.getEnvelope( 500, 500 );
            assertEquals( 423996.000, env.getMax().get0(), delta );
            assertEquals( 4520003.000, env.getMax().get1(), delta );
            assertEquals( 419996.000, env.getMin().get0(), delta );
            assertEquals( 4516003.000, env.getMin().get1(), delta );

            int[] size = renv.getSize( env );
            assertEquals( 500, size[0] );
            assertEquals( 500, size[1] );

        } catch ( Exception e ) {
            fail( "unexpected exception thrown: " + e.getMessage() );
        }

    }

}
