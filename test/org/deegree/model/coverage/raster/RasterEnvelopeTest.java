//$HeadURL:svn+ssh://otonnhofer@svn.wald.intevation.org/deegree/deegree3/model/trunk/src/org/deegree/model/coverage/raster/implementation/io/JAIRasterReader.java $
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2007 by:
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
package org.deegree.model.coverage.raster;

import static org.junit.Assert.*;

import org.deegree.model.coverage.raster.RasterEnvelope.Type;
import org.deegree.model.coverage.raster.data.RasterRect;
import org.deegree.model.geometry.GeometryFactory;
import org.deegree.model.geometry.GeometryFactoryCreator;
import org.deegree.model.geometry.primitive.Envelope;
import org.junit.Test;

/**
 * Test the RasterEnvelope implementation. Test calculations between raster(int) and world(double) coordinates,
 * calculation of raster sizes from envelopes and sub envelopes.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author: $
 * 
 * @version $Revision: $, $Date: $
 * 
 */
public class RasterEnvelopeTest {

    static final double DELTA = 0.001;

    static GeometryFactory geomFactory = GeometryFactoryCreator.getInstance().getGeometryFactory();

    /**
     * 
     */
    @Test
    public void testSimple() {
        RasterEnvelope r = new RasterEnvelope( Type.OUTER, 0.0, 0.0, 1.0, 1.0 );
        double[] result_d;
        result_d = r.convertToCRS( 10, 5 );
        assertEquals( 10.0, result_d[0], DELTA );
        assertEquals( 5.0, result_d[1], DELTA );

        int[] result_i;
        result_i = r.convertToRasterCRS( 1.8, 0.49 );
        assertEquals( 1, result_i[0] );
        assertEquals( 0, result_i[1] );

        r = new RasterEnvelope( 50.5, 30.5, 1.0, 1.0 );
        result_i = r.convertToRasterCRS( 50, 30 );
        assertEquals( 0, result_i[0] );
        assertEquals( 0, result_i[1] );

    }

    /**
     * 
     */
    @Test
    public void testOffCenter() {
        RasterEnvelope r = new RasterEnvelope( Type.OUTER, 10.0, -5.0, 1.0, 1.0 );
        double[] result_d;
        result_d = r.convertToCRS( 10, 5 );
        assertEquals( 20.0, result_d[0], DELTA );
        assertEquals( 0.0, result_d[1], DELTA );

        int[] result_i;
        result_i = r.convertToRasterCRS( 19.8, 10.3 );
        assertEquals( 9, result_i[0] );
        assertEquals( 15, result_i[1] );

    }

    /**
     * 
     */
    @Test
    public void testConvertToRasterCRS() {
        RasterEnvelope r = new RasterEnvelope( Type.OUTER, 0.0, 0.0, 1.0, 1.0 );
        int[] result_i;
        result_i = r.convertToRasterCRS( 0.0, 0.0 );
        assertEquals( 0, result_i[0] );
        assertEquals( 0, result_i[1] );

        result_i = r.convertToRasterCRS( 0.999999, 1.00001 );
        assertEquals( 0, result_i[0] );
        assertEquals( 1, result_i[1] );

        // result_i = r.convertToRasterCRS(-0.9999, -0.1);
        // assertEquals(-1, result_i[0]);
        // assertEquals(-1, result_i[1]);

    }

    /**
     * 
     */
    @Test
    public void testNegResolution() {
        RasterEnvelope r = new RasterEnvelope( 10.5, 25.5, 1.0, -1.0 );
        double[] result_d;
        result_d = r.convertToCRS( 10, 5 );
        assertEquals( 20.0, result_d[0], DELTA );
        assertEquals( 21.0, result_d[1], DELTA );

        int[] result_i;
        result_i = r.convertToRasterCRS( 10.001, 25.999 );
        assertEquals( 0, result_i[0] );
        assertEquals( 0, result_i[1] );

        result_i = r.convertToRasterCRS( 10.999, 25.001 );
        assertEquals( 0, result_i[0] );
        assertEquals( 0, result_i[1] );

        result_i = r.convertToRasterCRS( 19.8, 20.3 );
        assertEquals( 9, result_i[0] );
        assertEquals( 5, result_i[1] );

    }

