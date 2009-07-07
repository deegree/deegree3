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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.deegree.coverage.raster.geom.RasterReference;
import org.deegree.coverage.raster.geom.RasterReference.Type;
import org.deegree.coverage.raster.io.RasterFactory;
import org.deegree.crs.CRS;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests the loading of GeoTIFF files. It doesn't test the various TIFF formats, but only the georeferencing
 * metadata.
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class GeoTIFFTest {

    private static AbstractRaster raster;

    private static GeometryFactory geomFactory = new GeometryFactory();

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
        raster = RasterFactory.loadRasterFromFile( input );
    }

    /**
     * test the coordinate system of a GeoTIFF
     * @throws UnknownCRSException
     */
    @Test
    public void geoTIFFCRS() throws UnknownCRSException {
        assertTrue( raster.getEnvelope().getCoordinateSystem().getWrappedCRS().getEPSGCode().getCodeNo() == 4326);
    }

    /**
     * test the envelope of a GeoTIFF
     *
     * @throws UnknownCRSException
     */
    @Test
    public void geoTIFFEnvelope()
                            throws UnknownCRSException {
        // TODO handle precision
//        double precision = raster.getRasterReference().getDelta();
        CRS crs = new CRS( "EPSG:4326" );

        Envelope env = geomFactory.createEnvelope( new double[] { -113.69474315, 39.10223806 },
                                                   new double[] { -110.35882409, 41.54129761 }, crs );
        assertTrue( env.equals( raster.getEnvelope() ) );
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
        RasterReference renv = raster.getRasterReference();
        Type outer = RasterReference.Type.OUTER;
        // actual values by gdalinfo
        assertEquals( -113.6947431831, renv.getX0( outer ), delta );
        assertEquals( 41.5412977608, renv.getY0( outer ), delta );
        assertEquals( 0.0333591905, renv.getXRes(), delta );
        assertEquals( -0.0334117789, renv.getYRes(), delta );

    }
}
