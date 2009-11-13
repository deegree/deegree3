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

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import junit.framework.Assert;

import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.utils.RasterFactory;
import org.deegree.crs.CRS;
import org.deegree.crs.CRSCodeType;
import org.deegree.crs.coordinatesystems.CoordinateSystem;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.geometry.Envelope;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests the loading of GeoTIFF files. It doesn't test the various TIFF formats, but only the georeferencing
 * metadata. Be careful this test will only work with the IIORasterReader, the JAIRasterReader doesn't support meta-data
 * reading (yet?).
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class GeoTIFFTest {

    private static AbstractRaster raster;

    /**
     * load the GeoTIFF file to test
     * 
     * @throws IOException
     */
    @BeforeClass
    public static void init()
                            throws IOException {
        URL inputURL = GeoTIFFTest.class.getResource( "epsg4326.tiff" );
        File input = new File( inputURL.getFile() );
        RasterIOOptions options = RasterIOOptions.forFile( input );
        options.add( RasterIOOptions.GEO_ORIGIN_LOCATION, OriginLocation.OUTER.name() );
        raster = RasterFactory.loadRasterFromFile( input, options );
    }

    /**
     * test the coordinate system of a GeoTIFF
     * 
     * @throws UnknownCRSException
     */
    @Test
    public void geoTIFFCRS()
                            throws UnknownCRSException {
        Envelope env = raster.getEnvelope();
        Assert.assertNotNull( env );
        CRS crs = env.getCoordinateSystem();
        Assert.assertNotNull( crs );
        CoordinateSystem coordSys = crs.getWrappedCRS();
        assertNotNull( coordSys );
        CRSCodeType code = coordSys.getCode();
        assertNotNull( code );
        String c = code.getCode();
        assertNotNull( c );
        assertTrue( "4326".equals( c ) );
    }

    /**
     * test the envelope of a GeoTIFF
     * 
     */
    @Test
    public void geoTIFFEnvelope() {
        // TODO handle precision
        // double precision = raster.getRasterReference().getDelta();

        double[] renvMin = raster.getEnvelope().getMin().getAsArray();
        double[] renvMax = raster.getEnvelope().getMax().getAsArray();
        double delta = 1E-6;
        Assert.assertEquals( -113.69474315, renvMin[0], delta );
        Assert.assertEquals( 39.10223806, renvMin[1], delta );
        Assert.assertEquals( -110.35882409, renvMax[0], delta );
        Assert.assertEquals( 41.54129761, renvMax[1], delta );
        Assert.assertEquals( "WGS 84", raster.getEnvelope().getCoordinateSystem().getName() );
    }

    /**
     * test the raster size of a GeoTIFF
     */
    @Test
    public void geoTIFFSize() {
        assertEquals( 100, raster.getColumns() );
        assertEquals( 73, raster.getRows() );
    }

    /**
     * test the raster envelope of a GeoTIFF
     */
    @Test
    public void geoTIFFRasterEnvelope() {
        double delta = 0.0000000001;
        // double delta = 0.000001;
        RasterGeoReference renv = raster.getRasterReference();
        // actual values by gdalinfo
        double[] orig = renv.getOrigin();
        assertEquals( -113.6947431831, orig[0], delta );
        assertEquals( 41.5412977608, orig[1], delta );
        assertEquals( 0.0333591905, renv.getResolutionX(), delta );
        assertEquals( -0.0334117789, renv.getResolutionY(), delta );

    }
}
