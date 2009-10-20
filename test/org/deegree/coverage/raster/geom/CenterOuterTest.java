//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
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

package org.deegree.coverage.raster.geom;

import static org.deegree.coverage.raster.io.WorldFileAccess.readWorldFile;
import static org.deegree.coverage.raster.utils.RasterFactory.loadRasterFromStream;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.TiledRaster;
import org.deegree.coverage.raster.container.MemoryTileContainer;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.utils.RasterFactory;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.junit.Test;

/**
 * The <code>CenterOuterTest</code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class CenterOuterTest {

    private static final GeometryFactory geomFac = new GeometryFactory();

    private final Envelope rasterEnvelope = geomFac.createEnvelope( 1000, 2000, 1030, 2030, null );

    private TiledRaster buildRaster( RasterReference.Type type )
                            throws IOException {
        MemoryTileContainer mtc = new MemoryTileContainer();
        for ( int y = 0; y < 3; ++y ) {
            for ( int x = 0; x < 3; ++x ) {
                String f = y + "" + x;
                RasterReference ref = readWorldFile( CenterOuterTest.class.getResourceAsStream( f + ".wld" ), type );
                RasterIOOptions opts = new RasterIOOptions( ref, "png" );
                AbstractRaster raster = loadRasterFromStream( CenterOuterTest.class.getResourceAsStream( f + ".png" ),
                                                              opts );
                mtc.addTile( raster );
            }
        }
        return new TiledRaster( mtc );
    }

    @SuppressWarnings("null")
    @Test
    public void testCenter()
                            throws IOException {
        TiledRaster raster = null;
        try {
            raster = buildRaster( RasterReference.Type.CENTER );
        } catch ( IOException e ) {
            Assert.fail( e.getLocalizedMessage() );
        }
        Envelope request = geomFac.createEnvelope( 1000, 2028, 1002, 2030, null );
        TiledRaster subRaster = raster.getSubRaster( request );
        SimpleRaster simpleRaster = subRaster.getAsSimpleRaster();
        RasterFactory.saveRasterToFile( simpleRaster, new File( "/tmp/test_center.png" ) );
        Assert.assertEquals( 2, simpleRaster.getColumns() );
        Assert.assertEquals( 2, simpleRaster.getRows() );
    }

    @Test
    public void testOuter()
                            throws IOException {
        TiledRaster raster = null;
        try {
            raster = buildRaster( RasterReference.Type.OUTER );
        } catch ( IOException e ) {
            Assert.fail( e.getLocalizedMessage() );
        }
        Envelope request = geomFac.createEnvelope( 1000, 2018, 1002, 2020, null );
        TiledRaster subRaster = raster.getSubRaster( request );
        SimpleRaster simpleRaster = subRaster.getAsSimpleRaster();
        RasterFactory.saveRasterToFile( simpleRaster, new File( "/tmp/test_outer.png" ) );
        Assert.assertEquals( 2, simpleRaster.getColumns() );
        Assert.assertEquals( 2, simpleRaster.getRows() );
    }
}
