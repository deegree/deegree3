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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import junit.framework.Assert;

import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.TiledRaster;
import org.deegree.coverage.raster.container.GriddedBlobTileContainer;
import org.deegree.coverage.raster.container.GriddedTileContainer;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.io.grid.GridMetaInfoFile;
import org.deegree.coverage.raster.utils.RasterFactory;
import org.deegree.geometry.Envelope;
import org.junit.Test;

/**
 * The <code>GriddedTileContainerTest</code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class GriddedTileContainerTest extends CenterOuterTest {

    private double resX;

    private double resY;

    /**
     * method which can be used to output the partial images of the blob_0.bin file.
     * 
     * @throws IOException
     */
    @SuppressWarnings("unused")
    private void readTest()
                            throws IOException {
        GriddedTileContainer cont = GriddedBlobTileContainer.create( new File( System.getProperty( "java.io.tmpdir" ) + File.separatorChar ),
                                                                     new RasterIOOptions( OriginLocation.CENTER ) );
        for ( int x = 0; x < cont.getColumns(); ++x ) {
            for ( int y = 0; y < cont.getRows(); ++y ) {
                AbstractRaster tile = cont.getTile( y, x );
                RasterFactory.saveRasterToFile( tile, new File( System.getProperty( "java.io.tmpdir" )
                                                                + File.separatorChar + y + "_" + x + ".png" ) );
            }
        }
    }

    private TiledRaster rasterCenter = null;

    private TiledRaster rasterOuter = null;

    private TiledRaster buildRaster( OriginLocation type )
                            throws IOException, NumberFormatException, URISyntaxException {
        URL infoUrl = GriddedTileContainerTest.class.getResource( "gridded_raster.info" );
        URL blob = GriddedTileContainerTest.class.getResource( "blob_0.bin" );

        GridMetaInfoFile worldFile = GridMetaInfoFile.readFromFile( new File( infoUrl.toURI() ),
                                                                    new RasterIOOptions( type ) );
        GriddedTileContainer gtc = new GriddedBlobTileContainer( new File( blob.toURI() ), worldFile );
        return new TiledRaster( gtc );
    }

    @Override
    protected void buildRasters()
                            throws IOException, NumberFormatException, URISyntaxException {
        rasterCenter = buildRaster( OriginLocation.CENTER );
        rasterOuter = buildRaster( OriginLocation.OUTER );
        resX = rasterCenter.getRasterReference().getResolutionX();
        resY = rasterCenter.getRasterReference().getResolutionY();
    }

    /**
     * For consistency with other center outer test, move to the resolution used.
     * 
     * @param oldPosition
     * @return
     */
    double getPositionX( double oldPosition ) {
        return 1000 + ( ( oldPosition - 1000 ) * resX );
    }

    /**
     * For consistency with other center outer test, move to the resolution used.
     * 
     * @param oldPosition
     * @return
     */
    double getPositionY( double oldPosition ) {
        // rb: '-' because the resY is negative
        return 2030 - ( ( oldPosition - 2030 ) * resY );
    }

    /**
     * Test the translation functions
     */
    @Test
    public void testTranslation() {
        double oldPos = 1000;
        double newPos = getPositionX( oldPos );
        Assert.assertEquals( 1000, newPos, 0.0001 );
        oldPos = 998;
        newPos = getPositionX( oldPos );
        Assert.assertEquals( 948.8, newPos, 0.0001 );

        oldPos = 2030;
        newPos = getPositionY( oldPos );
        Assert.assertEquals( 2030, newPos, 0.0001 );

        oldPos = 2028;
        newPos = getPositionY( oldPos );
        Assert.assertEquals( 1978.8, newPos, 0.0001 );

    }

    /**
     * get a piece of the upper left corner of the 00.png file.
     */
    @Test
    public void ul0() {
        Envelope request = geomFac.createEnvelope( getPositionX( 1000 ), getPositionY( 2028 ), getPositionX( 1002 ),
                                                   getPositionY( 2030 ), null );
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
        Envelope request = geomFac.createEnvelope( getPositionX( 998 ), getPositionY( 2028 ), getPositionX( 1002 ),
                                                   getPositionY( 2032 ), null );
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
        Envelope request = geomFac.createEnvelope( getPositionX( 1028.5 ), getPositionY( 1998 ),
                                                   getPositionX( 1031.5 ), getPositionY( 2002 ), null );
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
        Envelope request = geomFac.createEnvelope( getPositionX( 996 ), getPositionY( 2026 ), getPositionX( 998 ),
                                                   getPositionY( 2032 ), null );
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
        Envelope request = geomFac.createEnvelope( getPositionX( 1001.5 ), getPositionY( 2026.5 ),
                                                   getPositionX( 1005.5 ), getPositionY( 2029.5 ), null );
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
        Envelope request = geomFac.createEnvelope( getPositionX( 1008 ), getPositionY( 2026.7 ), getPositionX( 1011 ),
                                                   getPositionY( 2029 ), null );
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
        Envelope request = geomFac.createEnvelope( getPositionX( 1002.5 ), getPositionY( 2017 ), getPositionX( 1006 ),
                                                   getPositionY( 2023.5 ), null );
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
        Envelope request = geomFac.createEnvelope( getPositionX( 1008.5 ), getPositionY( 2009.5 ),
                                                   getPositionX( 1021 ), getPositionY( 2022 ), null );
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
     * Bounding box overlaps and exceeds all raster data
     */
    @Test
    public void allOverlap() {
        Envelope request = geomFac.createEnvelope( getPositionX( 998 ), getPositionY( 1998 ), getPositionX( 1031.5 ),
                                                   getPositionY( 2031.5 ), null );
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