    /**
     * 
     */
    @Test
    public void testEnvelope() {
        RasterEnvelope r = new RasterEnvelope( Type.CENTER, 1.0, 19.0, 2.0, -2.0 );
        Envelope envelope = geomFactory.createEnvelope( new double[] { 0.0, 10.00 }, new double[] { 20.00, 20.00 },
                                                        0.001, null );
        RasterRect rEnv = r.convertEnvelopeToRasterCRS( envelope );
        assertEquals( 0, rEnv.x );
        assertEquals( 0, rEnv.y );
        assertEquals( 10, rEnv.width );
        assertEquals( 5, rEnv.height );

        r = new RasterEnvelope( Type.CENTER, 11.0, 29.0, 2.0, -2.0 );
        envelope = geomFactory.createEnvelope( new double[] { 20.00, 10.00 }, new double[] { 30.00, 20.00 }, 0.001,
                                               null );
        rEnv = r.convertEnvelopeToRasterCRS( envelope );
        assertEquals( 5, rEnv.x );
        assertEquals( 5, rEnv.y );
        assertEquals( 5, rEnv.width );
        assertEquals( 5, rEnv.height );

        envelope = geomFactory.createEnvelope( new double[] { 15.00, 10.00 }, new double[] { 20.00, 15.00 }, 0.001,
                                               null );
        rEnv = r.convertEnvelopeToRasterCRS( envelope );
        assertEquals( 2, rEnv.x );
        assertEquals( 7, rEnv.y );
        assertEquals( 3, rEnv.width );
        assertEquals( 3, rEnv.height );
    }

    /**
     * 
     */
    @Test
    public void testCreateSubEnvelope() {
        RasterEnvelope r = new RasterEnvelope( Type.CENTER, 100.5, 109.5, 1.0, -1.0 );
        Envelope envelope = geomFactory.createEnvelope( new double[] { 102.0, 106.0 }, new double[] { 104.0, 108.0 },
                                                        0.001, null );

        assertTrue( r.getX0( Type.OUTER ) >= 100.0 );
        assertTrue( r.getY0( Type.OUTER ) <= 110.0 );

        RasterEnvelope subEnv = r.createSubEnvelope( envelope );
        assertEquals( 102.0, subEnv.getX0( Type.OUTER ), DELTA );
        assertEquals( 108.0, subEnv.getY0( Type.OUTER ), DELTA );
        assertEquals( 1.0, subEnv.getXRes() );
        assertEquals( -1.0, subEnv.getYRes() );

        r = new RasterEnvelope( Type.CENTER, 101.0, 109.0, 2.0, -2.0 );
        envelope = geomFactory.createEnvelope( new double[] { 115.0, 100.0 }, new double[] { 120.0, 110.0 }, 0.001,
                                               null );

        subEnv = r.createSubEnvelope( envelope );
        int[] size = subEnv.getSize( envelope );
        assertEquals( 3, size[0] );
        assertEquals( 5, size[1] );

        assertEquals( 114.0, subEnv.getX0( Type.OUTER ), DELTA );
        assertEquals( 110.0, subEnv.getY0( Type.OUTER ), DELTA );
        assertEquals( 115.0, subEnv.getX0( Type.CENTER ), DELTA );
        assertEquals( 109.0, subEnv.getY0( Type.CENTER ), DELTA );
        assertEquals( 2.0, subEnv.getXRes() );
        assertEquals( -2.0, subEnv.getYRes() );
        RasterRect rec = subEnv.convertEnvelopeToRasterCRS( envelope );
        assertEquals( 0, rec.x );
        assertEquals( 0, rec.y );
        assertEquals( 3, rec.width );
        assertEquals( 5, rec.height );

    }

