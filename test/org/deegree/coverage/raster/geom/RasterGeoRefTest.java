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

import static junit.framework.Assert.assertEquals;
import static org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation.CENTER;
import static org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation.OUTER;
import junit.framework.Assert;

import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.crs.CRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.junit.Test;

/**
 * The <code>RasterGeoRefTest</code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class RasterGeoRefTest {

    private static final GeometryFactory geomFactor = new GeometryFactory();

    private final static double ORIG_X = 1000;

    private final static double ORIG_Y = 1000;

    private final static double RES_X = 10;

    private final static double RES_Y = -10;

    private static CRS defaultCRS = new CRS( "EPSG:31466" );

    private final static RasterGeoReference REF_CENTER = new RasterGeoReference(
                                                                                 RasterGeoReference.OriginLocation.CENTER,
                                                                                 RES_X, RES_Y, ORIG_X, ORIG_Y,
                                                                                 defaultCRS );

    private final static RasterGeoReference REF_OUTER = new RasterGeoReference(
                                                                                RasterGeoReference.OriginLocation.OUTER,
                                                                                RES_X, RES_Y, ORIG_X, ORIG_Y,
                                                                                defaultCRS );

    /**
     * Test the newly located origin.
     */
    @Test
    public void relocatedOrigin() {
        double[] origin = REF_CENTER.getOrigin( OUTER );
        Assert.assertEquals( 995., origin[0] );
        Assert.assertEquals( 1005., origin[1] );

        origin = REF_OUTER.getOrigin( CENTER );
        Assert.assertEquals( 1005., origin[0] );
        Assert.assertEquals( 995., origin[1] );
    }

    /**
     * Test {@link RasterGeoReference#getRasterCoordinate(double, double)} for a center reference
     */
    @Test
    public void getRasterCoordsCenter() {

        /**
         * Test the lower right domain of the origin.
         */
        // 991-> CENTER=[1,2[, OUTER=[0,1[
        int[] rasterCoords = REF_CENTER.getRasterCoordinate( 1031, 991 );
        Assert.assertEquals( 3, rasterCoords[0] );
        Assert.assertEquals( 1, rasterCoords[1] );

        // 981-> CENTER=[2,3[, OUTER=[1,2[
        rasterCoords = REF_CENTER.getRasterCoordinate( 1031, 981 );
        Assert.assertEquals( 3, rasterCoords[0] );
        Assert.assertEquals( 2, rasterCoords[1] );

        // 1025-> CENTER=[3,4[; OUTER=[2,3[
        // 981 -> CENTER=[2,3[, OUTER=[1,2[
        rasterCoords = REF_CENTER.getRasterCoordinate( 1025, 981 );
        Assert.assertEquals( 3, rasterCoords[0] );
        Assert.assertEquals( 2, rasterCoords[1] );

        /**
         * Test the lower left domain of the origin.
         */
        // 994 -> CENTER=[-1,0[; OUTER=[-1,0[
        // 995 -> CENTER=]0,1]; OUTER=[0,1[
        rasterCoords = REF_CENTER.getRasterCoordinate( 994, 995 );
        Assert.assertEquals( -1, rasterCoords[0] );
        Assert.assertEquals( 1, rasterCoords[1] );

        /**
         * Test the upper left domain of the origin.
         */
        // 994 -> CENTER=[-1,0[; OUTER=[
        // 1014 -> CENTER=]-1,0]; OUTER=[
        rasterCoords = REF_CENTER.getRasterCoordinate( 994, 1014 );
        Assert.assertEquals( -1, rasterCoords[0] );
        Assert.assertEquals( -1, rasterCoords[1] );

        /**
         * Test the upper right domain of the origin.
         */
        // 1014 -> CENTER=[1,2[; OUTER=[1,2[
        // 1014 -> CENTER=]-1,0]; OUTER=[-2,-1[
        rasterCoords = REF_CENTER.getRasterCoordinate( 1014, 1014 );
        Assert.assertEquals( 1, rasterCoords[0] );
        Assert.assertEquals( -1, rasterCoords[1] );

    }

    /**
     * Test {@link RasterGeoReference#getRasterCoordinate(double, double)} for an outer reference
     */
    @Test
    public void getRasterCoordsOuter() {

        /**
         * Test the lower right domain of the origin.
         */
        // 991-> CENTER=[1,2[, OUTER=[0,1[
        int[] rasterCoords = REF_OUTER.getRasterCoordinate( 1031, 991 );
        Assert.assertEquals( 3, rasterCoords[0] );
        Assert.assertEquals( 0, rasterCoords[1] );

        // 981-> CENTER=[2,3[, OUTER=[1,2[
        rasterCoords = REF_OUTER.getRasterCoordinate( 1031, 981 );
        Assert.assertEquals( 3, rasterCoords[0] );
        Assert.assertEquals( 1, rasterCoords[1] );

        // 1025-> CENTER=[3,4[; OUTER=[2,3[,
        // 981 -> CENTER=[2,3[, OUTER=[1,2[
        rasterCoords = REF_OUTER.getRasterCoordinate( 1025, 981 );
        Assert.assertEquals( 2, rasterCoords[0] );
        Assert.assertEquals( 1, rasterCoords[1] );

        /**
         * Test the left domain of the origin.
         */
        // 994 -> CENTER=[-1,0[; OUTER=[-1,0[
        // 995 -> CENTER=]0,1]; OUTER=[0,1[
        rasterCoords = REF_OUTER.getRasterCoordinate( 994, 995 );
        Assert.assertEquals( -1, rasterCoords[0] );
        Assert.assertEquals( 0, rasterCoords[1] );

        /**
         * Test the upper left domain of the origin.
         */
        // 994 -> CENTER=[-1,0[; OUTER=[-1,0[
        // 1014 -> CENTER=]-1,0]; OUTER=[-2,-1[
        rasterCoords = REF_OUTER.getRasterCoordinate( 994, 1014 );
        Assert.assertEquals( -1, rasterCoords[0] );
        Assert.assertEquals( -2, rasterCoords[1] );

        /**
         * Test the upper right domain of the origin.
         */
        // 1014 -> CENTER=[1,2[; OUTER=[1,2[
        // 1014 -> CENTER=]-1,0]; OUTER=[-2,-1[
        rasterCoords = REF_OUTER.getRasterCoordinate( 1014, 1014 );
        Assert.assertEquals( 1, rasterCoords[0] );
        Assert.assertEquals( -2, rasterCoords[1] );
    }

    /**
     * Test {@link RasterGeoReference#getRasterCoordinateUnrounded(double, double)} for a center reference
     */
    @Test
    public void getUnroundedRasterCoordCenter() {
        /**
         * Test the lower right domain of the origin.
         */
        // 991-> CENTER=[1,2[, OUTER=[0,1[
        double[] rasterCoords = REF_CENTER.getRasterCoordinateUnrounded( 1031, 991 );
        Assert.assertEquals( 3.6, rasterCoords[0], 0.00001 );
        Assert.assertEquals( 1.4, rasterCoords[1], 0.00001 );

        // 981-> CENTER=[2,3[, OUTER=[1,2[
        rasterCoords = REF_CENTER.getRasterCoordinateUnrounded( 1031, 981 );
        Assert.assertEquals( 3.6, rasterCoords[0], 0.00001 );
        Assert.assertEquals( 2.4, rasterCoords[1], 0.00001 );

        /**
         * Test the left domain of the origin.
         */
        // 994 -> CENTER=[-1,0[; OUTER=[-1,0[
        // 995 -> CENTER=]0,1]; OUTER=[0,1[
        rasterCoords = REF_CENTER.getRasterCoordinateUnrounded( 994, 995 );
        Assert.assertEquals( -0.1, rasterCoords[0], 0.00001 );
        Assert.assertEquals( 1, rasterCoords[1], 0.00001 );

        /**
         * Test the upper left domain of the origin.
         */
        // 994 -> CENTER=[-1,0[; OUTER=[-1,0[
        // 1014 -> CENTER=]-1,0]; OUTER=[-2,-1[
        rasterCoords = REF_CENTER.getRasterCoordinateUnrounded( 994, 1014 );
        Assert.assertEquals( -0.1, rasterCoords[0], 0.00001 );
        Assert.assertEquals( -0.9, rasterCoords[1], 0.00001 );

        /**
         * Test the upper right domain of the origin.
         */
        // 1014 -> CENTER=[1,2[; OUTER=[1,2[
        // 1014 -> CENTER=]-1,0]; OUTER=[-2,-1[
        rasterCoords = REF_CENTER.getRasterCoordinateUnrounded( 1014, 1014 );
        Assert.assertEquals( 1.9, rasterCoords[0], 0.00001 );
        Assert.assertEquals( -0.9, rasterCoords[1], 0.00001 );

    }

    /**
     * Test {@link RasterGeoReference#getRasterCoordinateUnrounded(double, double)} for an outer reference
     */
    @Test
    public void getUnroundedRasterCoordOuter() {
        /**
         * Test the lower right domain of the origin.
         */
        // 991-> CENTER=[1,2[, OUTER=[0,1[
        double[] rasterCoords = REF_OUTER.getRasterCoordinateUnrounded( 1031, 991 );
        Assert.assertEquals( 3.1, rasterCoords[0], 0.00001 );
        Assert.assertEquals( 0.9, rasterCoords[1], 0.00001 );

        // 981-> CENTER=[2,3[, OUTER=[1,2[
        rasterCoords = REF_OUTER.getRasterCoordinateUnrounded( 1031, 981 );
        Assert.assertEquals( 3.1, rasterCoords[0], 0.00001 );
        Assert.assertEquals( 1.9, rasterCoords[1], 0.00001 );

        /**
         * Test the left domain of the origin.
         */
        // 994 -> CENTER=[-1,0[; OUTER=[-1,0[
        // 995 -> CENTER=]0,1]; OUTER=[0,1[
        rasterCoords = REF_OUTER.getRasterCoordinateUnrounded( 994, 995 );
        Assert.assertEquals( -0.6, rasterCoords[0], 0.00001 );
        Assert.assertEquals( 0.5, rasterCoords[1], 0.00001 );

        /**
         * Test the upper left domain of the origin.
         */
        // 994 -> CENTER=[-1,0[; OUTER=[-1,0[
        // 1014 -> CENTER=]-1,0]; OUTER=[-2,-1[
        rasterCoords = REF_OUTER.getRasterCoordinateUnrounded( 994, 1014 );
        Assert.assertEquals( -0.6, rasterCoords[0], 0.00001 );
        Assert.assertEquals( -1.4, rasterCoords[1], 0.00001 );

        /**
         * Test the upper right domain of the origin.
         */
        // 1014 -> CENTER=[1,2[; OUTER=[1,2[
        // 1014 -> CENTER=]-1,0]; OUTER=[-2,-1[
        rasterCoords = REF_OUTER.getRasterCoordinateUnrounded( 1014, 1014 );
        Assert.assertEquals( 1.4, rasterCoords[0], 0.00001 );
        Assert.assertEquals( -1.4, rasterCoords[1], 0.00001 );

    }

    /**
     * Test {@link RasterGeoReference#getWorldCoordinate(double, double)} for a center reference
     */
    @Test
    public void getWorldCoordCenter() {

        /**
         * Following formula should apply for center: <code>
         *  ORIG_X + ((rasterX - 0.5) * RES_X)
         *  ORIG_Y + ((rasterY - 0.5) * RES_Y)
         *  </code>
         */

        /**
         * Test the lower right domain of the origin.
         */
        double[] rasterCoords = REF_CENTER.getWorldCoordinate( 2.1, 3.11 );
        // 2.1 : CENTER->1000 + ((2.1 - 0.5)* 10); OUTER -> 1000 + ( 2.1 * 10 )
        // 3.11: CENTER->1000 + ((3.11 - 0.5)*-10); OUTER -> 1000 + (3.11 * -10 )
        Assert.assertEquals( 1016, rasterCoords[0], 0.00001 );
        Assert.assertEquals( 973.9, rasterCoords[1], 0.00001 );

        /**
         * Test the lower left domain of the origin.
         */
        rasterCoords = REF_CENTER.getWorldCoordinate( 0.01, 2.18 );
        Assert.assertEquals( 995.1, rasterCoords[0], 0.00001 );
        Assert.assertEquals( 983.2, rasterCoords[1], 0.00001 );

        /**
         * Test the upper left domain of the origin.
         */
        rasterCoords = REF_CENTER.getWorldCoordinate( -1.02, 0.04 );
        Assert.assertEquals( 984.8, rasterCoords[0], 0.00001 );
        Assert.assertEquals( 1004.6, rasterCoords[1], 0.00001 );

        /**
         * Test the upper right domain of the origin.
         */
        rasterCoords = REF_CENTER.getWorldCoordinate( 1.56, -0.4 );
        Assert.assertEquals( 1010.6, rasterCoords[0], 0.00001 );
        Assert.assertEquals( 1009, rasterCoords[1], 0.00001 );
    }

    /**
     * Test {@link RasterGeoReference#getWorldCoordinate(double, double)} for an outer reference
     */
    @Test
    public void getWorldCoordOuter() {

        /**
         * Following formula should apply for outer: <code>
         *  ORIG_X + (rasterX * RES_X)
         *  ORIG_Y + (rasterY * RES_Y)
         *  </code>
         */

        /**
         * Test the lower right domain of the origin.
         */
        double[] rasterCoords = REF_OUTER.getWorldCoordinate( 2.1, 3.11 );
        // 2.1 : CENTER->1000 + ((2.1 - 0.5)* 10); OUTER -> 1000 + ( 2.1 * 10 )
        // 3.11: CENTER->1000 + ((3.11 - 0.5)*-10); OUTER -> 1000 + (3.11 * -10 )
        Assert.assertEquals( 1021, rasterCoords[0], 0.00001 );
        Assert.assertEquals( 968.9, rasterCoords[1], 0.00001 );

        /**
         * Test the lower left domain of the origin.
         */
        rasterCoords = REF_OUTER.getWorldCoordinate( 0.01, 2.18 );
        Assert.assertEquals( 1000.1, rasterCoords[0], 0.00001 );
        Assert.assertEquals( 978.20, rasterCoords[1], 0.00001 );

        /**
         * Test the upper left domain of the origin.
         */
        rasterCoords = REF_OUTER.getWorldCoordinate( -1.02, 0.04 );
        Assert.assertEquals( 989.80, rasterCoords[0], 0.00001 );
        Assert.assertEquals( 999.60, rasterCoords[1], 0.00001 );

        /**
         * Test the upper right domain of the origin.
         */
        rasterCoords = REF_OUTER.getWorldCoordinate( 1.56, -0.4 );
        Assert.assertEquals( 1015.6, rasterCoords[0], 0.00001 );
        Assert.assertEquals( 1004, rasterCoords[1], 0.00001 );
    }

    /**
     * Test {@link RasterGeoReference#convertEnvelopeToRasterCRS(Envelope)}
     */
    @Test
    public void convertEnvelopeToRasterCRS() {
        // test overlap over orign
        Envelope env = geomFactor.createEnvelope( 994, 995, 1016, 1014, defaultCRS );
        // CENTER, note the 995 maps to the raster interval [1,2[
        RasterRect rr = REF_CENTER.convertEnvelopeToRasterCRS( env );

        Assert.assertEquals( -1, rr.x );
        Assert.assertEquals( -1, rr.y );

        Assert.assertEquals( 4, rr.width );
        Assert.assertEquals( 2, rr.height );

        // OUTER, note the 995 maps to the raster interval [0,1[
        rr = REF_OUTER.convertEnvelopeToRasterCRS( env );

        Assert.assertEquals( -1, rr.x );
        Assert.assertEquals( -2, rr.y );

        Assert.assertEquals( 3, rr.width );
        Assert.assertEquals( 3, rr.height );
    }

    /**
     * Test {@link RasterGeoReference#getEnvelope(int, int, CRS)}
     */
    @Test
    public void getEnvelope() {

        int width = 3;
        int height = 2;

        // CENTER
        Envelope env = REF_CENTER.getEnvelope( width, height, defaultCRS );
        double[] min = env.getMin().getAsArray();
        double[] max = env.getMax().getAsArray();
        assertEquals( 30, env.getSpan0(), 0.00001 );
        assertEquals( 20, env.getSpan1(), 0.00001 );

        assertEquals( 1000, min[0], 0.00001 );
        assertEquals( 980, min[1], 0.00001 );
        assertEquals( 1030, max[0], 0.00001 );
        assertEquals( 1000, max[1], 0.00001 );

        env = REF_OUTER.getEnvelope( width, height, defaultCRS );
        min = env.getMin().getAsArray();
        max = env.getMax().getAsArray();
        assertEquals( 30, env.getSpan0(), 0.00001 );
        assertEquals( 20, env.getSpan1(), 0.00001 );

        assertEquals( 1000, min[0], 0.00001 );
        assertEquals( 980, min[1], 0.00001 );
        assertEquals( 1030, max[0], 0.00001 );
        assertEquals( 1000, max[1], 0.00001 );
    }

    /**
     * Test {@link RasterGeoReference#getEnvelope(OriginLocation, int, int, CRS)}
     */
    @Test
    public void getEnvelopeWithTarget() {

        int width = 3;
        int height = 2;

        // CENTER
        Envelope env = REF_CENTER.getEnvelope( OriginLocation.OUTER, width, height, defaultCRS );
        double[] min = env.getMin().getAsArray();
        double[] max = env.getMax().getAsArray();
        assertEquals( 30, env.getSpan0(), 0.00001 );
        assertEquals( 20, env.getSpan1(), 0.00001 );

        assertEquals( 995, min[0], 0.00001 );
        assertEquals( 985, min[1], 0.00001 );
        assertEquals( 1025, max[0], 0.00001 );
        assertEquals( 1005, max[1], 0.00001 );

        env = REF_OUTER.getEnvelope( OriginLocation.CENTER, width, height, defaultCRS );
        min = env.getMin().getAsArray();
        max = env.getMax().getAsArray();
        assertEquals( 30, env.getSpan0(), 0.00001 );
        assertEquals( 20, env.getSpan1(), 0.00001 );

        assertEquals( 1005, min[0], 0.00001 );
        assertEquals( 975, min[1], 0.00001 );
        assertEquals( 1035, max[0], 0.00001 );
        assertEquals( 995, max[1], 0.00001 );

    }

    /**
     * Test {@link RasterGeoReference#getEnvelope(OriginLocation, RasterRect, CRS)}
     */
    @Test
    public void getSubEnvelope() {

        int width = 3;
        int height = 2;
        RasterRect subRect = new RasterRect( 4, 2, width, height );

        // CENTER
        Envelope env = REF_CENTER.getEnvelope( CENTER, subRect, defaultCRS );
        double[] min = env.getMin().getAsArray();
        double[] max = env.getMax().getAsArray();
        assertEquals( 30, env.getSpan0(), 0.00001 );
        assertEquals( 20, env.getSpan1(), 0.00001 );

        assertEquals( 1040, min[0], 0.00001 );
        assertEquals( 960, min[1], 0.00001 );
        assertEquals( 1070, max[0], 0.00001 );
        assertEquals( 980, max[1], 0.00001 );

        env = REF_OUTER.getEnvelope( OUTER, subRect, defaultCRS );
        min = env.getMin().getAsArray();
        max = env.getMax().getAsArray();
        assertEquals( 30, env.getSpan0(), 0.00001 );
        assertEquals( 20, env.getSpan1(), 0.00001 );

        assertEquals( 1040, min[0], 0.00001 );
        assertEquals( 960, min[1], 0.00001 );
        assertEquals( 1070, max[0], 0.00001 );
        assertEquals( 980, max[1], 0.00001 );
    }

    /**
     * Test {@link RasterGeoReference#getEnvelope(OriginLocation, RasterRect, CRS)}
     */
    @Test
    public void getSubEnvelopeWithTarget() {

        int width = 3;
        int height = 2;
        RasterRect subRect = new RasterRect( 4, 2, width, height );

        // CENTER
        Envelope env = REF_CENTER.getEnvelope( OUTER, subRect, defaultCRS );
        double[] min = env.getMin().getAsArray();
        double[] max = env.getMax().getAsArray();
        assertEquals( 30, env.getSpan0(), 0.00001 );
        assertEquals( 20, env.getSpan1(), 0.00001 );

        assertEquals( 1035, min[0], 0.00001 );
        assertEquals( 965, min[1], 0.00001 );
        assertEquals( 1065, max[0], 0.00001 );
        assertEquals( 985, max[1], 0.00001 );

        env = REF_OUTER.getEnvelope( CENTER, subRect, defaultCRS );
        min = env.getMin().getAsArray();
        max = env.getMax().getAsArray();
        assertEquals( 30, env.getSpan0(), 0.00001 );
        assertEquals( 20, env.getSpan1(), 0.00001 );

        assertEquals( 1045, min[0], 0.00001 );
        assertEquals( 955, min[1], 0.00001 );
        assertEquals( 1075, max[0], 0.00001 );
        assertEquals( 975, max[1], 0.00001 );
    }

    /**
     * Test {@link RasterGeoReference#getSize(Envelope)}
     */
    @Test
    public void getSize() {
        Envelope env = geomFactor.createEnvelope( 994, 995, 1016, 1014, defaultCRS );
        // CENTER, note the 995 maps to the raster interval [1,2[
        int[] size = REF_CENTER.getSize( env );

        Assert.assertEquals( 4, size[0] );
        Assert.assertEquals( 2, size[1] );

        // OUTER, note the 995 maps to the raster interval [0,1[
        size = REF_OUTER.getSize( env );

        Assert.assertEquals( 3, size[0] );
        Assert.assertEquals( 3, size[1] );

        env = geomFactor.createEnvelope( 1005, 985, 1025, 1005, defaultCRS );
        size = REF_CENTER.getSize( env );
        Assert.assertEquals( 2, size[0] );
        Assert.assertEquals( 2, size[1] );

        size = REF_OUTER.getSize( env );

        Assert.assertEquals( 3, size[0] );
        Assert.assertEquals( 3, size[1] );

    }

    /**
     * Test {@link RasterGeoReference#merger(RasterGeoReference, RasterGeoReference)}
     */
    @Test
    public void merger() {
        // postive x and negative y res
        RasterGeoReference ref1 = new RasterGeoReference( OriginLocation.CENTER, 5, -4, 2, 1, 1999, 1000, defaultCRS );
        RasterGeoReference ref2 = new RasterGeoReference( OriginLocation.CENTER, 2, -5, 2, 1, 2000, 999, defaultCRS );
        RasterGeoReference result = RasterGeoReference.merger( ref1, ref2 );

        double[] origin = result.getOrigin();
        Assert.assertEquals( 1999, origin[0], 0.00001 );
        Assert.assertEquals( 1000, origin[1], 0.00001 );
        Assert.assertEquals( 2, result.getResolutionX(), 0.00001 );
        Assert.assertEquals( -4, result.getResolutionY(), 0.00001 );
        Assert.assertEquals( 2, result.getRotationX(), 0.00001 );
        Assert.assertEquals( 1, result.getRotationY(), 0.00001 );

        // negative x and y res
        ref1 = new RasterGeoReference( OriginLocation.CENTER, -5, -4, 2, 1, 1999, 1000, defaultCRS );
        ref2 = new RasterGeoReference( OriginLocation.CENTER, -2, -5, 2, 1, 2000, 999, defaultCRS );
        result = RasterGeoReference.merger( ref1, ref2 );

        origin = result.getOrigin();
        Assert.assertEquals( 2000, origin[0], 0.00001 );
        Assert.assertEquals( 1000, origin[1], 0.00001 );
        Assert.assertEquals( -2, result.getResolutionX(), 0.00001 );
        Assert.assertEquals( -4, result.getResolutionY(), 0.00001 );
        Assert.assertEquals( 2, result.getRotationX(), 0.00001 );
        Assert.assertEquals( 1, result.getRotationY(), 0.00001 );

        // negative x and postive y res
        ref1 = new RasterGeoReference( OriginLocation.CENTER, -5, 4, 2, 1, 1999, 1000, defaultCRS );
        ref2 = new RasterGeoReference( OriginLocation.CENTER, -2, 5, 2, 1, 2000, 999, defaultCRS );
        result = RasterGeoReference.merger( ref1, ref2 );

        origin = result.getOrigin();
        Assert.assertEquals( 2000, origin[0], 0.00001 );
        Assert.assertEquals( 999, origin[1], 0.00001 );
        Assert.assertEquals( -2, result.getResolutionX(), 0.00001 );
        Assert.assertEquals( 4, result.getResolutionY(), 0.00001 );
        Assert.assertEquals( 2, result.getRotationX(), 0.00001 );
        Assert.assertEquals( 1, result.getRotationY(), 0.00001 );

        // postive x and postive y res
        ref1 = new RasterGeoReference( OriginLocation.CENTER, 5, 4, 2, 1, 1999, 1000, defaultCRS );
        ref2 = new RasterGeoReference( OriginLocation.CENTER, 2, 5, 2, 1, 2000, 999, defaultCRS );
        result = RasterGeoReference.merger( ref1, ref2 );

        origin = result.getOrigin();
        Assert.assertEquals( 1999, origin[0], 0.00001 );
        Assert.assertEquals( 999, origin[1], 0.00001 );
        Assert.assertEquals( 2, result.getResolutionX(), 0.00001 );
        Assert.assertEquals( 4, result.getResolutionY(), 0.00001 );
        Assert.assertEquals( 2, result.getRotationX(), 0.00001 );
        Assert.assertEquals( 1, result.getRotationY(), 0.00001 );

    }

    /**
     * Test the {@link RasterGeoReference#create(OriginLocation, Envelope, int, int)} method.
     */
    @Test
    public void create() {
        Envelope env = geomFactor.createEnvelope( 0, 0, 3000, 2000, defaultCRS );
        RasterGeoReference result = RasterGeoReference.create( OriginLocation.CENTER, env, 300, 200 );
        double[] origin = result.getOrigin();
        Assert.assertEquals( 0, origin[0], 0.00001 );
        Assert.assertEquals( 2000, origin[1], 0.00001 );
        Assert.assertEquals( 10, result.getResolutionX(), 0.00001 );
        Assert.assertEquals( -10, result.getResolutionY(), 0.00001 );
        Assert.assertEquals( 0, result.getRotationX(), 0.00001 );
        Assert.assertEquals( 0, result.getRotationY(), 0.00001 );
        Assert.assertEquals( OriginLocation.CENTER, result.getOriginLocation() );

        result = RasterGeoReference.create( OriginLocation.OUTER, env, 300, 200 );
        origin = result.getOrigin();
        Assert.assertEquals( 0, origin[0], 0.00001 );
        Assert.assertEquals( 2000, origin[1], 0.00001 );
        Assert.assertEquals( 10, result.getResolutionX(), 0.00001 );
        Assert.assertEquals( -10, result.getResolutionY(), 0.00001 );
        Assert.assertEquals( 0, result.getRotationX(), 0.00001 );
        Assert.assertEquals( 0, result.getRotationY(), 0.00001 );
        Assert.assertEquals( OriginLocation.OUTER, result.getOriginLocation() );

    }

    /**
     * Test the {@link RasterGeoReference#createRelocatedReference(Envelope)} method.
     */
    @Test
    public void createRelocatedReference() {
        Envelope env = geomFactor.createEnvelope( 1005, 975, 1035, 995, defaultCRS );
        RasterGeoReference result = REF_CENTER.createRelocatedReference( env );
        double[] origin = result.getOrigin();
        Assert.assertEquals( 1010, origin[0], 0.00001 );
        Assert.assertEquals( 990, origin[1], 0.00001 );
        Assert.assertEquals( 10, result.getResolutionX(), 0.00001 );
        Assert.assertEquals( -10, result.getResolutionY(), 0.00001 );
        Assert.assertEquals( 0, result.getRotationX(), 0.00001 );
        Assert.assertEquals( 0, result.getRotationY(), 0.00001 );
        Assert.assertEquals( OriginLocation.CENTER, result.getOriginLocation() );

        result = REF_OUTER.createRelocatedReference( env );
        origin = result.getOrigin();
        Assert.assertEquals( 1000, origin[0], 0.00001 );
        Assert.assertEquals( 1000, origin[1], 0.00001 );
        Assert.assertEquals( 10, result.getResolutionX(), 0.00001 );
        Assert.assertEquals( -10, result.getResolutionY(), 0.00001 );
        Assert.assertEquals( 0, result.getRotationX(), 0.00001 );
        Assert.assertEquals( 0, result.getRotationY(), 0.00001 );
        Assert.assertEquals( OriginLocation.OUTER, result.getOriginLocation() );

    }

    /**
     * Test the {@link RasterGeoReference#createRelocatedReference(Envelope)} method.
     */
    @Test
    public void createRelocatedReferenceWithTarget() {
        Envelope env = geomFactor.createEnvelope( 1005, 975, 1035, 995, defaultCRS );
        RasterGeoReference result = REF_CENTER.createRelocatedReference( OUTER, env );
        double[] origin = result.getOrigin();
        Assert.assertEquals( 1005, origin[0], 0.00001 );
        Assert.assertEquals( 995, origin[1], 0.00001 );
        Assert.assertEquals( 10, result.getResolutionX(), 0.00001 );
        Assert.assertEquals( -10, result.getResolutionY(), 0.00001 );
        Assert.assertEquals( 0, result.getRotationX(), 0.00001 );
        Assert.assertEquals( 0, result.getRotationY(), 0.00001 );
        Assert.assertEquals( OUTER, result.getOriginLocation() );

        result = REF_OUTER.createRelocatedReference( CENTER, env );
        origin = result.getOrigin();
        Assert.assertEquals( 1005, origin[0], 0.00001 );
        Assert.assertEquals( 995, origin[1], 0.00001 );
        Assert.assertEquals( 10, result.getResolutionX(), 0.00001 );
        Assert.assertEquals( -10, result.getResolutionY(), 0.00001 );
        Assert.assertEquals( 0, result.getRotationX(), 0.00001 );
        Assert.assertEquals( 0, result.getRotationY(), 0.00001 );
        Assert.assertEquals( CENTER, result.getOriginLocation() );

    }

    /**
     * Test the {@link RasterGeoReference#relocateEnvelope(OriginLocation,Envelope)} method.
     */
    @Test
    public void relocateEnvelope() {
        Envelope env = geomFactor.createEnvelope( 1005, 975, 1035, 995, defaultCRS );
        Envelope result = REF_CENTER.relocateEnvelope( OUTER, env );
        double[] min = result.getMin().getAsArray();
        double[] max = result.getMax().getAsArray();
        Assert.assertEquals( 1000, min[0], 0.00001 );
        Assert.assertEquals( 980, min[1], 0.00001 );
        Assert.assertEquals( 1030, max[0], 0.00001 );
        Assert.assertEquals( 1000, max[1], 0.00001 );
        Assert.assertEquals( env.getCoordinateSystem(), defaultCRS );

        result = REF_OUTER.relocateEnvelope( CENTER, env );
        min = result.getMin().getAsArray();
        max = result.getMax().getAsArray();
        Assert.assertEquals( 1010, min[0], 0.00001 );
        Assert.assertEquals( 970, min[1], 0.00001 );
        Assert.assertEquals( 1040, max[0], 0.00001 );
        Assert.assertEquals( 990, max[1], 0.00001 );
        Assert.assertEquals( env.getCoordinateSystem(), defaultCRS );

    }

}
