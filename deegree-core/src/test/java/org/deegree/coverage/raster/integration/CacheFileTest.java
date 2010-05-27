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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URISyntaxException;

import junit.framework.Assert;

import org.deegree.commons.utils.FileUtils;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.cache.CacheRasterReader;
import org.deegree.coverage.raster.cache.RasterCache;
import org.deegree.coverage.raster.cache.TestRasterCache;
import org.deegree.coverage.raster.container.GriddedBlobTileContainer;
import org.deegree.coverage.raster.container.GriddedTileContainer;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.data.nio.ByteBufferRasterData;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.io.RasterReader;
import org.deegree.coverage.raster.utils.RasterFactory;
import org.deegree.geometry.Envelope;
import org.junit.Test;

/**
 * The <code>CacheFileTest</code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class CacheFileTest extends CenterOuterTest {

    private final static BigInteger[] FP_SOLEINSIDE_CENTER = new BigInteger[] { BigInteger.valueOf( 231416325000l ),
                                                                               BigInteger.valueOf( 231416325000l ),
                                                                               BigInteger.valueOf( 68871884355l ),
                                                                               BigInteger.valueOf( 68871884355l ) };

    private final static BigInteger[] FP_SOLEINSIDE_OUTER = new BigInteger[] { BigInteger.valueOf( 227591325000l ),
                                                                              BigInteger.valueOf( 227591325000l ),
                                                                              BigInteger.valueOf( 67542212355l ),
                                                                              BigInteger.valueOf( 67542212355l ) };

    private final static BigInteger[] FP_SOLEUL0OVERLAP_CENTER = new BigInteger[] {
                                                                                   BigInteger.valueOf( 1070548659180l ),
                                                                                   BigInteger.valueOf( 918334009683l ),
                                                                                   BigInteger.valueOf( 272684409120l ),
                                                                                   BigInteger.valueOf( 328021260930l ) };

    private final static BigInteger[] FP_SOLEUL0OVERLAP_OUTER = new BigInteger[] {
                                                                                  BigInteger.valueOf( 1058702005350l ),
                                                                                  BigInteger.valueOf( 916676514447l ),
                                                                                  BigInteger.valueOf( 266431637760l ),
                                                                                  BigInteger.valueOf( 322442090685l ) };

    /**
     * method which can be used to output the partial images of the blob_0.bin file.
     * 
     * @throws IOException
     */
    @SuppressWarnings("unused")
    private void readTest()
                            throws IOException {
        GriddedTileContainer cont = GriddedBlobTileContainer.create( new File( System.getProperty( "java.io.tmpdir" )
                                                                               + File.separatorChar ),
                                                                     new RasterIOOptions( OriginLocation.CENTER ) );
        for ( int x = 0; x < cont.getColumns(); ++x ) {
            for ( int y = 0; y < cont.getRows(); ++y ) {
                AbstractRaster tile = cont.getTile( y, x );
                RasterFactory.saveRasterToFile( tile, new File( System.getProperty( "java.io.tmpdir" )
                                                                + File.separatorChar + y + "_" + x + ".png" ) );
            }
        }
    }

    private SimpleRaster soleRasterCenter = null;

    private SimpleRaster soleRasterOuter = null;

    /**
     * @param outer
     * @return
     * @throws IOException
     */
    private SimpleRaster buildSoleRaster( OriginLocation type )
                            throws IOException {
        RasterIOOptions opts = new RasterIOOptions( type );
        String f = "overview";
        InputStream stream = TestRasterCache.class.getResourceAsStream( f + ".wld" );
        RasterGeoReference ref = readWorldFile( stream, opts );
        stream.close();
        opts.setRasterGeoReference( ref );
        opts.add( RasterIOOptions.OPT_FORMAT, "png" );
        opts.add( RasterIOOptions.ORIGIN_OF_RASTER, f + "_" + type );
        AbstractRaster raster = loadRasterFromStream( TestRasterCache.class.getResourceAsStream( f + ".png" ), opts );
        Assert.assertTrue( raster.isSimpleRaster() );
        SimpleRaster result = (SimpleRaster) raster;
        RasterData data = result.getReadOnlyRasterData();
        Assert.assertTrue( data instanceof ByteBufferRasterData );
        ByteBufferRasterData bbData = (ByteBufferRasterData) data;
        // really load all data into memory.
        bbData.getByteBuffer();

        RasterReader reader = bbData.getReader();
        Assert.assertTrue( reader instanceof CacheRasterReader );
        CacheRasterReader crReader = (CacheRasterReader) reader;

        crReader.flush();

        File cFile = crReader.file();
        Assert.assertNotNull( cFile );
        Assert.assertTrue( cFile.exists() );
        Assert.assertEquals( ( 334 * 334 * 3 * 9 ), cFile.length() );
        String infName = FileUtils.getFilename( cFile );
        File infoFile = new File( cFile.getParent(), infName + ".info" );
        Assert.assertTrue( infoFile.exists() );
        cFile.deleteOnExit();
        infoFile.deleteOnExit();
        SimpleRaster resultRaster = RasterCache.getInstance().createFromCache( null, infName );
        // RasterFactory.saveRasterToFile( resultRaster, new File( "/dev/shm/cFile_" + type + ".png" ) );
        Assert.assertEquals( resultRaster.getBands(), result.getBands() );
        // Assert.assertEquals( resultRaster.getColumns(), result.getColumns() );
        // Assert.assertEquals( resultRaster.getRows(), result.getRows() );
        Assert.assertEquals( resultRaster.getName(), result.getName() );
        // Assert.assertEquals( resultRaster.getEnvelope(), result.getEnvelope() );

        return resultRaster;
    }

    @Override
    protected void buildRasters()
                            throws IOException, NumberFormatException, URISyntaxException {
        soleRasterCenter = buildSoleRaster( OriginLocation.CENTER );
        soleRasterOuter = buildSoleRaster( OriginLocation.OUTER );
    }

    /**
     * inside a sole raster file/upper left raster cache tile.
     */
    @Test
    public void soleInside() {
        Envelope request = geomFac.createEnvelope( 1000, 2078, 1030, 2098, null );
        // rast_env( 0, 221 | 301, 20)
        // center, rb: visually verified 06.05.2010
        String name = "soleInside_center";
        SimpleRaster simpleRaster = soleRasterCenter.getSubRaster( request );
        Assert.assertEquals( 301, simpleRaster.getColumns() );
        Assert.assertEquals( 201, simpleRaster.getRows() );
        writeDebugFile( name, simpleRaster );
        testValues( FP_SOLEINSIDE_CENTER, simpleRaster );

        // outer, rb: visually verified 06.05.2010
        // rast_env( 0, 220 | 300, 20)
        name = "soleInside_outer";
        simpleRaster = soleRasterOuter.getSubRaster( request );
        Assert.assertEquals( 300, simpleRaster.getColumns() );
        Assert.assertEquals( 200, simpleRaster.getRows() );
        writeDebugFile( name, simpleRaster );
        testValues( FP_SOLEINSIDE_OUTER, simpleRaster );
    }

    /**
     * totally overlap the 00.png sole raster
     */
    @Test
    public void soleUL0Overlap() {
        Envelope request = geomFac.createEnvelope( 998.9, 2065.9, 1035.2, 2101.5, null );
        // center, rb: visually verified 06.05.2010
        String name = "soleUL0Overlap_center";
        SimpleRaster simpleRaster = soleRasterCenter.getSubRaster( request );
        double[] origin = simpleRaster.getRasterReference().getOrigin();
        Assert.assertEquals( 998.85, origin[0], 0.001 );
        Assert.assertEquals( 2101.55, origin[1], 0.001 );
        // the cache works with outer.
        Assert.assertEquals( OriginLocation.OUTER, simpleRaster.getRasterReference().getOriginLocation() );
        Assert.assertEquals( 364, simpleRaster.getColumns() );
        Assert.assertEquals( 357, simpleRaster.getRows() );
        writeDebugFile( name, simpleRaster );
        testValues( FP_SOLEUL0OVERLAP_CENTER, simpleRaster );

        // outer, rb: visually verified 06.05.2010
        name = "soleUL0Overlap_outer";
        simpleRaster = soleRasterOuter.getSubRaster( request );
        origin = simpleRaster.getRasterReference().getOrigin();
        Assert.assertEquals( 998.9, origin[0], 0.001 );
        Assert.assertEquals( 2101.5, origin[1], 0.001 );
        Assert.assertEquals( OriginLocation.OUTER, simpleRaster.getRasterReference().getOriginLocation() );
        Assert.assertEquals( 363, simpleRaster.getColumns() );
        Assert.assertEquals( 356, simpleRaster.getRows() );
        writeDebugFile( name, simpleRaster );
        testValues( FP_SOLEUL0OVERLAP_OUTER, simpleRaster );
    }

    // /**
    // * get the lowerright corner + no data values.
    // */
    // @Test
    // public void soleLROverlap() {
    // Envelope request = geomFac.createEnvelope( 1008.5, 2018, 1011.5, 2022, null );
    // // center, rb: visually verified 28.10.2009
    // String name = "soleLROverlap_center_";
    // SimpleRaster simpleRaster = soleRasterCenter.getSubRaster( request );
    // Assert.assertEquals( 3, simpleRaster.getColumns() );
    // Assert.assertEquals( 5, simpleRaster.getRows() );
    // writeDebugFile( name, simpleRaster );
    // testValues( SOLELROVERLAP_CENTER_RESULT, simpleRaster );
    //
    // // outer, rb: visually verified 28.10.2009
    // name = "soleLROverlap_outer_";
    // simpleRaster = soleRasterOuter.getSubRaster( request );
    // Assert.assertEquals( 4, simpleRaster.getColumns() );
    // Assert.assertEquals( 4, simpleRaster.getRows() );
    // writeDebugFile( name, simpleRaster );
    // testValues( SOLELROVERLAP_OUTER_RESULT, simpleRaster );
    // }
    //
    // /**
    // * Get only no data values.
    // */
    // @Test
    // public void sole0Outside() {
    // Envelope request = geomFac.createEnvelope( 996, 2026, 998, 2032, null );
    // // center, rb: visually verified 28.10.2009
    // String name = "sole0Outside_center_";
    // SimpleRaster simpleRaster = soleRasterCenter.getSubRaster( request );
    // Assert.assertEquals( 3, simpleRaster.getColumns() );
    // Assert.assertEquals( 7, simpleRaster.getRows() );
    // writeDebugFile( name, simpleRaster );
    // testValues( UL0OUTSIDE_CENTER_RESULT, simpleRaster );
    //
    // // outer, rb: visually verified 28.10.2009
    // name = "sole0Outside_outer_";
    // simpleRaster = soleRasterOuter.getSubRaster( request );
    // Assert.assertEquals( 2, simpleRaster.getColumns() );
    // Assert.assertEquals( 6, simpleRaster.getRows() );
    // writeDebugFile( name, simpleRaster );
    // testValues( UL0OUTSIDE_OUTER_RESULT, simpleRaster );
    // }
    //
    // /**
    // * get a total overlap of the sole raster
    // */
    // @Test
    // public void soleTotalOverlap() {
    // Envelope request = geomFac.createEnvelope( 998, 2018, 1011.5, 2031.5, null );
    // // center, rb: visually verified 28.10.2009
    // String name = "soleTotalOverlap_center_";
    // SimpleRaster simpleRaster = soleRasterCenter.getSubRaster( request );
    // Assert.assertEquals( 14, simpleRaster.getColumns() );
    // Assert.assertEquals( 14, simpleRaster.getRows() );
    // writeDebugFile( name, simpleRaster );
    // testValues( SOLETOTALOVERLAP_CENTER_RESULT, simpleRaster );
    //
    // // outer, rb: visually verified 28.10.2009
    // name = "soleTotalOverlap_outer_";
    // simpleRaster = soleRasterOuter.getSubRaster( request );
    // Assert.assertEquals( 14, simpleRaster.getColumns() );
    // Assert.assertEquals( 14, simpleRaster.getRows() );
    // writeDebugFile( name, simpleRaster );
    // testValues( SOLETOTALOVERLAP_OUTER_RESULT, simpleRaster );
    // }
}