    /**
     * 
     */
    @Test
    public void testCreateScaledEnvelope() {
        RasterEnvelope r = new RasterEnvelope( Type.OUTER, 10.0, 210.0, 1.0, -1.0 );
        Envelope envelope = geomFactory.createEnvelope( new double[] { 10.0, 10.0 }, new double[] { 110.0, 210.0 },
                                                        0.001, null );
        RasterEnvelope scaled = r.createResizedEnvelope( envelope, 50, 25 );
        assertEquals( 10.0, scaled.getX0( Type.OUTER ) );
        assertEquals( 210.0, scaled.getY0( Type.OUTER ) );
        assertEquals( 2.0, scaled.getXRes(), DELTA );
        assertEquals( -8.0, scaled.getYRes(), DELTA );
        envelope = scaled.getEnvelope( 50, 25 );
        assertEquals( 100.0, envelope.getWidth() );
        assertEquals( 200.0, envelope.getHeight() );
        int[] size = scaled.getSize( envelope );
        assertEquals( 50, size[0] );
        assertEquals( 25, size[1] );

    }

    /**
     * 
     */
    @Test
    public void testTiling() {
        Envelope envelope;
        RasterEnvelope subEnv;
        RasterRect rect;

        RasterEnvelope r = new RasterEnvelope( Type.OUTER, 10.0, 210.0, 1.0, -1.0 );
        envelope = geomFactory.createEnvelope( new double[] { 20.0, 160.01 }, new double[] { 110.0, 210.0 }, 0.001,
                                               null );
        subEnv = r.createSubEnvelope( envelope );
        assertEquals( 20.0, subEnv.getX0( Type.OUTER ) );
        assertEquals( 210.0, subEnv.getY0( Type.OUTER ) );
        rect = r.convertEnvelopeToRasterCRS( envelope );
        assertEquals( 10, rect.x );
        assertEquals( 0, rect.y );
        assertEquals( 90, rect.width );
        assertEquals( 50, rect.height );

        envelope = geomFactory.createEnvelope( new double[] { 10.0, 110.0 }, new double[] { 20.0, 160.01 }, 0.001, null );
        subEnv = r.createSubEnvelope( envelope );
        assertEquals( 10.0, subEnv.getX0( Type.OUTER ) );
        assertEquals( 161.0, subEnv.getY0( Type.OUTER ) );
        rect = r.convertEnvelopeToRasterCRS( envelope );
        assertEquals( 0, rect.x );
        assertEquals( 49, rect.y );
        assertEquals( 10, rect.width );
        assertEquals( 51, rect.height );

        envelope = geomFactory.createEnvelope( new double[] { 20.0, 110.0 }, new double[] { 110.0, 160.01 }, 0.001,
                                               null );
        subEnv = r.createSubEnvelope( envelope );
        assertEquals( 20.0, subEnv.getX0( Type.OUTER ) );
        assertEquals( 161.0, subEnv.getY0( Type.OUTER ) );
        rect = r.convertEnvelopeToRasterCRS( envelope );
        assertEquals( 10, rect.x );
        assertEquals( 49, rect.y );
        assertEquals( 90, rect.width );
        assertEquals( 51, rect.height );

        envelope = geomFactory.createEnvelope( new double[] { 10.0, 160.01 }, new double[] { 20.0, 210.0 }, 0.001, null );
        subEnv = r.createSubEnvelope( envelope );
        assertEquals( 10.0, subEnv.getX0( Type.OUTER ) );
        assertEquals( 210.0, subEnv.getY0( Type.OUTER ) );
        rect = r.convertEnvelopeToRasterCRS( envelope );
        assertEquals( 0, rect.x );
        assertEquals( 0, rect.y );
        assertEquals( 10, rect.width );
        assertEquals( 50, rect.height );
    }

    /**
     * 
     */
    @Test
    public void testGetSize() {
        RasterEnvelope r = new RasterEnvelope( 10.5, 209.5, 2.0, -1.0 );
        Envelope envelope = geomFactory.createEnvelope( new double[] { 10.0, 10.0 }, new double[] { 110.0, 210.0 },
                                                        0.001, null );
        int[] size = r.getSize( envelope );
        assertEquals( 50, size[0] );
        assertEquals( 200, size[1] );
    }

}
