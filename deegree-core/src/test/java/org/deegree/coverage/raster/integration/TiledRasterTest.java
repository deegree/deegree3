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

package org.deegree.coverage.raster.integration;

import static org.deegree.coverage.raster.io.WorldFileAccess.readWorldFile;
import static org.deegree.coverage.raster.utils.RasterFactory.loadRasterFromStream;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.Assert;

import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.TiledRaster;
import org.deegree.coverage.raster.container.MemoryTileContainer;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.geometry.Envelope;
import org.junit.Test;

/**
 * The <code>TiledRasterTest</code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class TiledRasterTest extends CenterOuterTest {

    private TiledRaster rasterCenter = null;

    private TiledRaster rasterOuter = null;

    private TiledRaster buildRaster( OriginLocation type )
                            throws IOException {
        MemoryTileContainer mtc = new MemoryTileContainer();
        for ( int y = 0; y < 3; ++y ) {
            for ( int x = 0; x < 3; ++x ) {
                String f = y + "" + x;
                RasterIOOptions opts = new RasterIOOptions( type );
                InputStream resourceAsStream = CenterOuterTest.class.getResourceAsStream( f + ".wld" );
                RasterGeoReference ref = readWorldFile( resourceAsStream, opts );
                resourceAsStream.close();
                opts.setRasterGeoReference( ref );
                opts.add( RasterIOOptions.OPT_FORMAT, "png" );
                AbstractRaster raster = loadRasterFromStream( CenterOuterTest.class.getResourceAsStream( f + ".png" ),
                                                              opts );
                mtc.addTile( raster );
            }
        }
        return new TiledRaster( mtc );
    }

    @Override
    protected void buildRasters()
                            throws IOException {
        rasterCenter = buildRaster( OriginLocation.CENTER );
        rasterOuter = buildRaster( OriginLocation.OUTER );
    }

    /**
     * get a piece of the upper left corner of the 00.png file.
     */
    @Test
    public void ul0() {
        Envelope request = geomFac.createEnvelope( 1000, 2028, 1002, 2030, null );
        // center, rb: visually verified 28.10.2009
        String name = "ul0_center_";
        TiledRaster subRaster = rasterCenter.getSubRaster( request );
        SimpleRaster simpleRaster = subRaster.getAsSimpleRaster();
        Assert.assertEquals( 3, simpleRaster.getColumns() );
        Assert.assertEquals( 3, simpleRaster.getRows() );
        writeDebugFile( name, simpleRaster );
        testValues( UL0_CENTER_RESULT, simpleRaster );

        // outer, rb: visually verified 28.10.2009
        name = "ul0_outer_";
        subRaster = rasterOuter.getSubRaster( request );
        simpleRaster = subRaster.getAsSimpleRaster();
        Assert.assertEquals( 2, simpleRaster.getColumns() );
        Assert.assertEquals( 2, simpleRaster.getRows() );
        writeDebugFile( name, simpleRaster );
        testValues( UL0_OUTER_RESULT, simpleRaster );
    }

    /**
     * Overlap and exceed the 00.png file.
     */
    @Test
    public void ul0Overlap() {
        Envelope request = geomFac.createEnvelope( 998, 2028, 1002, 2032, null );
        // center, rb: visually verified 28.10.2009
        String name = "ul0Overlap_center_";
        TiledRaster subRaster = rasterCenter.getSubRaster( request );
        SimpleRaster simpleRaster = subRaster.getAsSimpleRaster();
        Assert.assertEquals( 5, simpleRaster.getColumns() );
        Assert.assertEquals( 5, simpleRaster.getRows() );
        writeDebugFile( name, simpleRaster );
        testValues( UL0OVERLAP_CENTER_RESULT, simpleRaster );

        // outer, rb: visually verified 28.10.2009
        name = "ul0Overlap_outer_";
        subRaster = rasterOuter.getSubRaster( request );
        simpleRaster = subRaster.getAsSimpleRaster();
        Assert.assertEquals( 4, simpleRaster.getColumns() );
        Assert.assertEquals( 4, simpleRaster.getRows() );
        writeDebugFile( name, simpleRaster );
        testValues( UL0OVERLAP_OUTER_RESULT, simpleRaster );

    }

    /**
     * get lower right corner of the 22.png file.
     */
    @Test
    public void lr9Overlap() {
        Envelope request = geomFac.createEnvelope( 1028.5, 1998, 1031.5, 2002, null );
        // center, rb: visually verified 28.10.2009
        String name = "lr9Overlap_center_";
        TiledRaster subRaster = rasterCenter.getSubRaster( request );
        SimpleRaster simpleRaster = subRaster.getAsSimpleRaster();
        Assert.assertEquals( 3, simpleRaster.getColumns() );
        Assert.assertEquals( 5, simpleRaster.getRows() );
        writeDebugFile( name, simpleRaster );
        testValues( LR9OVERLAP_CENTER_RESULT, simpleRaster );

        // outer, rb: visually verified 28.10.2009
        name = "lr9Overlap_outer_";
        subRaster = rasterOuter.getSubRaster( request );
        simpleRaster = subRaster.getAsSimpleRaster();
        Assert.assertEquals( 4, simpleRaster.getColumns() );
        Assert.assertEquals( 4, simpleRaster.getRows() );
        writeDebugFile( name, simpleRaster );
        testValues( LR9OVERLAP_OUTER_RESULT, simpleRaster );
    }

    /**
     * test the tile raster totally outside (e.g. only no data values).
     */
    @Test
    public void ul0Outside() {
        Envelope request = geomFac.createEnvelope( 996, 2026, 998, 2032, null );
        // center, rb: visually verified 28.10.2009
        String name = "ul0Outside_center_";
        TiledRaster subRaster = rasterCenter.getSubRaster( request );
        SimpleRaster simpleRaster = subRaster.getAsSimpleRaster();
        Assert.assertEquals( 3, simpleRaster.getColumns() );
        Assert.assertEquals( 7, simpleRaster.getRows() );
        writeDebugFile( name, simpleRaster );
        testValues( UL0OUTSIDE_CENTER_RESULT, simpleRaster );

        // outer, rb: visually verified 28.10.2009
        name = "ul0Outside_outer_";
        subRaster = rasterOuter.getSubRaster( request );
        simpleRaster = subRaster.getAsSimpleRaster();
        Assert.assertEquals( 2, simpleRaster.getColumns() );
        Assert.assertEquals( 6, simpleRaster.getRows() );
        writeDebugFile( name, simpleRaster );
        testValues( UL0OUTSIDE_OUTER_RESULT, simpleRaster );
    }

    /**
     * totally inside the most upper left raster.
     */
    @Test
    public void ul0Inside() {
        Envelope request = geomFac.createEnvelope( 1001.5, 2026.5, 1005.5, 2029.5, null );
        // center, rb: visually verified 28.10.2009
        String name = "ul0Inside_center_";
        TiledRaster subRaster = rasterCenter.getSubRaster( request );
        SimpleRaster simpleRaster = subRaster.getAsSimpleRaster();
        Assert.assertEquals( 4, simpleRaster.getColumns() );
        Assert.assertEquals( 3, simpleRaster.getRows() );
        writeDebugFile( name, simpleRaster );
        testValues( UL0INSIDE_CENTER_RESULT, simpleRaster );

        // outer, rb: visually verified 28.10.2009
        name = "ul0Inside_outer_";
        subRaster = rasterOuter.getSubRaster( request );
        simpleRaster = subRaster.getAsSimpleRaster();
        Assert.assertEquals( 5, simpleRaster.getColumns() );
        Assert.assertEquals( 4, simpleRaster.getRows() );
        writeDebugFile( name, simpleRaster );
        testValues( UL0INSIDE_OUTER_RESULT, simpleRaster );
    }

    /**
     * Strife the 00.png and 01.png
     */
    @Test
    public void ul01Strife() {
        Envelope request = geomFac.createEnvelope( 1008, 2026.7, 1011, 2029, null );
        // center, rb: visually verified 28.10.2009
        String name = "ul01Strife_center_";
        TiledRaster subRaster = rasterCenter.getSubRaster( request );
        SimpleRaster simpleRaster = subRaster.getAsSimpleRaster();
        Assert.assertEquals( 4, simpleRaster.getColumns() );
        Assert.assertEquals( 3, simpleRaster.getRows() );
        writeDebugFile( name, simpleRaster );
        testValues( UL01STRIFE_CENTER_RESULT, simpleRaster );

        // outer, rb: visually verified 28.10.2009
        name = "ul01Strife_outer_";
        subRaster = rasterOuter.getSubRaster( request );
        simpleRaster = subRaster.getAsSimpleRaster();
        Assert.assertEquals( 3, simpleRaster.getColumns() );
        Assert.assertEquals( 3, simpleRaster.getRows() );
        writeDebugFile( name, simpleRaster );
        testValues( UL01STRIFE_OUTER_RESULT, simpleRaster );
    }

    /**
     * strife file 00.png and 10.png
     */
    @Test
    public void ul03Strife() {
        Envelope request = geomFac.createEnvelope( 1002.5, 2017, 1006, 2023.5, null );
        // center, rb: visually verified 28.10.2009
        String name = "ul03Strife_center_";
        TiledRaster subRaster = rasterCenter.getSubRaster( request );
        SimpleRaster simpleRaster = subRaster.getAsSimpleRaster();
        Assert.assertEquals( 4, simpleRaster.getColumns() );
        Assert.assertEquals( 7, simpleRaster.getRows() );
        writeDebugFile( name, simpleRaster );
        testValues( UL03STRIFE_CENTER_RESULT, simpleRaster );

        // outer, rb: visually verified 28.10.2009
        name = "ul03Strife_outer_";
        subRaster = rasterOuter.getSubRaster( request );
        simpleRaster = subRaster.getAsSimpleRaster();
        Assert.assertEquals( 4, simpleRaster.getColumns() );
        Assert.assertEquals( 7, simpleRaster.getRows() );
        writeDebugFile( name, simpleRaster );
        testValues( UL03STRIFE_OUTER_RESULT, simpleRaster );
    }

    /**
     * touch all files
     */
    @Test
    public void allStrife() {
        Envelope request = geomFac.createEnvelope( 1008.5, 2009.5, 1021, 2022, null );
        // center, rb: visually verified 28.10.2009
        String name = "allStrife_center_";
        TiledRaster subRaster = rasterCenter.getSubRaster( request );
        SimpleRaster simpleRaster = subRaster.getAsSimpleRaster();
        Assert.assertEquals( 13, simpleRaster.getColumns() );
        Assert.assertEquals( 13, simpleRaster.getRows() );
        writeDebugFile( name, simpleRaster );
        testValues( ALLSTRIFE_CENTER_RESULT, simpleRaster );

        // outer, rb: visually verified 28.10.2009
        name = "allStrife_outer_";
        subRaster = rasterOuter.getSubRaster( request );
        simpleRaster = subRaster.getAsSimpleRaster();
        Assert.assertEquals( 13, simpleRaster.getColumns() );
        Assert.assertEquals( 13, simpleRaster.getRows() );
        writeDebugFile( name, simpleRaster );
        testValues( ALLSTRIFE_OUTER_RESULT, simpleRaster );
    }

    /**
     * Bounding box overlaps and exeeds all raster data
     */
    @Test
    public void allOverlap() {
        Envelope request = geomFac.createEnvelope( 998, 1998, 1031.5, 2031.5, null );
        // center, rb: visually verified 28.10.2009
        String name = "allOverlap_center_";
        TiledRaster subRaster = rasterCenter.getSubRaster( request );
        SimpleRaster simpleRaster = subRaster.getAsSimpleRaster();
        Assert.assertEquals( 34, simpleRaster.getColumns() );
        Assert.assertEquals( 34, simpleRaster.getRows() );
        writeDebugFile( name, simpleRaster );
        testValues( ALLOVERLAP_CENTER_RESULT, simpleRaster );

        // outer, rb: visually verified 28.10.2009
        name = "allOverlap_outer_";
        subRaster = rasterOuter.getSubRaster( request );
        simpleRaster = subRaster.getAsSimpleRaster();
        Assert.assertEquals( 34, simpleRaster.getColumns() );
        Assert.assertEquals( 34, simpleRaster.getRows() );
        writeDebugFile( name, simpleRaster );
        testValues( ALLOVERLAP_OUTER_RESULT, simpleRaster );
    }
}
