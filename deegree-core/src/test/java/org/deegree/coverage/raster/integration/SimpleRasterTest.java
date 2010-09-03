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
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.geometry.Envelope;
import org.junit.Test;

/**
 * The <code>SimpleRasterTest</code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class SimpleRasterTest extends CenterOuterTest {

    private SimpleRaster soleRasterCenter;

    private SimpleRaster soleRasterOuter;

    /**
     * @param outer
     * @return
     * @throws IOException
     */
    private SimpleRaster buildSoleRaster( OriginLocation type )
                            throws IOException {
        RasterIOOptions opts = new RasterIOOptions( type );
        String f = "00";
        InputStream stream = CenterOuterTest.class.getResourceAsStream( f + ".wld" );
        RasterGeoReference ref = readWorldFile( stream, opts );
        stream.close();
        opts.setRasterGeoReference( ref );
        opts.add( RasterIOOptions.OPT_FORMAT, "png" );
        AbstractRaster raster = loadRasterFromStream( CenterOuterTest.class.getResourceAsStream( f + ".png" ), opts );
        return (SimpleRaster) raster;
    }

    @Override
    protected void buildRasters()
                            throws IOException {

        soleRasterCenter = buildSoleRaster( OriginLocation.CENTER );
        soleRasterOuter = buildSoleRaster( OriginLocation.OUTER );

    }

    /**
     * inside a sole raster file.
     */
    @Test
    public void soleInside() {
        Envelope request = geomFac.createEnvelope( 1000, 2028, 1002, 2030, null );
        // center, rb: visually verified 28.10.2009
        String name = "soleInside_center_";
        SimpleRaster simpleRaster = soleRasterCenter.getSubRaster( request );
        Assert.assertEquals( 3, simpleRaster.getColumns() );
        Assert.assertEquals( 3, simpleRaster.getRows() );
        writeDebugFile( name, simpleRaster );
        testValues( UL0_CENTER_RESULT, simpleRaster );

        // outer, rb: visually verified 28.10.2009
        name = "soleInside_outer_";
        simpleRaster = soleRasterOuter.getSubRaster( request );
        Assert.assertEquals( 2, simpleRaster.getColumns() );
        Assert.assertEquals( 2, simpleRaster.getRows() );
        writeDebugFile( name, simpleRaster );
        testValues( UL0_OUTER_RESULT, simpleRaster );
    }

    /**
     * totally overlap the 00.png sole raster
     */
    @Test
    public void soleUL0Overlap() {
        Envelope request = geomFac.createEnvelope( 998, 2028, 1002, 2032, null );
        // center, rb: visually verified 28.10.2009
        String name = "soleUL0Overlap_center_";
        SimpleRaster simpleRaster = soleRasterCenter.getSubRaster( request );
        Assert.assertEquals( 5, simpleRaster.getColumns() );
        Assert.assertEquals( 5, simpleRaster.getRows() );
        writeDebugFile( name, simpleRaster );
        testValues( UL0OVERLAP_CENTER_RESULT, simpleRaster );

        // outer, rb: visually verified 28.10.2009
        name = "soleUL0Overlap_outer_";
        simpleRaster = soleRasterOuter.getSubRaster( request );
        Assert.assertEquals( 4, simpleRaster.getColumns() );
        Assert.assertEquals( 4, simpleRaster.getRows() );
        writeDebugFile( name, simpleRaster );
        testValues( UL0OVERLAP_OUTER_RESULT, simpleRaster );
    }

    /**
     * get the lowerleft corner + no data values.
     */
    @Test
    public void soleLROverlap() {
        Envelope request = geomFac.createEnvelope( 1008.5, 2018, 1011.5, 2022, null );
        // center, rb: visually verified 28.10.2009
        String name = "soleLROverlap_center_";
        SimpleRaster simpleRaster = soleRasterCenter.getSubRaster( request );
        Assert.assertEquals( 3, simpleRaster.getColumns() );
        Assert.assertEquals( 5, simpleRaster.getRows() );
        writeDebugFile( name, simpleRaster );
        testValues( SOLELROVERLAP_CENTER_RESULT, simpleRaster );

        // outer, rb: visually verified 28.10.2009
        name = "soleLROverlap_outer_";
        simpleRaster = soleRasterOuter.getSubRaster( request );
        Assert.assertEquals( 4, simpleRaster.getColumns() );
        Assert.assertEquals( 4, simpleRaster.getRows() );
        writeDebugFile( name, simpleRaster );
        testValues( SOLELROVERLAP_OUTER_RESULT, simpleRaster );
    }

    /**
     * Get only no data values.
     */
    @Test
    public void sole0Outside() {
        Envelope request = geomFac.createEnvelope( 996, 2026, 998, 2032, null );
        // center, rb: visually verified 28.10.2009
        String name = "sole0Outside_center_";
        SimpleRaster simpleRaster = soleRasterCenter.getSubRaster( request );
        Assert.assertEquals( 3, simpleRaster.getColumns() );
        Assert.assertEquals( 7, simpleRaster.getRows() );
        writeDebugFile( name, simpleRaster );
        testValues( UL0OUTSIDE_CENTER_RESULT, simpleRaster );

        // outer, rb: visually verified 28.10.2009
        name = "sole0Outside_outer_";
        simpleRaster = soleRasterOuter.getSubRaster( request );
        Assert.assertEquals( 2, simpleRaster.getColumns() );
        Assert.assertEquals( 6, simpleRaster.getRows() );
        writeDebugFile( name, simpleRaster );
        testValues( UL0OUTSIDE_OUTER_RESULT, simpleRaster );
    }

    /**
     * get a total overlap of the sole raster
     */
    @Test
    public void soleTotalOverlap() {
        Envelope request = geomFac.createEnvelope( 998, 2018, 1011.5, 2031.5, null );
        // center, rb: visually verified 28.10.2009
        String name = "soleTotalOverlap_center_";
        SimpleRaster simpleRaster = soleRasterCenter.getSubRaster( request );
        Assert.assertEquals( 14, simpleRaster.getColumns() );
        Assert.assertEquals( 14, simpleRaster.getRows() );
        writeDebugFile( name, simpleRaster );
        testValues( SOLETOTALOVERLAP_CENTER_RESULT, simpleRaster );

        // outer, rb: visually verified 28.10.2009
        name = "soleTotalOverlap_outer_";
        simpleRaster = soleRasterOuter.getSubRaster( request );
        Assert.assertEquals( 14, simpleRaster.getColumns() );
        Assert.assertEquals( 14, simpleRaster.getRows() );
        writeDebugFile( name, simpleRaster );
        testValues( SOLETOTALOVERLAP_OUTER_RESULT, simpleRaster );
    }

}
